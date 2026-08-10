package com.example.dartscore.model

sealed interface LegVisitEntry {
    data class Scored(val points: Int) : LegVisitEntry
    data object Bust : LegVisitEntry
}
