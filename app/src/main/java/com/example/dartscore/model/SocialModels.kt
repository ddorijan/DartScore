package com.example.dartscore.model

data class SocialUser(
    val uid: String,
    val displayName: String,
    val isFriend: Boolean = false,
    val isFollowing: Boolean = false
)

enum class FeedPostType(val label: String) {
    GAME("Utakmica"),
    CHECKOUT("Checkout"),
    ANNOUNCEMENT("Objava"),
    GENERAL("Općenito")
}

data class FeedPost(
    val id: String,
    val authorUid: String,
    val authorName: String,
    val postType: FeedPostType,
    val message: String,
    val detail: String = "",
    val scoreHighlight: String = "",
    val createdAtMs: Long = 0L
) {
    fun toActivityItem(): ActivityItem = ActivityItem(
        userName = authorName,
        actionText = when (postType) {
            FeedPostType.GAME -> "je objavio utakmicu"
            FeedPostType.CHECKOUT -> "je postigao checkout"
            FeedPostType.ANNOUNCEMENT -> "je objavio"
            FeedPostType.GENERAL -> "je podijelio"
        },
        timeAgo = formatTimeAgo(createdAtMs),
        detail = detail.ifBlank { message },
        score = scoreHighlight
    )
}

data class ActivityItem(
    val userName: String,
    val actionText: String,
    val timeAgo: String,
    val detail: String,
    val score: String
)

data class MatchHistoryItem(
    val id: String,
    val playerNames: List<String>,
    val winnerName: String?,
    val winnerIndex: Int?,
    val status: String,
    val startScore: Int,
    val legsWon: List<Int>,
    val createdAtMs: Long,
    val threeDartAverage: Double
)

data class UserStatsSummary(
    val threeDartAverage: Double = 0.0,
    val matchesPlayed: Int = 0,
    val matchesWon: Int = 0,
    val totalVisits: Int = 0,
    val onlineRecord: String = "0W-0L",
    val highestCheckout: Int = 0
)

private fun formatTimeAgo(ms: Long): String {
    if (ms <= 0L) return "upravo sada"
    val diff = System.currentTimeMillis() - ms
    val minutes = diff / 60_000
    val hours = diff / 3_600_000
    val days = diff / 86_400_000
    return when {
        minutes < 1 -> "upravo sada"
        minutes < 60 -> "prije ${minutes}min"
        hours < 24 -> "prije ${hours}h"
        days < 7 -> "prije ${days}d"
        else -> "prije ${days / 7}t"
    }
}
