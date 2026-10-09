package com.example.materialrickandmorty.data.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.example.materialrickandmorty.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

object GoogleSignInHelper {

    fun getGoogleSignInOptions(context: Context): GoogleSignInOptions {
        val webClientId = getWebClientId(context)
        return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
    }

    fun getWebClientId(context: Context): String {
        return try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            "302924843361-oebfk50t6ma0vmt41hklltllkb7blgkp.apps.googleusercontent.com"
        }
    }

    suspend fun performCredentialManagerSignIn(context: Context): Result<String> {
        return runCatching {
            val webClientId = getWebClientId(context)

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val credentialManager = CredentialManager.create(context)
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential

            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                googleIdTokenCredential.idToken
            } else if (credential is GoogleIdTokenCredential) {
                credential.idToken
            } else {
                throw Exception("Unable to extract Google ID token")
            }
        }
    }
}
