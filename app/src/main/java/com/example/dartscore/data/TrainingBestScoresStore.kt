package com.example.dartscore.data

import android.content.Context
import com.example.dartscore.model.TrainingGameState
import com.example.dartscore.model.TrainingMode

data class TrainingBestScore(
    val mode: TrainingMode,
    val value: Int = 0,
    val secondary: Int = 0
) {
    val hasScore: Boolean get() = value > 0 || (mode == TrainingMode.RANDOM_CHECKOUT && secondary > 0)

    fun displayLabel(): String {
        if (!hasScore) return "Nema rekorda"
        return when (mode) {
            TrainingMode.CHECKOUT_121 -> "Najbolje: $value"
            TrainingMode.RANDOM_CHECKOUT -> {
                val rate = if (secondary == 0) 0 else (value * 100 / secondary)
                "Najbolje: $value/$secondary ($rate%)"
            }
            TrainingMode.SINGLES -> "Najbolje: $value / 180"
            TrainingMode.SCORE -> "Najbolji prosjek: %.1f".format(value / 10.0)
        }
    }
}

class TrainingBestScoresStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getAll(): Map<TrainingMode, TrainingBestScore> =
        TrainingMode.entries.associateWith { get(it) }

    fun get(mode: TrainingMode): TrainingBestScore = TrainingBestScore(
        mode = mode,
        value = prefs.getInt(keyValue(mode), 0),
        secondary = prefs.getInt(keySecondary(mode), 0)
    )

    /** Returns true if a new personal best was saved. */
    fun recordIfBetter(state: TrainingGameState): Boolean {
        return when (state) {
            is TrainingGameState.Checkout121 -> {
                if (state.successfulCheckouts <= 0 && state.highestReached <= 121) return false
                updateIfBetter(TrainingMode.CHECKOUT_121, state.highestReached, 0) { new, old ->
                    new.value > old.value
                }
            }

            is TrainingGameState.RandomCheckout -> {
                if (state.attempts <= 0) return false
                updateIfBetter(
                    TrainingMode.RANDOM_CHECKOUT,
                    state.successes,
                    state.attempts
                ) { new, old ->
                    if (!old.hasScore) return@updateIfBetter true
                    val newRate = new.value * 1000 / new.secondary
                    val oldRate = old.value * 1000 / old.secondary
                    newRate > oldRate || (newRate == oldRate && new.value > old.value)
                }
            }

            is TrainingGameState.Singles -> {
                if (!state.isFinished) return false
                updateIfBetter(TrainingMode.SINGLES, state.totalPoints, 0) { new, old ->
                    new.value > old.value
                }
            }

            is TrainingGameState.ScoreTraining -> {
                if (state.roundCount <= 0) return false
                val averageX10 = (state.threeDartAverage * 10).toInt()
                updateIfBetter(TrainingMode.SCORE, averageX10, state.roundCount) { new, old ->
                    new.value > old.value
                }
            }
        }
    }

    private fun updateIfBetter(
        mode: TrainingMode,
        value: Int,
        secondary: Int,
        isBetter: (TrainingBestScore, TrainingBestScore) -> Boolean
    ): Boolean {
        val current = get(mode)
        val candidate = TrainingBestScore(mode, value, secondary)
        if (!isBetter(candidate, current)) return false
        prefs.edit()
            .putInt(keyValue(mode), value)
            .putInt(keySecondary(mode), secondary)
            .apply()
        return true
    }

    private fun keyValue(mode: TrainingMode) = "best_${mode.name}_value"
    private fun keySecondary(mode: TrainingMode) = "best_${mode.name}_secondary"

    companion object {
        private const val PREFS_NAME = "training_best_scores"
    }
}
