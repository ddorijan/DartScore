package com.example.dartscore.model

enum class MatchFormat { FIRST_TO, BEST_OF }

enum class MatchUnit { LEGS, SETS }

enum class InRule(val label: String) {
    STRAIGHT("Straight in"),
    DOUBLE("Double in"),
    MASTER("Master in")
}

enum class OutRule(val label: String) {
    STRAIGHT("Straight out"),
    DOUBLE("Double out"),
    MASTER("Master out")
}

data class MatchSettings(
    val startScore: Int = 501,
    val playerNames: List<String>,
    val format: MatchFormat = MatchFormat.FIRST_TO,
    val unit: MatchUnit = MatchUnit.LEGS,
    val count: Int = 5,
    val inRule: InRule = InRule.STRAIGHT,
    val outRule: OutRule = OutRule.DOUBLE
) {
    init {
        require(playerNames.size in 2..8) { "Need 2-8 players" }
        require(count in 1..21) { "Count must be 1-21" }
    }

    val formatLabel: String
        get() = when (format) {
            MatchFormat.FIRST_TO -> "Prvi do $count"
            MatchFormat.BEST_OF -> "Najbolji od $count"
        }

    val unitLabel: String
        get() = when (unit) {
            MatchUnit.LEGS -> if (count == 1) "Leg" else "Legovi"
            MatchUnit.SETS -> if (count == 1) "Set" else "Setovi"
        }

    val modeLabel: String
        get() = "$startScore - STANDART"
}

data class PlayerGameState(
    val name: String,
    val remaining: Int,
    val legsWon: Int = 0,
    val setsWon: Int = 0,
    val legsInCurrentSet: Int = 0,
    /** Scores and busts from the current leg only (shown in Zadnji hitci). */
    val visitHistory: List<LegVisitEntry> = emptyList(),
    val hasOpened: Boolean = false,
    /** Match-wide totals used for 3-dart average across all legs. */
    val matchTotalScore: Int = 0,
    val matchVisitCount: Int = 0
) {
    val threeDartAverage: Double
        get() = if (matchVisitCount == 0) 0.0 else matchTotalScore.toDouble() / matchVisitCount
}

data class LocalGameState(
    val settings: MatchSettings,
    val players: List<PlayerGameState>,
    val currentPlayerIndex: Int = 0,
    val currentLeg: Int = 1,
    val currentSet: Int = 1,
    val lastTurnStartRemaining: Int? = null,
    val lastVisitPlayerIndex: Int? = null,
    val matchVisits: List<VisitRecord> = emptyList(),
    val matchId: String? = null,
    val isFinished: Boolean = false,
    val winnerIndex: Int? = null
) {
    val currentPlayer: PlayerGameState get() = players[currentPlayerIndex]

    /** Player 0 always on the left in head-to-head view. */
    val leftPlayerIndex: Int
        get() = if (players.size == 2) 0 else currentPlayerIndex

    /** Player 1 on the right for 2-player; next in rotation for 3+. */
    val rightPlayerIndex: Int
        get() = if (players.size == 2) 1 else (currentPlayerIndex + 1) % players.size

    val legsDisplay: String
        get() = if (players.size == 2) {
            "${players[0].legsWon} | ${players[1].legsWon}"
        } else {
            players.joinToString(" | ") { it.legsWon.toString() }
        }

    val progressLabel: String
        get() = when (settings.unit) {
            MatchUnit.LEGS -> "Leg $currentLeg / ${settings.count}"
            MatchUnit.SETS -> "Set $currentSet / ${settings.count}"
        }
}
