import application.models.MoveResult
import application.services.GameSessionManager
import data.repositories.InMemoryGameRepository
import domain.engine.YahtzeeRulesEngine
import domain.models.MoveRequest
import domain.models.ScoreCategory
import domain.models.ValidationResult
import org.junit.jupiter.api.BeforeEach
import java.util.UUID
import java.util.UUID.randomUUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GameSessionManagerTest {
    private lateinit var manager: GameSessionManager
    private lateinit var p1: UUID
    private lateinit var p2: UUID

    @BeforeEach
    fun setup() {
        val gameRepo = InMemoryGameRepository()
        val referee = YahtzeeRulesEngine()

        manager = GameSessionManager(referee, gameRepo)

        p1 = randomUUID()
        p2 = randomUUID()
        manager.startGame(listOf(p1, p2))
    }

    @Test
    fun `Turn must switch to next player after successfully move`() {
        val state = manager.currentState

        assertEquals(p1, state.currentPlayerId)

        val request = MoveRequest(p1, listOf(1, 1, 1, 1, 1), ScoreCategory.ONES)
        val result = manager.registerMove(request)

        assertTrue(result is MoveResult.Success)
        assertEquals(p2, state.currentPlayerId)
    }

    @Test
    fun `UndoLastMove must revert score, board and turn`() {
        val state = manager.currentState
        val tempEngine = YahtzeeRulesEngine()

        val request = MoveRequest(p1, listOf(2, 2, 2, 3, 3), ScoreCategory.TWOS)
        manager.registerMove(request)

        assertEquals(6, state.players[p1]?.currentScore)
        assertEquals(p2, state.currentPlayerId)

        manager.undoLastMove()

        assertEquals(0, state.players[p1]?.currentScore)
        assertEquals(p1, state.currentPlayerId)
        assertEquals(tempEngine.validateMove(manager.board, request), ValidationResult.Correct)
    }

    @Test
    fun `RegisterMove must return error for wrong player`() {
        val request = MoveRequest(p2, listOf(1, 2, 3, 4, 5), ScoreCategory.TWOS)
        val result = manager.registerMove(request)

        assertTrue(result is MoveResult.Error)
    }
}
