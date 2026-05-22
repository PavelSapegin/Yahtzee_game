import application.models.GameStatus
import application.models.MoveResult
import application.services.GameSessionManager
import data.models.PlayerProfile
import data.repositories.InMemoryGameRepository
import data.repositories.InMemoryPlayerRepository
import data.repositories.InMemoryStatsManager
import domain.engine.YahtzeeRulesEngine
import domain.models.MoveRequest
import domain.models.ScoreCategory
import java.util.UUID.randomUUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GameIntegrationTest {
    @Test
    fun `Full game must be finished successfully and stats must be updated`() {
        val playerRepo = InMemoryPlayerRepository()
        val gameRepo = InMemoryGameRepository()
        val statsManager = InMemoryStatsManager(playerRepo)
        val referee = YahtzeeRulesEngine()
        val manager = GameSessionManager(referee, gameRepo)

        val p1 = randomUUID()
        val p2 = randomUUID()

        playerRepo.save(PlayerProfile(p1, "Jora", 1000, 0, 0f))
        playerRepo.save(PlayerProfile(p2, "Vadim", 1000, 0, 0f))

        manager.startGame(listOf(p1, p2))
        assertEquals(GameStatus.IN_PROGRESS, manager.currentState.status)

        val allCats = ScoreCategory.entries.filter { it != ScoreCategory.BONUS }

        for (cat in allCats) {
            // p1 puts trash dices(will lose)
            val moveP1 = MoveRequest(p1, listOf(1, 2, 3, 4, 6), cat)
            val resultP1 = manager.registerMove(moveP1)
            assertTrue(resultP1 is MoveResult.Success)

            // p2 put good dices (will win)
            val moveP2 = MoveRequest(p2, listOf(5, 5, 5, 5, 5), cat)
            val resultP2 = manager.registerMove(moveP2)
            assertTrue(resultP2 is MoveResult.Success)
        }

        assertEquals(2 * allCats.size, manager.moveHistory.size)

        val record = manager.endGame()
        assertEquals(GameStatus.FINISHED, manager.currentState.status)

        statsManager.processGameResult(record)

        val historyP1 = gameRepo.getHistoryByPlayer(p1)
        assertEquals(1, historyP1.size)
        assertEquals(record.gameId, historyP1[0].gameId)

        val profileP1 = playerRepo.getById(p1)
        val profileP2 = playerRepo.getById(p2)
        assertEquals(1, profileP1?.gamesPlayed)
        assertEquals(1, profileP2?.gamesPlayed)

        val checkedProfileP1 = checkNotNull(profileP1)
        val checkedProfileP2 = checkNotNull(profileP2)

        assertTrue(checkedProfileP1.eloRating < 1000)
        assertTrue(checkedProfileP2.eloRating > 1000)

        assertEquals(0.0f, checkedProfileP1.winRate)
        assertEquals(1.0f, checkedProfileP2.winRate)
    }
}
