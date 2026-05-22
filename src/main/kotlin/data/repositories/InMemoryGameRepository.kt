package data.repositories

import data.models.GameRecord
import java.util.UUID

class InMemoryGameRepository : IGameRepository {
    private var storage = mutableMapOf<UUID, GameRecord>()

    override fun saveRecord(record: GameRecord) {
        storage[record.gameId] = record
    }

    override fun getHistoryByPlayer(playerId: UUID): List<GameRecord> {
        return storage.values.filter { game ->
            game.finalScores.any { it.playerId == playerId }
        }
    }
}
