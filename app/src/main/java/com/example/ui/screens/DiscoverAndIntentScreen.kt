package com.example.ui.screens

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
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Hub
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.example.model.CollectiveTeamRecommendation
import com.example.model.CommunityProfile
import com.example.model.DiscoveryCategory
import com.example.model.IndividualMatchResult
import com.example.model.IntentSearchResult
import com.example.model.MatchingMode
import com.example.model.Mission
import com.example.model.StructuredIntent
import com.example.model.UiState
import com.example.ui.components.AccessibilityBadge
import com.example.ui.components.AiBadge
import com.example.ui.components.CapabilityTagGroup
import com.example.ui.components.CoverageItemRow
import com.example.ui.components.CoverageMeterBar
import com.example.ui.components.InfoBannerCard
import com.example.ui.components.SectionHeading
import com.example.ui.theme.ComplementaryAmber
import com.example.ui.theme.CoverageGreen

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DiscoverAndIntentScreen(
    selectedMode: MatchingMode,
    individualMatches: List<IndividualMatchResult>,
    collectiveRecommendations: List<CollectiveTeamRecommendation>,
    allMissions: List<Mission>,
    intentSearchState: UiState<List<IntentSearchResult>>,
    currentInterpretedIntent: StructuredIntent?,
    onSelectMode: (MatchingMode) -> Unit,
    onRunIntentSearch: (title: String, description: String, useAi: Boolean) -> Unit,
    onSendInvitation: (recipient: CommunityProfile, missionId: String, missionTitle: String, role: String, rationale: String) -> Unit,
    onExpressMissionInterest: (Mission) -> Unit,
    onBlockUser: (CommunityProfile, String) -> Unit
) {
    var activeTab by remember { mutableIntStateOf(0) } // 0 = Matching Modes, 1 = Intent Discovery
    var searchFilter by remember { mutableStateOf("") }
    var availabilityFilter by remember { mutableStateOf("ALL") }
    val dismissedUserIds = remember { mutableStateListOf<String>() }
    val excludedMissionIds = remember { mutableStateListOf<String>() }
    var blockTarget by remember { mutableStateOf<CommunityProfile?>(null) }
    var blockReason by remember { mutableStateOf("") }

    var intentTitle by remember { mutableStateOf("Make our neighbourhood greener") }
    var intentDesc by remember { mutableStateOf("Plant trees, restore green spaces, and engage local neighbors.") }

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
                .widthIn(max = 780.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionHeading(
                title = "Discover Possibilities",
                subtitle = "Four matching modes & intent-based mission discovery"
            )

            TabRow(selectedTabIndex = activeTab) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    modifier = Modifier.testTag("discover_tab_matching"),
                    text = { Text("Matching Modes") }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    modifier = Modifier.testTag("discover_tab_intent"),
                    text = { Text("Intent Search") }
                )
            }

            if (activeTab == 0) {
                // Mode selector
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MatchingMode.entries.forEach { mode ->
                        FilterChip(
                            selected = selectedMode == mode,
                            onClick = { onSelectMode(mode) },
                            modifier = Modifier.testTag("mode_chip_${mode.name.lowercase()}"),
                            label = { Text(mode.title) }
                        )
                    }
                }

                InfoBannerCard(
                    icon = Icons.Rounded.Psychology,
                    title = selectedMode.title,
                    body = selectedMode.subtitle
                )

                if (selectedMode != MatchingMode.COLLECTIVE) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchFilter,
                            onValueChange = { searchFilter = it },
                            label = { Text("Filter by skill or interest") },
                            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        )
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("ALL", "FLEXIBLE", "WEEKENDS", "WEEKDAYS", "EVENINGS").forEach { av ->
                            FilterChip(
                                selected = availabilityFilter == av,
                                onClick = { availabilityFilter = av },
                                label = { Text(if (av == "ALL") "Any Schedule" else av.lowercase().replaceFirstChar { it.uppercase() }) }
                            )
                        }
                    }

                    val filtered = individualMatches.filter { m ->
                        m.profile.userId !in dismissedUserIds &&
                            (availabilityFilter == "ALL" || m.profile.availability == availabilityFilter || m.profile.availability == "FLEXIBLE") &&
                            (searchFilter.isBlank() ||
                                m.profile.displayName.contains(searchFilter, ignoreCase = true) ||
                                m.profile.allCapabilities().any { it.contains(searchFilter, ignoreCase = true) } ||
                                m.profile.ambitions.any { it.contains(searchFilter, ignoreCase = true) })
                    }

                    if (filtered.isEmpty()) {
                        InfoBannerCard(
                            title = "No matching profiles for this filter",
                            body = "Try clearing filters or switching to another matching mode."
                        )
                    } else {
                        filtered.forEach { match ->
                            IndividualMatchCard(
                                match = match,
                                onInvite = {
                                    val targetMission = allMissions.firstOrNull()
                                    onSendInvitation(
                                        match.profile,
                                        targetMission?.id ?: "mission_community_env",
                                        targetMission?.title ?: "Collaborative Mission",
                                        match.complementaryCapabilities.firstOrNull() ?: "Collaborator",
                                        match.explanation
                                    )
                                },
                                onDismiss = { dismissedUserIds.add(match.profile.userId) },
                                onBlock = { blockTarget = match.profile }
                            )
                        }
                    }
                } else {
                    if (collectiveRecommendations.isEmpty()) {
                        InfoBannerCard(
                            title = "No collective groups available yet",
                            body = "Add capabilities or create a mission to form multi-person teams."
                        )
                    } else {
                        collectiveRecommendations.forEachIndexed { index, team ->
                            CollectiveTeamCard(
                                rankIndex = index + 1,
                                recommendation = team,
                                onInviteTeam = {
                                    team.roleAssignments.drop(1).forEach { ra ->
                                        onSendInvitation(
                                            ra.participant,
                                            "mission_community_env",
                                            "Collective Community Mission",
                                            ra.roleTitle,
                                            team.explanation
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            } else {
                // Intent-Based Mission Discovery Engine
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
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Search by Underlying Intention",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            AiBadge("Semantic Engine")
                        }
                        Text(
                            text = "SYMBIOSIS searches for what you MEAN, not just exact keywords — separating Same, Related, and Complementary missions.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Quick demonstration chips
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                "Make our neighbourhood greener" to "Plant trees and build local green corners.",
                                "Build a neighborhood air-quality sensor" to "Measure local air quality with neighbors and publish accessible visuals.",
                                "Make neighborhood trails accessible" to "Create audio and tactile guides for blind visitors."
                            ).forEach { (presetTitle, presetDesc) ->
                                FilterChip(
                                    selected = intentTitle == presetTitle,
                                    onClick = {
                                        intentTitle = presetTitle
                                        intentDesc = presetDesc
                                        onRunIntentSearch(presetTitle, presetDesc, false)
                                    },
                                    label = { Text(presetTitle) }
                                )
                            }
                        }

                        OutlinedTextField(
                            value = intentTitle,
                            onValueChange = { intentTitle = it },
                            label = { Text("Mission idea or objective") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("intent_title_input"),
                            shape = RoundedCornerShape(14.dp)
                        )
                        OutlinedTextField(
                            value = intentDesc,
                            onValueChange = { intentDesc = it },
                            label = { Text("Intended outcome & context") },
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { onRunIntentSearch(intentTitle, intentDesc, false) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("intent_search_local_button")
                            ) {
                                Icon(Icons.Rounded.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Search Intent")
                            }
                            OutlinedButton(
                                onClick = { onRunIntentSearch(intentTitle, intentDesc, true) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("intent_search_ai_button")
                            ) {
                                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Gemini AI Intent")
                            }
                        }
                    }
                }

                if (currentInterpretedIntent != null) {
                    InfoBannerCard(
                        icon = Icons.Rounded.Hub,
                        title = "Interpreted Intention (${currentInterpretedIntent.sourceLabel})",
                        body = "${currentInterpretedIntent.mainObjective}\nDomain: ${currentInterpretedIntent.problemDomain}"
                    )
                }

                when (intentSearchState) {
                    is UiState.Loading -> {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    is UiState.Error -> {
                        InfoBannerCard(
                            title = "Discovery Error",
                            body = intentSearchState.message,
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    is UiState.Success -> {
                        val visibleResults = intentSearchState.data.filterNot { it.mission.id in excludedMissionIds }
                        DiscoveryCategory.entries.forEach { category ->
                            val group = visibleResults.filter { it.category == category }
                            if (group.isNotEmpty()) {
                                SectionHeading(
                                    title = category.label,
                                    subtitle = category.description
                                )
                                group.forEach { item ->
                                    IntentResultCard(
                                        result = item,
                                        onExpressInterest = { onExpressMissionInterest(item.mission) },
                                        onExclude = { excludedMissionIds.add(item.mission.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    if (blockTarget != null) {
        AlertDialog(
            onDismissRequest = { blockTarget = null },
            title = { Text("Block or Report ${blockTarget?.displayName}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "You are in control of your collaboration boundaries. Blocking hides this profile from your discovery results.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = blockReason,
                        onValueChange = { blockReason = it },
                        label = { Text("Optional safety note") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        blockTarget?.let { onBlockUser(it, blockReason) }
                        blockTarget = null
                        blockReason = ""
                    }
                ) { Text("Confirm Block") }
            },
            dismissButton = {
                TextButton(onClick = { blockTarget = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun IndividualMatchCard(
    match: IndividualMatchResult,
    onInvite: () -> Unit,
    onDismiss: () -> Unit,
    onBlock: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
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
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = match.profile.displayName,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = match.profile.headline,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = match.alignmentMetricLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Text(text = match.explanation, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = match.mutualExchangeSummary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )

            CapabilityTagGroup(tags = match.complementaryCapabilities.take(5))
            AccessibilityBadge(label = match.accessibilityNote)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onInvite,
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(Icons.Rounded.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Invite (Consent)")
                    }
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("Skip")
                    }
                }
                IconButton(onClick = onBlock) {
                    Icon(
                        imageVector = Icons.Rounded.Block,
                        contentDescription = "Block or report ${match.profile.displayName}",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun CollectiveTeamCard(
    rankIndex: Int,
    recommendation: CollectiveTeamRecommendation,
    onInviteTeam: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                Text(
                    text = "Team Option #$rankIndex: ${recommendation.teamMembers.joinToString(" + ") { it.displayName }}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = recommendation.feasibilityLabel,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            CoverageMeterBar(
                percent = recommendation.essentialCoveragePercent,
                label = "Essential Coverage"
            )

            Text(text = recommendation.explanation, style = MaterialTheme.typography.bodyMedium)

            HorizontalDivider()
            Text("Suggested Role Allocation", style = MaterialTheme.typography.titleSmall)
            recommendation.roleAssignments.forEach { ra ->
                Text(
                    text = "• ${ra.participant.displayName} — ${ra.roleTitle} (${ra.contributedCapabilities.joinToString(", ")})",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            recommendation.coverageItems.forEach { item ->
                CoverageItemRow(item)
            }

            Text(
                text = recommendation.workloadBalanceNote,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            AccessibilityBadge(label = recommendation.accessibilityAlignmentNote)

            if (onInviteTeam != null) {
                Button(
                    onClick = onInviteTeam,
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(Icons.Rounded.PersonAdd, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Send Consent-Based Invitations to Team")
                }
            }
        }
    }
}

@Composable
fun IntentResultCard(
    result: IntentSearchResult,
    onExpressInterest: () -> Unit,
    onExclude: () -> Unit
) {
    val badgeColor = when (result.category) {
        DiscoveryCategory.SAME_MISSION -> CoverageGreen
        DiscoveryCategory.COMPLEMENTARY_MISSION -> ComplementaryAmber
        DiscoveryCategory.RELATED_MISSION -> MaterialTheme.colorScheme.secondary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.4f))
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
                Surface(
                    color = badgeColor.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = "${result.category.label} • ${result.semanticSimilarityPercent}% intent match",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = badgeColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                IconButton(onClick = onExclude) {
                    Icon(Icons.Rounded.Close, contentDescription = "Exclude result")
                }
            }

            Text(
                text = result.mission.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "By ${result.mission.ownerName} • ${result.mission.description}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Interpreted Intent: ${result.interpretedIntent}",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
            )
            Text(
                text = result.relationshipExplanation,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Together: ${result.collaborationPotential} ${result.capabilityComplementNote}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )

            OutlinedButton(
                onClick = onExpressInterest,
                modifier = Modifier.height(48.dp)
            ) {
                Text("Express Collaboration Interest (Requires Consent)")
            }
        }
    }
}
