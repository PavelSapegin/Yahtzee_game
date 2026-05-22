package data.repositories

import data.models.GameRecord
import data.models.PlayerProfile
import java.util.UUID
import java.util.UUID.randomUUID

class InMemoryStatsManager(private val playerRepo: IPlayerRepository) :
    IStatsService {
    override fun processGameResult(record: GameRecord) {
        val maxScore = record.finalScores.maxOf { it.score }
        val winners = record.finalScores.filter { it.score == maxScore }

        for (player in record.finalScores) {
            val profile = playerRepo.getById(player.playerId) ?: continue

            profile.gamesPlayed++
            val win = winners.any { it.playerId == profile.id }
            profile.eloRating += if (win) 32 else -32

            profile.winRate = (profile.winRate * (profile.gamesPlayed - 1) + if (win) 1 else 0) / profile.gamesPlayed
            playerRepo.save(profile)
        }
    }

    override fun getPlayerStats(playerId: UUID): PlayerProfile {
        return playerRepo.getById(playerId) ?: PlayerProfile(randomUUID(), "Unknown", 0, 0, 0F)
    }

    override fun getLeaderBoard(): List<PlayerProfile> {
        val players = playerRepo.getAll()
        return players.sortedBy { it.eloRating }
    }
}
