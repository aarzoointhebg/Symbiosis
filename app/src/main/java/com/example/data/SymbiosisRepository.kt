package com.example.data

import android.content.Context
import com.example.R
import com.example.model.CollaborationInvitation
import com.example.model.CollaborationMessage
import com.example.model.CommunityComment
import com.example.model.CommunityPost
import com.example.model.CommunityProfile
import com.example.model.CommunityReport
import com.example.model.FollowedTopic
import com.example.model.InvitationStatus
import com.example.model.Mission
import com.example.model.MissionMilestone
import com.example.model.MissionStatus
import com.example.model.PostBookmark
import com.example.model.PrivateSettings
import com.example.model.SafetyBlock
import com.example.model.SavedPossibility
import com.example.model.SkillEndorsement
import com.example.model.UserAmbition
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import java.util.UUID

interface SymbiosisRepository {
    val isCloudBacked: Boolean
    val currentUserId: String

    fun observeOwnProfile(): Flow<CommunityProfile?>
    fun observeCommunityProfiles(): Flow<List<CommunityProfile>>
    fun observeAmbitions(): Flow<List<UserAmbition>>
    fun observeCommunityMissions(): Flow<List<Mission>>
    fun observeUserMemberMissions(): Flow<List<Mission>>
    fun observeInvitations(): Flow<List<CollaborationInvitation>>
    fun observeMilestones(missionId: String): Flow<List<MissionMilestone>>
    fun observeSavedPossibilities(): Flow<List<SavedPossibility>>
    fun observeSafetyBlocks(): Flow<List<SafetyBlock>>
    fun observePrivateSettings(): Flow<PrivateSettings?>
    fun observeSkillEndorsements(): Flow<List<SkillEndorsement>>
    fun observeCollaborationMessages(): Flow<List<CollaborationMessage>>
    fun observeCommunityPosts(): Flow<List<CommunityPost>>
    fun observeCommunityComments(): Flow<List<CommunityComment>>
    fun observeBookmarkedPostIds(): Flow<Set<String>>
    fun observePostBookmarks(): Flow<List<PostBookmark>>
    fun observeFollowedTopics(): Flow<List<FollowedTopic>>

    suspend fun saveProfile(profile: CommunityProfile): Result<Unit>
    suspend fun savePrivateSettings(settings: PrivateSettings): Result<Unit>
    suspend fun upsertAmbition(ambition: UserAmbition): Result<Unit>
    suspend fun deleteAmbition(ambitionId: String): Result<Unit>
    suspend fun createMission(mission: Mission, initialMilestones: List<Pair<String, String>> = emptyList()): Result<String>
    suspend fun updateMissionProgress(
        missionId: String,
        progressPercent: Long,
        status: MissionStatus,
        updateStatus: Boolean = true
    ): Result<Unit>
    suspend fun toggleMilestone(missionId: String, milestoneId: String, completed: Boolean): Result<Unit>
    suspend fun sendInvitation(invitation: CollaborationInvitation): Result<Unit>
    suspend fun respondToInvitation(invitationId: String, missionId: String, newStatus: InvitationStatus): Result<Unit>
    suspend fun savePossibility(possibility: SavedPossibility): Result<Unit>
    suspend fun blockUser(blockedUserId: String, reason: String): Result<Unit>
    suspend fun unblockUser(blockedUserId: String): Result<Unit>
    suspend fun toggleSkillEndorsement(
        recipientId: String,
        recipientName: String,
        endorserName: String,
        skillName: String
    ): Result<Boolean>
    suspend fun sendCollaborationMessage(
        recipientId: String,
        recipientName: String,
        senderName: String,
        body: String,
        contextLabel: String
    ): Result<Unit>
    suspend fun createCommunityPost(post: CommunityPost): Result<String>
    suspend fun togglePostAppreciation(post: CommunityPost): Result<Boolean>
    suspend fun addCommunityComment(comment: CommunityComment): Result<Unit>
    suspend fun togglePostBookmark(post: CommunityPost): Result<Boolean>
    suspend fun toggleFollowTopic(topic: String): Result<Boolean>
    suspend fun reportCommunityPost(post: CommunityPost, reason: String): Result<Unit>
}

/**
 * Cloud Firestore implementation strictly scoped to authenticated Firebase users
 * and initialized exclusively with R.string.firestore_database_id.
 */
class FirestoreSymbiosisRepository(
    private val db: FirebaseFirestore,
    override val currentUserId: String,
    private val auth: FirebaseAuth = Firebase.auth
) : SymbiosisRepository {

    constructor(context: Context, currentUserId: String) : this(
        db = FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        currentUserId = currentUserId
    )

    override val isCloudBacked: Boolean = true

    private fun requireActiveUid(): String {
        val uid = auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
        check(uid == currentUserId) { "Signed-in account changed; refusing stale write." }
        return uid
    }

    override fun observeOwnProfile(): Flow<CommunityProfile?> = flow {
        val uid = requireActiveUid()
        val path = "community_profiles/$uid"
        emitAll(
            db.collection("community_profiles").document(uid)
                .snapshots()
                .map { snap -> snap.toObject(CommunityProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error -> logFlowError(error, OperationType.GET, path, uid); throw error }
        )
    }

    override fun observeCommunityProfiles(): Flow<List<CommunityProfile>> = flow {
        val uid = requireActiveUid()
        val path = "community_profiles"
        emitAll(
            db.collection("community_profiles")
                .whereEqualTo("visibility", "COMMUNITY")
                .snapshots()
                .map { snap ->
                    snap.toObjects(CommunityProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error -> logFlowError(error, OperationType.LIST, path, uid); throw error }
        )
    }

    override fun observeAmbitions(): Flow<List<UserAmbition>> = flow {
        val uid = requireActiveUid()
        val path = "users/$uid/ambitions"
        emitAll(
            db.collection("users").document(uid).collection("ambitions")
                .snapshots()
                .map { snap -> snap.toObjects(UserAmbition::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error -> logFlowError(error, OperationType.LIST, path, uid); throw error }
        )
    }

    override fun observeCommunityMissions(): Flow<List<Mission>> = flow {
        val uid = requireActiveUid()
        val path = "missions"
        emitAll(
            db.collection("missions")
                .whereEqualTo("visibility", "COMMUNITY")
                .snapshots()
                .map { snap -> snap.toObjects(Mission::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error -> logFlowError(error, OperationType.LIST, path, uid); throw error }
        )
    }

    override fun observeUserMemberMissions(): Flow<List<Mission>> = flow {
        val uid = requireActiveUid()
        val path = "missions"
        emitAll(
            db.collection("missions")
                .whereArrayContains("memberUids", uid)
                .snapshots()
                .map { snap ->
                    snap.toObjects(Mission::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.updatedAt?.toDate()?.time ?: it.createdAt?.toDate()?.time ?: 0L }
                }
                .catch { error -> logFlowError(error, OperationType.LIST, path, uid); throw error }
        )
    }

    override fun observeInvitations(): Flow<List<CollaborationInvitation>> = flow {
        val uid = requireActiveUid()
        val path = "invitations"
        emitAll(
            db.collection("invitations")
                .whereArrayContains("participantUids", uid)
                .snapshots()
                .map { snap -> snap.toObjects(CollaborationInvitation::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error -> logFlowError(error, OperationType.LIST, path, uid); throw error }
        )
    }

    override fun observeMilestones(missionId: String): Flow<List<MissionMilestone>> = flow {
        val uid = requireActiveUid()
        val path = "missions/$missionId/milestones"
        emitAll(
            db.collection("missions").document(missionId).collection("milestones")
                .whereArrayContains("memberUids", uid)
                .snapshots()
                .map { snap -> snap.toObjects(MissionMilestone::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error -> logFlowError(error, OperationType.LIST, path, uid); throw error }
        )
    }

    override fun observeSavedPossibilities(): Flow<List<SavedPossibility>> = flow {
        val uid = requireActiveUid()
        val path = "users/$uid/saved_possibilities"
        emitAll(
            db.collection("users").document(uid).collection("saved_possibilities")
                .snapshots()
                .map { snap -> snap.toObjects(SavedPossibility::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error -> logFlowError(error, OperationType.LIST, path, uid); throw error }
        )
    }

    override fun observeSafetyBlocks(): Flow<List<SafetyBlock>> = flow {
        val uid = requireActiveUid()
        val path = "users/$uid/safety_blocks"
        emitAll(
            db.collection("users").document(uid).collection("safety_blocks")
                .snapshots()
                .map { snap -> snap.toObjects(SafetyBlock::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error -> logFlowError(error, OperationType.LIST, path, uid); throw error }
        )
    }

    override fun observePrivateSettings(): Flow<PrivateSettings?> = flow {
        val uid = requireActiveUid()
        val path = "users/$uid/private_settings/account"
        emitAll(
            db.collection("users").document(uid).collection("private_settings").document("account")
                .snapshots()
                .map { snap -> snap.toObject(PrivateSettings::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error -> logFlowError(error, OperationType.GET, path, uid); throw error }
        )
    }

    override fun observeSkillEndorsements(): Flow<List<SkillEndorsement>> = flow {
        val uid = requireActiveUid()
        val path = "skill_endorsements"
        emitAll(
            db.collection("skill_endorsements")
                .limit(200)
                .snapshots()
                .map { snap ->
                    snap.toObjects(SkillEndorsement::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error -> logFlowError(error, OperationType.LIST, path, uid); throw error }
        )
    }

    override fun observeCollaborationMessages(): Flow<List<CollaborationMessage>> = flow {
        val uid = requireActiveUid()
        val path = "collaboration_messages"
        emitAll(
            db.collection("collaboration_messages")
                .whereArrayContains("participantUids", uid)
                .limit(200)
                .snapshots()
                .map { snap ->
                    snap.toObjects(CollaborationMessage::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.createdAt?.toDate()?.time ?: Long.MAX_VALUE }
                }
                .catch { error -> logFlowError(error, OperationType.LIST, path, uid); throw error }
        )
    }

    override fun observeCommunityPosts(): Flow<List<CommunityPost>> = flow {
        val uid = requireActiveUid()
        val path = "community_posts"
        emitAll(
            db.collection("community_posts")
                .whereEqualTo("visibility", "COMMUNITY")
                .limit(100)
                .snapshots()
                .map { snap ->
                    snap.toObjects(CommunityPost::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.toDate()?.time ?: 0L }
                }
                .catch { error -> logFlowError(error, OperationType.LIST, path, uid); throw error }
        )
    }

    override fun observeCommunityComments(): Flow<List<CommunityComment>> = flow {
        val uid = requireActiveUid()
        val path = "community_comments"
        emitAll(
            db.collection("community_comments")
                .limit(200)
                .snapshots()
                .map { snap ->
                    snap.toObjects(CommunityComment::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.createdAt?.toDate()?.time ?: Long.MAX_VALUE }
                }
                .catch { error -> logFlowError(error, OperationType.LIST, path, uid); throw error }
        )
    }

    override fun observeBookmarkedPostIds(): Flow<Set<String>> =
        observePostBookmarks().map { bookmarks ->
            bookmarks.map { it.postId.ifBlank { it.id } }
                .filter { it.isNotBlank() }
                .toSet()
        }

    override fun observePostBookmarks(): Flow<List<PostBookmark>> = flow {
        val uid = requireActiveUid()
        val path = "users/$uid/post_bookmarks"
        emitAll(
            db.collection("users").document(uid).collection("post_bookmarks")
                .snapshots()
                .map { snap ->
                    snap.toObjects(PostBookmark::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.toDate()?.time ?: 0L }
                }
                .catch { error -> logFlowError(error, OperationType.LIST, path, uid); throw error }
        )
    }

    override fun observeFollowedTopics(): Flow<List<FollowedTopic>> = flow {
        val uid = requireActiveUid()
        val path = "users/$uid/followed_topics"
        emitAll(
            db.collection("users").document(uid).collection("followed_topics")
                .snapshots()
                .map { snap ->
                    snap.toObjects(FollowedTopic::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error -> logFlowError(error, OperationType.LIST, path, uid); throw error }
        )
    }

    override suspend fun saveProfile(profile: CommunityProfile): Result<Unit> {
        val path = "community_profiles/$currentUserId"
        return try {
            val uid = requireActiveUid()
            val safeName = profile.displayName.trim().ifBlank { "Explorer" }.take(60)
            require(safeName.length >= 2) { "Display name must be at least 2 characters." }
            val docRef = db.collection("community_profiles").document(uid)
            val existing = docRef.get().await()
            val normalized = profile.copy(userId = uid, displayName = safeName)
            if (existing.exists()) {
                docRef.update(normalized.toUpdateMap()).await()
            } else {
                docRef.set(normalized.toCreateMap()).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    override suspend fun savePrivateSettings(settings: PrivateSettings): Result<Unit> {
        val path = "users/$currentUserId/private_settings/account"
        return try {
            val uid = requireActiveUid()
            val docRef = db.collection("users").document(uid)
                .collection("private_settings").document("account")
            docRef.set(settings.copy(userId = uid).toWriteMap()).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    override suspend fun upsertAmbition(ambition: UserAmbition): Result<Unit> {
        val docId = ambition.id.ifBlank { "amb_${UUID.randomUUID().toString().replace("-", "").take(12)}" }
        val path = "users/$currentUserId/ambitions/$docId"
        return try {
            val uid = requireActiveUid()
            require(ambition.title.trim().length >= 2) { "Ambition title must be at least 2 characters." }
            val docRef = db.collection("users").document(uid).collection("ambitions").document(docId)
            val snap = docRef.get().await()
            if (snap.exists()) {
                docRef.update(
                    mapOf(
                        "title" to ambition.title.trim().take(120),
                        "description" to ambition.description.trim().take(500),
                        "horizon" to ambition.horizon,
                        "priority" to ambition.priority,
                        "tags" to ambition.tags.take(10),
                        "sharedToCommunity" to ambition.sharedToCommunity,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.set(ambition.copy(userId = uid).toCreateMap()).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    override suspend fun deleteAmbition(ambitionId: String): Result<Unit> {
        val path = "users/$currentUserId/ambitions/$ambitionId"
        return try {
            val uid = requireActiveUid()
            db.collection("users").document(uid).collection("ambitions").document(ambitionId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    override suspend fun createMission(
        mission: Mission,
        initialMilestones: List<Pair<String, String>>
    ): Result<String> {
        val missionId = mission.id.ifBlank { "mis_${UUID.randomUUID().toString().replace("-", "").take(12)}" }
        val path = "missions/$missionId"
        return try {
            val uid = requireActiveUid()
            require(mission.title.trim().length >= 3) { "Mission title must be at least 3 characters." }
            require(mission.description.trim().length >= 5) { "Mission description must be at least 5 characters." }
            val safeMembers = (mission.memberUids + uid).distinct().take(20)
            val docRef = db.collection("missions").document(missionId)
            docRef.set(
                mission.copy(
                    ownerId = uid,
                    ownerName = mission.ownerName.trim().ifBlank { "Explorer" }.take(60),
                    memberUids = safeMembers
                ).toCreateMap()
            ).await()

            initialMilestones.forEachIndexed { idx, (msTitle, role) ->
                if (msTitle.trim().length >= 2) {
                    val msId = "ms_${idx + 1}_${UUID.randomUUID().toString().replace("-", "").take(6)}"
                    val msRef = docRef.collection("milestones").document(msId)
                    msRef.set(
                        MissionMilestone(
                            missionId = missionId,
                            ownerId = uid,
                            memberUids = safeMembers,
                            title = msTitle.trim().take(160),
                            assignedRole = role.trim().take(80),
                            completed = false
                        ).toCreateMap()
                    ).await()
                }
            }
            Result.success(missionId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    override suspend fun updateMissionProgress(
        missionId: String,
        progressPercent: Long,
        status: MissionStatus,
        updateStatus: Boolean
    ): Result<Unit> {
        val path = "missions/$missionId"
        return try {
            requireActiveUid()
            val payload = buildMap<String, Any> {
                put("progressPercent", progressPercent.coerceIn(0L, 100L))
                if (updateStatus) {
                    put("status", status.name)
                }
                put("updatedAt", FieldValue.serverTimestamp())
            }
            db.collection("missions").document(missionId).update(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    override suspend fun toggleMilestone(missionId: String, milestoneId: String, completed: Boolean): Result<Unit> {
        val path = "missions/$missionId/milestones/$milestoneId"
        return try {
            requireActiveUid()
            db.collection("missions").document(missionId)
                .collection("milestones").document(milestoneId)
                .update(
                    mapOf(
                        "completed" to completed,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    override suspend fun sendInvitation(invitation: CollaborationInvitation): Result<Unit> {
        val invId = "inv_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val path = "invitations/$invId"
        return try {
            val uid = requireActiveUid()
            db.collection("invitations").document(invId).set(
                invitation.copy(
                    senderId = uid,
                    participantUids = listOf(uid, invitation.recipientId),
                    status = InvitationStatus.PENDING.name
                ).toCreateMap()
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    override suspend fun respondToInvitation(
        invitationId: String,
        missionId: String,
        newStatus: InvitationStatus
    ): Result<Unit> {
        val path = "invitations/$invitationId"
        return try {
            requireActiveUid()
            db.collection("invitations").document(invitationId).update(
                mapOf(
                    "status" to newStatus.name,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    override suspend fun savePossibility(possibility: SavedPossibility): Result<Unit> {
        val posId = "pos_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val path = "users/$currentUserId/saved_possibilities/$posId"
        return try {
            val uid = requireActiveUid()
            db.collection("users").document(uid).collection("saved_possibilities").document(posId)
                .set(possibility.copy(userId = uid).toCreateMap())
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    override suspend fun blockUser(blockedUserId: String, reason: String): Result<Unit> {
        val path = "users/$currentUserId/safety_blocks/$blockedUserId"
        return try {
            val uid = requireActiveUid()
            db.collection("users").document(uid).collection("safety_blocks").document(blockedUserId)
                .set(SafetyBlock(userId = uid, blockedUserId = blockedUserId, reason = reason).toCreateMap())
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    override suspend fun unblockUser(blockedUserId: String): Result<Unit> {
        val path = "users/$currentUserId/safety_blocks/$blockedUserId"
        return try {
            val uid = requireActiveUid()
            db.collection("users").document(uid).collection("safety_blocks").document(blockedUserId)
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

    override suspend fun toggleSkillEndorsement(
        recipientId: String,
        recipientName: String,
        endorserName: String,
        skillName: String
    ): Result<Boolean> {
        val cleanSkill = skillName.trim().take(60)
        val docId = SkillEndorsement.buildDocumentId(currentUserId, recipientId, cleanSkill)
        val path = "skill_endorsements/$docId"
        return try {
            val uid = requireActiveUid()
            require(recipientId.isNotBlank() && recipientId != uid) {
                "You can endorse skills belonging to other collaborators."
            }
            require(cleanSkill.isNotEmpty()) { "Skill name cannot be blank." }
            val actualDocId = SkillEndorsement.buildDocumentId(uid, recipientId, cleanSkill)
            val docRef = db.collection("skill_endorsements").document(actualDocId)
            val existing = docRef.get().await()
            if (existing.exists()) {
                docRef.delete().await()
                Result.success(false)
            } else {
                val payload = SkillEndorsement(
                    recipientId = recipientId,
                    recipientName = recipientName.trim().ifBlank { "Collaborator" }.take(60),
                    endorserId = uid,
                    endorserName = endorserName.trim().ifBlank { "Peer" }.take(60),
                    skillName = cleanSkill,
                    normalizedSkill = SkillEndorsement.normalizeSkillKey(cleanSkill),
                    note = "Endorsed on SYMBIOSIS profile"
                ).toCreateMap()
                docRef.set(payload).await()
                Result.success(true)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    override suspend fun sendCollaborationMessage(
        recipientId: String,
        recipientName: String,
        senderName: String,
        body: String,
        contextLabel: String
    ): Result<Unit> {
        val msgId = "msg_${UUID.randomUUID().toString().replace("-", "").take(14)}"
        val path = "collaboration_messages/$msgId"
        return try {
            val uid = requireActiveUid()
            val cleanBody = body.trim().take(1000)
            require(cleanBody.isNotEmpty()) { "Message cannot be empty." }
            require(recipientId.isNotBlank() && recipientId != uid) {
                "Choose another collaborator to start a discussion."
            }
            val threadId = CollaborationMessage.buildThreadId(uid, recipientId)
            val payload = CollaborationMessage(
                threadId = threadId,
                senderId = uid,
                senderName = senderName.trim().ifBlank { "Explorer" }.take(60),
                recipientId = recipientId,
                recipientName = recipientName.trim().ifBlank { "Collaborator" }.take(60),
                participantUids = listOf(uid, recipientId),
                body = cleanBody,
                contextLabel = contextLabel.trim().take(140)
            ).toCreateMap()
            db.collection("collaboration_messages").document(msgId).set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    override suspend fun createCommunityPost(post: CommunityPost): Result<String> {
        val postId = post.id.ifBlank { "post_${UUID.randomUUID().toString().replace("-", "").take(12)}" }
        val path = "community_posts/$postId"
        return try {
            val uid = requireActiveUid()
            val cleanTitle = post.title.trim()
            val cleanBody = post.body.trim()
            require(cleanTitle.length >= 3) { "Post title must be at least 3 characters." }
            require(cleanBody.length >= 5) { "Share a bit more detail (at least 5 characters)." }
            val payload = post.copy(authorId = uid, title = cleanTitle, body = cleanBody).toCreateMap()
            db.collection("community_posts").document(postId).set(payload).await()
            Result.success(postId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    override suspend fun togglePostAppreciation(post: CommunityPost): Result<Boolean> {
        val path = "community_posts/${post.id}"
        return try {
            val uid = requireActiveUid()
            val docRef = db.collection("community_posts").document(post.id)
            val snap = docRef.get().await()
            if (!snap.exists()) {
                return Result.failure(IllegalStateException("SEED_POST_LOCAL_ONLY"))
            }
            val currentList = (snap.get("appreciatorUids") as? List<*>)
                ?.filterIsInstance<String>()
                .orEmpty()
            val already = uid in currentList
            val nextList = if (already) {
                currentList.filterNot { it == uid }
            } else {
                (currentList + uid).distinct().take(200)
            }
            docRef.update(
                mapOf(
                    "appreciatorUids" to nextList,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(!already)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    override suspend fun addCommunityComment(comment: CommunityComment): Result<Unit> {
        val cmtId = "cmt_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val path = "community_comments/$cmtId"
        return try {
            val uid = requireActiveUid()
            val cleanBody = comment.body.trim().take(800)
            require(cleanBody.isNotEmpty()) { "Comment cannot be empty." }
            require(comment.postId.isNotBlank()) { "Post reference missing." }
            val payload = comment.copy(authorId = uid, body = cleanBody).toCreateMap()
            db.collection("community_comments").document(cmtId).set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    override suspend fun togglePostBookmark(post: CommunityPost): Result<Boolean> {
        val path = "users/$currentUserId/post_bookmarks/${post.id}"
        return try {
            val uid = requireActiveUid()
            val docRef = db.collection("users").document(uid)
                .collection("post_bookmarks").document(post.id)
            val existing = docRef.get().await()
            if (existing.exists()) {
                docRef.delete().await()
                Result.success(false)
            } else {
                docRef.set(
                    PostBookmark(userId = uid, postId = post.id, postTitle = post.title).toCreateMap()
                ).await()
                Result.success(true)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    override suspend fun toggleFollowTopic(topic: String): Result<Boolean> {
        val cleanTopic = topic.trim().take(80)
        val topicId = FollowedTopic.buildTopicId(cleanTopic)
        val path = "users/$currentUserId/followed_topics/$topicId"
        return try {
            val uid = requireActiveUid()
            require(cleanTopic.isNotEmpty()) { "Topic name cannot be empty." }
            val docRef = db.collection("users").document(uid)
                .collection("followed_topics").document(topicId)
            val existing = docRef.get().await()
            if (existing.exists()) {
                docRef.delete().await()
                Result.success(false)
            } else {
                docRef.set(FollowedTopic(userId = uid, topic = cleanTopic).toCreateMap()).await()
                Result.success(true)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    override suspend fun reportCommunityPost(post: CommunityPost, reason: String): Result<Unit> {
        val repId = "rep_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val path = "community_reports/$repId"
        return try {
            val uid = requireActiveUid()
            require(post.authorId != uid) { "You cannot report your own post; you can delete or edit it instead." }
            val cleanReason = reason.trim().ifBlank { "Community safety review requested" }.take(400)
            require(cleanReason.length >= 3) { "Please provide a brief reason." }
            val payload = CommunityReport(
                reporterId = uid,
                postId = post.id,
                targetAuthorId = post.authorId,
                reason = cleanReason
            ).toCreateMap()
            db.collection("community_reports").document(repId).set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    private fun logFlowError(error: Throwable, op: OperationType, path: String, uid: String) {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) return
        val exceptionToLog = firestoreError ?: (error as? Exception)
        if (exceptionToLog != null && auth.currentUser?.uid == uid) {
            handleFirestoreError(exceptionToLog, op, path)
        }
    }
}

/**
 * Local in-memory repository for offline/guest mode and passwordless OTP exploration
 * (executes zero Firestore calls when unauthenticated).
 */
class LocalDemoSymbiosisRepository(
    override val currentUserId: String,
    initialDisplayName: String,
    onboardingDone: Boolean = false
) : SymbiosisRepository {

    override val isCloudBacked: Boolean = false

    private val ownProfileFlow = MutableStateFlow<CommunityProfile?>(
        DemoSeedData.defaultUserProfile(currentUserId, initialDisplayName)
            .copy(onboardingCompleted = onboardingDone)
    )
    private val communityFlow = MutableStateFlow(DemoSeedData.communityCollaborators)
    private val ambitionsFlow = MutableStateFlow(DemoSeedData.defaultUserAmbitions(currentUserId))
    private val missionsFlow = MutableStateFlow(DemoSeedData.intentDiscoveryMissions)
    private val invitationsFlow = MutableStateFlow(
        DemoSeedData.defaultSampleInvitations(currentUserId, initialDisplayName)
    )
    private val milestonesFlow = MutableStateFlow(
        DemoSeedData.defaultMilestones.groupBy { it.missionId }
    )
    private val savedPossibilitiesFlow = MutableStateFlow<List<SavedPossibility>>(emptyList())
    private val safetyBlocksFlow = MutableStateFlow<List<SafetyBlock>>(emptyList())
    private val privateSettingsFlow = MutableStateFlow<PrivateSettings?>(
        PrivateSettings(userId = currentUserId)
    )
    private val endorsementsFlow = MutableStateFlow(
        listOf(
            SkillEndorsement(
                id = "seed_end_1",
                recipientId = "peer_mira",
                recipientName = "Mira",
                endorserId = "peer_aarav",
                endorserName = "Aarav",
                skillName = "Visual design",
                normalizedSkill = "visual design"
            ),
            SkillEndorsement(
                id = "seed_end_2",
                recipientId = "peer_aarav",
                recipientName = "Aarav",
                endorserId = "peer_mira",
                endorserName = "Mira",
                skillName = "Programming",
                normalizedSkill = "programming"
            ),
            SkillEndorsement(
                id = "seed_end_3",
                recipientId = "peer_zoya",
                recipientName = "Zoya",
                endorserId = "peer_rohan",
                endorserName = "Rohan",
                skillName = "Accessibility testing",
                normalizedSkill = "accessibility testing"
            )
        )
    )
    private val messagesFlow = MutableStateFlow(
        listOf(
            CollaborationMessage(
                id = "seed_msg_1",
                threadId = CollaborationMessage.buildThreadId(currentUserId, "demo_mira"),
                senderId = "demo_mira",
                senderName = "Mira",
                recipientId = currentUserId,
                recipientName = initialDisplayName.ifBlank { "Explorer" },
                participantUids = listOf("demo_mira", currentUserId),
                body = "Hi! I saw your programming focus—would love to pair on accessible air-quality graphics whenever you're free.",
                contextLabel = "Complementary: Visual design • Programming"
            )
        )
    )

    private val communityPostsFlow = MutableStateFlow(DemoSeedData.sampleCommunityPosts)
    private val communityCommentsFlow = MutableStateFlow(DemoSeedData.sampleCommunityComments)
    private val bookmarkedPostIdsFlow = MutableStateFlow<Set<String>>(emptySet())
    private val followedTopicsFlow = MutableStateFlow(
        listOf(
            FollowedTopic(id = "accessibility_testing", userId = currentUserId, topic = "Accessibility testing"),
            FollowedTopic(id = "programming", userId = currentUserId, topic = "Programming")
        )
    )
    private val communityReportsFlow = MutableStateFlow<List<CommunityReport>>(emptyList())

    override fun observeOwnProfile(): Flow<CommunityProfile?> = ownProfileFlow
    override fun observeCommunityProfiles(): Flow<List<CommunityProfile>> = communityFlow
    override fun observeAmbitions(): Flow<List<UserAmbition>> = ambitionsFlow
    override fun observeCommunityMissions(): Flow<List<Mission>> = missionsFlow
    override fun observeUserMemberMissions(): Flow<List<Mission>> =
        missionsFlow.map { list -> list.filter { it.ownerId == currentUserId || currentUserId in it.memberUids } }
    override fun observeInvitations(): Flow<List<CollaborationInvitation>> = invitationsFlow
    override fun observeMilestones(missionId: String): Flow<List<MissionMilestone>> =
        milestonesFlow.map { it[missionId].orEmpty() }
    override fun observeSavedPossibilities(): Flow<List<SavedPossibility>> = savedPossibilitiesFlow
    override fun observeSafetyBlocks(): Flow<List<SafetyBlock>> = safetyBlocksFlow
    override fun observePrivateSettings(): Flow<PrivateSettings?> = privateSettingsFlow
    override fun observeSkillEndorsements(): Flow<List<SkillEndorsement>> = endorsementsFlow
    override fun observeCollaborationMessages(): Flow<List<CollaborationMessage>> = messagesFlow
    override fun observeCommunityPosts(): Flow<List<CommunityPost>> = communityPostsFlow
    override fun observeCommunityComments(): Flow<List<CommunityComment>> = communityCommentsFlow
    override fun observeBookmarkedPostIds(): Flow<Set<String>> = bookmarkedPostIdsFlow
    override fun observePostBookmarks(): Flow<List<PostBookmark>> =
        bookmarkedPostIdsFlow.map { ids ->
            val postsById = communityPostsFlow.value.associateBy { it.id }
            ids.map { postId ->
                PostBookmark(
                    id = postId,
                    userId = currentUserId,
                    postId = postId,
                    postTitle = postsById[postId]?.title ?: "Saved Community Post"
                )
            }
        }
    override fun observeFollowedTopics(): Flow<List<FollowedTopic>> = followedTopicsFlow

    override suspend fun saveProfile(profile: CommunityProfile): Result<Unit> {
        ownProfileFlow.value = profile.copy(id = currentUserId, userId = currentUserId)
        return Result.success(Unit)
    }

    override suspend fun savePrivateSettings(settings: PrivateSettings): Result<Unit> {
        privateSettingsFlow.value = settings.copy(userId = currentUserId)
        return Result.success(Unit)
    }

    override suspend fun upsertAmbition(ambition: UserAmbition): Result<Unit> {
        val id = ambition.id.ifBlank { "amb_${UUID.randomUUID().toString().take(8)}" }
        val updated = ambition.copy(id = id, userId = currentUserId)
        ambitionsFlow.update { list ->
            val without = list.filterNot { it.id == id }
            listOf(updated) + without
        }
        return Result.success(Unit)
    }

    override suspend fun deleteAmbition(ambitionId: String): Result<Unit> {
        ambitionsFlow.update { list -> list.filterNot { it.id == ambitionId } }
        return Result.success(Unit)
    }

    override suspend fun createMission(
        mission: Mission,
        initialMilestones: List<Pair<String, String>>
    ): Result<String> {
        val id = mission.id.ifBlank { "mis_${UUID.randomUUID().toString().take(8)}" }
        val created = mission.copy(
            id = id,
            ownerId = currentUserId,
            memberUids = (mission.memberUids + currentUserId).distinct()
        )
        missionsFlow.update { listOf(created) + it }
        if (initialMilestones.isNotEmpty()) {
            val msList = initialMilestones.mapIndexed { idx, (t, r) ->
                MissionMilestone(
                    id = "ms_${id}_$idx",
                    missionId = id,
                    ownerId = currentUserId,
                    memberUids = created.memberUids,
                    title = t,
                    assignedRole = r,
                    completed = false
                )
            }
            milestonesFlow.update { it + (id to msList) }
        }
        return Result.success(id)
    }

    override suspend fun updateMissionProgress(
        missionId: String,
        progressPercent: Long,
        status: MissionStatus,
        updateStatus: Boolean
    ): Result<Unit> {
        missionsFlow.update { list ->
            list.map { m ->
                if (m.id == missionId) {
                    m.copy(
                        progressPercent = progressPercent.coerceIn(0L, 100L),
                        status = if (updateStatus) status.name else m.status
                    )
                } else {
                    m
                }
            }
        }
        return Result.success(Unit)
    }

    override suspend fun toggleMilestone(missionId: String, milestoneId: String, completed: Boolean): Result<Unit> {
        milestonesFlow.update { map ->
            val current = map[missionId].orEmpty()
            val updated = current.map { ms -> if (ms.id == milestoneId) ms.copy(completed = completed) else ms }
            map + (missionId to updated)
        }
        return Result.success(Unit)
    }

    override suspend fun sendInvitation(invitation: CollaborationInvitation): Result<Unit> {
        val id = "inv_${UUID.randomUUID().toString().take(8)}"
        invitationsFlow.update {
            listOf(
                invitation.copy(
                    id = id,
                    senderId = currentUserId,
                    participantUids = listOf(currentUserId, invitation.recipientId),
                    status = InvitationStatus.PENDING.name
                )
            ) + it
        }
        return Result.success(Unit)
    }

    override suspend fun respondToInvitation(
        invitationId: String,
        missionId: String,
        newStatus: InvitationStatus
    ): Result<Unit> {
        invitationsFlow.update { list ->
            list.map { inv -> if (inv.id == invitationId) inv.copy(status = newStatus.name) else inv }
        }
        if (newStatus == InvitationStatus.ACCEPTED) {
            missionsFlow.update { list ->
                list.map { m ->
                    if (m.id == missionId) m.copy(memberUids = (m.memberUids + currentUserId).distinct()) else m
                }
            }
        }
        return Result.success(Unit)
    }

    override suspend fun savePossibility(possibility: SavedPossibility): Result<Unit> {
        val id = "pos_${UUID.randomUUID().toString().take(8)}"
        savedPossibilitiesFlow.update { listOf(possibility.copy(id = id, userId = currentUserId)) + it }
        return Result.success(Unit)
    }

    override suspend fun blockUser(blockedUserId: String, reason: String): Result<Unit> {
        safetyBlocksFlow.update {
            listOf(SafetyBlock(id = blockedUserId, userId = currentUserId, blockedUserId = blockedUserId, reason = reason)) +
                it.filterNot { b -> b.blockedUserId == blockedUserId }
        }
        return Result.success(Unit)
    }

    override suspend fun unblockUser(blockedUserId: String): Result<Unit> {
        safetyBlocksFlow.update { list -> list.filterNot { it.blockedUserId == blockedUserId } }
        return Result.success(Unit)
    }

    override suspend fun toggleSkillEndorsement(
        recipientId: String,
        recipientName: String,
        endorserName: String,
        skillName: String
    ): Result<Boolean> {
        if (recipientId.isBlank() || recipientId == currentUserId) {
            return Result.failure(IllegalArgumentException("You can endorse skills belonging to other collaborators."))
        }
        val cleanSkill = skillName.trim().take(60)
        val normalized = SkillEndorsement.normalizeSkillKey(cleanSkill)
        val docId = SkillEndorsement.buildDocumentId(currentUserId, recipientId, cleanSkill)
        var added = false
        endorsementsFlow.update { list ->
            val exists = list.any { it.endorserId == currentUserId && it.recipientId == recipientId && it.normalizedSkill == normalized }
            if (exists) {
                added = false
                list.filterNot { it.endorserId == currentUserId && it.recipientId == recipientId && it.normalizedSkill == normalized }
            } else {
                added = true
                list + SkillEndorsement(
                    id = docId,
                    recipientId = recipientId,
                    recipientName = recipientName,
                    endorserId = currentUserId,
                    endorserName = endorserName,
                    skillName = cleanSkill,
                    normalizedSkill = normalized
                )
            }
        }
        return Result.success(added)
    }

    override suspend fun sendCollaborationMessage(
        recipientId: String,
        recipientName: String,
        senderName: String,
        body: String,
        contextLabel: String
    ): Result<Unit> {
        val cleanBody = body.trim().take(1000)
        if (cleanBody.isEmpty()) {
            return Result.failure(IllegalArgumentException("Message cannot be empty."))
        }
        val msg = CollaborationMessage(
            id = "msg_${UUID.randomUUID().toString().take(8)}",
            threadId = CollaborationMessage.buildThreadId(currentUserId, recipientId),
            senderId = currentUserId,
            senderName = senderName.ifBlank { "Explorer" },
            recipientId = recipientId,
            recipientName = recipientName.ifBlank { "Collaborator" },
            participantUids = listOf(currentUserId, recipientId),
            body = cleanBody,
            contextLabel = contextLabel.trim().take(140)
        )
        messagesFlow.update { it + msg }
        return Result.success(Unit)
    }

    override suspend fun createCommunityPost(post: CommunityPost): Result<String> {
        val cleanTitle = post.title.trim()
        val cleanBody = post.body.trim()
        if (cleanTitle.length < 3 || cleanBody.length < 5) {
            return Result.failure(IllegalArgumentException("Please enter a title and a few details."))
        }
        val id = "post_${UUID.randomUUID().toString().take(8)}"
        val created = post.copy(id = id, authorId = currentUserId, title = cleanTitle, body = cleanBody)
        communityPostsFlow.update { listOf(created) + it }
        return Result.success(id)
    }

    override suspend fun togglePostAppreciation(post: CommunityPost): Result<Boolean> {
        var nowAppreciated = false
        communityPostsFlow.update { list ->
            list.map { item ->
                if (item.id == post.id) {
                    val has = currentUserId in item.appreciatorUids
                    nowAppreciated = !has
                    val next = if (has) item.appreciatorUids - currentUserId else (item.appreciatorUids + currentUserId).distinct()
                    item.copy(appreciatorUids = next)
                } else item
            }
        }
        return Result.success(nowAppreciated)
    }

    override suspend fun addCommunityComment(comment: CommunityComment): Result<Unit> {
        val cleanBody = comment.body.trim().take(800)
        if (cleanBody.isEmpty()) {
            return Result.failure(IllegalArgumentException("Comment cannot be empty."))
        }
        val created = comment.copy(
            id = "cmt_${UUID.randomUUID().toString().take(8)}",
            authorId = currentUserId,
            body = cleanBody
        )
        communityCommentsFlow.update { it + created }
        return Result.success(Unit)
    }

    override suspend fun togglePostBookmark(post: CommunityPost): Result<Boolean> {
        var bookmarked = false
        bookmarkedPostIdsFlow.update { current ->
            if (post.id in current) {
                bookmarked = false
                current - post.id
            } else {
                bookmarked = true
                current + post.id
            }
        }
        return Result.success(bookmarked)
    }

    override suspend fun toggleFollowTopic(topic: String): Result<Boolean> {
        val clean = topic.trim().take(80)
        val id = FollowedTopic.buildTopicId(clean)
        var followed = false
        followedTopicsFlow.update { list ->
            if (list.any { it.id == id || it.topic.equals(clean, ignoreCase = true) }) {
                followed = false
                list.filterNot { it.id == id || it.topic.equals(clean, ignoreCase = true) }
            } else {
                followed = true
                list + FollowedTopic(id = id, userId = currentUserId, topic = clean)
            }
        }
        return Result.success(followed)
    }

    override suspend fun reportCommunityPost(post: CommunityPost, reason: String): Result<Unit> {
        communityReportsFlow.update {
            it + CommunityReport(
                id = "rep_${UUID.randomUUID().toString().take(8)}",
                reporterId = currentUserId,
                postId = post.id,
                targetAuthorId = post.authorId,
                reason = reason.trim().ifBlank { "Community safety review" }
            )
        }
        return Result.success(Unit)
    }
}
