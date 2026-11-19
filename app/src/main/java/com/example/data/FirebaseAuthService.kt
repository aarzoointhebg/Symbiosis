package com.example.data

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Represents a unique, authenticated Firebase user account.
 */
data class AuthenticatedAccount(
    val uid: String,
    val displayName: String,
    val email: String?,
    val photoUrl: String?
)

sealed interface AuthSignInResult {
    data class Success(val account: AuthenticatedAccount) : AuthSignInResult
    data object Cancelled : AuthSignInResult
    data object NoSavedCredential : AuthSignInResult
    data class Error(val message: String) : AuthSignInResult
}

/**
 * Contract for managing Firebase Authentication state and Google Credential Manager sign-in.
 */
interface FirebaseAuthService {
    val currentAccount: AuthenticatedAccount?
    fun observeAuthState(): Flow<AuthenticatedAccount?>
    fun requireAuthenticatedUserId(): String
    suspend fun attemptAutoSignIn(context: Context): AuthSignInResult
    suspend fun signInWithGoogle(activityContext: Context): AuthSignInResult
    suspend fun signOut()
}

/**
 * Production Firebase Authentication service backed by Jetpack Credential Manager
 * (`GetSignInWithGoogleOption` / `GetGoogleIdOption`) and `FirebaseAuth`.
 */
class CredentialManagerFirebaseAuthService(
    appContext: Context,
    private val auth: FirebaseAuth = Firebase.auth,
    private val credentialManager: CredentialManager = CredentialManager.create(appContext.applicationContext)
) : FirebaseAuthService {

    private val applicationContext = appContext.applicationContext

    override val currentAccount: AuthenticatedAccount?
        get() = auth.currentUser?.toAuthenticatedAccount()

    override fun observeAuthState(): Flow<AuthenticatedAccount?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser?.toAuthenticatedAccount())
        }
        auth.addAuthStateListener(listener)
        trySend(auth.currentUser?.toAuthenticatedAccount())
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override fun requireAuthenticatedUserId(): String {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
        }
        return uid
    }

    override suspend fun attemptAutoSignIn(context: Context): AuthSignInResult {
        auth.currentUser?.let { existing ->
            return AuthSignInResult.Success(existing.toAuthenticatedAccount())
        }
        val clientId = resolveWebClientId()
            ?: return AuthSignInResult.NoSavedCredential

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(clientId)
            .setAutoSelectEnabled(true)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val credential = credentialManager.getCredential(context, request).credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val result = auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null)).await()
                val signedInUser = result.user ?: auth.currentUser
                    ?: return AuthSignInResult.Error("Firebase sign-in succeeded without a user session.")
                AuthSignInResult.Success(signedInUser.toAuthenticatedAccount())
            } else {
                AuthSignInResult.NoSavedCredential
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.d(TAG, "Silent auto sign-in skipped: ${e.message}")
            AuthSignInResult.NoSavedCredential
        }
    }

    override suspend fun signInWithGoogle(activityContext: Context): AuthSignInResult {
        val clientId = resolveWebClientId()
            ?: return AuthSignInResult.Error("Google Sign-In configuration missing: default_web_client_id not found")

        val activity = activityContext as? Activity
            ?: return AuthSignInResult.Error("Activity context required for Google Sign-In.")

        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInOption)
            .build()

        return try {
            val credential = credentialManager.getCredential(activity, request).credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val result = auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null)).await()
                val signedInUser = result.user ?: auth.currentUser
                    ?: return AuthSignInResult.Error("Authentication completed without an active Firebase account.")
                AuthSignInResult.Success(signedInUser.toAuthenticatedAccount())
            } else {
                AuthSignInResult.Error("Unexpected credential type received.")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w(TAG, "Google Sign-In flow cancelled or dismissed: ${e.message}", e)
            AuthSignInResult.Cancelled
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Google Sign-In failed", e)
            AuthSignInResult.Error(e.localizedMessage ?: "Google Sign-In failed")
        }
    }

    override suspend fun signOut() {
        withContext(NonCancellable) {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.w(TAG, "Failed to clear credential state: ${e.message}")
            } finally {
                auth.signOut()
            }
        }
    }

    private fun resolveWebClientId(): String? = try {
        applicationContext.getString(R.string.default_web_client_id).takeIf { it.isNotBlank() }
    } catch (_: Exception) {
        null
    }

    private fun FirebaseUser.toAuthenticatedAccount(): AuthenticatedAccount {
        val resolvedName = displayName?.takeIf { it.isNotBlank() }
            ?: email?.substringBefore("@")?.takeIf { it.isNotBlank() }
            ?: "Explorer"
        return AuthenticatedAccount(
            uid = uid,
            displayName = resolvedName,
            email = email,
            photoUrl = photoUrl?.toString()
        )
    }

    companion object {
        private const val TAG = "Auth"
    }
}
