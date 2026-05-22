package data.repositories

import data.models.PlayerProfile
import java.util.UUID

interface IPlayerRepository {
    fun getById(id: UUID): PlayerProfile?

    fun save(profile: PlayerProfile)

    fun getAll(): List<PlayerProfile>
}
