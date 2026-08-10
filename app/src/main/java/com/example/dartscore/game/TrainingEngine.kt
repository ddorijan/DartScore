package com.example.dartscore.game

import com.example.dartscore.model.*
import kotlin.random.Random

object TrainingEngine {

    private val bogeyScores = setOf(159, 162, 163, 165, 166, 168, 169)
    private val checkoutableScores = (40..170).filter { it !in bogeyScores }

    fun initialState(mode: TrainingMode): TrainingGameState = when (mode) {
        TrainingMode.CHECKOUT_121 -> TrainingGameState.Checkout121()
        TrainingMode.RANDOM_CHECKOUT -> newRandomCheckout()
        TrainingMode.SINGLES -> TrainingGameState.Singles()
        TrainingMode.SCORE -> TrainingGameState.ScoreTraining()
    }

    fun submitCheckoutVisit(state: TrainingGameState, score: Int): TrainingVisitResult {
        if (score < 0 || score > 180) {
            return TrainingVisitResult(state, TrainingVisitOutcome.INVALID)
        }

        return when (state) {
            is TrainingGameState.Checkout121 -> submit121Visit(state, score)
            is TrainingGameState.RandomCheckout -> submitRandomVisit(state, score)
            else -> TrainingVisitResult(state, TrainingVisitOutcome.INVALID)
        }
    }

    fun submitSinglesRound(state: TrainingGameState.Singles, points: Int): TrainingVisitResult {
        if (state.isFinished) {
            return TrainingVisitResult(state, TrainingVisitOutcome.FINISHED)
        }
        if (points !in 0..9) {
            return TrainingVisitResult(state, TrainingVisitOutcome.INVALID)
        }

        val newRoundPoints = state.roundPoints + points
        val newTotal = state.totalPoints + points
        val nextNumber = state.currentNumber + 1
        val finished = nextNumber > 20

        val message = when {
            points == 9 -> "Savršeno! 3× treble na ${state.currentNumber}."
            points == 0 -> "0 bodova na broju ${state.currentNumber}."
            else -> "+$points boda na broju ${state.currentNumber}."
        }

        return TrainingVisitResult(
            state = state.copy(
                currentNumber = nextNumber,
                roundPoints = newRoundPoints,
                totalPoints = newTotal,
                lastMessage = if (finished) {
                    "Završeno! Ukupno $newTotal / 180 bodova."
                } else {
                    message
                }
            ),
            outcome = if (finished) TrainingVisitOutcome.FINISHED else TrainingVisitOutcome.SCORED
        )
    }

    fun submitScoreRound(state: TrainingGameState.ScoreTraining, score: Int): TrainingVisitResult {
        if (score < 0 || score > 180) {
            return TrainingVisitResult(state, TrainingVisitOutcome.INVALID)
        }

        val newScores = state.roundScores + score
        val newTotal = state.totalScore + score
        val message = when (score) {
            180 -> "Maksimum! 180!"
            in 140..179 -> "Odličan visit: $score"
            in 100..139 -> "Solidan visit: $score"
            else -> "Visit: $score"
        }

        return TrainingVisitResult(
            state = state.copy(
                roundScores = newScores,
                totalScore = newTotal,
                lastMessage = message
            ),
            outcome = TrainingVisitOutcome.SCORED
        )
    }

    fun undo121(state: TrainingGameState.Checkout121): TrainingGameState.Checkout121 {
        return state.copy(
            target = 121,
            remaining = 121,
            visitsUsed = 0,
            visitStartRemaining = 121,
            highestReached = 121,
            lockedBase = 121,
            successfulCheckouts = 0,
            lastMessage = "Sesija resetirana."
        )
    }

    fun undoRandom(state: TrainingGameState.RandomCheckout): TrainingGameState.RandomCheckout {
        return newRandomCheckout().copy(
            attempts = 0,
            successes = 0,
            lastMessage = "Sesija resetirana."
        )
    }

    fun undoSingles(state: TrainingGameState.Singles): TrainingGameState.Singles {
        return TrainingGameState.Singles(lastMessage = "Sesija resetirana.")
    }

    fun undoScore(state: TrainingGameState.ScoreTraining): TrainingGameState.ScoreTraining {
        return TrainingGameState.ScoreTraining(lastMessage = "Sesija resetirana.")
    }

    private fun submit121Visit(state: TrainingGameState.Checkout121, score: Int): TrainingVisitResult {
        val result = CheckoutTrainingEngine.applyVisit(state.remaining, score)

        if (result.checkout) {
            val dartsThisAttempt = (state.visitsUsed + 1) * 3
            val newTarget = state.target + 1
            val newLocked = if (dartsThisAttempt <= 3) newTarget else state.lockedBase
            val newHighest = maxOf(state.highestReached, newTarget)

            return TrainingVisitResult(
                state = state.copy(
                    target = newTarget,
                    remaining = newTarget,
                    visitsUsed = 0,
                    visitStartRemaining = newTarget,
                    highestReached = newHighest,
                    lockedBase = newLocked,
                    successfulCheckouts = state.successfulCheckouts + 1,
                    lastMessage = "Checkout! Sljedeći cilj: $newTarget."
                ),
                outcome = TrainingVisitOutcome.CHECKOUT
            )
        }

        val afterVisit = if (result.bust) {
            state.visitStartRemaining
        } else {
            result.remaining
        }

        val newVisitsUsed = state.visitsUsed + 1

        if (newVisitsUsed >= 3 && afterVisit > 0) {
            val droppedTarget = maxOf(state.lockedBase, state.target - 1)
            return TrainingVisitResult(
                state = state.copy(
                    target = droppedTarget,
                    remaining = droppedTarget,
                    visitsUsed = 0,
                    visitStartRemaining = droppedTarget,
                    lastMessage = if (result.bust) {
                        "Bust! Neuspjeh u 9 lotki. Cilj: $droppedTarget."
                    } else {
                        "Neuspjeh u 9 lotki. Cilj: $droppedTarget."
                    }
                ),
                outcome = if (result.bust) TrainingVisitOutcome.BUST else TrainingVisitOutcome.SCORED
            )
        }

        return TrainingVisitResult(
            state = state.copy(
                remaining = afterVisit,
                visitsUsed = newVisitsUsed,
                visitStartRemaining = if (result.bust) state.visitStartRemaining else afterVisit,
                lastMessage = when {
                    result.bust -> "Bust! Preostalo ${state.visitStartRemaining}."
                    newVisitsUsed == 2 -> "Još 1 visit (${afterVisit} preostalo)."
                    else -> "Visit ${newVisitsUsed}/3 · Preostalo $afterVisit."
                }
            ),
            outcome = if (result.bust) TrainingVisitOutcome.BUST else TrainingVisitOutcome.SCORED
        )
    }

    private fun submitRandomVisit(state: TrainingGameState.RandomCheckout, score: Int): TrainingVisitResult {
        val result = CheckoutTrainingEngine.applyVisit(state.remaining, score)
        val attempts = state.attempts + 1

        if (result.checkout) {
            val successes = state.successes + 1
            val next = newRandomCheckout()
            return TrainingVisitResult(
                state = next.copy(
                    attempts = attempts,
                    successes = successes,
                    lastMessage = "Checkout ${state.target}! Sljedeći: ${next.target}."
                ),
                outcome = TrainingVisitOutcome.CHECKOUT
            )
        }

        val next = newRandomCheckout()
        return TrainingVisitResult(
            state = next.copy(
                attempts = attempts,
                successes = state.successes,
                lastMessage = when {
                    result.bust -> "Bust na ${state.target}. Sljedeći: ${next.target}."
                    else -> "Promašaj. Sljedeći: ${next.target}."
                }
            ),
            outcome = if (result.bust) TrainingVisitOutcome.BUST else TrainingVisitOutcome.SCORED
        )
    }

    private fun newRandomCheckout(): TrainingGameState.RandomCheckout {
        val target = checkoutableScores.random(Random.Default)
        return TrainingGameState.RandomCheckout(target = target, remaining = target)
    }
}
