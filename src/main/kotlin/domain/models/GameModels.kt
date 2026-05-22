package domain.models

import java.time.LocalDateTime
import java.util.UUID

data class ScoreEvent(
    val playerID: UUID,
    val points: Int,
    val category: ScoreCategory,
    val isBonusApplied: Boolean,
)

data class MoveRequest(
    val playerID: UUID,
    val finalDice: List<Int>,
    val targetCategory: ScoreCategory,
)

data class MoveRecord(
    val moveNumber: Int,
    val requestData: MoveRequest,
    val timestamp: LocalDateTime,
    val pointScored: Int,
)

data class ScoreSheet(
    val filledCategories: MutableMap<ScoreCategory, Int> = mutableMapOf(),
)

sealed class ValidationResult {
    object Correct : ValidationResult()

    class Error(val message: String) : ValidationResult()
}
