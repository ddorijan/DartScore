package com.example.dartscore.game

import com.example.dartscore.model.OutRule

object CheckoutChart {
    private val BOGEY = setOf(159, 162, 163, 165, 166, 168, 169)

    private val ROUTES: Map<Int, List<String>> = buildMap {
        put(170, listOf("T20", "T20", "Bull"))
        put(167, listOf("T20", "T19", "Bull"))
        put(164, listOf("T20", "T18", "Bull"))
        put(161, listOf("T20", "T17", "Bull"))
        put(160, listOf("T20", "T20", "D20"))
        put(158, listOf("T20", "T20", "D19"))
        put(157, listOf("T20", "T19", "D20"))
        put(156, listOf("T20", "T20", "D18"))
        put(155, listOf("T20", "T19", "D19"))
        put(154, listOf("T20", "T18", "D20"))
        put(153, listOf("T20", "T19", "D18"))
        put(152, listOf("T20", "T20", "D16"))
        put(151, listOf("T20", "T17", "D20"))
        put(150, listOf("T20", "T18", "D18"))
        put(149, listOf("T20", "T19", "D16"))
        put(148, listOf("T20", "T16", "D20"))
        put(147, listOf("T20", "T17", "D18"))
        put(146, listOf("T20", "T18", "D16"))
        put(145, listOf("T20", "T19", "D14"))
        put(144, listOf("T20", "T20", "D12"))
        put(143, listOf("T20", "T17", "D16"))
        put(142, listOf("T20", "T18", "D16"))
        put(141, listOf("T20", "T19", "D12"))
        put(140, listOf("T20", "T20", "D10"))
        put(139, listOf("T20", "T19", "D11"))
        put(138, listOf("T20", "T18", "D12"))
        put(137, listOf("T20", "T19", "D10"))
        put(136, listOf("T20", "T20", "D8"))
        put(135, listOf("T20", "T17", "D12"))
        put(134, listOf("T20", "T14", "D20"))
        put(133, listOf("T20", "T19", "D8"))
        put(132, listOf("T20", "T20", "D6"))
        put(131, listOf("T20", "T13", "D16"))
        put(130, listOf("T20", "T18", "D12"))
        put(129, listOf("T19", "T16", "D18"))
        put(128, listOf("T20", "T20", "D4"))
        put(127, listOf("T20", "T17", "D8"))
        put(126, listOf("T19", "T19", "D6"))
        put(125, listOf("T20", "T15", "D10"))
        put(124, listOf("T20", "T16", "D8"))
        put(123, listOf("T19", "T10", "D18"))
        put(122, listOf("T18", "T18", "D7"))
        put(121, listOf("T20", "T11", "D14"))
        put(120, listOf("T20", "S20", "D20"))
        put(119, listOf("T19", "T10", "D16"))
        put(118, listOf("T20", "S18", "D20"))
        put(117, listOf("T20", "T19", "D10"))
        put(116, listOf("T20", "T16", "D14"))
        put(115, listOf("T20", "S15", "D20"))
        put(114, listOf("T20", "T14", "D16"))
        put(113, listOf("T20", "T13", "D20"))
        put(112, listOf("T20", "T12", "D20"))
        put(111, listOf("T20", "T11", "D19"))
        put(110, listOf("T20", "Bull"))
        put(109, listOf("T20", "S9", "D20"))
        put(108, listOf("T20", "S8", "D20"))
        put(107, listOf("T19", "T10", "D20"))
        put(106, listOf("T20", "S6", "D20"))
        put(105, listOf("T20", "S5", "D20"))
        put(104, listOf("T18", "T18", "D5"))
        put(103, listOf("T19", "S6", "D20"))
        put(102, listOf("T20", "S2", "D20"))
        put(101, listOf("T20", "S1", "D20"))
        put(100, listOf("T20", "D20"))
        put(99, listOf("T19", "S10", "D16"))
        put(98, listOf("T20", "D19"))
        put(97, listOf("T19", "D20"))
        put(96, listOf("T20", "D18"))
        put(95, listOf("T19", "D19"))
        put(94, listOf("T18", "D20"))
        put(93, listOf("T19", "D18"))
        put(92, listOf("T20", "D16"))
        put(91, listOf("T17", "D20"))
        put(90, listOf("T20", "D15"))
        put(89, listOf("T19", "D16"))
        put(88, listOf("T20", "D14"))
        put(87, listOf("T17", "D18"))
        put(86, listOf("T18", "D16"))
        put(85, listOf("T19", "D14"))
        put(84, listOf("T20", "D12"))
        put(83, listOf("T17", "D16"))
        put(82, listOf("T14", "D20"))
        put(81, listOf("T19", "D12"))
        put(80, listOf("T20", "D10"))
        put(79, listOf("T19", "D11"))
        put(78, listOf("T18", "D12"))
        put(77, listOf("T19", "D10"))
        put(76, listOf("T20", "D8"))
        put(75, listOf("T17", "D12"))
        put(74, listOf("T14", "D16"))
        put(73, listOf("T19", "D8"))
        put(72, listOf("T20", "D6"))
        put(71, listOf("T13", "D16"))
        put(70, listOf("T18", "D8"))
        put(69, listOf("T19", "D6"))
        put(68, listOf("T20", "D4"))
        put(67, listOf("T17", "D8"))
        put(66, listOf("T10", "D18"))
        put(65, listOf("T19", "D4"))
        put(64, listOf("T16", "D8"))
        put(63, listOf("T13", "D12"))
        put(62, listOf("T10", "D16"))
        put(61, listOf("T15", "D8"))
        put(60, listOf("S20", "D20"))
        put(59, listOf("S19", "D20"))
        put(58, listOf("S18", "D20"))
        put(57, listOf("S17", "D20"))
        put(56, listOf("T16", "D4"))
        put(55, listOf("S15", "D20"))
        put(54, listOf("S14", "D20"))
        put(53, listOf("S13", "D20"))
        put(52, listOf("T12", "D8"))
        put(51, listOf("S11", "D20"))
        put(50, listOf("Bull"))
        put(49, listOf("S9", "D20"))
        put(48, listOf("S16", "D16"))
        put(47, listOf("S15", "D16"))
        put(46, listOf("S6", "D20"))
        put(45, listOf("S13", "D16"))
        put(44, listOf("S12", "D16"))
        put(43, listOf("S11", "D16"))
        put(42, listOf("S10", "D16"))
        put(41, listOf("S9", "D16"))

        for (score in 2..40 step 2) {
            putIfAbsent(score, listOf("D${score / 2}"))
        }
    }

    private val BOGEY_SETUP = mapOf(
        169 to listOf("T20", "→160", "---"),
        168 to listOf("T20", "→160", "---"),
        166 to listOf("T20", "→160", "---"),
        165 to listOf("T20", "→160", "---"),
        163 to listOf("T20", "→160", "---"),
        162 to listOf("T20", "→160", "---"),
        159 to listOf("T20", "→160", "---")
    )

    private val NO_TWO_DART = BOGEY + setOf(103)

    fun suggestions(remaining: Int, outRule: OutRule = OutRule.DOUBLE): List<String> {
        if (remaining <= 0 || remaining > 170) {
            return listOf("---", "---", "---")
        }

        if (remaining in BOGEY) {
            return BOGEY_SETUP[remaining] ?: listOf("S1", "↓", "---")
        }

        if (outRule == OutRule.STRAIGHT && remaining == 1) {
            return listOf("S1", "---", "---")
        }

        if (outRule == OutRule.DOUBLE) {
            safeThreeDartRoute(remaining)?.let { return padToThree(it) }
        }

        val route = ROUTES[remaining] ?: defaultRoute(remaining)
        return padToThree(route)
    }

    fun hasTwoDartCheckout(remaining: Int, outRule: OutRule): Boolean {
        if (outRule != OutRule.DOUBLE) {
            return remaining in 2..60
        }
        if (remaining in NO_TWO_DART || remaining < 2 || remaining > 110) return false
        if (remaining in 2..40 && remaining % 2 == 0) return true
        if (remaining == 50) return true

        for (first in 1..60) {
            if (!isRealisticDartScore(first)) continue
            val left = remaining - first
            if (left == 50 || (left in 2..40 && left % 2 == 0)) return true
        }
        return false
    }

    private fun safeThreeDartRoute(remaining: Int): List<String>? {
        if (remaining !in 61..170) return null

        for (segment in listOf(19, 18, 20, 17, 16, 15, 14, 13, 12, 11, 10)) {
            val triple = segment * 3
            if (triple >= remaining) continue

            val afterSingleMiss = remaining - segment
            if (!hasTwoDartCheckout(afterSingleMiss, OutRule.DOUBLE)) continue

            val afterTriple = remaining - triple
            val tail = finishRoute(afterTriple, OutRule.DOUBLE, dartsLeft = 2) ?: continue
            return buildList {
                add("T$segment")
                addAll(tail)
            }
        }
        return null
    }

    private fun finishRoute(remaining: Int, outRule: OutRule, dartsLeft: Int): List<String>? {
        if (dartsLeft <= 0) return null
        if (dartsLeft == 1) {
            return when {
                remaining == 50 -> listOf("Bull")
                remaining in 2..40 && remaining % 2 == 0 -> listOf("D${remaining / 2}")
                else -> null
            }
        }
        if (dartsLeft == 2 && hasTwoDartCheckout(remaining, outRule)) {
            ROUTES[remaining]?.takeIf { it.size == 2 }?.let { return it }
            return twoDartRoute(remaining)
        }
        return ROUTES[remaining]
    }

    private fun twoDartRoute(remaining: Int): List<String>? {
        if (remaining == 110) return listOf("T20", "Bull")
        if (remaining == 104) return listOf("T20", "D22")
        if (remaining == 100) return listOf("T20", "D20")
        if (remaining == 50) return listOf("Bull")

        for (segment in listOf(20, 19, 18, 17, 16, 15, 14, 13, 12, 11, 10)) {
            val triple = segment * 3
            val left = remaining - triple
            if (left == 50) return listOf("T$segment", "Bull")
            if (left in 2..40 && left % 2 == 0) return listOf("T$segment", "D${left / 2}")
        }
        for (segment in 20 downTo 1) {
            val left = remaining - segment
            if (left == 50) return listOf("S$segment", "Bull")
            if (left in 2..40 && left % 2 == 0) return listOf("S$segment", "D${left / 2}")
        }
        return null
    }

    private fun isRealisticDartScore(score: Int): Boolean {
        if (score == 50) return true
        if (score in 2..40 && score % 2 == 0) return true
        if (score in 1..20) return true
        if (score in 3..60 && score % 3 == 0) return true
        return false
    }

    fun isValidFinish(remaining: Int, outRule: OutRule): Boolean {
        if (remaining <= 0) return false
        return when (outRule) {
            OutRule.STRAIGHT -> remaining <= 180
            OutRule.DOUBLE -> remaining in 2..170 && remaining !in BOGEY
            OutRule.MASTER -> remaining in 2..170 && remaining !in BOGEY
        }
    }

    private fun defaultRoute(remaining: Int): List<String> {
        if (remaining == 50) return listOf("Bull")
        if (remaining <= 40 && remaining % 2 == 0) return listOf("D${remaining / 2}")
        if (remaining % 2 == 1 && remaining <= 39) {
            val single = remaining - 32
            if (single in 1..19) return listOf("S$single", "D16")
        }
        return listOf("T20", "↓", "---")
    }

    private fun padToThree(route: List<String>): List<String> {
        return buildList {
            addAll(route.take(3))
            while (size < 3) add("---")
        }
    }
}
