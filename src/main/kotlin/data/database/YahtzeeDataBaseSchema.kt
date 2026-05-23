package data.database

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object PlayersTable : Table("players") {
    val id = uuid("id")
    val name = varchar("name", 50)
    val eloRating = integer("elo_rating")
    val gamesPlayed = integer("games_played")
    val wins = integer("wins")
    val winRate = float("win_rate")
    override val primaryKey = PrimaryKey(id)
}

object GamesTable : Table("games") {
    val id = uuid("id")
    val date = datetime("date")
    override val primaryKey = PrimaryKey(id)
}

object GameResultsTable : Table("game_results") {
    val gameId = uuid("game_id").references(GamesTable.id)
    val playerId = uuid("player_id").references(PlayersTable.id)
    val score = integer("score")
    val rank = integer("rank")
}

object MoveHistoryTable : Table("move_history") {
    val gameId = uuid("game_id").references(GamesTable.id)
    val moveNumber = integer("move_number")
    val playerId = uuid("player_id").references(PlayersTable.id)
    val category = varchar("category", 20)
    val pointsScored = integer("points_scored")
    val timestamp = datetime("timestamp")
    val dice = varchar("dice", 20)
}
