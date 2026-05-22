import domain.engine.YahtzeeRulesEngine
import domain.models.BoardState
import domain.models.MoveRequest
import domain.models.ScoreCategory
import domain.models.ValidationResult
import java.util.UUID.randomUUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class YahtzeeRulesEngineTest {
    private val engine = YahtzeeRulesEngine()
    private val tempPlayerId = randomUUID()
    private val tempBoard = BoardState(listOf(tempPlayerId))

    private fun getScore(
        dice: List<Int>,
        cat: ScoreCategory,
    ): Int {
        val request = MoveRequest(tempPlayerId, dice, cat)
        val event = engine.calculateIntermediateScore(tempBoard, request)
        return event.points
    }

    @Test
    fun `Upper sections has special sum only`() {
        assertEquals(6, getScore(listOf(3, 3, 1, 2, 5), ScoreCategory.THREES))
        assertEquals(0, getScore(listOf(1, 2, 6, 4, 5), ScoreCategory.THREES))
        assertEquals(25, getScore(listOf(5, 5, 5, 5, 5), ScoreCategory.FIFTHS))
    }

    @Test
    fun `Three of a kind has sum all dice`() {
        assertEquals(17, getScore(listOf(2, 2, 2, 5, 6), ScoreCategory.THREEKIND))
        assertEquals(0, getScore(listOf(1, 2, 2, 3, 4), ScoreCategory.THREEKIND))
    }

    @Test
    fun `Full House has sum 25`() {
        assertEquals(25, getScore(listOf(3, 3, 3, 5, 5), ScoreCategory.FULL_HOUSE))
        assertEquals(25, getScore(listOf(4, 4, 4, 4, 4), ScoreCategory.FULL_HOUSE))
        assertEquals(0, getScore(listOf(3, 3, 3, 4, 5), ScoreCategory.FULL_HOUSE))
    }

    @Test
    fun `Straight detects unsorted sequense`() {
        assertEquals(30, getScore(listOf(3, 1, 4, 2, 6), ScoreCategory.SMALL_STRAIGHT))
        assertEquals(30, getScore(listOf(1, 2, 3, 4, 3), ScoreCategory.SMALL_STRAIGHT))

        assertEquals(40, getScore(listOf(2, 3, 4, 5, 6), ScoreCategory.LARGE_STRAIGHT))
        assertEquals(0, getScore(listOf(1, 2, 4, 5, 6), ScoreCategory.LARGE_STRAIGHT))
    }

    @Test
    fun `ValidateMove must return false if category is taken`() {
        val request = MoveRequest(tempPlayerId, listOf(1, 2, 3, 4, 5), ScoreCategory.ONES)

        assertEquals(engine.validateMove(tempBoard, request), ValidationResult.Correct)
        tempBoard.applyMove(request, 5)
        assertNotEquals(engine.validateMove(tempBoard, request), ValidationResult.Correct)
    }
}
