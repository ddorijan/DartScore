package com.example.dartscore.data

import com.example.dartscore.model.FeedPost
import com.example.dartscore.model.FeedPostType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FeedRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val socialRepository: SocialRepository = SocialRepository(),
    private val userRepository: UserRepository = UserRepository()
) {
    suspend fun getNetworkFeed(limit: Int = 20): Result<List<FeedPost>> {
        if (auth.currentUser == null) return Result.success(emptyList())
        return try {
            val authorUids = socialRepository.getNetworkAuthorUids()
            if (authorUids.isEmpty()) return Result.success(emptyList())

            val snapshot = firestore.collection("feedPosts")
                .whereIn("authorUid", authorUids)
                .orderBy("createdAtMs", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            Result.success(snapshot.documents.mapNotNull { it.toFeedPost() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createMatchPost(detail: com.example.dartscore.model.MatchStatsDetail): Result<Unit> {
        return createPost(
            postType = FeedPostType.GAME,
            message = com.example.dartscore.game.MatchShareFormatter.toFeedMessage(detail),
            detail = com.example.dartscore.game.MatchShareFormatter.toFeedDetail(detail),
            scoreHighlight = com.example.dartscore.game.MatchShareFormatter.toFeedScoreHighlight(detail)
        )
    }

    suspend fun createPost(
        postType: FeedPostType,
        message: String,
        detail: String = "",
        scoreHighlight: String = ""
    ): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(IllegalStateException("Niste prijavljeni."))
        val trimmed = message.trim()
        if (trimmed.isBlank()) {
            return Result.failure(IllegalArgumentException("Objava ne može biti prazna."))
        }

        val authorName = userRepository.getCurrentUserDisplayName() ?: "Igrač"

        return try {
            firestore.collection("feedPosts").add(
                mapOf(
                    "authorUid" to user.uid,
                    "authorName" to authorName,
                    "postType" to postType.name,
                    "message" to trimmed,
                    "detail" to detail.trim(),
                    "scoreHighlight" to scoreHighlight.trim(),
                    "createdAtMs" to System.currentTimeMillis(),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toFeedPost(): FeedPost? {
        val authorUid = getString("authorUid") ?: return null
        val typeName = getString("postType")
        val postType = typeName?.let {
            runCatching { FeedPostType.valueOf(it) }.getOrNull()
        } ?: FeedPostType.GENERAL

        return FeedPost(
            id = id,
            authorUid = authorUid,
            authorName = getString("authorName").orEmpty(),
            postType = postType,
            message = getString("message").orEmpty(),
            detail = getString("detail").orEmpty(),
            scoreHighlight = getString("scoreHighlight").orEmpty(),
            createdAtMs = getLong("createdAtMs") ?: 0L
        )
    }
}
