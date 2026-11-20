package com.example.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class CollaborationStyle(val label: String) {
    ASYNC("Async & Flexible"),
    REALTIME("Live Co-Creation"),
    HYBRID("Hybrid Mix"),
    SMALL_GROUP("Small Group Circles");

    companion object {
        fun safeParse(raw: String?): CollaborationStyle =
            entries.firstOrNull { it.name == raw } ?: HYBRID
    }
}

enum class AvailabilityWindow(val label: String) {
    WEEKDAYS("Weekdays"),
    WEEKENDS("Weekends"),
    EVENINGS("Evenings"),
    FLEXIBLE("Flexible Schedule");

    companion object {
        fun safeParse(raw: String?): AvailabilityWindow =
            entries.firstOrNull { it.name == raw } ?: FLEXIBLE
    }
}

enum class ProfileVisibility(val label: String, val description: String) {
    COMMUNITY("Community Discoverable", "Visible to verified peers in your learning community."),
    INVITE_ONLY("Invite Only", "Hidden from open discovery; connect via direct mission invites."),
    PRIVATE("Private Draft", "Only visible to you while exploring capabilities.")
}

enum class AmbitionHorizon(val label: String) {
    SHORT_TERM("Short-Term Goal"),
    LONG_TERM("Long-Term Vision");

    companion object {
        fun safeParse(raw: String?): AmbitionHorizon =
            entries.firstOrNull { it.name == raw } ?: SHORT_TERM
    }
}

enum class AmbitionPriority(val label: String) {
    HIGH("High Focus"),
    MEDIUM("Steady"),
    LOW("Exploring");

    companion object {
        fun safeParse(raw: String?): AmbitionPriority =
            entries.firstOrNull { it.name == raw } ?: MEDIUM
    }
}

enum class MissionStatus(val label: String) {
    OPEN("Seeking Collaborators"),
    ACTIVE("Active Collaboration"),
    COMPLETED("Accomplished");

    companion object {
        fun safeParse(raw: String?): MissionStatus =
            entries.firstOrNull { it.name == raw } ?: OPEN
    }
}

enum class InvitationStatus(val label: String) {
    PENDING("Awaiting Response"),
    ACCEPTED("Accepted"),
    DECLINED("Declined"),
    CANCELLED("Withdrawn");

    companion object {
        fun safeParse(raw: String?): InvitationStatus =
            entries.firstOrNull { it.name == raw } ?: PENDING
    }
}

enum class SkillProficiency(
    val level: Int,
    val label: String,
    val shortLabel: String,
    val matchingWeight: Float,
    val description: String
) {
    BEGINNER(1, "Beginner", "Lv 1", 0.55f, "Learning fundamentals & open to mentorship"),
    INTERMEDIATE(2, "Intermediate", "Lv 2", 0.78f, "Comfortable contributing independently"),
    ADVANCED(3, "Advanced", "Lv 3", 0.96f, "Strong depth & ready to lead parts"),
    EXPERT(4, "Expert", "Lv 4", 1.15f, "Deep mastery & able to guide peers");

    companion object {
        fun safeParse(raw: String?): SkillProficiency =
            entries.firstOrNull {
                it.name.equals(raw, ignoreCase = true) ||
                    it.label.equals(raw, ignoreCase = true)
            } ?: INTERMEDIATE
    }
}

@Keep
@IgnoreExtraProperties
data class CommunityProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val headline: String = "",
    val bio: String = "",
    val skills: List<String> = emptyList(),
    val skillProficiencies: Map<String, String> = emptyMap(),
    val knowledgeAreas: List<String> = emptyList(),
    val practicalAbilities: List<String> = emptyList(),
    val interests: List<String> = emptyList(),
    val resources: List<String> = emptyList(),
    val supportWanted: List<String> = emptyList(),
    val ambitions: List<String> = emptyList(),
    val currentProjects: List<String> = emptyList(),
    val collaborationStyle: String = CollaborationStyle.HYBRID.name,
    val availability: String = AvailabilityWindow.FLEXIBLE.name,
    val communicationPrefs: List<String> = emptyList(),
    val accessibilityPrefs: List<String> = emptyList(),
    val constraintsNote: String = "",
    val visibility: String = ProfileVisibility.COMMUNITY.name,
    val onboardingCompleted: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun allCapabilities(): List<String> =
        (skills + knowledgeAreas + practicalAbilities + resources)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }

    fun proficiencyFor(skill: String): SkillProficiency {
        val clean = skill.trim()
        val raw = skillProficiencies[clean]
            ?: skillProficiencies.entries.firstOrNull { it.key.equals(clean, ignoreCase = true) }?.value
        return SkillProficiency.safeParse(raw)
    }

    fun sanitizedSkillProficiencies(): Map<String, String> =
        skillProficiencies.entries
            .mapNotNull { (k, v) ->
                val cleanKey = k.trim().take(60)
                if (cleanKey.isEmpty()) null else cleanKey to SkillProficiency.safeParse(v).name
            }
            .take(20)
            .toMap()

    fun toCreateMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "displayName" to displayName.trim().take(60),
        "headline" to headline.trim().take(140),
        "bio" to bio.trim().take(600),
        "skills" to skills.take(20),
        "skillProficiencies" to sanitizedSkillProficiencies(),
        "knowledgeAreas" to knowledgeAreas.take(20),
        "practicalAbilities" to practicalAbilities.take(20),
        "interests" to interests.take(20),
        "resources" to resources.take(15),
        "supportWanted" to supportWanted.take(15),
        "ambitions" to ambitions.take(15),
        "currentProjects" to currentProjects.take(10),
        "collaborationStyle" to collaborationStyle,
        "availability" to availability,
        "communicationPrefs" to communicationPrefs.take(6),
        "accessibilityPrefs" to accessibilityPrefs.take(8),
        "constraintsNote" to constraintsNote.trim().take(300),
        "visibility" to visibility,
        "onboardingCompleted" to onboardingCompleted,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )

    fun toUpdateMap(): Map<String, Any> = mapOf(
        "displayName" to displayName.trim().take(60),
        "headline" to headline.trim().take(140),
        "bio" to bio.trim().take(600),
        "skills" to skills.take(20),
        "skillProficiencies" to sanitizedSkillProficiencies(),
        "knowledgeAreas" to knowledgeAreas.take(20),
        "practicalAbilities" to practicalAbilities.take(20),
        "interests" to interests.take(20),
        "resources" to resources.take(15),
        "supportWanted" to supportWanted.take(15),
        "ambitions" to ambitions.take(15),
        "currentProjects" to currentProjects.take(10),
        "collaborationStyle" to collaborationStyle,
        "availability" to availability,
        "communicationPrefs" to communicationPrefs.take(6),
        "accessibilityPrefs" to accessibilityPrefs.take(8),
        "constraintsNote" to constraintsNote.trim().take(300),
        "visibility" to visibility,
        "onboardingCompleted" to onboardingCompleted,
        "updatedAt" to FieldValue.serverTimestamp()
    )
}

@Keep
@IgnoreExtraProperties
data class PrivateSettings(
    @DocumentId val id: String = "account",
    val userId: String = "",
    val schoolOrCommunityContext: String = "Community Collaboration Workspace",
    val consentToCollaborate: Boolean = true,
    val allowDirectInvitations: Boolean = true,
    val safetyGuidanceAcknowledged: Boolean = true,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toWriteMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "schoolOrCommunityContext" to schoolOrCommunityContext.trim().take(120),
        "consentToCollaborate" to consentToCollaborate,
        "allowDirectInvitations" to allowDirectInvitations,
        "safetyGuidanceAcknowledged" to safetyGuidanceAcknowledged,
        "updatedAt" to FieldValue.serverTimestamp()
    )
}

@Keep
@IgnoreExtraProperties
data class UserAmbition(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val horizon: String = AmbitionHorizon.SHORT_TERM.name,
    val priority: String = AmbitionPriority.MEDIUM.name,
    val tags: List<String> = emptyList(),
    val sharedToCommunity: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "title" to title.trim().take(120),
        "description" to description.trim().take(500),
        "horizon" to horizon,
        "priority" to priority,
        "tags" to tags.take(10),
        "sharedToCommunity" to sharedToCommunity,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )
}

@Keep
@IgnoreExtraProperties
data class Mission(
    @DocumentId val id: String = "",
    val ownerId: String = "",
    val ownerName: String = "",
    val title: String = "",
    val description: String = "",
    val objective: String = "",
    val intendedOutcome: String = "",
    val problemAddressed: String = "",
    val interpretedIntent: String = "",
    val intentDomain: String = "",
    val requiredCapabilities: List<String> = emptyList(),
    val optionalCapabilities: List<String> = emptyList(),
    val activities: List<String> = emptyList(),
    val availabilityReq: String = "Flexible",
    val accessibilityReq: String = "Inclusive & Accessible",
    val targetParticipants: Long = 3L,
    val memberUids: List<String> = emptyList(),
    val status: String = MissionStatus.OPEN.name,
    val visibility: String = "COMMUNITY",
    val progressPercent: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = mapOf(
        "ownerId" to ownerId,
        "ownerName" to ownerName.trim().take(60),
        "title" to title.trim().take(120),
        "description" to description.trim().take(1000),
        "objective" to objective.trim().take(300),
        "intendedOutcome" to intendedOutcome.trim().take(300),
        "problemAddressed" to problemAddressed.trim().take(300),
        "interpretedIntent" to interpretedIntent.trim().take(500),
        "intentDomain" to intentDomain.trim().take(80),
        "requiredCapabilities" to requiredCapabilities.take(15),
        "optionalCapabilities" to optionalCapabilities.take(15),
        "activities" to activities.take(15),
        "availabilityReq" to availabilityReq.trim().take(40),
        "accessibilityReq" to accessibilityReq.trim().take(120),
        "targetParticipants" to targetParticipants.coerceIn(1L, 20L),
        "memberUids" to memberUids.take(20),
        "status" to status,
        "visibility" to visibility,
        "progressPercent" to progressPercent.coerceIn(0L, 100L),
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )
}

@Keep
@IgnoreExtraProperties
data class MissionMilestone(
    @DocumentId val id: String = "",
    val missionId: String = "",
    val ownerId: String = "",
    val memberUids: List<String> = emptyList(),
    val title: String = "",
    val assignedRole: String = "",
    val completed: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = mapOf(
        "missionId" to missionId,
        "ownerId" to ownerId,
        "memberUids" to memberUids.take(20),
        "title" to title.trim().take(160),
        "assignedRole" to assignedRole.trim().take(80),
        "completed" to completed,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )
}

@Keep
@IgnoreExtraProperties
data class CollaborationInvitation(
    @DocumentId val id: String = "",
    val missionId: String = "",
    val missionTitle: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val recipientId: String = "",
    val recipientName: String = "",
    val participantUids: List<String> = emptyList(),
    val proposedRole: String = "",
    val rationale: String = "",
    val status: String = InvitationStatus.PENDING.name,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = mapOf(
        "missionId" to missionId,
        "missionTitle" to missionTitle.trim().take(120),
        "senderId" to senderId,
        "senderName" to senderName.trim().take(60),
        "recipientId" to recipientId,
        "recipientName" to recipientName.trim().take(60),
        "participantUids" to listOf(senderId, recipientId),
        "proposedRole" to proposedRole.trim().take(80),
        "rationale" to rationale.trim().take(400),
        "status" to InvitationStatus.PENDING.name,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )
}

@Keep
@IgnoreExtraProperties
data class SavedPossibility(
    @DocumentId val id: String = "",
    val userId: String = "",
    val objective: String = "",
    val summary: String = "",
    val missingCapabilities: List<String> = emptyList(),
    val suggestedCollaborators: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "objective" to objective.trim().take(300),
        "summary" to summary.trim().take(600),
        "missingCapabilities" to missingCapabilities.take(15),
        "suggestedCollaborators" to suggestedCollaborators.take(15),
        "createdAt" to FieldValue.serverTimestamp()
    )
}

@Keep
@IgnoreExtraProperties
data class SafetyBlock(
    @DocumentId val id: String = "",
    val userId: String = "",
    val blockedUserId: String = "",
    val reason: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "blockedUserId" to blockedUserId,
        "reason" to reason.trim().take(300),
        "createdAt" to FieldValue.serverTimestamp()
    )
}

@Keep
@IgnoreExtraProperties
data class SkillEndorsement(
    @DocumentId val id: String = "",
    val recipientId: String = "",
    val recipientName: String = "",
    val endorserId: String = "",
    val endorserName: String = "",
    val skillName: String = "",
    val normalizedSkill: String = "",
    val note: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = mapOf(
        "recipientId" to recipientId,
        "recipientName" to recipientName.trim().ifBlank { "Collaborator" }.take(60),
        "endorserId" to endorserId,
        "endorserName" to endorserName.trim().ifBlank { "Peer" }.take(60),
        "skillName" to skillName.trim().take(60),
        "normalizedSkill" to normalizeSkillKey(normalizedSkill.ifBlank { skillName }),
        "note" to note.trim().take(200),
        "createdAt" to FieldValue.serverTimestamp()
    )

    companion object {
        fun normalizeSkillKey(raw: String): String =
            raw.trim().lowercase().replace(Regex("\\s+"), " ").take(60)

        fun buildDocumentId(endorserId: String, recipientId: String, skillName: String): String {
            val normalized = normalizeSkillKey(skillName)
            val slug = normalized.replace(Regex("[^a-z0-9]+"), "_").trim('_').take(28).ifBlank { "skill" }
            val hash = (endorserId + "|" + recipientId + "|" + normalized).hashCode().toUInt().toString(16)
            val safeEndorser = endorserId.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(32)
            val safeRecipient = recipientId.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(32)
            return "${safeEndorser}_${safeRecipient}_${slug}_$hash".take(120)
        }
    }
}

data class SkillEndorsementSummary(
    val skillKey: String,
    val count: Int,
    val endorsedByCurrentUser: Boolean,
    val endorserNames: List<String> = emptyList()
)

@Keep
@IgnoreExtraProperties
data class CollaborationMessage(
    @DocumentId val id: String = "",
    val threadId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val recipientId: String = "",
    val recipientName: String = "",
    val participantUids: List<String> = emptyList(),
    val body: String = "",
    val contextLabel: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = mapOf(
        "threadId" to threadId,
        "senderId" to senderId,
        "senderName" to senderName.trim().ifBlank { "Explorer" }.take(60),
        "recipientId" to recipientId,
        "recipientName" to recipientName.trim().ifBlank { "Collaborator" }.take(60),
        "participantUids" to listOf(senderId, recipientId),
        "body" to body.trim().take(1000),
        "contextLabel" to contextLabel.trim().take(140),
        "createdAt" to FieldValue.serverTimestamp()
    )

    companion object {
        fun buildThreadId(uidA: String, uidB: String): String {
            val cleanA = uidA.replace(Regex("[^a-zA-Z0-9_-]"), "_")
            val cleanB = uidB.replace(Regex("[^a-zA-Z0-9_-]"), "_")
            val sorted = listOf(cleanA, cleanB).sorted()
            val hash = sorted.joinToString("|").hashCode().toUInt().toString(16)
            return "thr_${sorted[0].take(36)}_${sorted[1].take(36)}_$hash".take(120)
        }
    }
}

enum class AppThemeMode(
    val label: String,
    val description: String
) {
    CREAM("Warm Cream", "Botanical cream & forest warmth"),
    WHITE("White Mode", "Crisp pure white daylight canvas"),
    DARK("Dark Mode", "Low-glare deep forest night palette");

    companion object {
        fun safeParse(raw: String?): AppThemeMode =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: CREAM
    }
}

enum class PostCategory(
    val label: String,
    val shortLabel: String,
    val promptHint: String
) {
    IDEAS("Ideas", "Ideas", "Share concepts, inventions & creative thoughts"),
    QUESTIONS("Questions", "Questions", "Ask the community for knowledge or guidance"),
    LOOKING_FOR_COLLABORATORS("Looking for Collaborators", "Collaborators", "Find people with relevant skills & capabilities"),
    PROJECT_UPDATES("Project Updates", "Updates", "Share ongoing work & milestone progress"),
    OPPORTUNITIES("Opportunities", "Opportunities", "Announce workshops, events & open missions"),
    RESOURCES("Resources", "Resources", "Share learning guides, tools & discoveries"),
    ACHIEVEMENTS("Achievements", "Achievements", "Celebrate completed collaborations & milestones");

    companion object {
        fun safeParse(raw: String?): PostCategory =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: IDEAS
    }
}

@Keep
@IgnoreExtraProperties
data class CommunityPost(
    @DocumentId val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorHeadline: String = "",
    val category: String = PostCategory.IDEAS.name,
    val title: String = "",
    val body: String = "",
    val capabilityTags: List<String> = emptyList(),
    val requestedCapabilities: List<String> = emptyList(),
    val missionId: String = "",
    val missionTitle: String = "",
    val communityLocationTag: String = "",
    val shareLocationTag: Boolean = false,
    val linkUrl: String = "",
    val imageUri: String = "",
    val visibility: String = "COMMUNITY",
    val appreciatorUids: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun categoryEnum(): PostCategory = PostCategory.safeParse(category)

    fun toCreateMap(): Map<String, Any> = mapOf(
        "authorId" to authorId,
        "authorName" to authorName.trim().ifBlank { "Explorer" }.take(60),
        "authorHeadline" to authorHeadline.trim().take(140),
        "category" to PostCategory.safeParse(category).name,
        "title" to title.trim().take(140),
        "body" to body.trim().take(2000),
        "capabilityTags" to capabilityTags.map { it.trim() }.filter { it.isNotEmpty() }.distinct().take(15),
        "requestedCapabilities" to requestedCapabilities.map { it.trim() }.filter { it.isNotEmpty() }.distinct().take(10),
        "missionId" to missionId.trim().take(128),
        "missionTitle" to missionTitle.trim().take(140),
        "communityLocationTag" to if (shareLocationTag) communityLocationTag.trim().take(80) else "",
        "shareLocationTag" to shareLocationTag,
        "linkUrl" to linkUrl.trim().take(300),
        "imageUri" to imageUri.trim().take(500),
        "visibility" to if (visibility == "CONNECTIONS") "CONNECTIONS" else "COMMUNITY",
        "appreciatorUids" to appreciatorUids.distinct().take(200),
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )
}

@Keep
@IgnoreExtraProperties
data class CommunityComment(
    @DocumentId val id: String = "",
    val postId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val body: String = "",
    val replyToAuthor: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = mapOf(
        "postId" to postId,
        "authorId" to authorId,
        "authorName" to authorName.trim().ifBlank { "Explorer" }.take(60),
        "body" to body.trim().take(800),
        "replyToAuthor" to replyToAuthor.trim().take(60),
        "createdAt" to FieldValue.serverTimestamp()
    )
}

@Keep
@IgnoreExtraProperties
data class PostBookmark(
    @DocumentId val id: String = "",
    val userId: String = "",
    val postId: String = "",
    val postTitle: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "postId" to postId,
        "postTitle" to postTitle.trim().take(140),
        "createdAt" to FieldValue.serverTimestamp()
    )
}

@Keep
@IgnoreExtraProperties
data class FollowedTopic(
    @DocumentId val id: String = "",
    val userId: String = "",
    val topic: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "topic" to topic.trim().take(80),
        "createdAt" to FieldValue.serverTimestamp()
    )

    companion object {
        fun buildTopicId(topic: String): String =
            topic.trim().lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_').take(60).ifBlank { "topic" }
    }
}

@Keep
@IgnoreExtraProperties
data class CommunityReport(
    @DocumentId val id: String = "",
    val reporterId: String = "",
    val postId: String = "",
    val targetAuthorId: String = "",
    val reason: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = mapOf(
        "reporterId" to reporterId,
        "postId" to postId,
        "targetAuthorId" to targetAuthorId,
        "reason" to reason.trim().take(400),
        "createdAt" to FieldValue.serverTimestamp()
    )
}

data class IntentPostRecommendation(
    val post: CommunityPost,
    val relevancePercent: Int,
    val whyRelevant: String,
    val matchingCapabilities: List<String>,
    val complementaryNeedsYouHold: List<String>
)

enum class ActivityEventType(val label: String) {
    ENDORSEMENT_RECEIVED("Skill Endorsement"),
    ENDORSEMENT_GIVEN("Endorsed a Peer"),
    COLLABORATION_ACCEPTED("Collaboration Active"),
    MISSION_MILESTONE("Milestone Completed"),
    MISSION_ACCOMPLISHED("Mission Accomplished"),
    COMMUNITY_POST("Community Contribution")
}

data class ActivityFeedItem(
    val id: String,
    val type: ActivityEventType,
    val title: String,
    val detail: String,
    val peerName: String,
    val badgeText: String,
    val timestampMillis: Long,
    val targetPeerId: String = ""
)

data class AccessibilitySettings(
    val textScale: Float = 1.0f,
    val highContrastMode: Boolean = false,
    val reducedMotion: Boolean = false,
    val preferredComms: String = "Text & Screen-Reader Structured",
    val themeMode: AppThemeMode = AppThemeMode.CREAM
)

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

