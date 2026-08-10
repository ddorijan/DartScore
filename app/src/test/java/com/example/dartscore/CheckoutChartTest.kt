package com.example.dartscore.game

import com.example.dartscore.model.OutRule
import org.junit.Assert.*
import org.junit.Test

class CheckoutChartTest {

    @Test
    fun checkout123_prefersSafeT19Route() {
        val suggestions = CheckoutChart.suggestions(123, OutRule.DOUBLE)
        assertEquals("T19", suggestions[0])
        assertEquals("T10", suggestions[1])
        assertEquals("D18", suggestions[2])
    }

    @Test
    fun checkout104_hasTwoDartFinish() {
        assertTrue(CheckoutChart.hasTwoDartCheckout(104, OutRule.DOUBLE))
    }

    @Test
    fun checkout103_noTwoDartFinish_doubleOut() {
        assertFalse(CheckoutChart.hasTwoDartCheckout(103, OutRule.DOUBLE))
    }

    @Test
    fun checkoutSuggestions_for60() {
        val suggestions = CheckoutChart.suggestions(60, OutRule.DOUBLE)
        assertEquals(listOf("S20", "D20", "---"), suggestions)
    }
}
