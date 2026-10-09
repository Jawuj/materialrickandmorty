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
            is FirebaseAuthWeakPasswordException -> {
                val reason = e.reason?.takeIf { it.isNotBlank() }
                Exception(reason ?: "La contraseña debe tener al menos 6 caracteres.")
            }
            is FirebaseAuthUserCollisionException -> {
                Exception("Ya existe una cuenta con este correo. Inicia sesión o recupera tu contraseña.")
            }
            is FirebaseAuthInvalidUserException -> {
                when (e.errorCode) {
                    "ERROR_USER_NOT_FOUND", "ERROR_USER_DISABLED" -> Exception("No existe una cuenta activa con ese correo. Crea una cuenta o verifica el correo.")
                    else -> Exception("No se pudo encontrar la cuenta. Comprueba el correo e inténtalo de nuevo.")
                }
            }
            is FirebaseAuthInvalidCredentialsException -> {
                when (e.errorCode) {
                    "ERROR_WRONG_PASSWORD", "ERROR_INVALID_CREDENTIAL" -> Exception("La contraseña no coincide con esa cuenta. Comprueba que la escribiste bien o recupera tu contraseña.")
                    "ERROR_INVALID_EMAIL" -> Exception("El formato del correo no es válido.")
                    else -> Exception("El correo o la contraseña no son válidos. Comprueba tus datos e inténtalo de nuevo.")
                }
            }
            is FirebaseNetworkException -> {
                Exception("No se pudo conectar con Firebase. Comprueba tu conexión a Internet e inténtalo otra vez.")
            }
            is FirebaseAuthException -> {
                when (e.errorCode) {
                    "ERROR_OPERATION_NOT_ALLOWED" -> Exception("Este método de acceso está desactivado en Firebase Authentication.")
                    "ERROR_TOO_MANY_REQUESTS" -> Exception("Hubo demasiados intentos. Espera un momento e inténtalo de nuevo.")
                    else -> Exception("Firebase rechazó el acceso (${e.errorCode}). Revisa la configuración de Authentication en Firebase.")
                }
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
