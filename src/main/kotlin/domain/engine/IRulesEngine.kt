package domain.engine

import domain.models.BoardState
import domain.models.MoveRequest
import domain.models.ScoreEvent
import domain.models.ValidationResult

interface IRulesEngine {
    fun validateMove(
        board: BoardState,
        move: MoveRequest,
    ): ValidationResult

    fun calculateIntermediateScore(
        board: BoardState,
        move: MoveRequest,
    ): ScoreEvent

    fun calculateFinalScore(board: BoardState): List<ScoreEvent>
}
