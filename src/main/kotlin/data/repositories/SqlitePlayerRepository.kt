package data.repositories

import data.database.PlayersTable
import data.models.PlayerProfile
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.UUID

class SqlitePlayerRepository : IPlayerRepository {
    override fun getById(id: UUID): PlayerProfile? =
        transaction {
            PlayersTable.selectAll().where { PlayersTable.id eq id }.map {
                PlayerProfile(
                    it[PlayersTable.id],
                    it[PlayersTable.name],
                    it[PlayersTable.eloRating],
                    it[PlayersTable.gamesPlayed],
                    it[PlayersTable.winRate],
                )
            }.singleOrNull()
        }

    override fun getAll(): List<PlayerProfile> =
        transaction {
            PlayersTable.selectAll().map {
                PlayerProfile(
                    it[PlayersTable.id],
                    it[PlayersTable.name],
                    it[PlayersTable.eloRating],
                    it[PlayersTable.gamesPlayed],
                    it[PlayersTable.winRate],
                )
            }
        }

    override fun save(profile: PlayerProfile) {
        transaction {
            val exists = PlayersTable.selectAll().where { PlayersTable.id eq profile.id }.count() > 0
            if (exists) {
                PlayersTable.update({ PlayersTable.id eq profile.id }) {
                    it[eloRating] = profile.eloRating
                    it[gamesPlayed] = profile.gamesPlayed
                    it[winRate] = profile.winRate
                    it[wins] = profile.winRate.toInt() * profile.gamesPlayed
                }
            } else {
                PlayersTable.insert {
                    it[id] = profile.id
                    it[name] = profile.name
                    it[eloRating] = profile.eloRating
                    it[gamesPlayed] = profile.gamesPlayed
                    it[winRate] = profile.winRate
                    it[wins] = profile.winRate.toInt() * profile.gamesPlayed
                }
            }
        }
    }
}
