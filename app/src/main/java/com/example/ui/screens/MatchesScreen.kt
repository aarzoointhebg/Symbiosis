package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.PersonSearch
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.AvailabilityWindow
import com.example.model.CollaborationMessage
import com.example.model.CommunityProfile
import com.example.model.IndividualMatchResult
import com.example.model.MatchingMode
import com.example.model.Mission
import com.example.model.SkillEndorsementSummary
import com.example.model.UiState
import com.example.model.UserProfile
import com.example.ui.MatchCardItem
import com.example.ui.MatchesFeed
import com.example.ui.OverlappingSkillFeed
import com.example.ui.OverlappingSkillMatchItem
import com.example.ui.components.AccessibilityBadge
import com.example.ui.components.CollaborationDiscussionSheet
import com.example.ui.components.InfoBannerCard
import com.example.ui.components.ProfileSkillBadgesSection
import com.example.ui.components.SectionHeading
import com.example.ui.theme.ComplementaryAmber
import com.example.ui.theme.CoverageGreen
import com.example.ui.theme.DeepForestGreen
import com.example.ui.theme.SoftCream

private val QUICK_SKILL_KEYWORDS = listOf(
    "Programming",
    "Visual design",
    "Research",
    "Basic electronics",
    "Accessibility testing",
    "Audio engineering",
    "Workshop coordination",
    "Data collection"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MatchesScreen(
    matchesFeedState: UiState<MatchesFeed>,
    overlappingSkillFeedState: UiState<OverlappingSkillFeed> = UiState.Success(OverlappingSkillFeed()),
    isRefreshingSkillOverlap: Boolean = false,
    currentUserProfile: UserProfile,
    selectedMode: MatchingMode,
    searchQuery: String,
    selectedInterestCategory: String?,
    firestoreOnly: Boolean,
    isCloudSynced: Boolean,
    allMissions: List<Mission>,
    endorsementSummaryByUser: Map<String, Map<String, SkillEndorsementSummary>> = emptyMap(),
    messagesByPeerId: Map<String, List<CollaborationMessage>> = emptyMap(),
    onSearchQueryChange: (String) -> Unit,
    onSelectInterestCategory: (String?) -> Unit,
    onSelectMode: (MatchingMode) -> Unit,
    onToggleFirestoreOnly: (Boolean) -> Unit,
    onClearFilters: () -> Unit,
    onEditMatchingProfile: () -> Unit,
    onOpenIntentDiscovery: () -> Unit,
    onAutoLoadOverlappingSkillMatches: () -> Unit = {},
    onRefreshOverlappingSkillMatches: () -> Unit = {},
    onToggleSkillEndorsement: (CommunityProfile, String) -> Unit = { _, _ -> },
    onSendCollaborationMessage: (recipient: CommunityProfile, body: String, contextLabel: String) -> Unit = { _, _, _ -> },
    onSendInvitation: (recipient: CommunityProfile, missionId: String, missionTitle: String, role: String, rationale: String) -> Unit,
    onBlockUser: (CommunityProfile, String) -> Unit
) {
    val dismissedUserIds = remember { mutableStateListOf<String>() }
    var blockTarget by remember { mutableStateOf<CommunityProfile?>(null) }
    var blockReason by remember { mutableStateOf("") }
    var activeDiscussionPeer by remember { mutableStateOf<CommunityProfile?>(null) }
    var activeDiscussionSkills by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(isCloudSynced, currentUserProfile.skills) {
        onAutoLoadOverlappingSkillMatches()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 780.dp)
                .align(Alignment.TopCenter),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item(key = "header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeading(
                        title = "Complementary Matches",
                        subtitle = if (isCloudSynced) {
                            "People retrieved from Firestore whose skills balance yours"
                        } else {
                            "Collaborators whose skills complement your profile"
                        },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = onOpenIntentDiscovery,
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("matches_open_intent_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Rounded.Explore, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Intent")
                    }
                }
            }

            item(key = "visual_bridge_banner") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(132.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .testTag("matches_visual_banner")
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_matches_bridge),
                        contentDescription = "Sunlit hillside bridge connecting gardens — symbolizing complementary collaboration",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        DeepForestGreen.copy(alpha = 0.82f),
                                        DeepForestGreen.copy(alpha = 0.38f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(horizontal = 18.dp, vertical = 12.dp)
                            .widthIn(max = 260.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Complementary Strengths",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = SoftCream
                        )
                        Text(
                            text = "Compare skill proficiency levels, endorse peers, or start a discussion directly.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftCream.copy(alpha = 0.92f)
                        )
                    }
                }
            }

            // Current User Matching Profile Context Strip
            item(key = "active_profile_strip") {
                val assessedSkillsSummary = remember(currentUserProfile.skills, currentUserProfile.skillProficiencies) {
                    currentUserProfile.skills.take(3).joinToString(", ") { skill ->
                        "$skill (${currentUserProfile.proficiencyFor(skill).label})"
                    }.ifBlank { "None added yet" }
                }
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Matching against ${currentUserProfile.displayName.ifBlank { "your profile" }}",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Your skills: $assessedSkillsSummary",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Availability: ${currentUserProfile.formattedAvailabilitySummary()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        OutlinedButton(
                            onClick = onEditMatchingProfile,
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("matches_edit_profile_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Rounded.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Assess")
                        }
                    }
                }
            }

            // Firestore Overlapping-Skill Collaborator Suggestions
            item(key = "overlapping_skill_suggestions") {
                OverlappingSkillSuggestionsSection(
                    feedState = overlappingSkillFeedState,
                    isCloudSynced = isCloudSynced,
                    isRefreshing = isRefreshingSkillOverlap,
                    dismissedUserIds = dismissedUserIds,
                    messagesByPeerId = messagesByPeerId,
                    onRefresh = onRefreshOverlappingSkillMatches,
                    onEditProfile = onEditMatchingProfile,
                    onDiscuss = { overlapItem ->
                        activeDiscussionPeer = overlapItem.match.profile
                        activeDiscussionSkills = overlapItem.match.overlappingSkills
                    },
                    onInvite = { overlapItem ->
                        val targetMission = allMissions.firstOrNull()
                        onSendInvitation(
                            overlapItem.match.profile,
                            targetMission?.id ?: "mission_community_env",
                            targetMission?.title ?: "Collaborative Mission",
                            overlapItem.match.overlappingSkills.firstOrNull() ?: "Skill Peer",
                            overlapItem.match.explanation
                        )
                    }
                )
            }

            // Search Bar for Skill Keywords & Interest Categories
            item(key = "search_bar") {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search skill keywords or interest categories…") },
                    label = { Text("Filter profiles by skill or interest") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Search profiles",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { onSearchQueryChange("") },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("matches_search_clear_button")
                            ) {
                                Icon(Icons.Rounded.Close, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("matches_search_input")
                )
            }

            // Quick Skill Keyword Chips
            item(key = "skill_keywords") {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Quick Skill Keywords",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(QUICK_SKILL_KEYWORDS) { keyword ->
                            val active = searchQuery.equals(keyword, ignoreCase = true)
                            FilterChip(
                                selected = active,
                                onClick = {
                                    onSearchQueryChange(if (active) "" else keyword)
                                },
                                modifier = Modifier.testTag(
                                    "matches_skill_chip_${keyword.lowercase().replace(" ", "_")}"
                                ),
                                label = { Text(keyword) }
                            )
                        }
                    }
                }
            }

            // Interest Category Filter Bar + Mode + Firestore Source Toggle
            item(key = "filters_and_modes") {
                val categories = (matchesFeedState as? UiState.Success)?.data?.availableInterestCategories
                    .orEmpty()
                val firestoreCount = (matchesFeedState as? UiState.Success)?.data?.firestorePeerCount ?: 0

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Rounded.FilterList,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Interest Categories",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (isCloudSynced) {
                            FilterChip(
                                selected = firestoreOnly,
                                onClick = { onToggleFirestoreOnly(!firestoreOnly) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Rounded.CloudDone,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                label = { Text("Firestore Only ($firestoreCount)") },
                                modifier = Modifier.testTag("matches_firestore_only_chip")
                            )
                        }
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item(key = "all_interests") {
                            FilterChip(
                                selected = selectedInterestCategory == null,
                                onClick = { onSelectInterestCategory(null) },
                                modifier = Modifier.testTag("matches_interest_all_chip"),
                                label = { Text("All Interests") }
                            )
                        }
                        items(categories) { category ->
                            val isSelected = selectedInterestCategory.equals(category, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    onSelectInterestCategory(if (isSelected) null else category)
                                },
                                modifier = Modifier.testTag(
                                    "matches_interest_chip_${category.lowercase().replace(" ", "_")}"
                                ),
                                label = { Text(category) }
                            )
                        }
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            MatchingMode.CAPABILITY,
                            MatchingMode.SKILL_OVERLAP,
                            MatchingMode.ASSISTANCE,
                            MatchingMode.AMBITION
                        ).forEach { mode ->
                            val active = (selectedMode == mode) ||
                                (selectedMode == MatchingMode.COLLECTIVE && mode == MatchingMode.CAPABILITY)
                            FilterChip(
                                selected = active,
                                onClick = { onSelectMode(mode) },
                                modifier = Modifier.testTag("matches_mode_${mode.name.lowercase()}"),
                                label = { Text(mode.title) }
                            )
                        }
                    }
                }
            }

            // Main Firestore Matches Feed Content
            when (matchesFeedState) {
                is UiState.Loading -> {
                    item(key = "loading") {
                        Card(
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(26.dp), strokeWidth = 2.5.dp)
                                Column {
                                    Text("Loading complementary profiles…", style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        text = "Querying community profiles and ranking complementary capabilities.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                is UiState.Error -> {
                    item(key = "error") {
                        InfoBannerCard(
                            title = "Could not load Firestore matches",
                            body = matchesFeedState.message,
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                is UiState.Success -> {
                    val feed = matchesFeedState.data
                    val visibleItems = feed.items.filter { it.match.profile.userId !in dismissedUserIds }

                    item(key = "summary_bar") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${visibleItems.size} complementary ${if (visibleItems.size == 1) "match" else "matches"}" +
                                    if (isCloudSynced) " • ${feed.firestorePeerCount} in Firestore" else "",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            if (searchQuery.isNotBlank() || selectedInterestCategory != null) {
                                TextButton(
                                    onClick = onClearFilters,
                                    modifier = Modifier.testTag("matches_clear_filters_button")
                                ) {
                                    Text("Clear Filters")
                                }
                            }
                        }
                    }

                    if (isCloudSynced && feed.firestorePeerCount == 0 && !firestoreOnly) {
                        item(key = "firestore_seed_notice") {
                            InfoBannerCard(
                                icon = Icons.Rounded.Groups,
                                title = "Connected to Firestore",
                                body = "No other signed-in user accounts have published a public profile to Firestore yet. Showing community directory collaborators alongside live Firestore results."
                            )
                        }
                    }

                    if (visibleItems.isEmpty()) {
                        item(key = "empty_matches") {
                            Card(
                                shape = RoundedCornerShape(22.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.PersonSearch,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Text(
                                        text = if (firestoreOnly && feed.firestorePeerCount == 0) {
                                            "No other Firestore profiles yet"
                                        } else {
                                            "No profiles match your search"
                                        },
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = if (firestoreOnly && feed.firestorePeerCount == 0) {
                                            "Turn off 'Firestore Only' to preview community directory profiles, or invite collaborators to sign in and publish their profile."
                                        } else {
                                            "Try another skill keyword, switch interest categories, or clear active filters."
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (searchQuery.isNotBlank() || selectedInterestCategory != null || firestoreOnly) {
                                        OutlinedButton(
                                            onClick = {
                                                onClearFilters()
                                                if (firestoreOnly) onToggleFirestoreOnly(false)
                                            },
                                            modifier = Modifier.heightIn(min = 48.dp)
                                        ) {
                                            Text("Reset Filters")
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        items(
                            items = visibleItems,
                            key = { it.match.profile.userId }
                        ) { item ->
                            val peerEndorsements = endorsementSummaryByUser[item.match.profile.userId].orEmpty()
                            val peerMessages = messagesByPeerId[item.match.profile.userId].orEmpty()
                            ComplementaryProfileMatchCard(
                                item = item,
                                peerEndorsements = peerEndorsements,
                                messageCount = peerMessages.size,
                                onTapSkill = { skill -> onSearchQueryChange(skill) },
                                onTapInterest = { interest ->
                                    onSelectInterestCategory(
                                        if (selectedInterestCategory.equals(interest, ignoreCase = true)) null else interest
                                    )
                                },
                                onToggleEndorseSkill = { skill ->
                                    onToggleSkillEndorsement(item.match.profile, skill)
                                },
                                onDiscuss = {
                                    activeDiscussionPeer = item.match.profile
                                    activeDiscussionSkills = item.match.complementaryCapabilities
                                },
                                onInvite = {
                                    val targetMission = allMissions.firstOrNull()
                                    onSendInvitation(
                                        item.match.profile,
                                        targetMission?.id ?: "mission_community_env",
                                        targetMission?.title ?: "Collaborative Mission",
                                        item.match.complementaryCapabilities.firstOrNull() ?: "Collaborator",
                                        item.match.explanation
                                    )
                                },
                                onDismiss = { dismissedUserIds.add(item.match.profile.userId) },
                                onBlock = { blockTarget = item.match.profile }
                            )
                        }
                    }
                }
            }

            item(key = "bottom_spacer") {
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    if (activeDiscussionPeer != null) {
        val targetPeer = activeDiscussionPeer!!
        CollaborationDiscussionSheet(
            peer = targetPeer,
            currentUserId = currentUserProfile.userId,
            complementarySkills = activeDiscussionSkills,
            messages = messagesByPeerId[targetPeer.userId].orEmpty(),
            onSendMessage = { body, contextLabel ->
                onSendCollaborationMessage(targetPeer, body, contextLabel)
            },
            onDismiss = { activeDiscussionPeer = null }
        )
    }

    if (blockTarget != null) {
        AlertDialog(
            onDismissRequest = { blockTarget = null },
            title = { Text("Safety & Boundary Controls") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Hide and block ${blockTarget!!.displayName}? You can manage blocked users anytime in My Space.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = blockReason,
                        onValueChange = { blockReason = it },
                        label = { Text("Reason (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onBlockUser(blockTarget!!, blockReason)
                        blockTarget = null
                        blockReason = ""
                    }
                ) {
                    Text("Block User")
                }
            },
            dismissButton = {
                TextButton(onClick = { blockTarget = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ComplementaryProfileMatchCard(
    item: MatchCardItem,
    peerEndorsements: Map<String, SkillEndorsementSummary>,
    messageCount: Int,
    onTapSkill: (String) -> Unit,
    onTapInterest: (String) -> Unit,
    onToggleEndorseSkill: (String) -> Unit,
    onDiscuss: () -> Unit,
    onInvite: () -> Unit,
    onDismiss: () -> Unit,
    onBlock: () -> Unit
) {
    val match = item.match
    val profile = match.profile
    val scorePercent = (match.alignmentScore * 100).toInt().coerceIn(0, 100)
    val totalEndorsements = peerEndorsements.values.sumOf { it.count }
    val displaySkills = remember(match.complementaryCapabilities, profile.skills) {
        (match.complementaryCapabilities + profile.skills)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }
            .take(8)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("matches_card_${profile.userId}"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Identity + Source + Score + Trust Count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = profile.displayName.take(1).uppercase(),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = profile.displayName,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (profile.headline.isNotBlank()) {
                            Text(
                                text = profile.headline,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        color = CoverageGreen.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "$scorePercent% match",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = CoverageGreen,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (totalEndorsements > 0) {
                            Surface(
                                color = CoverageGreen.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(50)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Verified,
                                        contentDescription = "$totalEndorsements peer endorsements",
                                        tint = CoverageGreen,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "$totalEndorsements endorsed",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = CoverageGreen
                                    )
                                }
                            }
                        }
                        Surface(
                            color = if (item.isFromFirestore) {
                                MaterialTheme.colorScheme.secondaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = if (item.isFromFirestore) "Firestore" else "Directory",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            LinearProgressIndicator(
                progress = { (scorePercent / 100f).coerceIn(0f, 1f) },
                color = CoverageGreen,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
            )

            // Scannable Visual Skill Badges + Proficiency Levels + One-Tap Peer Endorsements
            if (displaySkills.isNotEmpty()) {
                ProfileSkillBadgesSection(
                    skills = displaySkills,
                    complementarySkills = match.complementaryCapabilities.toSet(),
                    endorsementsBySkill = peerEndorsements,
                    skillProficiencies = profile.skillProficiencies,
                    title = "Visual Skill & Expertise Badges",
                    subtitle = "Tap skill to filter • Endorse to build trust",
                    onTapSkillFilter = onTapSkill,
                    onToggleEndorseSkill = onToggleEndorseSkill
                )
            }

            // Interests (tap any interest chip to filter)
            if (profile.interests.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Interests",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        profile.interests.take(6).forEach { interest ->
                            Surface(
                                color = ComplementaryAmber.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, ComplementaryAmber.copy(alpha = 0.35f)),
                                modifier = Modifier.clickable { onTapInterest(interest) }
                            ) {
                                Text(
                                    text = interest,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Complementarity Explanation
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Why You Complement Each Other",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(match.explanation, style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = match.mutualExchangeSummary,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AccessibilityBadge(
                    label = "${AvailabilityWindow.safeParse(profile.availability).label} • ${match.accessibilityNote}",
                    modifier = Modifier.weight(1f, fill = false)
                )
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onDiscuss,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("matches_discuss_${profile.userId}"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Rounded.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (messageCount > 0) "Discuss ($messageCount)" else "Discuss",
                        maxLines = 1
                    )
                }

                Button(
                    onClick = onInvite,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("matches_invite_${profile.userId}"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Rounded.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Invite", maxLines = 1)
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("matches_hide_${profile.userId}")
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = "Hide match")
                }

                IconButton(
                    onClick = onBlock,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("matches_block_${profile.userId}")
                ) {
                    Icon(
                        Icons.Rounded.Block,
                        contentDescription = "Block user",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OverlappingSkillSuggestionsSection(
    feedState: UiState<OverlappingSkillFeed>,
    isCloudSynced: Boolean,
    isRefreshing: Boolean,
    dismissedUserIds: List<String>,
    messagesByPeerId: Map<String, List<CollaborationMessage>>,
    onRefresh: () -> Unit,
    onEditProfile: () -> Unit,
    onDiscuss: (OverlappingSkillMatchItem) -> Unit,
    onInvite: (OverlappingSkillMatchItem) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("overlapping_skill_suggestions_section"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Overlapping Skill Collaborators",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (isCloudSynced) {
                            "Queried from Firestore profiles sharing skills with you"
                        } else {
                            "Peers sharing overlapping skills and interests"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(
                    onClick = onRefresh,
                    enabled = !isRefreshing,
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("matches_refresh_overlap_button")
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.width(4.dp))
                    Text("Refresh")
                }
            }

            when (feedState) {
                is UiState.Loading -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Text(
                            text = "Finding collaborators with overlapping skill sets…",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                is UiState.Error -> {
                    Text(
                        text = feedState.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                is UiState.Success -> {
                    val feed = feedState.data
                    val visibleItems = feed.items.filter { it.match.profile.userId !in dismissedUserIds }

                    if (feed.seekerSkills.isEmpty()) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Add your skills in Profile so Firestore can match collaborators with overlapping skill sets.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(Modifier.width(8.dp))
                                OutlinedButton(
                                    onClick = onEditProfile,
                                    modifier = Modifier.heightIn(min = 48.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Add Skills")
                                }
                            }
                        }
                    } else if (visibleItems.isEmpty()) {
                        Text(
                            text = "No collaborators currently share overlapping skills with ${feed.seekerSkills.joinToString(", ")}. Try adding more skills or tapping Refresh.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = visibleItems,
                                key = { "overlap_${it.match.profile.userId}" }
                            ) { overlapItem ->
                                OverlappingSkillCollaboratorCard(
                                    item = overlapItem,
                                    messageCount = messagesByPeerId[overlapItem.match.profile.userId].orEmpty().size,
                                    onDiscuss = { onDiscuss(overlapItem) },
                                    onInvite = { onInvite(overlapItem) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OverlappingSkillCollaboratorCard(
    item: OverlappingSkillMatchItem,
    messageCount: Int,
    onDiscuss: () -> Unit,
    onInvite: () -> Unit
) {
    val match = item.match
    val profile = match.profile
    Card(
        modifier = Modifier
            .width(292.dp)
            .testTag("overlapping_skill_card_${profile.userId}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = profile.displayName,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1
                    )
                    if (profile.headline.isNotBlank()) {
                        Text(
                            text = profile.headline,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                        color = CoverageGreen.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "${match.overlappingSkills.size} shared • ${match.overlapPercent}%",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = CoverageGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Surface(
                        color = if (item.isFromFirestore) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = if (item.isFromFirestore) "Firestore" else "Directory",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                match.overlappingSkills.take(4).forEach { skill ->
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = skill,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Text(
                text = match.explanation,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDiscuss,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("overlap_discuss_${profile.userId}"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (messageCount > 0) "Chat ($messageCount)" else "Chat", maxLines = 1)
                }
                Button(
                    onClick = onInvite,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("overlap_invite_${profile.userId}"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Invite", maxLines = 1)
                }
            }
        }
    }
}

