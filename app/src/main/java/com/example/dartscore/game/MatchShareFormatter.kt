package com.example.dartscore.game

import com.example.dartscore.model.MatchStatsDetail

object MatchShareFormatter {

    fun toShareText(detail: MatchStatsDetail): String = buildString {
        appendLine("🎯 DartScore — Statistika meča")
        appendLine()
        appendLine("Pobjednik: ${detail.winnerName}")

        if (detail.playerNames.size == 2) {
            appendLine(
                "${detail.playerNames[0]} ${detail.legsWon.getOrElse(0) { 0 }} : " +
                    "${detail.legsWon.getOrElse(1) { 0 }} ${detail.playerNames[1]}"
            )
            val left = detail.playerStats.getOrNull(0)
            val right = detail.playerStats.getOrNull(1)
            if (left != null && right != null) {
                appendLine(
                    "Prosjek: ${"%.2f".format(left.threeDartAverage)} vs ${"%.2f".format(right.threeDartAverage)}"
                )
                appendLine(
                    "Checkout: ${left.checkoutsHit}/${left.checkoutAttempts} vs ${right.checkoutsHit}/${right.checkoutAttempts}"
                )
            }
        } else {
            appendLine("Legovi: ${detail.legsScoreLabel()}")
            detail.playerStats.forEach { stats ->
                appendLine(
                    "${stats.name}: prosjek ${"%.2f".format(stats.threeDartAverage)}, " +
                        "max score ${stats.highestScore}"
                )
            }
        }

        if (detail.keyMoments.isNotEmpty()) {
            appendLine()
            appendLine("Ključni trenutci:")
            detail.keyMoments.forEach { moment ->
                appendLine("• ${moment.title}: ${moment.value} (${moment.playerName})")
            }
        }

        appendLine()
        append("DartScore")
    }

    fun toFeedMessage(detail: MatchStatsDetail): String =
        "${detail.winnerName} pobjeđuje!"

    fun toFeedDetail(detail: MatchStatsDetail): String {
        val lines = mutableListOf<String>()
        lines += detail.playerNames.joinToString(" vs ")
        if (detail.playerNames.size == 2) {
            val left = detail.playerStats.getOrNull(0)
            val right = detail.playerStats.getOrNull(1)
            if (left != null && right != null) {
                lines += "Prosjek: ${"%.2f".format(left.threeDartAverage)} | ${"%.2f".format(right.threeDartAverage)}"
                lines += "Checkout %: ${left.checkoutPercentage.toInt()}% | ${right.checkoutPercentage.toInt()}%"
            }
        }
        detail.keyMoments.firstOrNull()?.let { lines += "${it.title}: ${it.value} (${it.playerName})" }
        return lines.joinToString("\n")
    }

    fun toFeedScoreHighlight(detail: MatchStatsDetail): String =
        if (detail.playerNames.size >= 2) {
            "${detail.legsWon.getOrElse(0) { 0 }} : ${detail.legsWon.getOrElse(1) { 0 }}"
        } else {
            detail.legsScoreLabel()
        }
}
