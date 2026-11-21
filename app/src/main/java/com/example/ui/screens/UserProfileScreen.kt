package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkRemove
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Interests
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.CommunityComment
import com.example.model.CommunityPost
import com.example.model.CommunityProfile
import com.example.model.Mission
import com.example.model.MissionMilestone
import com.example.model.MissionStatus
import com.example.model.SkillEndorsementSummary
import com.example.model.UserProfile
import com.example.ui.ActiveCollaborationsProgressSummary
import com.example.ui.BookmarkedPostEntry
import com.example.ui.components.MascotBadgeAvatar
import com.example.ui.components.ProfileSkillBadgesSection
import com.example.ui.components.ProfileSkillsAndInterestsEditor
import com.example.ui.theme.BotanicalGreen
import com.example.ui.theme.CoverageGreen
import com.example.ui.theme.DarkMintPrimary
import com.example.ui.theme.DeepForestGreen
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.LocalSymbiosisIsDark
import com.example.ui.theme.SoftCream
import com.example.ui.theme.WarmGoldenYellow

private enum class MissionHistoryFilter(val label: String) {
    ALL("All"),
    ACTIVE("Active"),
    COMPLETED("Completed")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UserProfileScreen(
    isOwnProfile: Boolean,
    userProfile: UserProfile,
    communityProfile: CommunityProfile,
    accountEmail: String?,
    isCloudSynced: Boolean,
    endorsementsBySkill: Map<String, SkillEndorsementSummary>,
    missionHistory: List<Mission>,
    activeCollaborationsProgress: ActiveCollaborationsProgressSummary = ActiveCollaborationsProgressSummary(),
    bookmarkedEntries: List<BookmarkedPostEntry>,
    commentsByPostId: Map<String, List<CommunityComment>>,
    authoredPosts: List<CommunityPost> = emptyList(),
    isProfileSaving: Boolean = false,
    onSaveSkillsAndInterests: (skills: List<String>, interests: List<String>, onDone: () -> Unit) -> Unit = { _, _, _ -> },
    onEditProfile: () -> Unit = {},
    onStartOnboarding: () -> Unit = {},
    onToggleSkillEndorsement: (CommunityProfile, String) -> Unit = { _, _ -> },
    onOpenChatWithMember: (CommunityProfile) -> Unit = {},
    onInviteMember: (CommunityProfile) -> Unit = {},
    onToggleMissionTask: (Mission, MissionMilestone) -> Unit = { _, _ -> },
    onOpenMission: (Mission) -> Unit = {},
    onExploreMissions: () -> Unit = {},
    onOpenBookmarkedPost: (CommunityPost) -> Unit = {},
    onRemoveBookmark: (BookmarkedPostEntry) -> Unit = {},
    onBrowseCommunity: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isDark = LocalSymbiosisIsDark.current
    var missionFilter by remember { mutableStateOf(MissionHistoryFilter.ALL) }
    var showInlineSkillsEditor by remember { mutableStateOf(false) }

    val mergedSkills = remember(userProfile.skills, communityProfile) {
        (userProfile.skills + communityProfile.allCapabilities())
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }
    }
    val mergedInterests = remember(userProfile.interests, communityProfile.interests) {
        (userProfile.interests + communityProfile.interests)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }
    }
    val mergedSupport = remember(userProfile.supportWanted, communityProfile.supportWanted) {
        (userProfile.supportWanted + communityProfile.supportWanted)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }
    }

    val totalEndorsements = remember(endorsementsBySkill) {
        endorsementsBySkill.values.sumOf { it.count }
    }

    val activeMissionsCount = remember(missionHistory) {
        missionHistory.count { MissionStatus.safeParse(it.status) != MissionStatus.COMPLETED }
    }
    val completedMissionsCount = remember(missionHistory) {
        missionHistory.count { MissionStatus.safeParse(it.status) == MissionStatus.COMPLETED }
    }
    val filteredMissions = remember(missionHistory, missionFilter) {
        when (missionFilter) {
            MissionHistoryFilter.ALL -> missionHistory
            MissionHistoryFilter.ACTIVE -> missionHistory.filter { MissionStatus.safeParse(it.status) != MissionStatus.COMPLETED }
            MissionHistoryFilter.COMPLETED -> missionHistory.filter { MissionStatus.safeParse(it.status) == MissionStatus.COMPLETED }
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Profile Hero Card
            item(key = "hero") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("user_profile_hero_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            DeepForestGreen,
                                            ForestGreenDark,
                                            BotanicalGreen
                                        )
                                    )
                                )
                                .padding(18.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                MascotBadgeAvatar(isDuck = !isOwnProfile)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = userProfile.displayName.ifBlank { communityProfile.displayName.ifBlank { "Community Member" } },
                                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                        color = SoftCream
                                    )
                                    val headline = userProfile.headline.ifBlank { communityProfile.headline }
                                    if (headline.isNotBlank()) {
                                        Text(
                                            text = headline,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SoftCream.copy(alpha = 0.88f)
                                        )
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = WarmGoldenYellow.copy(alpha = 0.22f)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.CloudDone,
                                                    contentDescription = null,
                                                    tint = WarmGoldenYellow,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Text(
                                                    text = if (isCloudSynced) "Firestore Profile" else "Local Workspace",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                                    color = SoftCream
                                                )
                                            }
                                        }
                                        if (isOwnProfile && !accountEmail.isNullOrBlank()) {
                                            Text(
                                                text = accountEmail,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = SoftCream.copy(alpha = 0.8f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val bio = userProfile.bio.ifBlank { communityProfile.bio }
                            if (bio.isNotBlank()) {
                                Text(
                                    text = bio,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Schedule,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "${userProfile.formattedAvailabilitySummary()} • ${userProfile.preferredStyle.label}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ProfileStatPill(
                                    value = "${mergedSkills.size}",
                                    label = "Skills",
                                    modifier = Modifier.weight(1f)
                                )
                                ProfileStatPill(
                                    value = "${missionHistory.size}",
                                    label = "Missions",
                                    modifier = Modifier.weight(1f)
                                )
                                ProfileStatPill(
                                    value = if (isOwnProfile) "${bookmarkedEntries.size}" else "$totalEndorsements",
                                    label = if (isOwnProfile) "Bookmarked" else "Endorsements",
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            if (isOwnProfile) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = onEditProfile,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .testTag("user_profile_edit_button"),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Edit Skills & Profile")
                                    }
                                    OutlinedButton(
                                        onClick = onStartOnboarding,
                                        modifier = Modifier
                                            .height(48.dp)
                                            .testTag("user_profile_onboarding_button"),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Icon(Icons.Rounded.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Setup")
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { onOpenChatWithMember(communityProfile) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .testTag("user_profile_message_button"),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Icon(Icons.Rounded.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Message")
                                    }
                                    OutlinedButton(
                                        onClick = { onInviteMember(communityProfile) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .testTag("user_profile_invite_button"),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Icon(Icons.Rounded.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Invite")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. Skills, Proficiency & Interests
            item(key = "skills") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("user_profile_skills_section"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Rounded.Verified, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text(
                                    text = "Skills & Proficiency",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (isOwnProfile) {
                                TextButton(
                                    onClick = { showInlineSkillsEditor = !showInlineSkillsEditor },
                                    modifier = Modifier
                                        .heightIn(min = 48.dp)
                                        .testTag("user_profile_inline_edit_skills_interests_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Edit,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(if (showInlineSkillsEditor) "Close Editor" else "Edit Skills & Interests")
                                }
                            }
                        }

                        AnimatedVisibility(visible = isOwnProfile && showInlineSkillsEditor) {
                            ProfileSkillsAndInterestsEditor(
                                initialSkills = mergedSkills,
                                initialInterests = mergedInterests,
                                isCloudSynced = isCloudSynced,
                                isSaving = isProfileSaving,
                                onSaveSkillsAndInterests = { updatedSkills, updatedInterests ->
                                    onSaveSkillsAndInterests(updatedSkills, updatedInterests) {
                                        showInlineSkillsEditor = false
                                    }
                                },
                                onCancel = { showInlineSkillsEditor = false }
                            )
                        }

                        if (mergedSkills.isEmpty()) {
                            Text(
                                text = if (isOwnProfile) {
                                    "No skills added yet. Tap Edit Skills & Interests above to add capabilities and store them in Firestore."
                                } else {
                                    "This member is still adding capability tags to their profile."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            ProfileSkillBadgesSection(
                                skills = mergedSkills,
                                skillProficiencies = userProfile.skillProficiencies.ifEmpty { communityProfile.skillProficiencies },
                                endorsementsBySkill = endorsementsBySkill,
                                title = "Capability Badges",
                                subtitle = if (totalEndorsements > 0) "$totalEndorsements peer endorsements" else null,
                                onToggleEndorseSkill = if (!isOwnProfile) {
                                    { skill -> onToggleSkillEndorsement(communityProfile, skill) }
                                } else null
                            )
                        }

                        if (mergedInterests.isNotEmpty()) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Interests,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Interests & Focus Areas",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                mergedInterests.forEach { interest ->
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                                    ) {
                                        Text(
                                            text = interest,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }
                        }

                        if (mergedSupport.isNotEmpty()) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Text(
                                text = "Looking to Collaborate / Learn",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                mergedSupport.forEach { support ->
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.75f)
                                    ) {
                                        Text(
                                            text = support,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2.5 Active Collaborations Progress Tracker (Completed vs Total Tasks)
            item(key = "collaboration_progress_tracker") {
                ActiveCollaborationsProgressTrackerCard(
                    summary = activeCollaborationsProgress,
                    isOwnProfile = isOwnProfile,
                    onToggleTask = onToggleMissionTask,
                    onOpenMission = onOpenMission,
                    onExploreMissions = onExploreMissions
                )
            }

            // 3. Mission History Section (Firestore-backed)
            item(key = "missions_header") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("user_profile_mission_history_section"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Rounded.RocketLaunch, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text(
                                    text = "Mission History",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "${missionHistory.size} total",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                                )
                            }
                        }

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            MissionHistoryFilter.entries.forEach { filter ->
                                val count = when (filter) {
                                    MissionHistoryFilter.ALL -> missionHistory.size
                                    MissionHistoryFilter.ACTIVE -> activeMissionsCount
                                    MissionHistoryFilter.COMPLETED -> completedMissionsCount
                                }
                                FilterChip(
                                    selected = missionFilter == filter,
                                    onClick = { missionFilter = filter },
                                    label = { Text("${filter.label} ($count)") },
                                    modifier = Modifier.testTag("mission_history_filter_${filter.name.lowercase()}")
                                )
                            }
                        }

                        if (filteredMissions.isEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = if (missionHistory.isEmpty()) {
                                            if (isOwnProfile) {
                                                "You haven't joined or created any collaborative missions yet."
                                            } else {
                                                "No public mission history recorded for this member yet."
                                            }
                                        } else {
                                            "No missions match the ${missionFilter.label.lowercase()} filter."
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (isOwnProfile && missionHistory.isEmpty()) {
                                        OutlinedButton(
                                            onClick = onExploreMissions,
                                            modifier = Modifier.height(48.dp)
                                        ) {
                                            Icon(Icons.Rounded.Groups, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text("Explore & Create Missions")
                                        }
                                    }
                                }
                            }
                        } else {
                            filteredMissions.forEach { mission ->
                                val isLead = mission.ownerId == userProfile.userId
                                val statusEnum = MissionStatus.safeParse(mission.status)
                                val statusColor = when (statusEnum) {
                                    MissionStatus.COMPLETED -> if (isDark) DarkMintPrimary else CoverageGreen
                                    MissionStatus.ACTIVE -> if (isDark) DarkMintPrimary else BotanicalGreen
                                    MissionStatus.OPEN -> WarmGoldenYellow
                                }
                                Surface(
                                    onClick = { onOpenMission(mission) },
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("user_profile_mission_${mission.id}")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(50),
                                                color = statusColor.copy(alpha = 0.16f)
                                            ) {
                                                Text(
                                                    text = statusEnum.label,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = if (statusEnum == MissionStatus.OPEN && !isDark) DeepForestGreen else statusColor,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(50),
                                                color = MaterialTheme.colorScheme.secondaryContainer
                                            ) {
                                                Text(
                                                    text = if (isLead) "Mission Lead" else "Collaborator",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                        }

                                        Text(
                                            text = mission.title,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        val missionSummary = mission.objective.ifBlank { mission.description }
                                        if (missionSummary.isNotBlank()) {
                                            Text(
                                                text = missionSummary,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "${mission.memberUids.size.coerceAtLeast(1)} member(s)",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = "${mission.progressPercent.coerceIn(0, 100)}%",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            LinearProgressIndicator(
                                                progress = { (mission.progressPercent.coerceIn(0, 100) / 100f) },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(6.dp)
                                                    .clip(RoundedCornerShape(50))
                                            )
                                        }

                                        if (mission.requiredCapabilities.isNotEmpty()) {
                                            FlowRow(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                mission.requiredCapabilities.take(4).forEach { cap ->
                                                    Surface(
                                                        shape = RoundedCornerShape(50),
                                                        color = MaterialTheme.colorScheme.surface
                                                    ) {
                                                        Text(
                                                            text = cap,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. Bookmarked Posts Section (Firestore users/{uid}/post_bookmarks)
            item(key = "bookmarks") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("user_profile_bookmarks_section"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (isOwnProfile) Icons.Rounded.Bookmark else Icons.Rounded.Forum,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = if (isOwnProfile) "Bookmarked Posts" else "Community Contributions",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (isOwnProfile && bookmarkedEntries.isNotEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = "${bookmarkedEntries.size}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        if (isOwnProfile) {
                            if (bookmarkedEntries.isEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "No bookmarked posts yet. Save ideas, questions, and collaboration opportunities in the Community feed to access them quickly from your profile.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        OutlinedButton(
                                            onClick = onBrowseCommunity,
                                            modifier = Modifier.height(48.dp)
                                        ) {
                                            Icon(Icons.Rounded.Explore, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text("Explore Community Feed")
                                        }
                                    }
                                }
                            } else {
                                bookmarkedEntries.forEach { entry ->
                                    val post = entry.post
                                    val replyCount = commentsByPostId[entry.postId]?.size ?: 0
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.52f),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("user_profile_bookmark_${entry.postId}")
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Surface(
                                                    shape = RoundedCornerShape(50),
                                                    color = MaterialTheme.colorScheme.primaryContainer
                                                ) {
                                                    Text(
                                                        text = post?.categoryEnum()?.shortLabel ?: "Saved Thread",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                    )
                                                }
                                                if (post != null) {
                                                    Text(
                                                        text = "${post.authorName} • $replyCount ${if (replyCount == 1) "reply" else "replies"}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }

                                            Text(
                                                text = entry.displayTitle,
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )

                                            if (post != null && post.body.isNotBlank()) {
                                                Text(
                                                    text = post.body,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                OutlinedButton(
                                                    onClick = {
                                                        if (post != null) onOpenBookmarkedPost(post)
                                                        else onBrowseCommunity()
                                                    },
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(48.dp)
                                                        .testTag("user_profile_open_bookmark_${entry.postId}")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(Modifier.width(6.dp))
                                                    Text(if (post != null) "Open Thread" else "View in Community")
                                                }
                                                IconButton(
                                                    onClick = { onRemoveBookmark(entry) },
                                                    modifier = Modifier
                                                        .size(48.dp)
                                                        .testTag("user_profile_remove_bookmark_${entry.postId}")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.BookmarkRemove,
                                                        contentDescription = "Remove bookmark for ${entry.displayTitle}",
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // Viewing another member's profile respects owner-only Firestore bookmark privacy
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Lock,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Bookmarked posts are private to each member in Firestore.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (authoredPosts.isNotEmpty()) {
                                authoredPosts.forEach { post ->
                                    Surface(
                                        onClick = { onOpenBookmarkedPost(post) },
                                        shape = RoundedCornerShape(16.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = post.categoryEnum().shortLabel,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = post.title,
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = post.body,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveCollaborationsProgressTrackerCard(
    summary: ActiveCollaborationsProgressSummary,
    isOwnProfile: Boolean,
    onToggleTask: (Mission, MissionMilestone) -> Unit,
    onOpenMission: (Mission) -> Unit,
    onExploreMissions: () -> Unit
) {
    val isDark = LocalSymbiosisIsDark.current
    val ringColor = if (isDark) DarkMintPrimary else BotanicalGreen
    val trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
    val accentDotColor = WarmGoldenYellow

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("user_profile_progress_tracker_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.TaskAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Active Collaborations Progress",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "${summary.activeCollaborations.size} active",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                    )
                }
            }

            if (summary.activeCollaborations.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (isOwnProfile) {
                                "No active collaborations yet. Join or launch a mission to track milestone tasks here."
                            } else {
                                "This member has no active collaborations at the moment."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (isOwnProfile) {
                            OutlinedButton(
                                onClick = onExploreMissions,
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("progress_tracker_explore_missions")
                            ) {
                                Icon(Icons.Rounded.Groups, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Browse Active Missions")
                            }
                        }
                    }
                }
            } else {
                // Overall Progress Ring + Tasks Completed vs Total Tasks Summary
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("progress_tracker_summary_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(82.dp)
                                .semantics {
                                    contentDescription = if (summary.totalTasks > 0) {
                                        "${summary.totalCompletedTasks} of ${summary.totalTasks} tasks completed, ${summary.overallPercent} percent"
                                    } else {
                                        "Overall active collaboration progress ${summary.overallPercent} percent"
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val strokeWidthPx = 9.dp.toPx()
                                val diameter = size.minDimension - strokeWidthPx
                                val topLeft = Offset(
                                    x = (size.width - diameter) / 2f,
                                    y = (size.height - diameter) / 2f
                                )
                                val arcSize = Size(diameter, diameter)
                                drawArc(
                                    color = trackColor,
                                    startAngle = -90f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                                )
                                val sweep = (summary.overallRatio * 360f).coerceIn(0f, 360f)
                                if (sweep > 0f) {
                                    drawArc(
                                        color = ringColor,
                                        startAngle = -90f,
                                        sweepAngle = sweep,
                                        useCenter = false,
                                        topLeft = topLeft,
                                        size = arcSize,
                                        style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                                    )
                                }
                                drawCircle(
                                    color = accentDotColor,
                                    radius = 3.5.dp.toPx(),
                                    center = Offset(size.width / 2f, topLeft.y)
                                )
                            }
                            Text(
                                text = "${summary.overallPercent}%",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (summary.totalTasks > 0) {
                                    "${summary.totalCompletedTasks} of ${summary.totalTasks} tasks completed"
                                } else {
                                    "${summary.overallPercent}% overall collaboration progress"
                                },
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Across ${summary.activeCollaborations.size} active ${if (summary.activeCollaborations.size == 1) "collaboration" else "collaborations"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            LinearProgressIndicator(
                                progress = { summary.overallRatio },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(7.dp)
                                    .clip(RoundedCornerShape(50))
                            )
                        }
                    }
                }

                // Per-mission Task Breakdown
                summary.activeCollaborations.forEach { item ->
                    val mission = item.mission
                    val statusEnum = MissionStatus.safeParse(mission.status)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("progress_tracker_mission_${mission.id}")
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = mission.title,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${statusEnum.label} • ${mission.memberUids.size.coerceAtLeast(1)} collaborator(s)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = ringColor.copy(alpha = 0.16f)
                                ) {
                                    Text(
                                        text = if (item.hasExplicitTasks) {
                                            "${item.completedTasks} / ${item.totalTasks} tasks"
                                        } else {
                                            "${item.progressPercent}%"
                                        },
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            // Visual segmented task indicator (shows exact completed vs remaining task blocks)
                            if (item.hasExplicitTasks && item.totalTasks <= 10) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    item.tasks.forEach { ms ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(8.dp)
                                                .clip(RoundedCornerShape(50))
                                                .background(if (ms.completed) ringColor else trackColor)
                                        )
                                    }
                                }
                            } else {
                                LinearProgressIndicator(
                                    progress = { item.completionRatio },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(50))
                                )
                            }

                            if (item.hasExplicitTasks) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    item.tasks.forEach { ms ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("progress_tracker_task_${ms.id}"),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            if (isOwnProfile) {
                                                Checkbox(
                                                    checked = ms.completed,
                                                    onCheckedChange = { onToggleTask(mission, ms) }
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = if (ms.completed) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                                                    contentDescription = if (ms.completed) "Task completed" else "Task pending",
                                                    tint = if (ms.completed) ringColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = ms.title,
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontWeight = if (ms.completed) FontWeight.Normal else FontWeight.Medium,
                                                        textDecoration = if (ms.completed) TextDecoration.LineThrough else TextDecoration.None
                                                    ),
                                                    color = if (ms.completed) {
                                                        MaterialTheme.colorScheme.onSurfaceVariant
                                                    } else {
                                                        MaterialTheme.colorScheme.onSurface
                                                    }
                                                )
                                                if (ms.assignedRole.isNotBlank()) {
                                                    Text(
                                                        text = ms.assignedRole,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.secondary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                Text(
                                    text = "No granular milestone tasks recorded yet (${item.progressPercent}% overall progress).",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = { onOpenMission(mission) },
                                    modifier = Modifier.height(48.dp)
                                ) {
                                    Text("Open Mission Workspace")
                                    Spacer(Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileStatPill(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
