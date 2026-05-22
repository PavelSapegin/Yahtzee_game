package application.models

import java.util.UUID

data class PlayerInGameState(
    var currentScore: Int,
)

enum class GameStatus {
    IN_PROGRESS,
    FINISHED,
}

data class SessionState(
    val gameId: UUID,
    var status: GameStatus,
    var currentPlayerId: UUID,
    val turnOrder: List<UUID>,
    var players: MutableMap<UUID, PlayerInGameState>,
)

sealed class MoveResult(
    val errorMessage: String?,
) {
    object Success : MoveResult(errorMessage = null)

    class Error(message: String) : MoveResult(errorMessage = message)
}
