package com.example.dartscore.model

data class OnlineLobby(
    val id: String,
    val code: String,
    val hostUid: String,
    val hostName: String,
    val minAvg: Int,
    val maxAvg: Int,
    val startScore: Int = 501,
    val format: MatchFormat = MatchFormat.FIRST_TO,
    val unit: MatchUnit = MatchUnit.LEGS,
    val count: Int = 5,
    val inRule: InRule = InRule.STRAIGHT,
    val outRule: OutRule = OutRule.DOUBLE,
    val status: String,
    val guestUid: String?,
    val guestName: String?
) {
    val settingsSummary: String
        get() = LobbySettings(
            minAvg = minAvg,
            maxAvg = maxAvg,
            startScore = startScore,
            format = format,
            unit = unit,
            count = count,
            inRule = inRule,
            outRule = outRule
        ).summary
}
