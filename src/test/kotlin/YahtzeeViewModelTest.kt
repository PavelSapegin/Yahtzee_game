import application.services.GameSessionManager
import data.repositories.InMemoryGameRepository
import data.repositories.InMemoryPlayerRepository
import data.repositories.InMemoryStatsManager
import domain.engine.YahtzeeRulesEngine
import domain.models.ScoreCategory
import gui.AssistMood
import gui.YahtzeeViewModel
import kotlin.test.Test
import kotlin.test.assertEquals

class YahtzeeViewModelTest {
    @Test
    fun `ViewModel should update assist message on high score`() {
        val playerRepo = InMemoryPlayerRepository()
        val stats = InMemoryStatsManager(playerRepo)
        val manager = GameSessionManager(YahtzeeRulesEngine(), InMemoryGameRepository())

        val viewModel = YahtzeeViewModel(manager, playerRepo, stats)
        viewModel.addPlayer("Jora")
        viewModel.startGame()
        viewModel.submitMove("55555", ScoreCategory.YAHTZEE)

        val state = viewModel.uiState.value
        assertEquals(AssistMood.EXCITED, state.assistMood)
        assertEquals("incredible", state.assistMessage)
        assertEquals(50, state.totalScores.values.first())
    }

    @Test
    fun `ViewModel should show error when trying to use same category twice`() {
        val playerRepo = InMemoryPlayerRepository()
        val stats = InMemoryStatsManager(playerRepo)
        val manager = GameSessionManager(YahtzeeRulesEngine(), InMemoryGameRepository())

        val viewModel = YahtzeeViewModel(manager, playerRepo, stats)

        viewModel.addPlayer("Vadim")
        viewModel.startGame()

        viewModel.submitMove("11122", ScoreCategory.ONES)
        viewModel.submitMove("22233", ScoreCategory.ONES)

        assertEquals(true, viewModel.uiState.value.errorText.isNotEmpty())
        assertEquals(AssistMood.SURPRISED, viewModel.uiState.value.assistMood)
    }
}
