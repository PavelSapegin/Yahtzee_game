package application

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
import java.util.UUID
import java.util.UUID.randomUUID

fun readDice(): List<Int> {
    while (true) {
        println("Write 5 dices splitting by space")
        val input = readln().trim().split(" ")
        try {
            val dice = input.map { it.toInt() }
            if (dice.size == 5 && dice.all { it in 1..6 }) {
                return dice
            } else {
                println("Must be 5 numbers from 1 to 6!")
            }
        } catch (e: NumberFormatException) {
            println("Write only numbers!")
        }
    }
}

fun readCat(): ScoreCategory {
    val cats = ScoreCategory.entries

    println("Available categories:")
    cats.forEachIndexed { ind, it ->
        println("${ind + 1} - ${it.name}")
    }

    while (true) {
        println("Choose a number of category")
        val input = readln().toIntOrNull()
        if (input != null && input in 1..cats.size) {
            return cats[input - 1]
        }
        println("Error,please, write input from 1 to ${cats.size}")
    }
}

fun main() {
    val playerRepo = InMemoryPlayerRepository()
    val gameRepo = InMemoryGameRepository()
    val statsManager = InMemoryStatsManager(playerRepo)

    val referee = YahtzeeRulesEngine()
    val manager = GameSessionManager(referee, gameRepo)

    val players = mutableMapOf<UUID, String>()

    while (true) {
        println("Please, write name of new player or write exit for the ending.")
        val name = readlnOrNull() ?: break
        if (name != "exit") {
            val id = randomUUID()
            playerRepo.save(PlayerProfile(id, name, 1000, 0, 0F))
            players[id] = name
        } else {
            break
        }
    }

    manager.startGame(players.keys.toList())

    while (manager.currentState.status == GameStatus.IN_PROGRESS) {
        val state = manager.currentState
        val currentPlayerName = players[state.currentPlayerId]
        println("Current move: $currentPlayerName. Current score: ${state.players[state.currentPlayerId]?.currentScore}")
        println("Write cancel to cancel last move or Enter to continue:")
        val input = readln()
        if (input.lowercase() == "cancel") {
            try {
                manager.undoLastMove()
                println("Last move was canceled successfully!")
            } catch (e: Exception) {
                println("It is first move.")
            }
            continue
        }

        val dice = readDice()
        val cat = readCat()

        val request = MoveRequest(state.currentPlayerId, dice, cat)
        val result = manager.registerMove(request)
        if (result is MoveResult.Error) println("Error: ${result.errorMessage}") else println("Move was accepted!")
    }

    val record = manager.endGame()
    println("Game was ended.")

    statsManager.processGameResult(record)
    println("Final scores")
    record.finalScores.sortedByDescending { it.score }.forEach {
        println("${players[it.playerId]} : ${it.score}")
    }
}
