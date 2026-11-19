package com.example.data

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Compatibility helper delegating to [CredentialManagerFirebaseAuthService].
 */
object GoogleAuthHelper {

    fun attemptAutoSignIn(
        context: Context,
        credentialManager: CredentialManager,
        onAuthSuccess: () -> Unit,
        onUnauthenticated: () -> Unit,
        scope: CoroutineScope
    ) {
        val service = CredentialManagerFirebaseAuthService(
            appContext = context.applicationContext,
            credentialManager = credentialManager
        )
        scope.launch {
            when (service.attemptAutoSignIn(context)) {
                is AuthSignInResult.Success -> onAuthSuccess()
                else -> onUnauthenticated()
            }
        }
    }

    fun onGoogleSignInClicked(
        context: Context,
        credentialManager: CredentialManager,
        onAuthSuccess: () -> Unit,
        onAuthError: (String) -> Unit,
        scope: CoroutineScope,
        onAuthCancelled: () -> Unit = {}
    ) {
        val service = CredentialManagerFirebaseAuthService(
            appContext = context.applicationContext,
            credentialManager = credentialManager
        )
        scope.launch {
            when (val result = service.signInWithGoogle(context)) {
                is AuthSignInResult.Success -> onAuthSuccess()
                is AuthSignInResult.Cancelled -> onAuthCancelled()
                is AuthSignInResult.Error -> onAuthError(result.message)
                is AuthSignInResult.NoSavedCredential -> onAuthCancelled()
            }
        }
    }

    fun signOut(
        credentialManager: CredentialManager,
        scope: CoroutineScope,
        onSignOutComplete: () -> Unit = {}
    ) {
        scope.launch {
            withContext(NonCancellable) {
                try {
                    credentialManager.clearCredentialState(ClearCredentialStateRequest())
                } catch (_: Exception) {
                } finally {
                    Firebase.auth.signOut()
                    onSignOutComplete()
                }
            }
        }
    }
}
