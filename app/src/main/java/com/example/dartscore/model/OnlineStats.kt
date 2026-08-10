package com.example.dartscore.model

data class OnlineStats(
    val wins: Int = 0,
    val losses: Int = 0
) {
    val recordLabel: String get() = "${wins}W-${losses}L"
}
