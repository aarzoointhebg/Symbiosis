package com.example.data

import android.content.Context
import com.example.R
import com.example.model.CommunityProfile
import com.example.model.UserProfile
import com.example.model.toUserProfile
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

/**
 * Persistence contract for loading, observing, and saving a user's [UserProfile].
 */
interface UserProfileRepository {
    val currentUserId: String
    val isCloudBacked: Boolean

    fun observeUserProfile(): Flow<UserProfile?>
    suspend fun loadUserProfileOnce(): Result<UserProfile?>
    suspend fun saveUserProfile(profile: UserProfile): Result<Unit>
    suspend fun deleteUserProfile(): Result<Unit>
}

/**
 * Cloud Firestore implementation of [UserProfileRepository] bound strictly to the
 * authenticated account's UID (`users/{uid}/user_profile/profile`) and initialized
 * with the custom named database ID (`R.string.firestore_database_id`).
 *
 * Also mirrors core matching fields to `community_profiles/{uid}` so capability
 * discovery and matching engines stay synchronized in real time.
 */
class FirestoreUserProfileRepository(
    private val db: FirebaseFirestore,
    override val currentUserId: String,
    private val authService: FirebaseAuthService
) : UserProfileRepository {

    constructor(
        context: Context,
        currentUserId: String,
        authService: FirebaseAuthService
    ) : this(
        db = FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        currentUserId = currentUserId,
        authService = authService
    )

    override val isCloudBacked: Boolean = true

    private fun requireActiveUid(): String {
        val uid = authService.requireAuthenticatedUserId()
        check(uid == currentUserId) { "Authenticated account changed; refusing stale UserProfile access." }
        return uid
    }

    override fun observeUserProfile(): Flow<UserProfile?> = flow {
        val uid = requireActiveUid()
        val path = "users/$uid/user_profile/$PROFILE_DOC_ID"
        emitAll(
            db.collection("users").document(uid)
                .collection("user_profile").document(PROFILE_DOC_ID)
                .snapshots()
                .map { snap ->
                    snap.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    logFlowError(error, OperationType.GET, path, uid)
                    throw error
                }
        )
    }

    override suspend fun loadUserProfileOnce(): Result<UserProfile?> {
        val path = "users/$currentUserId/user_profile/$PROFILE_DOC_ID"
        return try {
            val uid = requireActiveUid()
            val profileSnap = db.collection("users").document(uid)
                .collection("user_profile").document(PROFILE_DOC_ID)
                .get()
                .await()
            val direct = profileSnap.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            if (direct != null) {
                return Result.success(direct)
            }
            // Fall back to existing community profile if user previously onboarded before creating user_profile
            val communitySnap = db.collection("community_profiles").document(uid).get().await()
            val community = communitySnap.toObject(CommunityProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            Result.success(community?.toUserProfile())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, path)
            Result.failure(e)
        }
    }

    override suspend fun saveUserProfile(profile: UserProfile): Result<Unit> {
        val path = "users/$currentUserId/user_profile/$PROFILE_DOC_ID"
        return try {
            val uid = requireActiveUid()
            val safeName = profile.displayName.trim()
            require(safeName.length in 2..60) { "Display name must be between 2 and 60 characters." }
            require(profile.skills.isNotEmpty()) { "Select or add at least one skill for matching." }
            require(profile.interests.isNotEmpty()) { "Select or add at least one interest for matching." }

            val normalized = profile.copy(
                id = PROFILE_DOC_ID,
                userId = uid,
                displayName = safeName,
                headline = profile.headline.trim().take(140),
                bio = profile.bio.trim().take(600),
                onboardingCompleted = true
            )

            // 1. Persist dedicated UserProfile document under owner-scoped subcollection
            val userProfileRef = db.collection("users").document(uid)
                .collection("user_profile").document(PROFILE_DOC_ID)
            val existingUserProfile = userProfileRef.get().await()
            if (existingUserProfile.exists()) {
                userProfileRef.update(normalized.toUpdateMap()).await()
            } else {
                userProfileRef.set(normalized.toCreateMap()).await()
            }

            // 2. Sync matching fields into community_profiles/{uid} for discovery engines
            val communityRef = db.collection("community_profiles").document(uid)
            val existingCommunitySnap = communityRef.get().await()
            val existingCommunity = existingCommunitySnap.toObject(
                CommunityProfile::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            )
            val syncedCommunity = normalized.toCommunityProfile(existing = existingCommunity)
                .copy(userId = uid, displayName = safeName)
            if (existingCommunitySnap.exists()) {
                communityRef.update(syncedCommunity.toUpdateMap()).await()
            } else {
                communityRef.set(syncedCommunity.toCreateMap()).await()
            }

            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    override suspend fun deleteUserProfile(): Result<Unit> {
        val path = "users/$currentUserId/user_profile/$PROFILE_DOC_ID"
        return try {
            val uid = requireActiveUid()
            db.collection("users").document(uid)
                .collection("user_profile").document(PROFILE_DOC_ID)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    private fun logFlowError(error: Throwable, op: OperationType, path: String, uid: String) {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) return
        val exceptionToLog = firestoreError ?: (error as? Exception)
        if (exceptionToLog != null && authService.currentAccount?.uid == uid) {
            handleFirestoreError(exceptionToLog, op, path)
        }
    }

    companion object {
        private const val PROFILE_DOC_ID = "profile"
    }
}

/**
 * Local in-memory fallback for unauthenticated local workspace exploration.
 * Executes zero Firestore operations.
 */
class LocalUserProfileRepository(
    override val currentUserId: String,
    initialDisplayName: String,
    private val backingRepo: LocalDemoSymbiosisRepository? = null
) : UserProfileRepository {

    override val isCloudBacked: Boolean = false

    private val profileFlow = MutableStateFlow<UserProfile?>(
        DemoSeedData.defaultUserProfile(currentUserId, initialDisplayName).toUserProfile()
    )

    override fun observeUserProfile(): Flow<UserProfile?> = profileFlow

    override suspend fun loadUserProfileOnce(): Result<UserProfile?> = Result.success(profileFlow.value)

    override suspend fun saveUserProfile(profile: UserProfile): Result<Unit> {
        val saved = profile.copy(id = currentUserId, userId = currentUserId)
        profileFlow.value = saved
        backingRepo?.saveProfile(saved.toCommunityProfile())
        return Result.success(Unit)
    }

    override suspend fun deleteUserProfile(): Result<Unit> {
        profileFlow.value = null
        return Result.success(Unit)
    }
}
