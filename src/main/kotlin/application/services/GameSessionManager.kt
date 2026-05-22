package application.services

import application.interfaces.IGameSession
import application.models.GameStatus
import application.models.MoveResult
import application.models.PlayerInGameState
import application.models.SessionState
import data.models.GameRecord
import data.models.PlayerResult
import data.repositories.IGameRepository
import domain.engine.IRulesEngine
import domain.models.BoardState
import domain.models.MoveRecord
import domain.models.MoveRequest
import domain.models.ScoreCategory
import domain.models.ValidationResult
import java.time.LocalDateTime
import java.util.UUID
import java.util.UUID.randomUUID

class GameSessionManager(
    private val referee: IRulesEngine,
    private val gameRepo: IGameRepository,
) : IGameSession {
    override lateinit var currentState: SessionState
    var moveHistory: MutableList<MoveRecord> = mutableListOf<MoveRecord>()
    override lateinit var board: BoardState
    private var currentPlayerIdx = 0

    override fun startGame(playerIds: List<UUID>) {
        if (playerIds.isEmpty()) {
            throw NoSuchElementException("Player list is empty")
        }
        board = BoardState(playerIds)
        currentState =
            SessionState(
                randomUUID(),
                status = GameStatus.IN_PROGRESS,
                currentPlayerId = playerIds[currentPlayerIdx],
                turnOrder = playerIds,
                players = playerIds.associateWith { PlayerInGameState(0) }.toMutableMap(),
            )
    }

    override fun registerMove(move: MoveRequest): MoveResult {
        if (currentState.status == GameStatus.FINISHED) {
            return MoveResult.Error("Game finished.")
        }

        if (currentState.currentPlayerId != move.playerID) {
            return MoveResult.Error("It's another player's turn now.")
        }

        if (
            !(
                move.finalDice.size == 5 &&
                    move.finalDice.all { it in 1..6 }
            )
        ) {
            return MoveResult.Error("There must be five dices and values correct")
        }

        val resultChecking = referee.validateMove(board, move)

        if (resultChecking is ValidationResult.Correct) {
            val resultMove = referee.calculateIntermediateScore(board, move)
            board.applyMove(move, resultMove.points)
            currentState.players[currentState.currentPlayerId]?.currentScore += resultMove.points
            moveHistory.add(MoveRecord(moveHistory.size, move, LocalDateTime.now(), resultMove.points))
            currentPlayerIdx = (currentPlayerIdx + 1) % currentState.turnOrder.size
            currentState.currentPlayerId = currentState.turnOrder[currentPlayerIdx]

            return MoveResult.Success
        } else {
            val errorMessage = (resultChecking as ValidationResult.Error).message
            return MoveResult.Error(errorMessage)
        }
    }

    override fun undoLastMove() {
        if (moveHistory.isEmpty()) {
            throw IndexOutOfBoundsException("It is first move yet.")
        }
        val lastMove = moveHistory.removeLast()
        val playerState = currentState.players[lastMove.requestData.playerID]
        if (playerState != null) {
            playerState.currentScore -= lastMove.pointScored
        }
        board.revertMove(lastMove)
        currentPlayerIdx = if (currentPlayerIdx == 0) currentState.turnOrder.size - 1 else currentPlayerIdx - 1
        currentState.currentPlayerId = currentState.turnOrder[currentPlayerIdx]
    }

    override fun endGame(): GameRecord {
        if (moveHistory.size != currentState.players.size * 13) {
            throw IllegalStateException()
        }

        val finalEvents = referee.calculateFinalScore(board)
        for (event in finalEvents) {
            val currentScore = currentState.players[event.playerID]?.currentScore ?: 0
            currentState.players[event.playerID] = PlayerInGameState(currentScore + event.points)

            val bonusMove = MoveRequest(event.playerID, emptyList(), targetCategory = ScoreCategory.BONUS)
            board.applyMove(bonusMove, event.points)
            moveHistory.add(MoveRecord(moveHistory.size, bonusMove, LocalDateTime.now(), event.points))
        }

        val results =
            currentState.players.map {
                    (player, state) ->
                PlayerResult(
                    player,
                    state.currentScore,
                    0,
                )
            }

        val finalRecord =
            GameRecord(
                randomUUID(),
                LocalDateTime.now(),
                results,
                moveHistory,
            )
        currentState.status = GameStatus.FINISHED
        gameRepo.saveRecord(finalRecord)
        return finalRecord
    }
}
