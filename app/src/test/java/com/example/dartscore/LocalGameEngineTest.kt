package com.example.dartscore

import com.example.dartscore.game.CheckoutChart
import com.example.dartscore.game.CheckoutDartOptions
import com.example.dartscore.game.LocalGameEngine
import com.example.dartscore.game.MatchStatsCalculator
import com.example.dartscore.game.VisitOutcome
import com.example.dartscore.model.*
import org.junit.Assert.*
import org.junit.Test

class LocalGameEngineTest {

    @Test
    fun checkout60_isValidWithDoubleOut() {
        val state = LocalGameEngine.createInitialState(
            MatchSettings(
                playerNames = listOf("Alice", "Bob"),
                startScore = 60,
                outRule = OutRule.DOUBLE
            )
        )
        val pending = LocalGameEngine.submitVisit(state, 60)
        assertEquals(VisitOutcome.CHECKOUT_PENDING, pending.outcome)
        val result = LocalGameEngine.confirmCheckout(state, 60, 2)
        assertEquals(VisitOutcome.LEG_WON, result.outcome)
        assertEquals(1, result.state.players[0].legsWon)
        assertEquals(2, result.state.matchVisits.last().dartsUsed)
    }

    @Test
    fun bestLegCountsDartsNotVisits() {
        var state = LocalGameEngine.createInitialState(
            MatchSettings(
                playerNames = listOf("Alice", "Bob"),
                startScore = 120,
                outRule = OutRule.STRAIGHT,
                count = 3
            )
        )
        state = LocalGameEngine.submitVisit(state, 60).state
        state = LocalGameEngine.submitVisit(state, 0).state
        val pending = LocalGameEngine.submitVisit(state, 60)
        state = LocalGameEngine.confirmCheckout(pending.state, 60, 2).state

        val detail = MatchStatsCalculator.fromGameState(
            state.copy(isFinished = true, winnerIndex = 0)
        )
        assertEquals(5, detail.playerStats[0].bestLegDarts)
    }

    @Test
    fun turnsAlternateBetweenTwoPlayers() {
        var state = LocalGameEngine.createInitialState(
            MatchSettings(playerNames = listOf("Alice", "Bob"))
        )
        assertEquals(0, state.currentPlayerIndex)

        state = LocalGameEngine.submitVisit(state, 60).state
        assertEquals(1, state.currentPlayerIndex)
        assertEquals(441, state.players[0].remaining)

        state = LocalGameEngine.submitVisit(state, 60).state
        assertEquals(0, state.currentPlayerIndex)
        assertEquals(441, state.players[1].remaining)
    }

    @Test
    fun bustRestoresScoreAndAdvancesTurn() {
        var state = LocalGameEngine.createInitialState(
            MatchSettings(
                playerNames = listOf("Alice", "Bob"),
                startScore = 40,
                outRule = OutRule.DOUBLE
            )
        )
        state = LocalGameEngine.submitVisit(state, 39).state
        assertEquals(40, state.players[0].remaining)
        assertEquals(1, state.currentPlayerIndex)

        state = LocalGameEngine.submitVisit(state, 20).state
        assertEquals(20, state.players[1].remaining)
        assertEquals(0, state.currentPlayerIndex)
    }

    @Test
    fun rejectsVisitAbove180() {
        val state = LocalGameEngine.createInitialState(
            MatchSettings(playerNames = listOf("Alice", "Bob"))
        )
        val result = LocalGameEngine.submitVisit(state, 200)
        assertEquals(VisitOutcome.INVALID_SCORE, result.outcome)
        assertEquals(501, result.state.players[0].remaining)
    }

    @Test
    fun checkoutSuggestions_for60() {
        val suggestions = CheckoutChart.suggestions(60, OutRule.DOUBLE)
        assertEquals(listOf("S20", "D20", "---"), suggestions)
    }

    @Test
    fun legStarterRotatesBetweenPlayers() {
        assertEquals(0, LocalGameEngine.legStarterIndex(1, 2))
        assertEquals(1, LocalGameEngine.legStarterIndex(2, 2))
        assertEquals(0, LocalGameEngine.legStarterIndex(3, 2))
        assertEquals(1, LocalGameEngine.legStarterIndex(4, 2))
    }

    @Test
    fun threeDartAveragePersistsAcrossLegs() {
        var state = LocalGameEngine.createInitialState(
            MatchSettings(
                playerNames = listOf("Alice", "Bob"),
                startScore = 60,
                outRule = OutRule.STRAIGHT,
                count = 3
            )
        )
        state = LocalGameEngine.submitVisit(state, 60).state
        val pending = LocalGameEngine.submitVisit(state, 60)
        state = LocalGameEngine.confirmCheckout(pending.state, 60, 1).state
        assertEquals(60.0, state.players[0].threeDartAverage, 0.01)
        assertEquals(2, state.currentLeg)
        assertEquals(1, state.currentPlayerIndex)

        state = LocalGameEngine.submitVisit(state, 30).state
        assertEquals(60.0, state.players[0].threeDartAverage, 0.01)
        assertEquals(30.0, state.players[1].threeDartAverage, 0.01)
    }

    @Test
    fun bustShowsInVisitHistory() {
        var state = LocalGameEngine.createInitialState(
            MatchSettings(
                playerNames = listOf("Alice", "Bob"),
                startScore = 40,
                outRule = OutRule.DOUBLE
            )
        )
        state = LocalGameEngine.submitVisit(state, 39).state
        assertEquals(LegVisitEntry.Bust, state.players[0].visitHistory.last())
    }
}
