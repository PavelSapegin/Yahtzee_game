package domain.models

enum class ScoreCategory {
    // UPPER PART
    ONES,
    TWOS,
    THREES,
    FOURS,
    FIFTHS,
    SIXES,

    // LOWER PART
    THREEKIND,
    FOURKIND,
    FULL_HOUSE,
    SMALL_STRAIGHT,
    LARGE_STRAIGHT,
    YAHTZEE,
    CHANCE,

    BONUS,
}
