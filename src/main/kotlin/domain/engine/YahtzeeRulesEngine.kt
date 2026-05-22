package domain.engine

import domain.models.BoardState
import domain.models.MoveRequest
import domain.models.ScoreCategory
import domain.models.ScoreEvent
import domain.models.ValidationResult

class YahtzeeRulesEngine : IRulesEngine {
    private val upperCategories =
        listOf(
            ScoreCategory.ONES,
            ScoreCategory.TWOS,
            ScoreCategory.THREES,
            ScoreCategory.FOURS,
            ScoreCategory.FIFTHS,
            ScoreCategory.SIXES,
        )

    private val lowerCategories =
        listOf(
            ScoreCategory.THREEKIND,
            ScoreCategory.FOURKIND,
            ScoreCategory.FULL_HOUSE,
            ScoreCategory.SMALL_STRAIGHT,
            ScoreCategory.LARGE_STRAIGHT,
            ScoreCategory.CHANCE,
        )

    override fun validateMove(
        board: BoardState,
        move: MoveRequest,
    ): ValidationResult {
        val sheet = board.playerSheets[move.playerID] ?: return ValidationResult.Error("Player not found")

        if (sheet.filledCategories.containsKey(move.targetCategory)) {
            return ValidationResult.Error("This category is already taken")
        }

        val isJoker = isYahtzee(move.finalDice) && sheet.filledCategories[ScoreCategory.YAHTZEE] == 50

        if (isJoker) {
            val requiredUpperCat = upperCategories[move.finalDice[0] - 1]
            if (!sheet.filledCategories.containsKey(requiredUpperCat)) {
                return if (requiredUpperCat == move.targetCategory) {
                    ValidationResult.Correct
                } else {
                    ValidationResult.Error("With Joker rule you MUST choose the category $requiredUpperCat")
                }
            } else {
                val freeLowerCats = lowerCategories.filter { !sheet.filledCategories.containsKey(it) }
                if (freeLowerCats.isNotEmpty()) {
                    return if (move.targetCategory in freeLowerCats) {
                        ValidationResult.Correct
                    } else {
                        ValidationResult.Error("With Joker rule you MUST choose some free category in down section.")
                    }
                }
            }
        }

        return ValidationResult.Correct
    }

    private fun isNOfAKind(
        dice: List<Int>,
        num: Int,
    ): Boolean {
        return dice.groupBy { it }.values.any { it.size >= num }
    }

    private fun isFullHouse(dice: List<Int>): Boolean {
        val counter = dice.groupingBy { it }.eachCount()
        val counts = counter.values.sorted()
        if (counts.size == 2) {
            return counts == listOf(2, 3)
        } else if (counts.size == 1) {
            return counts == listOf(5)
        }
        return false
    }

    private fun isAnyStraight(
        dice: List<Int>,
        num: Int,
    ): Boolean {
        val newDice = dice.distinct().sorted()
        var sortedLen = 1
        for (i in 1 until newDice.size) {
            if (newDice[i] != newDice[i - 1] + 1) {
                sortedLen = 1
            } else {
                sortedLen++
            }
            if (sortedLen == num) {
                return true
            }
        }

        return false
    }

    private fun isYahtzee(dice: List<Int>): Boolean {
        return dice.groupingBy { it }.eachCount().values.size == 1
    }

    override fun calculateIntermediateScore(
        board: BoardState,
        move: MoveRequest,
    ): ScoreEvent {
        if (move.finalDice.size != 5) {
            throw NoSuchElementException("There should be 5 dices")
        }

        val sheet = board.playerSheets[move.playerID]
        val isJoker = isYahtzee(move.finalDice) && sheet?.filledCategories[ScoreCategory.YAHTZEE] == 50
        val bonusPoints = if (isJoker) 100 else 0

        val points =
            when (move.targetCategory) {
                // UPPER PART
                ScoreCategory.ONES -> move.finalDice.filter { it == 1 }.sum()
                ScoreCategory.TWOS -> move.finalDice.filter { it == 2 }.sum()
                ScoreCategory.THREES -> move.finalDice.filter { it == 3 }.sum()
                ScoreCategory.FOURS -> move.finalDice.filter { it == 4 }.sum()
                ScoreCategory.FIFTHS -> move.finalDice.filter { it == 5 }.sum()
                ScoreCategory.SIXES -> move.finalDice.filter { it == 6 }.sum()

                // LOWER PART
                ScoreCategory.THREEKIND -> if (isJoker || isNOfAKind(move.finalDice, 3)) move.finalDice.sum() else 0
                ScoreCategory.FOURKIND -> if (isJoker || isNOfAKind(move.finalDice, 4)) move.finalDice.sum() else 0
                ScoreCategory.FULL_HOUSE -> if (isJoker || isFullHouse(move.finalDice)) 25 else 0
                ScoreCategory.SMALL_STRAIGHT -> if (isJoker || isAnyStraight(move.finalDice, 4)) 30 else 0
                ScoreCategory.LARGE_STRAIGHT -> if (isJoker || isAnyStraight(move.finalDice, 5)) 40 else 0
                ScoreCategory.YAHTZEE -> if (isYahtzee(move.finalDice)) 50 else 0

                ScoreCategory.CHANCE -> move.finalDice.sum()

                ScoreCategory.BONUS -> 0
            }
        return ScoreEvent(
            move.playerID,
            points = points + bonusPoints,
            category = move.targetCategory,
            isBonusApplied = isJoker,
        )
    }

    override fun calculateFinalScore(board: BoardState): List<ScoreEvent> {
        val bonusEvent = mutableListOf<ScoreEvent>()
        for ((player, sheet) in board.playerSheets) {
            var upperPartScore = 0

            for (cat in upperCategories) {
                val catValue: Int = sheet.filledCategories[cat] ?: 0

                upperPartScore += catValue
            }

            if (upperPartScore >= 63) {
                bonusEvent.add(
                    ScoreEvent(
                        player,
                        points = 35,
                        category = ScoreCategory.BONUS,
                        isBonusApplied = true,
                    ),
                )
            }
        }

        return bonusEvent
    }
}
