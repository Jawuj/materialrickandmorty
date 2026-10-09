package com.example.materialrickandmorty.data.repository

import com.example.materialrickandmorty.data.model.UserProfile
import com.example.materialrickandmorty.data.model.UserSettings
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val usersCollection = firestore.collection("users")

    suspend fun getUserProfile(uid: String): Result<UserProfile> {
        return runCatching {
            val snapshot = usersCollection.document(uid).get().await()
            if (snapshot.exists()) {
                val data = snapshot.data ?: emptyMap()
                parseUserProfile(uid, data)
            } else {
                UserProfile(uid = uid)
            }
        }
    }

    fun observeUserProfile(uid: String): Flow<UserProfile?> = callbackFlow {
        if (uid.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val listener = usersCollection.document(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(null)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val data = snapshot.data ?: emptyMap()
                val profile = parseUserProfile(uid, data)
                trySend(profile)
            } else {
                trySend(null)
            }
        }

        awaitClose { listener.remove() }
    }

    suspend fun saveUserProfile(profile: UserProfile): Result<Unit> {
        return runCatching {
            val map = mapOf(
                "uid" to profile.uid,
                "name" to profile.name,
                "email" to profile.email,
                "photoUrl" to profile.photoUrl,
                "favorites" to profile.favorites,
                "favoriteEpisodes" to profile.favoriteEpisodes,
                "episodesWatched" to profile.episodesWatched,
                "settings" to mapOf(
                    "darkTheme" to profile.settings.darkTheme,
                    "language" to profile.settings.language,
                    "hideEmail" to profile.settings.hideEmail
                )
            )
            usersCollection.document(profile.uid).set(map, SetOptions.merge()).await()
        }
    }

    suspend fun updateName(uid: String, newName: String): Result<Unit> {
        return runCatching {
            val map = mapOf("name" to newName)
            usersCollection.document(uid).set(map, SetOptions.merge()).await()
        }
    }

    suspend fun toggleFavoriteCharacter(uid: String, characterId: Int, currentFavs: List<Int>): Result<Unit> {
        return runCatching {
            val updated = if (currentFavs.contains(characterId)) {
                currentFavs - characterId
            } else {
                currentFavs + characterId
            }
            val map = mapOf("favorites" to updated)
            usersCollection.document(uid).set(map, SetOptions.merge()).await()
        }
    }

    suspend fun toggleFavoriteEpisode(uid: String, episodeId: Int, currentFavEpisodes: List<Int>): Result<Unit> {
        return runCatching {
            val updated = if (currentFavEpisodes.contains(episodeId)) {
                currentFavEpisodes - episodeId
            } else {
                currentFavEpisodes + episodeId
            }
            val map = mapOf("favoriteEpisodes" to updated)
            usersCollection.document(uid).set(map, SetOptions.merge()).await()
        }
    }

    suspend fun toggleEpisodeWatched(uid: String, episodeId: Int, currentWatched: List<Int>): Result<Unit> {
        return runCatching {
            val updated = if (currentWatched.contains(episodeId)) {
                currentWatched - episodeId
            } else {
                currentWatched + episodeId
            }
            val map = mapOf("episodesWatched" to updated)
            usersCollection.document(uid).set(map, SetOptions.merge()).await()
        }
    }

    suspend fun updateHideEmailSetting(uid: String, hideEmail: Boolean): Result<Unit> {
        return runCatching {
            val map = mapOf(
                "settings" to mapOf(
                    "hideEmail" to hideEmail
                )
            )
            usersCollection.document(uid).set(map, SetOptions.merge()).await()
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseUserProfile(uid: String, data: Map<String, Any>): UserProfile {
        val name = data["name"] as? String ?: ""
        val email = data["email"] as? String ?: ""
        val photoUrl = data["photoUrl"] as? String ?: ""
        val favorites = (data["favorites"] as? List<*>)?.mapNotNull { (it as? Long)?.toInt() ?: (it as? Int) } ?: emptyList()
        val favoriteEpisodes = (data["favoriteEpisodes"] as? List<*>)?.mapNotNull { (it as? Long)?.toInt() ?: (it as? Int) } ?: emptyList()
        val episodesWatched = (data["episodesWatched"] as? List<*>)?.mapNotNull { (it as? Long)?.toInt() ?: (it as? Int) } ?: emptyList()

        val settingsMap = data["settings"] as? Map<String, Any> ?: emptyMap()
        val darkTheme = settingsMap["darkTheme"] as? Boolean ?: true
        val language = settingsMap["language"] as? String ?: "English"
        val hideEmail = settingsMap["hideEmail"] as? Boolean ?: false

        return UserProfile(
            uid = uid,
            name = name,
            email = email,
            photoUrl = photoUrl,
            favorites = favorites,
            favoriteEpisodes = favoriteEpisodes,
            episodesWatched = episodesWatched,
            settings = UserSettings(
                darkTheme = darkTheme,
                language = language,
                hideEmail = hideEmail
            )
        )
    }
}
