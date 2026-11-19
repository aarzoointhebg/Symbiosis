package com.example.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

/**
 * Core matching-focused user profile model collecting skills, interests,
 * and schedule availability for capability and ambition matching.
 */
@Keep
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val headline: String = "",
    val bio: String = "",
    val skills: List<String> = emptyList(),
    val skillProficiencies: Map<String, String> = emptyMap(),
    val interests: List<String> = emptyList(),
    val availability: String = AvailabilityWindow.FLEXIBLE.name,
    val availableDays: List<String> = emptyList(),
    val timeSlots: List<String> = emptyList(),
    val collaborationStyle: String = CollaborationStyle.HYBRID.name,
    val supportWanted: List<String> = emptyList(),
    val onboardingCompleted: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val availabilityWindow: AvailabilityWindow
        get() = AvailabilityWindow.safeParse(availability)

    val preferredStyle: CollaborationStyle
        get() = CollaborationStyle.safeParse(collaborationStyle)

    fun proficiencyFor(skill: String): SkillProficiency {
        val clean = skill.trim()
        val raw = skillProficiencies[clean]
            ?: skillProficiencies.entries.firstOrNull { it.key.equals(clean, ignoreCase = true) }?.value
        return SkillProficiency.safeParse(raw)
    }

    fun sanitizedSkillProficiencies(): Map<String, String> {
        val activeSkills = skills.map { it.trim() }.filter { it.isNotEmpty() }.distinctBy { it.lowercase() }
        val map = linkedMapOf<String, String>()
        activeSkills.take(20).forEach { skill ->
            map[skill.take(60)] = proficiencyFor(skill).name
        }
        return map
    }

    fun formattedAvailabilitySummary(): String {
        val parts = mutableListOf(availabilityWindow.label)
        if (availableDays.isNotEmpty()) {
            parts.add(availableDays.joinToString(", "))
        }
        if (timeSlots.isNotEmpty()) {
            parts.add(timeSlots.joinToString(" / "))
        }
        return parts.joinToString(" • ")
    }

    fun toCreateMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "displayName" to displayName.trim().take(60),
        "headline" to headline.trim().take(140),
        "bio" to bio.trim().take(600),
        "skills" to skills.map { it.trim() }.filter { it.isNotEmpty() }.distinctBy { it.lowercase() }.take(20),
        "skillProficiencies" to sanitizedSkillProficiencies(),
        "interests" to interests.map { it.trim() }.filter { it.isNotEmpty() }.distinctBy { it.lowercase() }.take(20),
        "availability" to availabilityWindow.name,
        "availableDays" to availableDays.map { it.trim() }.filter { it.isNotEmpty() }.distinct().take(7),
        "timeSlots" to timeSlots.map { it.trim() }.filter { it.isNotEmpty() }.distinct().take(3),
        "collaborationStyle" to preferredStyle.name,
        "supportWanted" to supportWanted.map { it.trim() }.filter { it.isNotEmpty() }.distinctBy { it.lowercase() }.take(15),
        "onboardingCompleted" to onboardingCompleted,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )

    fun toUpdateMap(): Map<String, Any> = mapOf(
        "displayName" to displayName.trim().take(60),
        "headline" to headline.trim().take(140),
        "bio" to bio.trim().take(600),
        "skills" to skills.map { it.trim() }.filter { it.isNotEmpty() }.distinctBy { it.lowercase() }.take(20),
        "skillProficiencies" to sanitizedSkillProficiencies(),
        "interests" to interests.map { it.trim() }.filter { it.isNotEmpty() }.distinctBy { it.lowercase() }.take(20),
        "availability" to availabilityWindow.name,
        "availableDays" to availableDays.map { it.trim() }.filter { it.isNotEmpty() }.distinct().take(7),
        "timeSlots" to timeSlots.map { it.trim() }.filter { it.isNotEmpty() }.distinct().take(3),
        "collaborationStyle" to preferredStyle.name,
        "supportWanted" to supportWanted.map { it.trim() }.filter { it.isNotEmpty() }.distinctBy { it.lowercase() }.take(15),
        "onboardingCompleted" to onboardingCompleted,
        "updatedAt" to FieldValue.serverTimestamp()
    )

    fun toCommunityProfile(existing: CommunityProfile? = null): CommunityProfile {
        val base = existing ?: CommunityProfile(id = id, userId = userId)
        val scheduleNote = buildString {
            if (availableDays.isNotEmpty()) append("Days: ${availableDays.joinToString(", ")}")
            if (timeSlots.isNotEmpty()) {
                if (isNotEmpty()) append(" | ")
                append("Times: ${timeSlots.joinToString(", ")}")
            }
        }
        val mergedProficiencies = (base.skillProficiencies + sanitizedSkillProficiencies())
            .entries
            .take(20)
            .associate { it.key to it.value }
        return base.copy(
            id = id.ifBlank { base.id },
            userId = userId.ifBlank { base.userId },
            displayName = displayName.trim().ifBlank { base.displayName.ifBlank { "Explorer" } },
            headline = headline.trim().ifBlank { base.headline },
            bio = bio.trim().ifBlank { base.bio },
            skills = skills.distinctBy { it.lowercase() },
            skillProficiencies = mergedProficiencies,
            interests = interests.distinctBy { it.lowercase() },
            availability = availabilityWindow.name,
            collaborationStyle = preferredStyle.name,
            supportWanted = supportWanted.ifEmpty { base.supportWanted },
            constraintsNote = scheduleNote.ifBlank { base.constraintsNote },
            onboardingCompleted = onboardingCompleted
        )
    }
}

fun CommunityProfile.toUserProfile(): UserProfile {
    val parsedDays = Regex("""Days:\s*([^|]+)""")
        .find(constraintsNote)
        ?.groupValues?.getOrNull(1)
        ?.split(",")
        ?.map { it.trim() }
        ?.filter { it.isNotEmpty() }
        ?: emptyList()
    val parsedTimes = Regex("""Times:\s*([^|]+)""")
        .find(constraintsNote)
        ?.groupValues?.getOrNull(1)
        ?.split(",")
        ?.map { it.trim() }
        ?.filter { it.isNotEmpty() }
        ?: emptyList()

    return UserProfile(
        id = id.ifBlank { userId },
        userId = userId,
        displayName = displayName,
        headline = headline,
        bio = bio,
        skills = skills,
        skillProficiencies = skillProficiencies,
        interests = interests,
        availability = AvailabilityWindow.safeParse(availability).name,
        availableDays = parsedDays,
        timeSlots = parsedTimes,
        collaborationStyle = CollaborationStyle.safeParse(collaborationStyle).name,
        supportWanted = supportWanted,
        onboardingCompleted = onboardingCompleted,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
