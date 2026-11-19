package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.DemoSeedData
import com.example.data.FirebaseAuthService
import com.example.data.UserProfileRepository
import com.example.model.UiState
import com.example.model.UserProfile
import com.example.model.toUserProfile
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Dedicated ViewModel managing state, validation, and Firestore persistence
 * for the authenticated user's [UserProfile].
 */
class UserProfileViewModel(
    private val repository: UserProfileRepository,
    val currentUserId: String,
    val initialDisplayName: String,
    val accountEmail: String? = null,
    private val authService: FirebaseAuthService? = null
) : ViewModel() {

    val isCloudBacked: Boolean get() = repository.isCloudBacked

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saveError = MutableStateFlow<String?>(null)
    val saveError: StateFlow<String?> = _saveError.asStateFlow()

    private val _saveSuccessMessage = MutableStateFlow<String?>(null)
    val saveSuccessMessage: StateFlow<String?> = _saveSuccessMessage.asStateFlow()

    private val _manualRefreshProfile = MutableStateFlow<UserProfile?>(null)

    val profileState: StateFlow<UiState<UserProfile>> = repository.observeUserProfile()
        .map<UserProfile?, UiState<UserProfile>> { observed ->
            val resolved = observed
                ?: _manualRefreshProfile.value
                ?: DemoSeedData.defaultUserProfile(currentUserId, initialDisplayName).toUserProfile()
            UiState.Success(resolved)
        }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.e(TAG, "Failed to observe UserProfile", rootError)
            emit(UiState.Error(rootError.localizedMessage ?: "Failed to load matching profile from Firestore."))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    val isProfileComplete: StateFlow<Boolean> = profileState
        .map { state ->
            val profile = (state as? UiState.Success)?.data
            profile != null &&
                profile.displayName.trim().length >= 2 &&
                profile.skills.isNotEmpty() &&
                profile.interests.isNotEmpty()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), false)

    fun loadProfile() {
        viewModelScope.launch {
            _saveError.value = null
            repository.loadUserProfileOnce()
                .onSuccess { loaded ->
                    if (loaded != null) {
                        _manualRefreshProfile.value = loaded
                    }
                }
                .onFailure { err ->
                    _saveError.value = err.localizedMessage ?: "Unable to refresh profile."
                }
        }
    }

    fun saveProfile(
        profile: UserProfile,
        onSaved: () -> Unit = {}
    ) {
        if (_isSaving.value) return

        val verifiedUid = authService?.currentAccount?.uid ?: currentUserId
        if (verifiedUid.isBlank()) {
            _saveError.value = "You must be signed in to save your matching profile."
            return
        }

        val cleanName = profile.displayName.trim()
        when {
            cleanName.length < 2 -> {
                _saveError.value = "Display name must be at least 2 characters."
                return
            }
            profile.skills.isEmpty() -> {
                _saveError.value = "Select or add at least one skill for matching."
                return
            }
            profile.interests.isEmpty() -> {
                _saveError.value = "Select or add at least one interest for matching."
                return
            }
        }

        viewModelScope.launch {
            _isSaving.value = true
            _saveError.value = null
            _saveSuccessMessage.value = null

            val normalized = profile.copy(
                userId = verifiedUid,
                displayName = cleanName,
                headline = profile.headline.trim().take(140),
                bio = profile.bio.trim().take(600),
                onboardingCompleted = true
            )

            repository.saveUserProfile(normalized)
                .onSuccess {
                    _manualRefreshProfile.value = normalized
                    _saveSuccessMessage.value = if (isCloudBacked) {
                        "Matching profile synced to your Firebase account."
                    } else {
                        "Matching profile saved locally."
                    }
                    onSaved()
                }
                .onFailure { err ->
                    _saveError.value = err.localizedMessage ?: "Failed to save matching profile."
                }

            _isSaving.value = false
        }
    }

    fun clearMessages() {
        _saveError.value = null
        _saveSuccessMessage.value = null
    }

    companion object {
        private const val TAG = "UserProfileViewModel"

        fun provideFactory(
            repository: UserProfileRepository,
            currentUserId: String,
            initialDisplayName: String,
            accountEmail: String? = null,
            authService: FirebaseAuthService? = null
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return UserProfileViewModel(
                    repository = repository,
                    currentUserId = currentUserId,
                    initialDisplayName = initialDisplayName,
                    accountEmail = accountEmail,
                    authService = authService
                ) as T
            }
        }
    }
}
