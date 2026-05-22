package application

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import application.services.GameSessionManager
import data.repositories.InMemoryGameRepository
import data.repositories.InMemoryPlayerRepository
import data.repositories.InMemoryStatsManager
import domain.engine.YahtzeeRulesEngine
import gui.YahtzeeViewModel
import gui.yahtzeeAppScreen

fun main() =
    application {
        val playerRepo = InMemoryPlayerRepository()
        val gameRepo = InMemoryGameRepository()
        val statsManager = InMemoryStatsManager(playerRepo)
        val referee = YahtzeeRulesEngine()

        val manager = GameSessionManager(referee, gameRepo)

        val viewModel = YahtzeeViewModel(manager, playerRepo, statsManager)

        Window(onCloseRequest = ::exitApplication, title = "Yahtzee Assistant") {
            yahtzeeAppScreen(viewModel)
        }
    }
