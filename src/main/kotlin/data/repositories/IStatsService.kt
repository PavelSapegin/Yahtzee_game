package data.repositories

import data.models.GameRecord
import data.models.PlayerProfile
import java.util.UUID

interface IStatsService {
    fun processGameResult(record: GameRecord)

    fun getPlayerStats(playerId: UUID): PlayerProfile

    fun getLeaderBoard(): List<PlayerProfile>
}
