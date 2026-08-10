package com.example.dartscore.model

enum class TrainingMode(val title: String, val subtitle: String) {
    CHECKOUT_121(
        title = "121 Checkout",
        subtitle = "Checkout od 121 naviše · max 9 lotki · double out"
    ),
    RANDOM_CHECKOUT(
        title = "Random checkout",
        subtitle = "Nasumični checkout 40–170 · 3 lotke · double out"
    ),
    SINGLES(
        title = "Singles training",
        subtitle = "Brojevi 1–20 · single=1, double=2, treble=3 bod"
    ),
    SCORE(
        title = "Score training",
        subtitle = "Maksimalan rezultat svaki visit · 3 lotke"
    )
}

sealed class TrainingGameState {
    abstract val mode: TrainingMode

    data class Checkout121(
        val target: Int = 121,
        val remaining: Int = 121,
        val visitsUsed: Int = 0,
        val visitStartRemaining: Int = 121,
        val highestReached: Int = 121,
        val lockedBase: Int = 121,
        val successfulCheckouts: Int = 0,
        val lastMessage: String? = null
    ) : TrainingGameState() {
        override val mode = TrainingMode.CHECKOUT_121
    }

    data class RandomCheckout(
        val target: Int,
        val remaining: Int,
        val attempts: Int = 0,
        val successes: Int = 0,
        val lastMessage: String? = null
    ) : TrainingGameState() {
        override val mode = TrainingMode.RANDOM_CHECKOUT
    }

    data class Singles(
        val currentNumber: Int = 1,
        val roundPoints: List<Int> = emptyList(),
        val totalPoints: Int = 0,
        val lastMessage: String? = null
    ) : TrainingGameState() {
        override val mode = TrainingMode.SINGLES
        val isFinished: Boolean get() = currentNumber > 20
    }

    data class ScoreTraining(
        val roundScores: List<Int> = emptyList(),
        val totalScore: Int = 0,
        val lastMessage: String? = null
    ) : TrainingGameState() {
        override val mode = TrainingMode.SCORE
        val roundCount: Int get() = roundScores.size
        val threeDartAverage: Double
            get() = if (roundScores.isEmpty()) 0.0 else roundScores.average()
    }
}

enum class TrainingVisitOutcome {
    SCORED,
    BUST,
    CHECKOUT,
    INVALID,
    FINISHED
}

data class TrainingVisitResult(
    val state: TrainingGameState,
    val outcome: TrainingVisitOutcome
)
