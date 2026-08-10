package com.example.dartscore.game

import com.example.dartscore.model.OutRule

object CheckoutDartOptions {

    fun optionsFor(outRule: OutRule): List<Int> = when (outRule) {
        OutRule.DOUBLE -> listOf(2, 3)
        OutRule.STRAIGHT, OutRule.MASTER -> listOf(1, 2, 3)
    }

    fun isValid(dartsUsed: Int, outRule: OutRule): Boolean =
        dartsUsed in optionsFor(outRule)

    fun requiresPrompt(score: Int, remainingBefore: Int, outRule: OutRule): Boolean {
        if (remainingBefore - score != 0) return false
        return CheckoutChart.isValidFinish(remainingBefore, outRule)
    }
}
