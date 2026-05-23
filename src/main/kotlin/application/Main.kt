package application

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import application.services.GameSessionManager
import data.database.GameResultsTable
import data.database.GamesTable
import data.database.MoveHistoryTable
import data.database.PlayersTable
import data.repositories.InMemoryStatsManager
import data.repositories.SqliteGameRepository
import data.repositories.SqlitePlayerRepository
import domain.engine.YahtzeeRulesEngine
import gui.YahtzeeViewModel
import gui.yahtzeeAppScreen
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun main() =
    application {
        Database.connect(url = "jdbc:sqlite:./yahtzee.db", driver = "org.sqlite.JDBC")
        transaction {
            SchemaUtils.create(PlayersTable, GamesTable, GameResultsTable, MoveHistoryTable)
        }
        val playerRepo = SqlitePlayerRepository()
        val gameRepo = SqliteGameRepository()
        val statsManager = InMemoryStatsManager(playerRepo)
        val referee = YahtzeeRulesEngine()

        val manager = GameSessionManager(referee, gameRepo)

        val viewModel = YahtzeeViewModel(manager, playerRepo, statsManager)

        Window(onCloseRequest = ::exitApplication, title = "Yahtzee Assistant") {
            yahtzeeAppScreen(viewModel)
        }
    }
