import data.database.GameResultsTable
import data.database.GamesTable
import data.database.MoveHistoryTable
import data.database.PlayersTable
import data.models.PlayerProfile
import data.repositories.SqlitePlayerRepository
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.sql.Connection
import java.sql.DriverManager
import java.util.UUID.randomUUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SqlitePlayerRepositoryTest {
    private var keepAliveConnection: Connection? = null
    private val dbUrl = "jdbc:sqlite:file:testdb?mode=memory&cache=shared"

    @BeforeEach
    fun setup() {
        keepAliveConnection = DriverManager.getConnection(dbUrl)
        Database.connect(url = dbUrl, driver = "org.sqlite.JDBC")

        transaction {
            SchemaUtils.create(PlayersTable, GamesTable, GameResultsTable, MoveHistoryTable)
        }
    }

    @AfterEach
    fun tearDown() {
        keepAliveConnection?.close()
    }

    @Test
    fun `Player profile should be save and updated`() {
        val repo = SqlitePlayerRepository()
        val id = randomUUID()
        val profile = PlayerProfile(id, "Test", 1000, 5, 0.5f)

        repo.save(profile)
        val saved = repo.getById(id)
        assertNotNull(saved)
        assertEquals("Test", saved.name)
        assertEquals(1000, saved.eloRating)

        profile.eloRating = 1100
        repo.save(profile)
        val updated = repo.getById(id)
        assertEquals(1100, updated?.eloRating)
    }
}
