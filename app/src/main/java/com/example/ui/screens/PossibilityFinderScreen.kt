package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.model.CapabilityAvailabilityStatus
import com.example.model.CommunityProfile
import com.example.model.PossibilityAnalysis
import com.example.model.UiState
import com.example.ui.components.AiBadge
import com.example.ui.components.InfoBannerCard
import com.example.ui.components.SectionHeading
import com.example.ui.theme.ComplementaryAmber
import com.example.ui.theme.CoverageGreen
import com.example.ui.theme.DeepForestGreen
import com.example.ui.theme.GapTerracotta
import com.example.ui.theme.SoftCream

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PossibilityFinderScreen(
    possibilityState: UiState<PossibilityAnalysis>?,
    onAnalyze: (
        objective: String,
        contributions: List<String>,
        learningGoals: List<String>,
        supportWanted: List<String>,
        useAi: Boolean
    ) -> Unit,
    onSavePossibility: (PossibilityAnalysis) -> Unit,
    onInviteCollaborator: (CommunityProfile, String, String) -> Unit,
    onTurnIntoMission: (PossibilityAnalysis) -> Unit
) {
    var objective by remember { mutableStateOf("Build an open neighborhood air-quality monitoring network") }
    var contributionsInput by remember { mutableStateOf("Programming, Logical problem-solving") }
    var learningInput by remember { mutableStateOf("Environmental research, Basic electronics") }
    var supportInput by remember { mutableStateOf("Visual design, Data collection") }

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
                title = "Possibility Finder",
                subtitle = "Reveal what becomes achievable with what you bring and the right collaborators"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(136.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .testTag("possibility_visual_banner")
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_possibility_constellation),
                    contentDescription = "Sunlit craft tools and golden woven threads symbolizing collective skill assembly",
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
                                    DeepForestGreen.copy(alpha = 0.35f),
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
                        text = "Every Craft Counts",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = SoftCream
                    )
                    Text(
                        text = "Turn individual gaps into multi-person group possibilities.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftCream.copy(alpha = 0.92f)
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = objective.contains("air-quality", ignoreCase = true),
                            onClick = {
                                objective = "Build an open neighborhood air-quality monitoring network"
                                contributionsInput = "Programming, Logical problem-solving"
                                learningInput = "Basic electronics, Environmental research"
                                supportInput = "Visual design, Data collection"
                            },
                            label = { Text("Example: Env. Monitoring") }
                        )
                        FilterChip(
                            selected = objective.contains("tactile", ignoreCase = true),
                            onClick = {
                                objective = "Create an accessible tactile & audio botanical guide for neighborhood parks"
                                contributionsInput = "Visual design, Creative communication"
                                learningInput = "Audio engineering, Tactile prototyping"
                                supportInput = "Accessibility testing, Programming"
                            },
                            label = { Text("Example: Tactile Guide") }
                        )
                    }

                    OutlinedTextField(
                        value = objective,
                        onValueChange = { objective = it },
                        label = { Text("What do you want to accomplish?") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth().testTag("possibility_objective_input"),
                        shape = RoundedCornerShape(14.dp)
                    )
                    OutlinedTextField(
                        value = contributionsInput,
                        onValueChange = { contributionsInput = it },
                        label = { Text("What can you contribute? (comma-separated)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )
                    OutlinedTextField(
                        value = learningInput,
                        onValueChange = { learningInput = it },
                        label = { Text("What do you want to learn?") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )
                    OutlinedTextField(
                        value = supportInput,
                        onValueChange = { supportInput = it },
                        label = { Text("Where would you appreciate support?") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                onAnalyze(
                                    objective,
                                    splitCsv(contributionsInput),
                                    splitCsv(learningInput),
                                    splitCsv(supportInput),
                                    false
                                )
                            },
                            enabled = objective.isNotBlank(),
                            modifier = Modifier.weight(1f).height(48.dp).testTag("possibility_analyze_button")
                        ) {
                            Icon(Icons.Rounded.Lightbulb, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Reveal Possibilities")
                        }
                        OutlinedButton(
                            onClick = {
                                onAnalyze(
                                    objective,
                                    splitCsv(contributionsInput),
                                    splitCsv(learningInput),
                                    splitCsv(supportInput),
                                    true
                                )
                            },
                            enabled = objective.isNotBlank() && possibilityState !is UiState.Loading,
                            modifier = Modifier.weight(1f).height(48.dp).testTag("possibility_ai_analyze_button")
                        ) {
                            Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Analyze with AI")
                        }
                    }
                }
            }

            when (possibilityState) {
                null -> {
                    InfoBannerCard(
                        title = "Start with what you bring",
                        body = "Tap \"Reveal Possibilities\" to see available vs. missing capabilities, real potential collaborators, multi-person team combinations, and alternative paths."
                    )
                }
                is UiState.Loading -> {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is UiState.Error -> {
                    InfoBannerCard(
                        title = "Analysis Notice",
                        body = possibilityState.message,
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                is UiState.Success -> {
                    val analysis = possibilityState.data
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
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
                                    text = "Capability & Gap Breakdown",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.weight(1f)
                                )
                                AiBadge(if (analysis.analysisSource.contains("Gemini")) "Gemini AI" else "Engine")
                            }
                            Text(
                                text = "Objective: ${analysis.objective}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                            )

                            analysis.capabilityBreakdown.forEach { item ->
                                val (icon, tint, label) = when (item.status) {
                                    CapabilityAvailabilityStatus.AVAILABLE -> Triple(Icons.Rounded.CheckCircle, CoverageGreen, "Available")
                                    CapabilityAvailabilityStatus.MISSING -> Triple(Icons.Rounded.ErrorOutline, GapTerracotta, "Missing")
                                    CapabilityAvailabilityStatus.OPTIONAL -> Triple(Icons.Rounded.RadioButtonUnchecked, ComplementaryAmber, "Optional")
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(20.dp))
                                    Column {
                                        Text("${item.name}: $label", style = MaterialTheme.typography.titleSmall)
                                        Text(item.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }

                            HorizontalDivider()

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onSavePossibility(analysis) },
                                    modifier = Modifier.weight(1f).height(48.dp).testTag("save_possibility_button")
                                ) {
                                    Icon(Icons.Rounded.BookmarkAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Save Possibility")
                                }
                                Button(
                                    onClick = { onTurnIntoMission(analysis) },
                                    modifier = Modifier.weight(1f).height(48.dp).testTag("create_mission_from_possibility_button")
                                ) {
                                    Icon(Icons.Rounded.RocketLaunch, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Launch Mission")
                                }
                            }
                        }
                    }

                    if (analysis.groupCombinations.isNotEmpty()) {
                        SectionHeading(
                            title = "Possible Group Combinations",
                            subtitle = "Teams formed from actual stored profiles"
                        )
                        analysis.groupCombinations.forEachIndexed { idx, team ->
                            CollectiveTeamCard(
                                rankIndex = idx + 1,
                                recommendation = team,
                                onInviteTeam = {
                                    team.roleAssignments.drop(1).forEach { ra ->
                                        onInviteCollaborator(ra.participant, ra.roleTitle, team.explanation)
                                    }
                                }
                            )
                        }
                    }

                    if (analysis.potentialCollaborators.isNotEmpty()) {
                        SectionHeading(
                            title = "Potential Collaborators",
                            subtitle = "Peers whose capabilities bridge your missing requirements"
                        )
                        analysis.potentialCollaborators.take(3).forEach { match ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(match.profile.displayName, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                                        Text(match.explanation, style = MaterialTheme.typography.bodySmall)
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    OutlinedButton(
                                        onClick = {
                                            onInviteCollaborator(
                                                match.profile,
                                                match.complementaryCapabilities.firstOrNull() ?: "Collaborator",
                                                match.explanation
                                            )
                                        },
                                        modifier = Modifier.height(48.dp)
                                    ) {
                                        Text("Invite")
                                    }
                                }
                            }
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Alternative Ways to Accomplish This", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                            analysis.alternativeApproaches.forEach { alt ->
                                Text("• $alt", style = MaterialTheme.typography.bodySmall)
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            Text("Suggested Collaboration Plan", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                            analysis.suggestedPlan.forEach { step ->
                                Text(step, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

private fun splitCsv(raw: String): List<String> =
    raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }.distinct()
