package data.repositories

import data.models.GameRecord
import java.util.UUID

interface IGameRepository {
    fun saveRecord(record: GameRecord)

    fun getHistoryByPlayer(playerId: UUID): List<GameRecord>
}
