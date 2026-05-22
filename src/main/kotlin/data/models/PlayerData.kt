package data.models

import domain.models.MoveRecord
import java.time.LocalDateTime
import java.util.UUID

data class PlayerResult(
    val playerId: UUID,
    val score: Int,
    val rank: Int,
)

data class GameRecord(
    val gameId: UUID,
    val date: LocalDateTime,
    val finalScores: List<PlayerResult>,
    val history: List<MoveRecord>,
)

data class PlayerProfile(
    val id: UUID,
    val name: String,
    var eloRating: Int,
    var gamesPlayed: Int,
    var winRate: Float,
)
