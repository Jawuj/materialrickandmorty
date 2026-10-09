package com.example.materialrickandmorty.data.repository

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val authStateFlow: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun loginWithEmail(email: String, password: String): Result<FirebaseUser> {
        return runCatching {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user ?: throw Exception("Login failed. Please check credentials.")
        }.mapFailure { parseAuthException(it) }
    }

    suspend fun signUpWithEmail(email: String, password: String, displayName: String? = null): Result<FirebaseUser> {
        return runCatching {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("Sign up failed.")
            if (!displayName.isNullOrBlank()) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build()
                user.updateProfile(profileUpdates).await()
            }
            user
        }.mapFailure { parseAuthException(it) }
    }

    suspend fun signInWithGoogleCredential(idToken: String): Result<FirebaseUser> {
        return runCatching {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            result.user ?: throw Exception("Google sign-in failed.")
        }.mapFailure { parseAuthException(it) }
    }

    suspend fun updateDisplayName(newName: String): Result<Unit> {
        return runCatching {
            val user = auth.currentUser ?: throw Exception("User not authenticated.")
            val updates = UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .build()
            user.updateProfile(updates).await()
            Unit
        }.mapFailure { parseAuthException(it) }
    }

    fun signOut() {
        auth.signOut()
    }

    private fun parseAuthException(e: Throwable): Throwable {
        return when (e) {
            is FirebaseAuthInvalidUserException, is FirebaseAuthInvalidCredentialsException -> {
                Exception("Invalid email or password.")
            }
            is FirebaseAuthUserCollisionException -> {
                Exception("An account already exists with this email address.")
            }
            is FirebaseAuthWeakPasswordException -> {
                Exception("Password should be at least 6 characters long.")
            }
            is FirebaseNetworkException -> {
                Exception("Network error. Please check your connection.")
            }
            is FirebaseAuthException -> {
                Exception(e.localizedMessage ?: "Authentication failed.")
            }
            else -> e
        }
    }

    private inline fun <T> Result<T>.mapFailure(transform: (Throwable) -> Throwable): Result<T> {
        return when (val exception = exceptionOrNull()) {
            null -> this
            else -> Result.failure(transform(exception))
        }
    }
}
