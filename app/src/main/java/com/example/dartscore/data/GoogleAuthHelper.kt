package com.example.dartscore.data

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.example.dartscore.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

/** Launches the Credential Manager "Sign in with Google" flow and returns the Google ID token. */
suspend fun requestGoogleIdToken(context: Context): Result<String> {
    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(context.getString(R.string.default_web_client_id))
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    return try {
        val response = CredentialManager.create(context).getCredential(context, request)
        val credential = response.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            Result.success(GoogleIdTokenCredential.createFrom(credential.data).idToken)
        } else {
            Result.failure(IllegalStateException("Neočekivana vrsta Google vjerodajnice."))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
