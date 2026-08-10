package com.example.dartscore.game

import com.example.dartscore.model.OutRule

object CheckoutTrainingEngine {

    data class ApplyResult(
        val remaining: Int,
        val bust: Boolean,
        val checkout: Boolean
    )

    fun applyVisit(
        remainingBefore: Int,
        score: Int,
        outRule: OutRule = OutRule.DOUBLE
    ): ApplyResult {
        if (score < 0 || score > 180) {
            return ApplyResult(remainingBefore, bust = false, checkout = false)
        }

        if (score == 0) {
            return ApplyResult(remainingBefore, bust = false, checkout = false)
        }

        val newRemaining = remainingBefore - score

        if (newRemaining < 0) {
            return ApplyResult(remainingBefore, bust = true, checkout = false)
        }

        if (outRule == OutRule.DOUBLE && newRemaining == 1) {
            return ApplyResult(remainingBefore, bust = true, checkout = false)
        }

        if (newRemaining == 0) {
            val valid = CheckoutChart.isValidFinish(remainingBefore, outRule)
            return ApplyResult(
                remaining = if (valid) 0 else remainingBefore,
                bust = !valid,
                checkout = valid
            )
        }

        return ApplyResult(newRemaining, bust = false, checkout = false)
    }
}
