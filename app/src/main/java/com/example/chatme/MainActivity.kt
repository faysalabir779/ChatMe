package com.example.chatme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.chatme.googleSignIn.GoogleAuthClient
import com.example.chatme.screen.SignInScreen
import com.example.chatme.ui.theme.ChatMeTheme
import com.example.chatme.view_model.ChatViewModel
import com.google.android.gms.auth.api.identity.Identity
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: ChatViewModel by viewModels()
    private val googleAuthClient by lazy {
        GoogleAuthClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext),
            viewModel = viewModel
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChatMeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val launcher =
                        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartIntentSenderForResult(),
                            onResult = {result->
                                if (result.resultCode == RESULT_OK) {
                                    lifecycleScope.launch {
                                        val signInResult =
                                            googleAuthClient.signInWithIntent(
                                                intent = result.data ?: return@launch
                                            )
                                    }
                                }
                            })
                    SignInScreen(onSignInClick = {
                        lifecycleScope.launch {
                            val signInIntentSender = googleAuthClient.signIn()
                            launcher.launch(
                                IntentSenderRequest.Builder(
                                    signInIntentSender ?: return@launch
                                ).build()
                            )
                        }
                    })
                }
            }
        }
    }
}
