package data.repositories

import data.database.GameResultsTable
import data.database.GamesTable
import data.database.MoveHistoryTable
import data.models.GameRecord
import data.models.PlayerResult
import domain.models.MoveRecord
import domain.models.MoveRequest
import domain.models.ScoreCategory
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.util.UUID

class SqliteGameRepository : IGameRepository {
    override fun saveRecord(record: GameRecord) {
        transaction {
            GamesTable.insert {
                it[id] = record.gameId
                it[date] = record.date
            }

            record.finalScores.forEach { result ->
                GameResultsTable.insert {
                    it[gameId] = record.gameId
                    it[playerId] = result.playerId
                    it[score] = result.score
                    it[rank] = result.rank
                }
            }

            record.history.forEach { move ->
                MoveHistoryTable.insert {
                    it[gameId] = record.gameId
                    it[moveNumber] = move.moveNumber
                    it[playerId] = move.requestData.playerID
                    it[category] = move.requestData.targetCategory.name
                    it[pointsScored] = move.pointScored
                    it[timestamp] = move.timestamp
                    it[dice] = move.requestData.finalDice.joinToString(",")
                }
            }
        }
    }

    override fun getHistoryByPlayer(playerId: UUID): List<GameRecord> =
        transaction {
            val gameIds =
                GameResultsTable
                    .slice(GameResultsTable.gameId)
                    .selectAll().where { GameResultsTable.playerId eq playerId }
                    .map { it[GameResultsTable.gameId] }

            val records = mutableListOf<GameRecord>()

            for (game in gameIds) {
                val date: LocalDateTime = GamesTable.selectAll().where { GamesTable.id eq game }.single()[GamesTable.date]
                val results =
                    GameResultsTable.selectAll().where { GameResultsTable.gameId eq game }.map {
                        PlayerResult(
                            playerId = it[GameResultsTable.playerId],
                            score = it[GameResultsTable.score],
                            rank = it[GameResultsTable.rank],
                        )
                    }

                val history =
                    MoveHistoryTable.selectAll().where {
                        MoveHistoryTable.gameId eq game
                    }.map { row ->
                        val diceList = row[MoveHistoryTable.dice].split(",").map { it.toInt() }
                        MoveRecord(
                            moveNumber = row[MoveHistoryTable.moveNumber],
                            requestData =
                                MoveRequest(
                                    playerID = row[MoveHistoryTable.playerId],
                                    finalDice = diceList,
                                    targetCategory = ScoreCategory.valueOf(row[MoveHistoryTable.category]),
                                ),
                            timestamp = row[MoveHistoryTable.timestamp],
                            pointScored = row[MoveHistoryTable.pointsScored],
                        )
                    }

                records.add(GameRecord(game, date, results, history))
            }
            return@transaction records
        }
}
