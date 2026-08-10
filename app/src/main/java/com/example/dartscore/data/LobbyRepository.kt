package com.example.dartscore.data

import com.example.dartscore.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

class LobbyRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val userRepository: UserRepository = UserRepository()
) {
    fun observeOpenLobbies(): Flow<List<OnlineLobby>> = callbackFlow {
        val listener = firestore.collection("lobbies")
            .whereEqualTo("status", "waiting")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val lobbies = snapshot?.documents?.mapNotNull { doc ->
                    doc.toOnlineLobby()
                }.orEmpty()
                    .filter { it.guestUid == null }
                trySend(lobbies)
            }
        awaitClose { listener.remove() }
    }

    fun observeLobby(lobbyId: String): Flow<OnlineLobby?> = callbackFlow {
        val listener = firestore.collection("lobbies")
            .document(lobbyId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toOnlineLobby())
            }
        awaitClose { listener.remove() }
    }

    suspend fun createLobby(settings: LobbySettings): Result<String> {
        val user = auth.currentUser ?: return Result.failure(IllegalStateException("Niste prijavljeni."))
        if (settings.minAvg > settings.maxAvg) {
            return Result.failure(IllegalArgumentException("Minimalni prosjek ne može biti veći od maksimalnog."))
        }

        val hostName = userRepository.getCurrentUserDisplayName() ?: "Igrač"
        val codeResult = resolveLobbyCode(settings.customCode)
        if (codeResult.isFailure) {
            return Result.failure(codeResult.exceptionOrNull()!!)
        }
        val code = codeResult.getOrThrow()

        return try {
            val doc = firestore.collection("lobbies").document()
            val data = hashMapOf(
                "code" to code,
                "hostUid" to user.uid,
                "hostName" to hostName,
                "minAvg" to settings.minAvg,
                "maxAvg" to settings.maxAvg,
                "startScore" to settings.startScore,
                "format" to settings.format.name,
                "unit" to settings.unit.name,
                "count" to settings.count,
                "inRule" to settings.inRule.name,
                "outRule" to settings.outRule.name,
                "status" to "waiting",
                "guestUid" to null,
                "guestName" to null,
                "createdAt" to FieldValue.serverTimestamp()
            )
            doc.set(data).await()
            Result.success(doc.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun joinLobby(lobbyId: String): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(IllegalStateException("Niste prijavljeni."))
        val guestName = userRepository.getCurrentUserDisplayName() ?: "Igrač"

        return try {
            val docRef = firestore.collection("lobbies").document(lobbyId)
            firestore.runTransaction { tx ->
                val snapshot = tx.get(docRef)
                val status = snapshot.getString("status")
                val guestUid = snapshot.getString("guestUid")
                val hostUid = snapshot.getString("hostUid")

                if (status != "waiting" || guestUid != null) {
                    throw IllegalStateException("Lobby više nije dostupan.")
                }
                if (hostUid == user.uid) {
                    throw IllegalStateException("Ne možete se pridružiti vlastitom lobiju.")
                }

                tx.update(
                    docRef,
                    mapOf(
                        "guestUid" to user.uid,
                        "guestName" to guestName,
                        "status" to "ready"
                    )
                )
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun joinLobbyByCode(code: String): Result<String> {
        val normalized = normalizeLobbyCode(code)
        if (normalized.length !in LOBBY_CODE_MIN..LOBBY_CODE_MAX) {
            return Result.failure(IllegalArgumentException("Kod mora imati $LOBBY_CODE_MIN–$LOBBY_CODE_MAX znakova."))
        }

        return try {
            val snapshot = firestore.collection("lobbies")
                .whereEqualTo("code", normalized)
                .limit(5)
                .get()
                .await()

            val doc = snapshot.documents.firstOrNull { document ->
                document.getString("status") == "waiting" && document.getString("guestUid") == null
            } ?: return Result.failure(IllegalStateException("Lobby s kodom $normalized nije pronađen ili je pun."))

            joinLobby(doc.id).map { doc.id }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isLobbyCodeAvailable(code: String): Boolean {
        val normalized = normalizeLobbyCode(code)
        if (normalized.length !in LOBBY_CODE_MIN..LOBBY_CODE_MAX) return false
        return try {
            val snapshot = firestore.collection("lobbies")
                .whereEqualTo("code", normalized)
                .whereEqualTo("status", "waiting")
                .limit(1)
                .get()
                .await()
            snapshot.isEmpty
        } catch (_: Exception) {
            false
        }
    }

    suspend fun leaveLobby(lobbyId: String): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(IllegalStateException("Niste prijavljeni."))

        return try {
            val docRef = firestore.collection("lobbies").document(lobbyId)
            firestore.runTransaction { tx ->
                val snapshot = tx.get(docRef)
                val hostUid = snapshot.getString("hostUid")
                val guestUid = snapshot.getString("guestUid")

                when (user.uid) {
                    hostUid -> tx.delete(docRef)
                    guestUid -> tx.update(
                        docRef,
                        mapOf(
                            "guestUid" to null,
                            "guestName" to null,
                            "status" to "waiting"
                        )
                    )
                    else -> throw IllegalStateException("Niste u ovom lobiju.")
                }
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun recordOnlineResult(winnerUid: String, loserUid: String): Result<Unit> {
        return try {
            firestore.runTransaction { tx ->
                val winnerRef = firestore.collection("users").document(winnerUid)
                val loserRef = firestore.collection("users").document(loserUid)
                val winnerSnap = tx.get(winnerRef)
                val loserSnap = tx.get(loserRef)

                val winnerWins = (winnerSnap.getLong("onlineStats.wins") ?: 0L).toInt() + 1
                val loserLosses = (loserSnap.getLong("onlineStats.losses") ?: 0L).toInt() + 1

                tx.update(winnerRef, "onlineStats.wins", winnerWins)
                tx.update(loserRef, "onlineStats.losses", loserLosses)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun resolveLobbyCode(customCode: String): Result<String> {
        val normalized = normalizeLobbyCode(customCode)
        if (normalized.isEmpty()) {
            return Result.success(generateLobbyCode())
        }
        if (normalized.length !in LOBBY_CODE_MIN..LOBBY_CODE_MAX) {
            return Result.failure(
                IllegalArgumentException("Kod lobija mora imati $LOBBY_CODE_MIN–$LOBBY_CODE_MAX znakova.")
            )
        }
        if (!normalized.all { it in LOBBY_CODE_CHARS }) {
            return Result.failure(
                IllegalArgumentException("Kod smije sadržavati samo slova i brojeve (bez I, O, 0, 1).")
            )
        }
        if (!isLobbyCodeAvailable(normalized)) {
            return Result.failure(IllegalArgumentException("Kod $normalized je već zauzet. Odaberite drugi."))
        }
        return Result.success(normalized)
    }

    private fun normalizeLobbyCode(code: String): String =
        code.trim().uppercase().filter { it.isLetterOrDigit() }

    private fun generateLobbyCode(): String =
        (1..DEFAULT_CODE_LENGTH).map { LOBBY_CODE_CHARS[Random.nextInt(LOBBY_CODE_CHARS.length)] }
            .joinToString("")

    companion object {
        private const val LOBBY_CODE_MIN = 4
        private const val LOBBY_CODE_MAX = 12
        private const val DEFAULT_CODE_LENGTH = 6
        private const val LOBBY_CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

        fun formatCodeForDisplay(code: String): String {
            val normalized = code.filter { it.isLetterOrDigit() }.uppercase()
            return if (normalized.length <= 4) normalized else {
                normalized.chunked(4).joinToString("-")
            }
        }

        fun lobbyShareText(code: String, hostName: String): String =
            "Pridruži se mom DartScore lobiju!\nDomaćin: $hostName\nKod: ${formatCodeForDisplay(code)}"
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toOnlineLobby(): OnlineLobby? {
        val id = id
        val hostUid = getString("hostUid") ?: return null
        return OnlineLobby(
            id = id,
            code = getString("code").orEmpty(),
            hostUid = hostUid,
            hostName = getString("hostName").orEmpty(),
            minAvg = getLong("minAvg")?.toInt() ?: 0,
            maxAvg = getLong("maxAvg")?.toInt() ?: 0,
            startScore = getLong("startScore")?.toInt() ?: 501,
            format = getString("format")?.let { runCatching { MatchFormat.valueOf(it) }.getOrNull() }
                ?: MatchFormat.FIRST_TO,
            unit = getString("unit")?.let { runCatching { MatchUnit.valueOf(it) }.getOrNull() }
                ?: MatchUnit.LEGS,
            count = getLong("count")?.toInt() ?: 5,
            inRule = getString("inRule")?.let { runCatching { InRule.valueOf(it) }.getOrNull() }
                ?: InRule.STRAIGHT,
            outRule = getString("outRule")?.let { runCatching { OutRule.valueOf(it) }.getOrNull() }
                ?: OutRule.DOUBLE,
            status = getString("status").orEmpty(),
            guestUid = getString("guestUid"),
            guestName = getString("guestName")
        )
    }
}
