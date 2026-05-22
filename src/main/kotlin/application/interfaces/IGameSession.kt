package application.interfaces

import application.models.MoveResult
import application.models.SessionState
import data.models.GameRecord
import domain.models.BoardState
import domain.models.MoveRequest
import java.util.UUID

interface IGameSession {
    fun startGame(playerIds: List<UUID>)

    fun registerMove(move: MoveRequest): MoveResult

    fun undoLastMove()

    fun endGame(): GameRecord

    val currentState: SessionState

    val board: BoardState
}
