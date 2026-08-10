package com.example.dartscore.model

data class LobbySettings(
    val minAvg: Int,
    val maxAvg: Int,
    val startScore: Int,
    val format: MatchFormat,
    val unit: MatchUnit,
    val count: Int,
    val inRule: InRule,
    val outRule: OutRule,
    /** Optional custom join code (4–12 chars). Empty = auto-generated. */
    val customCode: String = ""
) {
    val formatLabel: String
        get() = when (format) {
            MatchFormat.FIRST_TO -> "Prvi do $count"
            MatchFormat.BEST_OF -> "Najbolji od $count"
        }

    val unitLabel: String
        get() = when (unit) {
            MatchUnit.LEGS -> if (count == 1) "leg" else "legova"
            MatchUnit.SETS -> if (count == 1) "set" else "setova"
        }

    val summary: String
        get() = "$startScore · $formatLabel $unitLabel · ${inRule.label} / ${outRule.label}"
}
