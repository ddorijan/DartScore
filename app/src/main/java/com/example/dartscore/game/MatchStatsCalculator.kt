package com.example.dartscore.game

import com.example.dartscore.model.*

object MatchStatsCalculator {

    fun fromGameState(state: LocalGameState): MatchStatsDetail {
        return build(
            matchId = state.matchId,
            settings = state.settings,
            playerNames = state.settings.playerNames,
            winnerIndex = state.winnerIndex ?: 0,
            legsWon = state.players.map { it.legsWon },
            visits = state.matchVisits,
            startScore = state.settings.startScore,
            outRule = state.settings.outRule,
            isCompleted = state.isFinished
        )
    }

    fun build(
        matchId: String?,
        settings: MatchSettings?,
        playerNames: List<String>,
        winnerIndex: Int,
        legsWon: List<Int>,
        visits: List<VisitRecord>,
        startScore: Int,
        outRule: OutRule = OutRule.DOUBLE,
        isCompleted: Boolean = true
    ): MatchStatsDetail {
        val playerCount = playerNames.size
        val stats = (0 until playerCount).map { index ->
            computePlayerStats(index, playerNames[index], visits, outRule)
        }
        val keyMoments = computeKeyMoments(playerNames, visits, stats)

        return MatchStatsDetail(
            matchId = matchId,
            settings = settings,
            playerNames = playerNames,
            winnerIndex = winnerIndex,
            legsWon = legsWon,
            playerStats = stats,
            keyMoments = keyMoments,
            visits = visits,
            startScore = startScore,
            outRule = outRule,
            isCompleted = isCompleted
        )
    }

    private fun computePlayerStats(
        playerIndex: Int,
        name: String,
        visits: List<VisitRecord>,
        outRule: OutRule
    ): PlayerMatchStats {
        val playerVisits = visits.filter { it.playerIndex == playerIndex }
        var totalScore = 0
        var scoringVisits = 0
        var checkoutsHit = 0
        var checkoutAttempts = 0
        var highestCheckout = 0
        var highestScore = 0

        playerVisits.forEach { visit ->
            if (!visit.bust && visit.score > 0) {
                totalScore += visit.score
                scoringVisits++
                highestScore = maxOf(highestScore, visit.score)
            }
            if (visit.remainingAfter == 0 && !visit.bust) {
                checkoutsHit++
                highestCheckout = maxOf(highestCheckout, visit.remainingBefore)
            }
            if (isCheckoutAttempt(visit.remainingBefore, outRule) && !visit.bust) {
                checkoutAttempts++
            }
        }

        val bestLegDarts = computeBestLegDarts(playerIndex, visits)

        return PlayerMatchStats(
            playerIndex = playerIndex,
            name = name,
            threeDartAverage = if (scoringVisits > 0) totalScore.toDouble() / scoringVisits else 0.0,
            checkoutPercentage = if (checkoutAttempts > 0) {
                checkoutsHit * 100.0 / checkoutAttempts
            } else 0.0,
            checkoutsHit = checkoutsHit,
            checkoutAttempts = checkoutAttempts,
            highestCheckout = highestCheckout,
            highestScore = highestScore,
            bestLegDarts = bestLegDarts
        )
    }

    private fun isCheckoutAttempt(remaining: Int, outRule: OutRule): Boolean {
        return CheckoutChart.isValidFinish(remaining, outRule) || remaining in 2..170
    }

    private fun computeBestLegDarts(playerIndex: Int, visits: List<VisitRecord>): Int? {
        val legGroups = visits.filter { it.playerIndex == playerIndex }
            .groupBy { it.leg }

        val legDartCounts = legGroups.mapNotNull { (_, legVisits) ->
            if (!legVisits.any { it.legWon }) return@mapNotNull null
            legVisits.sumOf { it.dartsUsed }
        }
        return legDartCounts.minOrNull()
    }

    private fun computeKeyMoments(
        playerNames: List<String>,
        visits: List<VisitRecord>,
        stats: List<PlayerMatchStats>
    ): List<MatchKeyMoment> {
        val moments = mutableListOf<MatchKeyMoment>()

        stats.maxByOrNull { it.highestScore }?.let { best ->
            if (best.highestScore > 0) {
                moments.add(
                    MatchKeyMoment(
                        title = "Najveći score",
                        value = best.highestScore.toString(),
                        playerName = best.name
                    )
                )
            }
        }

        stats.maxByOrNull { it.highestCheckout }?.let { best ->
            if (best.highestCheckout > 0) {
                moments.add(
                    MatchKeyMoment(
                        title = "Najveći checkout",
                        value = best.highestCheckout.toString(),
                        playerName = best.name
                    )
                )
            }
        }

        val formDrop = findFormDrop(playerNames, visits)
        if (formDrop != null) moments.add(formDrop)

        return moments.take(3)
    }

    private fun findFormDrop(playerNames: List<String>, visits: List<VisitRecord>): MatchKeyMoment? {
        var worst: MatchKeyMoment? = null
        var worstDrop = 0.0

        playerNames.indices.forEach { playerIndex ->
            val byLeg = visits.filter { it.playerIndex == playerIndex && !it.bust && it.score > 0 }
                .groupBy { it.leg }
                .toSortedMap()

            var previousAvg: Double? = null
            byLeg.forEach { (leg, legVisits) ->
                val avg = legVisits.sumOf { it.score }.toDouble() / legVisits.size
                if (previousAvg != null) {
                    val drop = previousAvg - avg
                    if (drop > worstDrop) {
                        worstDrop = drop
                        worst = MatchKeyMoment(
                            title = "Pad forme",
                            value = "Leg $leg",
                            playerName = playerNames[playerIndex]
                        )
                    }
                }
                previousAvg = avg
            }
        }
        return worst
    }
}
