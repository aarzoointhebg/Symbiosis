package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AccessibilityNew
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.ExtractedCapabilityTags
import com.example.model.AccessibilitySettings
import com.example.model.ActivityFeedItem
import com.example.model.AmbitionHorizon
import com.example.model.AmbitionPriority
import com.example.model.AppThemeMode
import com.example.model.AvailabilityWindow
import com.example.model.CollaborationInvitation
import com.example.model.CollaborationStyle
import com.example.model.CommunityComment
import com.example.model.CommunityPost
import com.example.model.CommunityProfile
import com.example.model.InvitationStatus
import com.example.model.PrivateSettings
import com.example.model.ProfileVisibility
import com.example.model.SafetyBlock
import com.example.model.SavedPossibility
import com.example.model.SkillEndorsementSummary
import com.example.model.UiState
import com.example.model.UserAmbition
import com.example.model.UserProfile
import com.example.model.toUserProfile
import com.example.ui.components.ActivityFeedSection
import com.example.ui.components.AiBadge
import com.example.ui.components.CapabilityTagGroup
import com.example.ui.components.InfoBannerCard
import com.example.ui.components.MascotBadgeAvatar
import com.example.ui.components.ProfileSkillBadgesSection
import com.example.ui.components.SafetyAndDignityNotice
import com.example.ui.components.SectionHeading
import com.example.ui.components.UserProfileForm
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.BookmarkRemove
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.ui.text.style.TextOverflow
import java.util.UUID
import kotlin.math.roundToInt

@Composable
fun MySpaceAndCapabilitiesScreen(
    initialTab: Int = 3,
    ownProfile: CommunityProfile,
    userProfile: UserProfile,
    isProfileSaving: Boolean,
    accountEmail: String? = null,
    ambitions: List<UserAmbition>,
    savedPossibilities: List<SavedPossibility>,
    invitations: List<CollaborationInvitation>,
    privateSettings: PrivateSettings,
    safetyBlocks: List<SafetyBlock>,
    accessibilitySettings: AccessibilitySettings,
    aiTagState: UiState<ExtractedCapabilityTags>?,
    ownEndorsementsBySkill: Map<String, SkillEndorsementSummary> = emptyMap(),
    activityFeed: List<ActivityFeedItem> = emptyList(),
    bookmarkedPosts: List<CommunityPost> = emptyList(),
    commentsByPostId: Map<String, List<CommunityComment>> = emptyMap(),
    isCloudSynced: Boolean,
    onSaveUserProfile: (UserProfile) -> Unit,
    onSaveProfile: (CommunityProfile) -> Unit,
    onExtractAiTags: (String) -> Unit,
    onClearAiTags: () -> Unit,
    onSaveAmbition: (UserAmbition) -> Unit,
    onDeleteAmbition: (String) -> Unit,
    onSavePrivateSettings: (PrivateSettings) -> Unit,
    onUnblockUser: (String) -> Unit,
    onSetTextScale: (Float) -> Unit,
    onSetHighContrast: (Boolean) -> Unit,
    onSetThemeMode: (AppThemeMode) -> Unit = {},
    onSetReducedMotion: (Boolean) -> Unit,
    onSetPreferredComms: (String) -> Unit,
    onOpenChatWithPeerId: (String) -> Unit = {},
    onOpenBookmarkedPost: (CommunityPost) -> Unit = {},
    onTogglePostBookmark: (CommunityPost) -> Unit = {},
    onOpenUserProfile: () -> Unit = {},
    onSignOut: () -> Unit,
    onSwitchToGoogleSignIn: () -> Unit,
    onStartOnboarding: () -> Unit
) {
    var activeTab by remember(initialTab) { mutableIntStateOf(initialTab) } // 0 = Matching Form, 1 = Capabilities, 2 = Ambitions, 3 = My Space

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 760.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    modifier = Modifier.testTag("myspace_tab_matching_form"),
                    text = { Text("Matching") }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    modifier = Modifier.testTag("myspace_tab_capabilities"),
                    text = { Text("Details") }
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    modifier = Modifier.testTag("myspace_tab_ambitions"),
                    text = { Text("Ambitions") }
                )
                Tab(
                    selected = activeTab == 3,
                    onClick = { activeTab = 3 },
                    modifier = Modifier.testTag("myspace_tab_space"),
                    text = { Text("My Space") }
                )
            }

            when (activeTab) {
                0 -> UserProfileForm(
                    initialProfile = userProfile,
                    isSaving = isProfileSaving,
                    onSaveProfile = onSaveUserProfile
                )
                1 -> CapabilitiesEditorSection(
                    profile = ownProfile,
                    aiTagState = aiTagState,
                    onExtractAiTags = onExtractAiTags,
                    onClearAiTags = onClearAiTags,
                    onSaveProfile = onSaveProfile
                )
                2 -> AmbitionsManagerSection(
                    userId = ownProfile.userId,
                    ambitions = ambitions,
                    onSaveAmbition = onSaveAmbition,
                    onDeleteAmbition = onDeleteAmbition
                )
                else -> MySpaceOverviewSection(
                    ownProfile = ownProfile,
                    accountEmail = accountEmail,
                    savedPossibilities = savedPossibilities,
                    bookmarkedPosts = bookmarkedPosts,
                    commentsByPostId = commentsByPostId,
                    invitations = invitations,
                    privateSettings = privateSettings,
                    safetyBlocks = safetyBlocks,
                    accessibilitySettings = accessibilitySettings,
                    ownEndorsementsBySkill = ownEndorsementsBySkill,
                    activityFeed = activityFeed,
                    isCloudSynced = isCloudSynced,
                    onUpdateVisibility = { vis -> onSaveProfile(ownProfile.copy(visibility = vis.name)) },
                    onSavePrivateSettings = onSavePrivateSettings,
                    onUnblockUser = onUnblockUser,
                    onSetTextScale = onSetTextScale,
                    onSetHighContrast = onSetHighContrast,
                    onSetThemeMode = onSetThemeMode,
                    onSetReducedMotion = onSetReducedMotion,
                    onSetPreferredComms = onSetPreferredComms,
                    onOpenChatWithPeerId = onOpenChatWithPeerId,
                    onOpenBookmarkedPost = onOpenBookmarkedPost,
                    onTogglePostBookmark = onTogglePostBookmark,
                    onOpenUserProfile = onOpenUserProfile,
                    onSignOut = onSignOut,
                    onSwitchToGoogleSignIn = onSwitchToGoogleSignIn,
                    onOpenMatchingForm = { activeTab = 0 },
                    onStartOnboarding = onStartOnboarding
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CapabilitiesEditorSection(
    profile: CommunityProfile,
    aiTagState: UiState<ExtractedCapabilityTags>?,
    onExtractAiTags: (String) -> Unit,
    onClearAiTags: () -> Unit,
    onSaveProfile: (CommunityProfile) -> Unit
) {
    var displayName by remember(profile.userId, profile.displayName) { mutableStateOf(profile.displayName) }
    var headline by remember(profile.userId, profile.headline) { mutableStateOf(profile.headline) }
    var bio by remember(profile.userId, profile.bio) { mutableStateOf(profile.bio) }
    var constraintsNote by remember(profile.userId, profile.constraintsNote) { mutableStateOf(profile.constraintsNote) }
    var collabStyle by remember(profile.userId, profile.collaborationStyle) {
        mutableStateOf(CollaborationStyle.safeParse(profile.collaborationStyle))
    }
    var availability by remember(profile.userId, profile.availability) {
        mutableStateOf(AvailabilityWindow.safeParse(profile.availability))
    }

    val skills = remember(profile.userId, profile.skills) {
        mutableStateListOf<String>().apply { addAll(profile.skills) }
    }
    val knowledgeAreas = remember(profile.userId, profile.knowledgeAreas) {
        mutableStateListOf<String>().apply { addAll(profile.knowledgeAreas) }
    }
    val practicalAbilities = remember(profile.userId, profile.practicalAbilities) {
        mutableStateListOf<String>().apply { addAll(profile.practicalAbilities) }
    }
    val interests = remember(profile.userId, profile.interests) {
        mutableStateListOf<String>().apply { addAll(profile.interests) }
    }
    val resources = remember(profile.userId, profile.resources) {
        mutableStateListOf<String>().apply { addAll(profile.resources) }
    }
    val supportWanted = remember(profile.userId, profile.supportWanted) {
        mutableStateListOf<String>().apply { addAll(profile.supportWanted) }
    }

    SectionHeading(
        title = "My Capabilities & Preferences",
        subtitle = "Share what you bring, what you'd enjoy learning, and how you collaborate best"
    )

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MascotBadgeAvatar(isDuck = true)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Natural-Language Capability Helper",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "You review and approve every AI-suggested tag before saving.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                AiBadge()
            }

            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("Display Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("cap_display_name_input"),
                shape = RoundedCornerShape(14.dp)
            )

            OutlinedTextField(
                value = headline,
                onValueChange = { headline = it },
                label = { Text("Short Headline") },
                placeholder = { Text("e.g., Curious coder & accessibility advocate") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Describe your abilities, tools, or passions naturally") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth().testTag("cap_bio_input"),
                shape = RoundedCornerShape(14.dp)
            )

            OutlinedButton(
                onClick = { onExtractAiTags(bio) },
                enabled = bio.isNotBlank() && aiTagState !is UiState.Loading,
                modifier = Modifier.height(48.dp).testTag("cap_extract_ai_button")
            ) {
                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (aiTagState is UiState.Loading) "Extracting with Gemini..." else "Suggest Tags with Gemini AI")
            }

            AnimatedVisibility(visible = aiTagState is UiState.Success) {
                val extracted = (aiTagState as? UiState.Success)?.data
                if (extracted != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Review Suggested Capabilities (tap to add/remove):",
                                style = MaterialTheme.typography.titleSmall
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                extracted.skills.forEach { tag ->
                                    val added = skills.any { it.equals(tag, ignoreCase = true) }
                                    FilterChip(
                                        selected = added,
                                        onClick = {
                                            if (added) skills.removeAll { it.equals(tag, ignoreCase = true) }
                                            else skills.add(tag)
                                        },
                                        label = { Text(if (added) "Skill: $tag" else "+ Skill: $tag") }
                                    )
                                }
                                extracted.knowledgeAreas.forEach { tag ->
                                    val added = knowledgeAreas.any { it.equals(tag, ignoreCase = true) }
                                    FilterChip(
                                        selected = added,
                                        onClick = {
                                            if (added) knowledgeAreas.removeAll { it.equals(tag, ignoreCase = true) }
                                            else knowledgeAreas.add(tag)
                                        },
                                        label = { Text(if (added) "Knowledge: $tag" else "+ Knowledge: $tag") }
                                    )
                                }
                                extracted.practicalAbilities.forEach { tag ->
                                    val added = practicalAbilities.any { it.equals(tag, ignoreCase = true) }
                                    FilterChip(
                                        selected = added,
                                        onClick = {
                                            if (added) practicalAbilities.removeAll { it.equals(tag, ignoreCase = true) }
                                            else practicalAbilities.add(tag)
                                        },
                                        label = { Text(if (added) "Practical: $tag" else "+ Practical: $tag") }
                                    )
                                }
                                extracted.supportWanted.forEach { tag ->
                                    val added = supportWanted.any { it.equals(tag, ignoreCase = true) }
                                    FilterChip(
                                        selected = added,
                                        onClick = {
                                            if (added) supportWanted.removeAll { it.equals(tag, ignoreCase = true) }
                                            else supportWanted.add(tag)
                                        },
                                        label = { Text(if (added) "Support: $tag" else "+ Support: $tag") }
                                    )
                                }
                            }
                            TextButton(onClick = onClearAiTags) { Text("Finish Review") }
                        }
                    }
                }
            }

            if (aiTagState is UiState.Error) {
                Text(
                    text = aiTagState.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    CapabilityCategoryEditorCard("Skills", "e.g., Programming, Visual design, Audio editing", skills)
    CapabilityCategoryEditorCard("Knowledge Areas", "e.g., Environmental science, Accessibility, History", knowledgeAreas)
    CapabilityCategoryEditorCard("Practical Abilities", "e.g., Facilitation, Prototyping, Note synthesis", practicalAbilities)
    CapabilityCategoryEditorCard("Interests & Curiosities", "e.g., Clean energy, Tactile art, Community gardens", interests)
    CapabilityCategoryEditorCard("Tools & Resources", "e.g., Laptop, Soldering kit, Quiet meeting space", resources)
    CapabilityCategoryEditorCard("Support Wanted / Learning Goals", "Areas where complementary teammates help", supportWanted)

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Collaboration Preferences & Constraints", style = MaterialTheme.typography.titleMedium)

            Text("Collaboration Style", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CollaborationStyle.entries.forEach { style ->
                    FilterChip(
                        selected = collabStyle == style,
                        onClick = { collabStyle = style },
                        label = { Text(style.label) }
                    )
                }
            }

            Text("Availability Window", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AvailabilityWindow.entries.forEach { av ->
                    FilterChip(
                        selected = availability == av,
                        onClick = { availability = av },
                        label = { Text(av.label) }
                    )
                }
            }

            OutlinedTextField(
                value = constraintsNote,
                onValueChange = { constraintsNote = it },
                label = { Text("Energy, sensory, or schedule constraints (optional)") },
                placeholder = { Text("e.g., Prefer async check-ins on weekday evenings") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            Button(
                onClick = {
                    onSaveProfile(
                        profile.copy(
                            displayName = displayName.trim().ifBlank { "Explorer" },
                            headline = headline.trim(),
                            bio = bio.trim(),
                            skills = skills.toList(),
                            knowledgeAreas = knowledgeAreas.toList(),
                            practicalAbilities = practicalAbilities.toList(),
                            interests = interests.toList(),
                            resources = resources.toList(),
                            supportWanted = supportWanted.toList(),
                            collaborationStyle = collabStyle.name,
                            availability = availability.name,
                            constraintsNote = constraintsNote.trim(),
                            onboardingCompleted = true
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_capabilities_button"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Rounded.Check, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Save Capabilities Profile")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CapabilityCategoryEditorCard(
    title: String,
    hint: String,
    items: MutableList<String>
) {
    var draft by remember { mutableStateOf("") }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            if (items.isEmpty()) {
                Text(
                    text = "None added yet — add what fits you or explore freely.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items.forEach { tag ->
                        InputChip(
                            selected = true,
                            onClick = { items.remove(tag) },
                            label = { Text(tag) },
                            trailingIcon = {
                                Icon(
                                    Icons.Rounded.Close,
                                    contentDescription = "Remove $tag",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    label = { Text("Add to $title") },
                    placeholder = { Text(hint) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                )
                Button(
                    onClick = {
                        val clean = draft.trim()
                        if (clean.isNotEmpty() && items.none { it.equals(clean, ignoreCase = true) }) {
                            items.add(clean)
                            draft = ""
                        }
                    },
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("Add")
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AmbitionsManagerSection(
    userId: String,
    ambitions: List<UserAmbition>,
    onSaveAmbition: (UserAmbition) -> Unit,
    onDeleteAmbition: (String) -> Unit
) {
    var showEditor by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<UserAmbition?>(null) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SectionHeading(
            title = "My Ambitions",
            subtitle = "Short-term goals & long-term visions ready for complementary partners",
            modifier = Modifier.weight(1f)
        )
        Button(
            onClick = {
                editing = null
                showEditor = true
            },
            modifier = Modifier.height(48.dp).testTag("add_ambition_button")
        ) {
            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Add")
        }
    }

    if (ambitions.isEmpty()) {
        InfoBannerCard(
            title = "No Ambitions Added Yet",
            body = "Add a goal — whether building a community garden, teaching a topic, or launching a neighborhood initiative — to discover people sharing compatible intents."
        )
    } else {
        ambitions.forEach { amb ->
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(amb.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                            Text(
                                text = "${AmbitionHorizon.safeParse(amb.horizon).label} • ${AmbitionPriority.safeParse(amb.priority).label}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Row {
                            TextButton(
                                onClick = {
                                    editing = amb
                                    showEditor = true
                                }
                            ) { Text("Edit") }
                            IconButton(
                                onClick = { onDeleteAmbition(amb.id) }
                            ) {
                                Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete ambition")
                            }
                        }
                    }
                    if (amb.description.isNotBlank()) {
                        Text(amb.description, style = MaterialTheme.typography.bodyMedium)
                    }
                    CapabilityTagGroup(tags = amb.tags)
                    Text(
                        text = if (amb.sharedToCommunity) "Shared on Ambition Map & Intent Discovery" else "Private Ambition Draft",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (showEditor) {
        AmbitionEditorDialog(
            userId = userId,
            existing = editing,
            onDismiss = { showEditor = false },
            onSave = { saved ->
                onSaveAmbition(saved)
                showEditor = false
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AmbitionEditorDialog(
    userId: String,
    existing: UserAmbition?,
    onDismiss: () -> Unit,
    onSave: (UserAmbition) -> Unit
) {
    var title by remember(existing) { mutableStateOf(existing?.title ?: "") }
    var description by remember(existing) { mutableStateOf(existing?.description ?: "") }
    var horizon by remember(existing) { mutableStateOf(AmbitionHorizon.safeParse(existing?.horizon)) }
    var priority by remember(existing) { mutableStateOf(AmbitionPriority.safeParse(existing?.priority)) }
    var tagsText by remember(existing) { mutableStateOf(existing?.tags?.joinToString(", ") ?: "Collaboration, Accessibility") }
    var shared by remember(existing) { mutableStateOf(existing?.sharedToCommunity ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "New Ambition" else "Edit Ambition") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Ambition Title (min 3 chars)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("What do you hope to explore or build?") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Horizon", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AmbitionHorizon.entries.forEach { h ->
                        FilterChip(
                            selected = horizon == h,
                            onClick = { horizon = h },
                            label = { Text(h.label) }
                        )
                    }
                }
                Text("Priority", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AmbitionPriority.entries.forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(p.label) }
                        )
                    }
                }
                OutlinedTextField(
                    value = tagsText,
                    onValueChange = { tagsText = it },
                    label = { Text("Tags (comma-separated)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Share with community discovery", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    Switch(checked = shared, onCheckedChange = { shared = it })
                }
            }
        },
        confirmButton = {
            Button(
                enabled = title.trim().length >= 3,
                onClick = {
                    val tags = tagsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    onSave(
                        UserAmbition(
                            id = existing?.id?.takeIf { it.isNotBlank() } ?: "amb_${UUID.randomUUID().toString().take(8)}",
                            userId = userId,
                            title = title.trim(),
                            description = description.trim(),
                            horizon = horizon.name,
                            priority = priority.name,
                            tags = tags,
                            sharedToCommunity = shared
                        )
                    )
                }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MySpaceOverviewSection(
    ownProfile: CommunityProfile,
    accountEmail: String?,
    savedPossibilities: List<SavedPossibility>,
    bookmarkedPosts: List<CommunityPost> = emptyList(),
    commentsByPostId: Map<String, List<CommunityComment>> = emptyMap(),
    invitations: List<CollaborationInvitation>,
    privateSettings: PrivateSettings,
    safetyBlocks: List<SafetyBlock>,
    accessibilitySettings: AccessibilitySettings,
    ownEndorsementsBySkill: Map<String, SkillEndorsementSummary> = emptyMap(),
    activityFeed: List<ActivityFeedItem> = emptyList(),
    isCloudSynced: Boolean,
    onUpdateVisibility: (ProfileVisibility) -> Unit,
    onSavePrivateSettings: (PrivateSettings) -> Unit,
    onUnblockUser: (String) -> Unit,
    onSetTextScale: (Float) -> Unit,
    onSetHighContrast: (Boolean) -> Unit,
    onSetThemeMode: (AppThemeMode) -> Unit,
    onSetReducedMotion: (Boolean) -> Unit,
    onSetPreferredComms: (String) -> Unit,
    onOpenChatWithPeerId: (String) -> Unit,
    onOpenBookmarkedPost: (CommunityPost) -> Unit,
    onTogglePostBookmark: (CommunityPost) -> Unit,
    onOpenUserProfile: () -> Unit,
    onSignOut: () -> Unit,
    onSwitchToGoogleSignIn: () -> Unit,
    onOpenMatchingForm: () -> Unit,
    onStartOnboarding: () -> Unit
) {
    val activeCollabs = invitations.count { it.status == InvitationStatus.ACCEPTED.name }
    val pendingCollabs = invitations.count { it.status == InvitationStatus.PENDING.name }

    // Identity & Session Card
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MascotBadgeAvatar(isDuck = false)
                Column(modifier = Modifier.weight(1f)) {
                    Text(ownProfile.displayName, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                    Text(
                        text = if (isCloudSynced) {
                            buildString {
                                append("Authenticated Google Account")
                                if (!accountEmail.isNullOrBlank()) append(" • $accountEmail")
                            }
                        } else {
                            "Local Guest Workspace"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    if (isCloudSynced && ownProfile.userId.isNotBlank()) {
                        Text(
                            text = "Account ID: ${ownProfile.userId.take(12)}… • Firestore Synced",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("$activeCollabs", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                        Text("Active Collabs", style = MaterialTheme.typography.labelSmall)
                    }
                }
                Surface(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("$pendingCollabs", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onTertiaryContainer)
                        Text("Pending Invites", style = MaterialTheme.typography.labelSmall)
                    }
                }
                Surface(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("${bookmarkedPosts.size}", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                        Text("Saved Threads", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onStartOnboarding,
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Icon(Icons.Rounded.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Guided Setup")
                }
                OutlinedButton(
                    onClick = onOpenMatchingForm,
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Icon(Icons.Rounded.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Matching Form")
                }
            }

            Button(
                onClick = onOpenUserProfile,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("open_user_profile_screen_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Rounded.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Open User Profile (Skills, Missions & Bookmarks)")
            }

            if (!isCloudSynced) {
                OutlinedButton(
                    onClick = onSwitchToGoogleSignIn,
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("myspace_google_sync_button")
                ) {
                    Icon(Icons.Rounded.VerifiedUser, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Connect Google Account for Cloud Sync")
                }
            }
        }
    }

    // Visual Skill Badges & Peer Trust Summary
    val ownSkills = remember(ownProfile) { ownProfile.allCapabilities().take(10) }
    val totalEndorsements = ownEndorsementsBySkill.values.sumOf { it.count }
    val endorsers = ownEndorsementsBySkill.values.flatMap { it.endorserNames }.distinct()
    if (ownSkills.isNotEmpty()) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Verified,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Skills & Peer Trust Badges",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                ProfileSkillBadgesSection(
                    skills = ownSkills,
                    endorsementsBySkill = ownEndorsementsBySkill,
                    skillProficiencies = ownProfile.skillProficiencies,
                    title = "Your Visual Skill Badges",
                    subtitle = if (totalEndorsements > 0) {
                        "$totalEndorsements peer endorsements"
                    } else {
                        "Ready for peer endorsement"
                    }
                )
                if (endorsers.isNotEmpty()) {
                    Text(
                        text = "Endorsed by: ${endorsers.take(4).joinToString(", ")}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                OutlinedButton(
                    onClick = onOpenMatchingForm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("myspace_assess_proficiency_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Rounded.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Self-Assess Skill Proficiency Levels")
                }
            }
        }
    }

    // Profile Visibility Controls
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Rounded.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Profile Visibility", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            }
            ProfileVisibility.entries.forEach { vis ->
                val selected = ownProfile.visibility == vis.name
                Surface(
                    onClick = { onUpdateVisibility(vis) },
                    shape = RoundedCornerShape(14.dp),
                    color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = if (selected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                    modifier = Modifier.fillMaxWidth().testTag("visibility_${vis.name.lowercase()}")
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(vis.label, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                        Text(vis.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }

    // Accessibility Controls (Part 13)
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Rounded.AccessibilityNew, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Accessibility & Sensory Comfort", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            }

            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Text Size Scale", style = MaterialTheme.typography.bodyMedium)
                    Text("${(accessibilitySettings.textScale * 100).roundToInt()}%", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                }
                Slider(
                    value = accessibilitySettings.textScale,
                    onValueChange = onSetTextScale,
                    valueRange = 0.85f..1.35f,
                    steps = 4,
                    modifier = Modifier.testTag("accessibility_text_scale_slider")
                )
            }

            Text("Display Theme", style = MaterialTheme.typography.labelLarge)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AppThemeMode.entries.forEach { mode ->
                    FilterChip(
                        selected = accessibilitySettings.themeMode == mode,
                        onClick = { onSetThemeMode(mode) },
                        label = { Text(mode.label) },
                        modifier = Modifier.testTag("theme_mode_${mode.name.lowercase()}")
                    )
                }
            }
            Text(
                text = accessibilitySettings.themeMode.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("High-Contrast Mode", style = MaterialTheme.typography.bodyMedium)
                    Text("Strengthens contrast for Dark, White, or Cream themes.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = accessibilitySettings.highContrastMode,
                    onCheckedChange = onSetHighContrast,
                    modifier = Modifier.testTag("accessibility_high_contrast_switch")
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Reduced Motion", style = MaterialTheme.typography.bodyMedium)
                    Text("Minimizes transitions for calmer visual scanning.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = accessibilitySettings.reducedMotion,
                    onCheckedChange = onSetReducedMotion,
                    modifier = Modifier.testTag("accessibility_reduced_motion_switch")
                )
            }

            Text("Preferred Communication Format", style = MaterialTheme.typography.labelLarge)
            val commOptions = listOf(
                "Text & Screen-Reader Structured",
                "Visual Diagrams + Captions",
                "Voice / Audio Summaries",
                "Asynchronous Written Check-Ins"
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                commOptions.forEach { opt ->
                    FilterChip(
                        selected = accessibilitySettings.preferredComms == opt,
                        onClick = { onSetPreferredComms(opt) },
                        label = { Text(opt) }
                    )
                }
            }
        }
    }

    // Activity & Collaboration Trust Feed
    ActivityFeedSection(
        items = activityFeed,
        title = "Your Activity & Endorsement Feed",
        subtitle = "Recent collaboration milestones, accepted chats & received endorsements",
        maxItems = 8,
        onOpenChatWithPeerId = onOpenChatWithPeerId
    )

    // Saved / Bookmarked Community Threads
    Card(
        modifier = Modifier.testTag("myspace_saved_threads_card"),
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Rounded.Forum, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Saved Community Threads", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                }
                if (bookmarkedPosts.isNotEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "${bookmarkedPosts.size}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            if (bookmarkedPosts.isEmpty()) {
                Text(
                    text = "Tap the Bookmark icon on any Community post to save favorite discussions, questions, and opportunities right here in your profile.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                bookmarkedPosts.forEach { post ->
                    val replyCount = commentsByPostId[post.id]?.size ?: 0
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
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
                                        text = post.categoryEnum().shortLabel,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = "${post.authorName} • $replyCount ${if (replyCount == 1) "reply" else "replies"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = { onOpenBookmarkedPost(post) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("open_bookmarked_post_${post.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text("Open Thread")
                                }
                                IconButton(
                                    onClick = { onTogglePostBookmark(post) },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("remove_bookmarked_post_${post.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.BookmarkRemove,
                                        contentDescription = "Remove saved thread",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Saved Possibilities
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Rounded.Bookmark, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Saved Possibilities", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            }
            if (savedPossibilities.isEmpty()) {
                Text(
                    text = "Save analyses from the Possibility Finder to revisit collaborator pathways anytime.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                savedPossibilities.forEach { item ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(item.objective, style = MaterialTheme.typography.titleSmall)
                            Text(item.summary, style = MaterialTheme.typography.bodySmall)
                            if (item.suggestedCollaborators.isNotEmpty()) {
                                Text(
                                    text = "Suggested partners: ${item.suggestedCollaborators.joinToString(", ")}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Safety, Consent & Blocked Users (Part 14)
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Rounded.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Safety, Consent & Privacy", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Allow direct consent-based invitations", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Switch(
                    checked = privateSettings.allowDirectInvitations,
                    onCheckedChange = { onSavePrivateSettings(privateSettings.copy(allowDirectInvitations = it)) }
                )
            }

            HorizontalDivider()

            Text("Blocked Users (${safetyBlocks.size})", style = MaterialTheme.typography.titleSmall)
            if (safetyBlocks.isEmpty()) {
                Text(
                    text = "No blocked accounts. You can block or report any interaction from Discover at any time.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                safetyBlocks.forEach { block ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("User ID: ${block.blockedUserId.take(16)}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
                            Text(block.reason, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(onClick = { onUnblockUser(block.blockedUserId) }) { Text("Unblock") }
                    }
                }
            }
        }
    }

    SafetyAndDignityNotice()

    // Extensible Architecture Note (Part 16: XTRA ORDINARY future extension)
    InfoBannerCard(
        icon = Icons.Rounded.Extension,
        title = "Extensible Architecture • Future XTRA ORDINARY Module",
        body = "SYMBIOSIS separates capability profiles, accessibility preferences, and matching logic into clean modular layers so specialized accessibility companions — such as a future XTRA ORDINARY assistive extension — can plug in cleanly without changing core collaboration workflows.",
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    OutlinedButton(
        onClick = onSignOut,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("sign_out_button"),
        shape = RoundedCornerShape(16.dp)
    ) {
        Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text("Sign Out")
    }
}
