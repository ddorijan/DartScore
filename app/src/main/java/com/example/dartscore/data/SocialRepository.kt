package com.example.dartscore.data

import com.example.dartscore.model.SocialUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class SocialRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun findUserByNickname(nickname: String): Result<SocialUser?> {
        val user = auth.currentUser ?: return Result.failure(IllegalStateException("Niste prijavljeni."))
        val query = nickname.trim().lowercase()
        if (query.isBlank()) {
            return Result.failure(IllegalArgumentException("Unesite nadimak."))
        }

        return try {
            val snapshot = firestore.collection("users")
                .whereEqualTo("displayNameLower", query)
                .limit(1)
                .get()
                .await()

            val doc = snapshot.documents.firstOrNull()
                ?: return Result.success(null)

            val uid = doc.id
            if (uid == user.uid) {
                return Result.failure(IllegalArgumentException("Ne možete dodati sebe."))
            }

            val friend = isFriend(user.uid, uid)
            val following = isFollowing(user.uid, uid)

            Result.success(
                SocialUser(
                    uid = uid,
                    displayName = doc.getString("displayName") ?: query,
                    isFriend = friend,
                    isFollowing = following
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFriends(): Result<List<SocialUser>> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Niste prijavljeni."))
        return try {
            val snapshot = firestore.collection("friends").document(uid)
                .collection("list").get().await()
            val friends = snapshot.documents.mapNotNull { doc ->
                SocialUser(
                    uid = doc.id,
                    displayName = doc.getString("displayName").orEmpty(),
                    isFriend = true,
                    isFollowing = isFollowing(uid, doc.id)
                )
            }.sortedBy { it.displayName.lowercase() }
            Result.success(friends)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFollowing(): Result<List<SocialUser>> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Niste prijavljeni."))
        return try {
            val snapshot = firestore.collection("follows").document(uid)
                .collection("list").get().await()
            val following = snapshot.documents.mapNotNull { doc ->
                SocialUser(
                    uid = doc.id,
                    displayName = doc.getString("displayName").orEmpty(),
                    isFriend = isFriend(uid, doc.id),
                    isFollowing = true
                )
            }.sortedBy { it.displayName.lowercase() }
            Result.success(following)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getNetworkAuthorUids(): List<String> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        return try {
            val friends = firestore.collection("friends").document(uid)
                .collection("list").get().await()
                .documents.map { it.id }
            val follows = firestore.collection("follows").document(uid)
                .collection("list").get().await()
                .documents.map { it.id }
            // Always include self so own posts appear in Aktivnosti / Zid objava.
            (listOf(uid) + friends + follows).distinct().take(30)
        } catch (_: Exception) {
            listOf(uid)
        }
    }

    suspend fun addFriend(target: SocialUser): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Niste prijavljeni."))
        return try {
            val data = mapOf(
                "displayName" to target.displayName,
                "addedAt" to FieldValue.serverTimestamp()
            )
            firestore.collection("friends").document(uid)
                .collection("list").document(target.uid)
                .set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeFriend(friendUid: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Niste prijavljeni."))
        return try {
            firestore.collection("friends").document(uid)
                .collection("list").document(friendUid)
                .delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun followUser(target: SocialUser): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Niste prijavljeni."))
        return try {
            firestore.collection("follows").document(uid)
                .collection("list").document(target.uid)
                .set(
                    mapOf(
                        "displayName" to target.displayName,
                        "followedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unfollowUser(targetUid: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Niste prijavljeni."))
        return try {
            firestore.collection("follows").document(uid)
                .collection("list").document(targetUid)
                .delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun isFriend(uid: String, otherUid: String): Boolean {
        return try {
            firestore.collection("friends").document(uid)
                .collection("list").document(otherUid)
                .get().await().exists()
        } catch (_: Exception) {
            false
        }
    }

    private suspend fun isFollowing(uid: String, otherUid: String): Boolean {
        return try {
            firestore.collection("follows").document(uid)
                .collection("list").document(otherUid)
                .get().await().exists()
        } catch (_: Exception) {
            false
        }
    }
}
