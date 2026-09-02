package com.example.dartscore.data

import com.example.dartscore.model.OnlineStats
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class UserProfile(
    val uid: String,
    val displayName: String,
    val email: String
)

class UserRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    val currentUser get() = auth.currentUser

    suspend fun signInWithGoogleIdToken(idToken: String): Result<UserProfile> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val user = auth.signInWithCredential(credential).await().user
                ?: return Result.failure(IllegalStateException("Prijava Googleom nije uspjela."))

            val displayName = ensureUserProfileDocument(
                uid = user.uid,
                displayName = user.displayName.orEmpty(),
                email = user.email.orEmpty()
            )
            Result.success(UserProfile(uid = user.uid, displayName = displayName, email = user.email.orEmpty()))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Creates the users/{uid} Firestore document on first sign-in; returns the resolved display name. */
    private suspend fun ensureUserProfileDocument(uid: String, displayName: String, email: String): String {
        val docRef = firestore.collection("users").document(uid)
        val existing = docRef.get().await()
        if (existing.exists()) {
            return existing.getString("displayName")?.takeIf { it.isNotBlank() } ?: displayName
        }

        val cleanName = displayName.ifBlank { email.substringBefore("@") }
        val profile = hashMapOf(
            "displayName" to cleanName,
            "displayNameLower" to cleanName.lowercase(),
            "email" to email,
            "birthDate" to "",
            "avatarUrl" to "",
            "country" to "",
            "createdAt" to FieldValue.serverTimestamp(),
            "defaultGameSettings" to mapOf(
                "startScore" to 501,
                "doubleIn" to false,
                "doubleOut" to true,
                "setsOrLegs" to "legs"
            ),
            "onlineStats" to mapOf(
                "wins" to 0,
                "losses" to 0
            )
        )
        docRef.set(profile).await()
        return cleanName
    }

    suspend fun getCurrentUserDisplayName(): String? {
        val user = auth.currentUser ?: return null

        user.displayName?.takeIf { it.isNotBlank() }?.let { return it }

        return try {
            firestore.collection("users")
                .document(user.uid)
                .get()
                .await()
                .getString("displayName")
                ?.takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        } ?: user.email?.substringBefore("@")?.takeIf { it.isNotBlank() }
    }

    suspend fun getCurrentUserProfile(): UserProfile? {
        val user = auth.currentUser ?: return null
        val firestoreName = try {
            firestore.collection("users")
                .document(user.uid)
                .get()
                .await()
                .getString("displayName")
        } catch (_: Exception) {
            null
        }

        val displayName = firestoreName?.takeIf { it.isNotBlank() }
            ?: user.displayName?.takeIf { it.isNotBlank() }
            ?: user.email?.substringBefore("@")
            ?: ""

        return UserProfile(
            uid = user.uid,
            displayName = displayName,
            email = user.email.orEmpty()
        )
    }

    suspend fun getOnlineStats(): OnlineStats {
        val user = auth.currentUser ?: return OnlineStats()
        return try {
            val doc = firestore.collection("users").document(user.uid).get().await()
            OnlineStats(
                wins = doc.getLong("onlineStats.wins")?.toInt() ?: 0,
                losses = doc.getLong("onlineStats.losses")?.toInt() ?: 0
            )
        } catch (_: Exception) {
            OnlineStats()
        }
    }

    suspend fun updateDisplayName(name: String): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(IllegalStateException("Niste prijavljeni."))
        val trimmed = name.trim()
        if (trimmed.isBlank()) {
            return Result.failure(IllegalArgumentException("Korisničko ime ne može biti prazno."))
        }

        return try {
            user.updateProfile(
                UserProfileChangeRequest.Builder()
                    .setDisplayName(trimmed)
                    .build()
            ).await()
            firestore.collection("users")
                .document(user.uid)
                .update(
                    mapOf(
                        "displayName" to trimmed,
                        "displayNameLower" to trimmed.lowercase()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateEmail(newEmail: String, currentPassword: String): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(IllegalStateException("Niste prijavljeni."))
        val trimmedEmail = newEmail.trim()
        if (trimmedEmail.isBlank()) {
            return Result.failure(IllegalArgumentException("E-mail ne može biti prazan."))
        }
        val currentEmail = user.email ?: return Result.failure(IllegalStateException("E-mail nije dostupan."))

        return try {
            user.reauthenticate(EmailAuthProvider.getCredential(currentEmail, currentPassword)).await()
            user.updateEmail(trimmedEmail).await()
            firestore.collection("users")
                .document(user.uid)
                .update("email", trimmedEmail)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePassword(newPassword: String, currentPassword: String): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(IllegalStateException("Niste prijavljeni."))
        if (newPassword.length < 6) {
            return Result.failure(IllegalArgumentException("Lozinka mora imati najmanje 6 znakova."))
        }
        val currentEmail = user.email ?: return Result.failure(IllegalStateException("E-mail nije dostupan."))

        return try {
            user.reauthenticate(EmailAuthProvider.getCredential(currentEmail, currentPassword)).await()
            user.updatePassword(newPassword).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
