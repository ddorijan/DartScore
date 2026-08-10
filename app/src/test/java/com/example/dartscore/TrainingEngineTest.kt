package com.example.dartscore

import com.example.dartscore.game.TrainingEngine
import com.example.dartscore.model.TrainingGameState
import com.example.dartscore.model.TrainingMode
import com.example.dartscore.model.TrainingVisitOutcome
import org.junit.Assert.*
import org.junit.Test

class TrainingEngineTest {

    @Test
    fun checkout121_successIncrementsTarget() {
        var state = TrainingEngine.initialState(TrainingMode.CHECKOUT_121) as TrainingGameState.Checkout121
        val result = TrainingEngine.submitCheckoutVisit(state, 121)
        assertEquals(TrainingVisitOutcome.CHECKOUT, result.outcome)
        val next = result.state as TrainingGameState.Checkout121
        assertEquals(122, next.target)
        assertEquals(1, next.successfulCheckouts)
    }

    @Test
    fun randomCheckout_successIncrementsSuccessCount() {
        var state = TrainingEngine.initialState(TrainingMode.RANDOM_CHECKOUT) as TrainingGameState.RandomCheckout
        val checkoutScore = state.target
        val result = TrainingEngine.submitCheckoutVisit(state, checkoutScore)
        assertEquals(TrainingVisitOutcome.CHECKOUT, result.outcome)
        val next = result.state as TrainingGameState.RandomCheckout
        assertEquals(1, next.successes)
        assertEquals(1, next.attempts)
    }

    @Test
    fun singlesTraining_maxNinePointsPerRound() {
        var state = TrainingEngine.initialState(TrainingMode.SINGLES) as TrainingGameState.Singles
        val result = TrainingEngine.submitSinglesRound(state, 9)
        assertEquals(2, (result.state as TrainingGameState.Singles).currentNumber)
        assertEquals(9, (result.state as TrainingGameState.Singles).totalPoints)
    }

    @Test
    fun scoreTraining_accumulatesTotal() {
        var state = TrainingEngine.initialState(TrainingMode.SCORE) as TrainingGameState.ScoreTraining
        val result = TrainingEngine.submitScoreRound(state, 140)
        val next = result.state as TrainingGameState.ScoreTraining
        assertEquals(140, next.totalScore)
        assertEquals(1, next.roundCount)
    }
}
