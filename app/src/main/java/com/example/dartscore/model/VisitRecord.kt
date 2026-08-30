package com.example.dartscore.model

data class VisitRecord(
    val playerIndex: Int,
    val playerName: String,
    val score: Int,
    val remainingBefore: Int,
    val remainingAfter: Int,
    val leg: Int,
    val set: Int,
    val bust: Boolean = false,
    val legWon: Boolean = false,
    val dartsUsed: Int = 3
)
