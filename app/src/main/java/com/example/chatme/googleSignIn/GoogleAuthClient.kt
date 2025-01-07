package com.example.chatme.googleSignIn

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.example.chatme.SignInResult
import com.example.chatme.UserData
import com.example.chatme.view_model.ChatViewModel
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.BeginSignInRequest.GoogleIdTokenRequestOptions
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class GoogleAuthClient(
    private val context: Context,
    private val oneTapClient: SignInClient,
    private val viewModel: ChatViewModel
) {
    private val auth = Firebase.auth

    suspend fun signIn(): IntentSender? {
        val result = try {
            oneTapClient.beginSignIn(
                buildSignInRequest()
            ).await()
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            null
        }
        return result?.pendingIntent?.intentSender
    }

    private fun buildSignInRequest(): BeginSignInRequest {
        return BeginSignInRequest.builder().setGoogleIdTokenRequestOptions(
            GoogleIdTokenRequestOptions.builder().setSupported(true)
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId("222282046861-nq3kordgod5mcqh8l51j6kgvtvisspo0.apps.googleusercontent.com")
                .build()
        ).setAutoSelectEnabled(true).build()
    }


    suspend fun signInWithIntent(intent: Intent): SignInResult {
        viewModel.resetSate()
        val cred = oneTapClient.getSignInCredentialFromIntent(intent)
        val googleIdToken = cred.googleIdToken
        val googleCred = GoogleAuthProvider.getCredential(googleIdToken, null)
        return try {
            val user = auth.signInWithCredential(googleCred).await().user
            SignInResult(
                errorMessage = null,
                data = user?.run {
                    UserData(
                        userId = uid,
                        username = displayName.toString(),
                        profilePictureUrl = photoUrl.toString()
                            .substring(0, photoUrl.toString().length - 6),
                        email = email.toString()
                    )

                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            SignInResult(
                data = null,
                errorMessage = e.message
            )

        }
    }


}