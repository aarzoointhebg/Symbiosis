package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Hub
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.MissionRequirementSuggestion
import com.example.model.CollaborationInvitation
import com.example.model.CommunityProfile
import com.example.model.InvitationStatus
import com.example.model.Mission
import com.example.model.MissionMilestone
import com.example.model.MissionStatus
import com.example.model.UiState
import com.example.model.UserAmbition
import com.example.ui.components.AccessibilityBadge
import com.example.ui.components.CapabilityTagGroup
import com.example.ui.components.InfoBannerCard
import com.example.ui.components.SectionHeading
import com.example.ui.theme.BotanicalGreen
import com.example.ui.theme.DeepForestGreen
import com.example.ui.theme.SoftCream
import com.example.ui.theme.SoftEarthBrown
import com.example.ui.theme.WarmGoldenYellow
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

@Composable
fun MissionsAndCollaborateScreen(
    initialTab: Int = 0,
    currentUserId: String,
    missions: List<Mission>,
    invitations: List<CollaborationInvitation>,
    milestones: List<MissionMilestone>,
    profiles: List<CommunityProfile>,
    ambitions: List<UserAmbition>,
    aiMissionSuggestion: UiState<MissionRequirementSuggestion>?,
    onCreateMission: (
        title: String,
        description: String,
        objective: String,
        requiredCaps: List<String>,
        optionalCaps: List<String>,
        accessibilityReq: String,
        targetParticipants: Int
    ) -> Unit,
    onSuggestRequirementsAi: (String, String) -> Unit,
    onClearMissionSuggestion: () -> Unit,
    onRespondInvitation: (CollaborationInvitation, InvitationStatus) -> Unit,
    onToggleMilestone: (String) -> Unit,
    onUpdateMissionProgress: (Mission, Long) -> Unit,
    onExpressMissionInterest: (Mission) -> Unit
) {
    var activeTab by remember(initialTab) { mutableIntStateOf(initialTab) } // 0 = Missions, 1 = Collaborate, 2 = Ambition Map
    var showCreateDialog by remember { mutableStateOf(false) }

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
                .widthIn(max = 800.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeading(
                    title = "Missions & Collaboration",
                    subtitle = "Consent-based teams, milestones, and shared ambition networks",
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier.height(48.dp).testTag("create_mission_button")
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("New Mission")
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(128.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .testTag("missions_visual_banner")
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_missions_grove),
                    contentDescription = "Sunlit conservatory workspace with botanical specimens and shared blueprints",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    DeepForestGreen.copy(alpha = 0.80f),
                                    DeepForestGreen.copy(alpha = 0.34f),
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
                        text = "Co-Create Together",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = SoftCream
                    )
                    Text(
                        text = "Consent-gated invitations, milestones, and shared ambition constellations.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftCream.copy(alpha = 0.92f)
                    )
                }
            }

            TabRow(selectedTabIndex = activeTab) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    modifier = Modifier.testTag("missions_tab_browse"),
                    text = { Text("Shared Missions") }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    modifier = Modifier.testTag("missions_tab_collaborate"),
                    text = {
                        val pending = invitations.count { it.status == InvitationStatus.PENDING.name && it.recipientId == currentUserId }
                        Text(if (pending > 0) "Collaborate ($pending)" else "Collaborate")
                    }
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    modifier = Modifier.testTag("missions_tab_map"),
                    text = { Text("Ambition Map") }
                )
            }

            when (activeTab) {
                0 -> {
                    missions.forEach { mission ->
                        MissionCard(
                            mission = mission,
                            isMember = currentUserId in mission.memberUids || mission.ownerId == currentUserId,
                            onExpressInterest = { onExpressMissionInterest(mission) },
                            onUpdateProgress = { pct -> onUpdateMissionProgress(mission, pct) }
                        )
                    }
                }

                1 -> {
                    SectionHeading(
                        title = "Consent-Gated Invitations",
                        subtitle = "No connection is ever forced — accept or decline respectfully"
                    )
                    if (invitations.isEmpty()) {
                        InfoBannerCard(
                            title = "No invitations yet",
                            body = "Use Discover or Possibility Finder to invite complementary collaborators."
                        )
                    } else {
                        invitations.forEach { inv ->
                            InvitationCard(
                                invitation = inv,
                                currentUserId = currentUserId,
                                onAccept = { onRespondInvitation(inv, InvitationStatus.ACCEPTED) },
                                onDecline = { onRespondInvitation(inv, InvitationStatus.DECLINED) },
                                onWithdraw = { onRespondInvitation(inv, InvitationStatus.CANCELLED) }
                            )
                        }
                    }

                    SectionHeading(
                        title = "Active Workspace & Milestones",
                        subtitle = "Interactive Environmental Monitoring Project • Roles & Progress"
                    )
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val doneCount = milestones.count { it.completed }
                            val totalMs = milestones.size.coerceAtLeast(1)
                            Text(
                                text = "Milestone Progress: $doneCount / $totalMs completed",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            LinearProgressIndicator(
                                progress = { doneCount / totalMs.toFloat() },
                                modifier = Modifier.fillMaxWidth().height(8.dp)
                            )
                            milestones.forEach { ms ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = ms.completed,
                                        onCheckedChange = { onToggleMilestone(ms.id) }
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(ms.title, style = MaterialTheme.typography.bodyMedium)
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
                }

                2 -> {
                    SharedAmbitionMapSection(
                        profiles = profiles,
                        missions = missions,
                        ambitions = ambitions
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    if (showCreateDialog) {
        CreateMissionDialog(
            aiSuggestionState = aiMissionSuggestion,
            onSuggestWithAi = onSuggestRequirementsAi,
            onDismiss = {
                onClearMissionSuggestion()
                showCreateDialog = false
            },
            onCreate = { t, d, o, reqs, opts, access, count ->
                onCreateMission(t, d, o, reqs, opts, access, count)
                onClearMissionSuggestion()
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun MissionCard(
    mission: Mission,
    isMember: Boolean,
    onExpressInterest: () -> Unit,
    onUpdateProgress: (Long) -> Unit
) {
    val status = MissionStatus.safeParse(mission.status)
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = status.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                Text(
                    text = "${mission.memberUids.size}/${mission.targetParticipants} collaborators",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = mission.title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Initiated by ${mission.ownerName} • ${mission.description}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (mission.requiredCapabilities.isNotEmpty()) {
                Text("Essential Capabilities:", style = MaterialTheme.typography.labelMedium)
                CapabilityTagGroup(tags = mission.requiredCapabilities)
            }
            if (mission.optionalCapabilities.isNotEmpty()) {
                Text("Optional Enhancements:", style = MaterialTheme.typography.labelMedium)
                CapabilityTagGroup(
                    tags = mission.optionalCapabilities,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }

            AccessibilityBadge(label = mission.accessibilityReq)

            if (isMember && status != MissionStatus.COMPLETED) {
                Column {
                    Text("Progress: ${mission.progressPercent}%", style = MaterialTheme.typography.labelMedium)
                    Slider(
                        value = mission.progressPercent.toFloat(),
                        onValueChange = {},
                        onValueChangeFinished = { },
                        valueRange = 0f..100f,
                        enabled = false
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { onUpdateProgress((mission.progressPercent + 20L).coerceAtMost(100L)) }) {
                            Text("+20% Progress")
                        }
                    }
                }
            } else if (!isMember) {
                OutlinedButton(
                    onClick = onExpressInterest,
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("Express Interest to Join")
                }
            }
        }
    }
}

@Composable
private fun InvitationCard(
    invitation: CollaborationInvitation,
    currentUserId: String,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onWithdraw: () -> Unit
) {
    val status = InvitationStatus.safeParse(invitation.status)
    val isRecipient = invitation.recipientId == currentUserId

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = invitation.missionTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = status.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Text(
                text = if (isRecipient) "From ${invitation.senderName} • Proposed role: ${invitation.proposedRole}"
                else "To ${invitation.recipientName} • Proposed role: ${invitation.proposedRole}",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
            )
            Text(invitation.rationale, style = MaterialTheme.typography.bodySmall)

            if (status == InvitationStatus.PENDING) {
                if (isRecipient) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onAccept, modifier = Modifier.height(48.dp)) {
                            Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Accept")
                        }
                        OutlinedButton(onClick = onDecline, modifier = Modifier.height(48.dp)) {
                            Icon(Icons.Rounded.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Decline")
                        }
                    }
                } else {
                    OutlinedButton(onClick = onWithdraw, modifier = Modifier.height(48.dp)) {
                        Text("Withdraw Invitation")
                    }
                }
            }
        }
    }
}

private data class MapNode(
    val id: String,
    val label: String,
    val detail: String,
    val kind: String, // "MISSION", "CAPABILITY", "COLLABORATOR", "AMBITION"
    val color: Color
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SharedAmbitionMapSection(
    profiles: List<CommunityProfile>,
    missions: List<Mission>,
    ambitions: List<UserAmbition>
) {
    val nodes = remember(profiles, missions, ambitions) {
        buildList {
            val centerMission = missions.firstOrNull()
            add(
                MapNode(
                    id = "center",
                    label = centerMission?.title?.take(22) ?: "Shared Possibilities",
                    detail = "Central Mission: ${centerMission?.objective ?: "Collective collaboration"}\nNo popularity ranks — connects strengths to shared goals.",
                    kind = "MISSION",
                    color = DeepForestGreen
                )
            )
            ambitions.take(2).forEachIndexed { idx, amb ->
                add(
                    MapNode(
                        id = "amb_$idx",
                        label = amb.title.take(18),
                        detail = "Your Ambition: ${amb.title} — ${amb.description}",
                        kind = "AMBITION",
                        color = WarmGoldenYellow
                    )
                )
            }
            profiles.take(4).forEachIndexed { idx, p ->
                add(
                    MapNode(
                        id = "peer_$idx",
                        label = p.displayName,
                        detail = "${p.displayName}: ${p.skills.take(3).joinToString(", ")}\nAmbition: ${p.ambitions.firstOrNull() ?: p.headline}",
                        kind = "COLLABORATOR",
                        color = BotanicalGreen
                    )
                )
            }
            missions.drop(1).take(2).forEachIndexed { idx, m ->
                add(
                    MapNode(
                        id = "mis_$idx",
                        label = m.title.take(18),
                        detail = "Related/Complementary Mission: ${m.title}\nIntent: ${m.interpretedIntent}",
                        kind = "MISSION",
                        color = SoftEarthBrown
                    )
                )
            }
        }
    }

    var selectedNode by remember(nodes) { mutableStateOf(nodes.firstOrNull()) }

    SectionHeading(
        title = "Shared Ambition Map",
        subtitle = "Connected-node constellation of ambitions, capabilities, and missions (zero popularity scores)"
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(270.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .semantics {
                            contentDescription = "Connected-node Shared Ambition Map showing missions, ambitions, and complementary collaborators"
                        }
                        .pointerInput(nodes) {
                            detectTapGestures { tap ->
                                val w = size.width.toFloat()
                                val h = size.height.toFloat()
                                val center = Offset(w / 2f, h / 2f)
                                val outerCount = (nodes.size - 1).coerceAtLeast(1)
                                val radius = minOf(w, h) * 0.34f
                                val positions = nodes.mapIndexed { idx, _ ->
                                    if (idx == 0) center
                                    else {
                                        val angle = (2.0 * Math.PI * (idx - 1) / outerCount) - Math.PI / 2.0
                                        Offset(
                                            x = center.x + (radius * cos(angle)).toFloat(),
                                            y = center.y + (radius * sin(angle)).toFloat()
                                        )
                                    }
                                }
                                val hitIdx = positions.indexOfFirst { pos -> hypot(tap.x - pos.x, tap.y - pos.y) < 48f }
                                if (hitIdx >= 0) {
                                    selectedNode = nodes[hitIdx]
                                }
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height
                    val center = Offset(w / 2f, h / 2f)
                    val outerCount = (nodes.size - 1).coerceAtLeast(1)
                    val radius = minOf(w, h) * 0.34f

                    val positions = nodes.mapIndexed { idx, _ ->
                        if (idx == 0) center
                        else {
                            val angle = (2.0 * Math.PI * (idx - 1) / outerCount) - Math.PI / 2.0
                            Offset(
                                x = center.x + (radius * cos(angle)).toFloat(),
                                y = center.y + (radius * sin(angle)).toFloat()
                            )
                        }
                    }

                    // Draw connecting edges
                    for (i in 1 until positions.size) {
                        drawLine(
                            color = nodes[i].color.copy(alpha = 0.45f),
                            start = center,
                            end = positions[i],
                            strokeWidth = 4f
                        )
                        if (i > 1) {
                            drawLine(
                                color = Color.Gray.copy(alpha = 0.22f),
                                start = positions[i - 1],
                                end = positions[i],
                                strokeWidth = 2f
                            )
                        }
                    }

                    // Draw nodes
                    positions.forEachIndexed { idx, pos ->
                        val node = nodes[idx]
                        val isSelected = selectedNode?.id == node.id
                        val r = if (idx == 0) 36f else 24f
                        if (isSelected) {
                            drawCircle(color = node.color.copy(alpha = 0.22f), radius = r + 10f, center = pos)
                        }
                        drawCircle(color = node.color, radius = r, center = pos)
                        drawCircle(color = Color.White.copy(alpha = 0.85f), radius = r * 0.32f, center = pos)
                    }
                }
            }

            // Accessible legend & tap-chips (works cleanly for screen readers and keyboard users too!)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                nodes.forEach { node ->
                    Surface(
                        onClick = { selectedNode = node },
                        shape = RoundedCornerShape(50),
                        color = if (selectedNode?.id == node.id) node.color else node.color.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = "${node.kind}: ${node.label}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selectedNode?.id == node.id) Color.White else node.color,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            if (selectedNode != null) {
                InfoBannerCard(
                    icon = Icons.Rounded.Hub,
                    title = "${selectedNode!!.kind} • ${selectedNode!!.label}",
                    body = selectedNode!!.detail
                )
            }
        }
    }
}

@Composable
private fun CreateMissionDialog(
    aiSuggestionState: UiState<MissionRequirementSuggestion>?,
    onSuggestWithAi: (String, String) -> Unit,
    onDismiss: () -> Unit,
    onCreate: (
        title: String,
        description: String,
        objective: String,
        requiredCaps: List<String>,
        optionalCaps: List<String>,
        accessibilityReq: String,
        targetParticipants: Int
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var objective by remember { mutableStateOf("") }
    var requiredText by remember { mutableStateOf("Programming, Research") }
    var optionalText by remember { mutableStateOf("Visual presentation") }
    var accessReq by remember { mutableStateOf("Alt-text visuals & captioned check-ins") }

    val suggestion = (aiSuggestionState as? UiState.Success)?.data
    androidx.compose.runtime.LaunchedEffect(suggestion) {
        if (suggestion != null) {
            if (suggestion.essentialCapabilities.isNotEmpty()) {
                requiredText = suggestion.essentialCapabilities.joinToString(", ")
            }
            if (suggestion.optionalCapabilities.isNotEmpty()) {
                optionalText = suggestion.optionalCapabilities.joinToString(", ")
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Shared Mission") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Mission Title (min 3 chars)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description & problem addressed") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = objective,
                    onValueChange = { objective = it },
                    label = { Text("Intended outcome") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedButton(
                    onClick = { onSuggestWithAi(title, description) },
                    enabled = title.length >= 3 && aiSuggestionState !is UiState.Loading
                ) {
                    Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (aiSuggestionState is UiState.Loading) "Suggesting..." else "Suggest Requirements with AI")
                }
                if (aiSuggestionState is UiState.Error) {
                    Text(aiSuggestionState.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
                OutlinedTextField(
                    value = requiredText,
                    onValueChange = { requiredText = it },
                    label = { Text("Essential capabilities (comma-separated)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = optionalText,
                    onValueChange = { optionalText = it },
                    label = { Text("Optional capabilities") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = accessReq,
                    onValueChange = { accessReq = it },
                    label = { Text("Accessibility requirements") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                enabled = title.trim().length >= 3 && description.trim().length >= 5,
                onClick = {
                    onCreate(
                        title,
                        description,
                        objective,
                        requiredText.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                        optionalText.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                        accessReq,
                        3
                    )
                }
            ) { Text("Publish Mission") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
