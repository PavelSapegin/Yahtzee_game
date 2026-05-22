package data.repositories

import data.models.PlayerProfile
import java.util.UUID

class InMemoryPlayerRepository : IPlayerRepository {
    private var storage = mutableMapOf<UUID, PlayerProfile>()

    override fun getById(id: UUID): PlayerProfile? {
        return if (id in storage) storage[id] else null
    }

    override fun save(profile: PlayerProfile) {
        storage[profile.id] = profile
    }

    override fun getAll(): List<PlayerProfile> {
        return storage.values.toList()
    }
}
