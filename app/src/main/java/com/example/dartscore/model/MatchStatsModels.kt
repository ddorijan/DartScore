package com.example.dartscore.model

import com.example.dartscore.game.CheckoutChart

data class PlayerMatchStats(
    val playerIndex: Int,
    val name: String,
    val threeDartAverage: Double,
    val checkoutPercentage: Double,
    val checkoutsHit: Int,
    val checkoutAttempts: Int,
    val highestCheckout: Int,
    val highestScore: Int,
    val bestLegDarts: Int?
)

data class MatchKeyMoment(
    val title: String,
    val value: String,
    val playerName: String
)

data class MatchStatsDetail(
    val matchId: String?,
    val settings: MatchSettings?,
    val playerNames: List<String>,
    val winnerIndex: Int,
    val legsWon: List<Int>,
    val playerStats: List<PlayerMatchStats>,
    val keyMoments: List<MatchKeyMoment>,
    val visits: List<VisitRecord>,
    val startScore: Int,
    val outRule: OutRule = OutRule.DOUBLE,
    val isCompleted: Boolean = true
) {
    val winnerName: String get() = playerNames.getOrElse(winnerIndex) { "?" }

    fun legsScoreLabel(): String =
        legsWon.joinToString(" : ") { it.toString() }
}
