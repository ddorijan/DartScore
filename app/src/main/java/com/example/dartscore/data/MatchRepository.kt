package com.example.dartscore.data

import android.util.Log
import com.example.dartscore.game.MatchStatsCalculator
import com.example.dartscore.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class MatchRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    suspend fun createMatch(settings: MatchSettings): String? {
        val uid = auth.currentUser?.uid ?: return null
        return try {
            val matchRef = firestore.collection("matches").document()
            val data = hashMapOf(
                "hostUid" to uid,
                "players" to listOf(uid),
                "playerNames" to settings.playerNames,
                "playerDetails" to settings.playerNames.mapIndexed { index, name ->
                    buildMap {
                        put("index", index)
                        put("name", name)
                        if (index == 0) put("uid", uid)
                    }
                },
                "settings" to hashMapOf(
                    "startScore" to settings.startScore,
                    "format" to settings.format.name,
                    "unit" to settings.unit.name,
                    "count" to settings.count,
                    "inRule" to settings.inRule.name,
                    "outRule" to settings.outRule.name
                ),
                "status" to "in_progress",
                "visits" to emptyList<Map<String, Any>>(),
                "createdAtMs" to System.currentTimeMillis(),
                "createdAt" to FieldValue.serverTimestamp()
            )
            matchRef.set(data).await()
            matchRef.id
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create match", e)
            null
        }
    }

    suspend fun recordVisit(
        matchId: String,
        visit: VisitRecord,
        gameState: LocalGameState
    ) {
        if (auth.currentUser == null) return

        try {
            // serverTimestamp() cannot be used inside array elements — use epoch millis
            val visitData = hashMapOf(
                "playerIndex" to visit.playerIndex,
                "playerName" to visit.playerName,
                "score" to visit.score,
                "remainingBefore" to visit.remainingBefore,
                "remainingAfter" to visit.remainingAfter,
                "leg" to visit.leg,
                "set" to visit.set,
                "bust" to visit.bust,
                "legWon" to visit.legWon,
                "dartsUsed" to visit.dartsUsed,
                "recordedAt" to System.currentTimeMillis()
            )

            firestore.collection("matches").document(matchId).update(
                mapOf(
                    "visits" to FieldValue.arrayUnion(visitData),
                    "currentLeg" to gameState.currentLeg,
                    "currentSet" to gameState.currentSet,
                    "legsWon" to gameState.players.map { it.legsWon },
                    "setsWon" to gameState.players.map { it.setsWon }
                )
            ).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to record visit", e)
        }
    }

    suspend fun completeMatch(
        matchId: String,
        winnerIndex: Int,
        winnerName: String,
        gameState: LocalGameState
    ) {
        if (auth.currentUser == null) return

        try {
            firestore.collection("matches").document(matchId).set(
                hashMapOf(
                    "status" to "completed",
                    "winnerIndex" to winnerIndex,
                    "winnerName" to winnerName,
                    "finalLegsWon" to gameState.players.map { it.legsWon },
                    "finalSetsWon" to gameState.players.map { it.setsWon },
                    "completedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            ).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to complete match", e)
        }
    }

    suspend fun getUserStats(): Result<UserStatsSummary> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Niste prijavljeni."))
        return try {
            val snapshot = firestore.collection("matches")
                .whereArrayContains("players", uid)
                .limit(50)
                .get()
                .await()

            val completedDocs = snapshot.documents.filter { it.getString("status") == "completed" }
            val onlineStats = UserRepository().getOnlineStats()
            var totalScore = 0
            var visitCount = 0
            var matchesWon = 0
            var highestCheckout = 0

            completedDocs.forEach { doc ->
                val winnerIndex = doc.getLong("winnerIndex")?.toInt()
                if (winnerIndex == 0) matchesWon++

                val visits = parseVisits(doc.get("visits") as? List<Map<String, Any>>)
                visits.forEach { visit ->
                    if (!visit.bust && visit.score > 0) {
                        totalScore += visit.score
                        visitCount++
                    }
                    if (!visit.bust && visit.remainingAfter == 0) {
                        highestCheckout = maxOf(highestCheckout, visit.remainingBefore)
                    }
                }
            }

            Result.success(
                UserStatsSummary(
                    threeDartAverage = if (visitCount > 0) totalScore.toDouble() / visitCount else 0.0,
                    matchesPlayed = completedDocs.size,
                    matchesWon = matchesWon,
                    totalVisits = visitCount,
                    onlineRecord = onlineStats.recordLabel,
                    highestCheckout = highestCheckout
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMatchDetail(matchId: String): Result<MatchStatsDetail> {
        return try {
            val doc = firestore.collection("matches").document(matchId).get().await()
            if (!doc.exists()) {
                return Result.failure(IllegalStateException("Utakmica nije pronađena."))
            }
            Result.success(doc.toMatchStatsDetail())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMatchHistory(limit: Int = 30): Result<List<MatchHistoryItem>> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Niste prijavljeni."))
        return try {
            Result.success(getUserMatchesInternal(uid, limit))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun getUserMatchesInternal(uid: String, limit: Int): List<MatchHistoryItem> {
        val snapshot = firestore.collection("matches")
            .whereArrayContains("players", uid)
            .limit((limit * 2).toLong())
            .get()
            .await()

        return snapshot.documents
            .filter { it.getString("status") == "completed" }
            .mapNotNull { doc -> doc.toMatchHistoryItem() }
            .sortedByDescending { it.createdAtMs }
            .take(limit)
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toMatchHistoryItem(): MatchHistoryItem? {
        val visits = parseVisits(get("visits") as? List<Map<String, Any>>)
        var totalScore = 0
        var visitCount = 0
        visits.forEach { visit ->
            if (!visit.bust && visit.score > 0) {
                totalScore += visit.score
                visitCount++
            }
        }
        val settingsMap = get("settings") as? Map<*, *>
        val startScore = (settingsMap?.get("startScore") as? Number)?.toInt() ?: 501
        val legsWon = (get("finalLegsWon") as? List<*>)?.mapNotNull { (it as? Number)?.toInt() }
            ?: (get("legsWon") as? List<*>)?.mapNotNull { (it as? Number)?.toInt() }
            ?: emptyList()

        return MatchHistoryItem(
            id = id,
            playerNames = (get("playerNames") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
            winnerName = getString("winnerName"),
            winnerIndex = getLong("winnerIndex")?.toInt(),
            status = getString("status") ?: "unknown",
            startScore = startScore,
            legsWon = legsWon,
            createdAtMs = getLong("createdAtMs") ?: 0L,
            threeDartAverage = if (visitCount > 0) totalScore.toDouble() / visitCount else 0.0
        )
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toMatchStatsDetail(): MatchStatsDetail {
        val playerNames = (get("playerNames") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
        val settingsMap = get("settings") as? Map<*, *>
        val settings = parseSettings(settingsMap, playerNames)
        val visits = parseVisits(get("visits") as? List<Map<String, Any>>)
        val legsWon = (get("finalLegsWon") as? List<*>)?.mapNotNull { (it as? Number)?.toInt() }
            ?: (get("legsWon") as? List<*>)?.mapNotNull { (it as? Number)?.toInt() }
            ?: List(playerNames.size) { 0 }
        val winnerIndex = getLong("winnerIndex")?.toInt() ?: 0
        val outRule = settings?.outRule ?: OutRule.DOUBLE

        return MatchStatsCalculator.build(
            matchId = id,
            settings = settings,
            playerNames = playerNames,
            winnerIndex = winnerIndex,
            legsWon = legsWon,
            visits = visits,
            startScore = settings?.startScore ?: 501,
            outRule = outRule,
            isCompleted = getString("status") == "completed"
        )
    }

    private fun parseVisits(raw: List<Map<String, Any>>?): List<VisitRecord> {
        if (raw == null) return emptyList()
        return raw.mapNotNull { visit ->
            VisitRecord(
                playerIndex = (visit["playerIndex"] as? Number)?.toInt() ?: return@mapNotNull null,
                playerName = visit["playerName"] as? String ?: "",
                score = (visit["score"] as? Number)?.toInt() ?: 0,
                remainingBefore = (visit["remainingBefore"] as? Number)?.toInt() ?: 0,
                remainingAfter = (visit["remainingAfter"] as? Number)?.toInt() ?: 0,
                leg = (visit["leg"] as? Number)?.toInt() ?: 1,
                set = (visit["set"] as? Number)?.toInt() ?: 1,
                bust = visit["bust"] as? Boolean ?: false,
                legWon = visit["legWon"] as? Boolean ?: false,
                dartsUsed = (visit["dartsUsed"] as? Number)?.toInt() ?: 3
            )
        }
    }

    private fun parseSettings(settingsMap: Map<*, *>?, playerNames: List<String>): MatchSettings? {
        if (settingsMap == null || playerNames.isEmpty()) return null
        return MatchSettings(
            startScore = (settingsMap["startScore"] as? Number)?.toInt() ?: 501,
            playerNames = playerNames,
            format = settingsMap["format"]?.toString()?.let { runCatching { MatchFormat.valueOf(it) }.getOrNull() }
                ?: MatchFormat.FIRST_TO,
            unit = settingsMap["unit"]?.toString()?.let { runCatching { MatchUnit.valueOf(it) }.getOrNull() }
                ?: MatchUnit.LEGS,
            count = (settingsMap["count"] as? Number)?.toInt() ?: 5,
            inRule = settingsMap["inRule"]?.toString()?.let { runCatching { InRule.valueOf(it) }.getOrNull() }
                ?: InRule.STRAIGHT,
            outRule = settingsMap["outRule"]?.toString()?.let { runCatching { OutRule.valueOf(it) }.getOrNull() }
                ?: OutRule.DOUBLE
        )
    }

    companion object {
        private const val TAG = "MatchRepository"
    }
}
