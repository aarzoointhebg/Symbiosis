package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.AuthSignInResult
import com.example.data.ExtractedCapabilityTags
import com.example.data.FirebaseAuthService
import com.example.model.AvailabilityWindow
import com.example.model.CollaborationStyle
import com.example.model.CommunityProfile
import com.example.model.ProfileVisibility
import com.example.model.UiState
import com.example.ui.components.AiBadge
import com.example.ui.components.InfoBannerCard
import com.example.ui.components.MascotBadgeAvatar
import com.example.ui.components.SafetyAndDignityNotice
import com.example.ui.components.SymbiosisWordmark
import com.example.ui.components.ThemeModeToggleButton
import com.example.ui.theme.LocalSymbiosisIsDark
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    authService: FirebaseAuthService,
    onGoogleAuthSuccess: () -> Unit,
    onLaunchLocalWorkspace: () -> Unit,
    isDarkMode: Boolean = LocalSymbiosisIsDark.current,
    onToggleTheme: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var googleLoading by remember { mutableStateOf(false) }
    var googleError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 580.dp)
                    .align(Alignment.TopCenter)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Brand Hero + Quick Theme Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        SymbiosisWordmark(showTagline = true)
                    }
                    if (onToggleTheme != null) {
                        Spacer(Modifier.width(8.dp))
                        ThemeModeToggleButton(
                            isDarkMode = isDarkMode,
                            onToggle = onToggleTheme,
                            testTagName = "auth_theme_toggle_button"
                        )
                    }
                }

                Image(
                    painter = painterResource(id = R.drawable.img_symbiosis_hero),
                    contentDescription = "Intertwining botanical ribbons celebrating shared human possibilities",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(22.dp))
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "You don't have to be everything.\nYou just have to bring something.",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Discover people whose abilities, ambitions, and curiosity complement yours — and explore what you can make possible together.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Auth Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "Google Cloud Sign-In",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Sign in with Google via Jetpack Credential Manager to persist your user profile, mission history, bookmarked posts, and collaborations in Cloud Firestore.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (googleError != null) {
                                InfoBannerCard(
                                    title = "Sign-In Notice",
                                    body = googleError!!,
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }

                            Button(
                                onClick = {
                                    googleLoading = true
                                    googleError = null
                                    scope.launch {
                                        when (val res = authService.signInWithGoogle(context)) {
                                            is AuthSignInResult.Success -> {
                                                googleLoading = false
                                                onGoogleAuthSuccess()
                                            }
                                            is AuthSignInResult.Cancelled,
                                            is AuthSignInResult.NoSavedCredential -> {
                                                googleLoading = false
                                            }
                                            is AuthSignInResult.Error -> {
                                                googleLoading = false
                                                googleError = res.message
                                            }
                                        }
                                    }
                                },
                                enabled = !googleLoading,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("google_sign_in_button"),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                if (googleLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(22.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(Icons.Rounded.VerifiedUser, contentDescription = null)
                                    Spacer(Modifier.width(10.dp))
                                    Text("Sign in with Google")
                                }
                            }
                        }

                        HorizontalDivider()

                        // Local Guest Workspace Entry
                        OutlinedButton(
                            onClick = onLaunchLocalWorkspace,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("launch_guest_workspace_button"),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f))
                        ) {
                            Icon(Icons.Rounded.Explore, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Explore Local Workspace")
                        }
                    }
                }

                SafetyAndDignityNotice()
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    initialProfile: CommunityProfile,
    aiTagState: UiState<ExtractedCapabilityTags>?,
    onExtractAiTags: (String) -> Unit,
    onClearAiTags: () -> Unit,
    onSaveAndFinish: (CommunityProfile) -> Unit,
    onSkipWithoutPressure: (CommunityProfile) -> Unit
) {
    var step by remember { mutableIntStateOf(0) }
    val totalSteps = 5

    var displayName by remember { mutableStateOf(initialProfile.displayName) }
    var ambitionText by remember { mutableStateOf(initialProfile.ambitions.firstOrNull() ?: "") }
    var naturalStrengths by remember { mutableStateOf(initialProfile.bio) }
    val selectedSkills = remember { mutableStateListOf<String>().apply { addAll(initialProfile.skills) } }
    val selectedSupport = remember { mutableStateListOf<String>().apply { addAll(initialProfile.supportWanted) } }
    val selectedInterests = remember { mutableStateListOf<String>().apply { addAll(initialProfile.interests) } }
    val selectedAccessPrefs = remember { mutableStateListOf<String>().apply { addAll(initialProfile.accessibilityPrefs) } }
    var collabStyle by remember { mutableStateOf(CollaborationStyle.safeParse(initialProfile.collaborationStyle)) }
    var availability by remember { mutableStateOf(AvailabilityWindow.safeParse(initialProfile.availability)) }
    var customTag by remember { mutableStateOf("") }

    val starterTags = listOf(
        "Programming", "Visual design", "Scientific research", "Data collection",
        "Documentation", "Basic electronics", "Creative communication", "Workshop coordination",
        "Accessibility testing", "Audio engineering", "Community gardening"
    )
    val starterAccess = listOf(
        "Screen-reader structured text", "Alt-text on visuals", "Captioned calls",
        "Clear written milestones", "Step-free venues", "Flexible asynchronous pacing"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 620.dp)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MascotBadgeAvatar(isDuck = step % 2 == 0)
                Column {
                    Text(
                        text = "Welcome to SYMBIOSIS",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Step ${step + 1} of $totalSteps • Go at your own pace",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            TextButton(
                onClick = { onSkipWithoutPressure(initialProfile.copy(onboardingCompleted = true)) },
                modifier = Modifier.testTag("onboarding_skip_button")
            ) {
                Text("Explore First")
            }
        }

        LinearProgressIndicator(
            progress = { (step + 1) / totalSteps.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
        )

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (step) {
                    0 -> {
                        Text(
                            text = "Every person has something to bring.",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "You never have to justify your worth here or possess every skill. Still discovering what you enjoy? You can always leave fields open and explore.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        OutlinedTextField(
                            value = displayName,
                            onValueChange = { displayName = it },
                            label = { Text("How should collaborators greet you?") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("onboarding_name_input"),
                            shape = RoundedCornerShape(14.dp)
                        )
                    }

                    1 -> {
                        Text(
                            text = "What would you love to accomplish?",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Share a curiosity, project idea, or goal — even a rough one.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = ambitionText,
                            onValueChange = { ambitionText = it },
                            label = { Text("My ambition or idea (optional)") },
                            placeholder = { Text("e.g., Build an accessible environmental monitor") },
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )
                    }

                    2 -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "What can you bring?",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            AiBadge("AI Tag Helper")
                        }
                        Text(
                            text = "Describe your strengths or hobbies naturally, and optionally let Gemini suggest structured tags for you to review and approve.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = naturalStrengths,
                            onValueChange = { naturalStrengths = it },
                            label = { Text("Describe what you enjoy or know") },
                            placeholder = { Text("e.g., I enjoy coding Python, solving puzzles, and organizing notes.") },
                            minLines = 3,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )
                        OutlinedButton(
                            onClick = { onExtractAiTags(naturalStrengths) },
                            enabled = naturalStrengths.isNotBlank() && aiTagState !is UiState.Loading,
                            modifier = Modifier.testTag("onboarding_ai_extract_button")
                        ) {
                            Icon(Icons.Rounded.AutoAwesome, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(if (aiTagState is UiState.Loading) "Interpreting..." else "Suggest Tags with Gemini AI")
                        }

                        AnimatedVisibility(visible = aiTagState is UiState.Success) {
                            val tags = (aiTagState as? UiState.Success)?.data
                            if (tags != null) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "AI Suggested Tags (tap to approve):",
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            (tags.skills + tags.knowledgeAreas + tags.practicalAbilities).distinct().forEach { tag ->
                                                val added = selectedSkills.any { it.equals(tag, ignoreCase = true) }
                                                FilterChip(
                                                    selected = added,
                                                    onClick = {
                                                        if (added) selectedSkills.removeAll { it.equals(tag, ignoreCase = true) }
                                                        else selectedSkills.add(tag)
                                                    },
                                                    label = { Text(if (added) "Approved: $tag" else "+ $tag") }
                                                )
                                            }
                                        }
                                        TextButton(onClick = onClearAiTags) { Text("Done reviewing") }
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

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            starterTags.forEach { tag ->
                                val selected = selectedSkills.contains(tag)
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        if (selected) selectedSkills.remove(tag) else selectedSkills.add(tag)
                                    },
                                    label = { Text(tag) }
                                )
                            }
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = customTag,
                                onValueChange = { customTag = it },
                                label = { Text("Add custom capability") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp)
                            )
                            Button(
                                onClick = {
                                    val clean = customTag.trim()
                                    if (clean.isNotEmpty() && !selectedSkills.contains(clean)) {
                                        selectedSkills.add(clean)
                                        customTag = ""
                                    }
                                }
                            ) { Text("Add") }
                        }
                    }

                    3 -> {
                        Text(
                            text = "Where would you appreciate support or like to learn?",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Asking for complementary teammates is how collective possibilities begin.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            starterTags.forEach { tag ->
                                val selected = selectedSupport.contains(tag)
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        if (selected) selectedSupport.remove(tag) else selectedSupport.add(tag)
                                    },
                                    label = { Text(tag) }
                                )
                            }
                        }
                        Text("Preferred collaboration rhythm", style = MaterialTheme.typography.titleSmall)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CollaborationStyle.entries.forEach { style ->
                                FilterChip(
                                    selected = collabStyle == style,
                                    onClick = { collabStyle = style },
                                    label = { Text(style.label) }
                                )
                            }
                        }
                        Text("Availability", style = MaterialTheme.typography.titleSmall)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AvailabilityWindow.entries.forEach { av ->
                                FilterChip(
                                    selected = availability == av,
                                    onClick = { availability = av },
                                    label = { Text(av.label) }
                                )
                            }
                        }
                    }

                    4 -> {
                        Text(
                            text = "Accessibility & Profile Review",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "You define your own preferences. SYMBIOSIS never infers capabilities from disability.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            starterAccess.forEach { pref ->
                                val selected = selectedAccessPrefs.contains(pref)
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        if (selected) selectedAccessPrefs.remove(pref) else selectedAccessPrefs.add(pref)
                                    },
                                    label = { Text(pref) }
                                )
                            }
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("Profile Summary", style = MaterialTheme.typography.titleSmall)
                                Text("Name: ${displayName.ifBlank { "Explorer" }}", style = MaterialTheme.typography.bodySmall)
                                Text("Capabilities: ${selectedSkills.joinToString(", ").ifBlank { "Still discovering" }}", style = MaterialTheme.typography.bodySmall)
                                Text("Appreciates support with: ${selectedSupport.joinToString(", ").ifBlank { "Open to anything" }}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (step > 0) {
                OutlinedButton(
                    onClick = { step -= 1 },
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("Back")
                }
            } else {
                Spacer(Modifier.width(1.dp))
            }

            Button(
                onClick = {
                    if (step < totalSteps - 1) {
                        step += 1
                    } else {
                        val updated = initialProfile.copy(
                            displayName = displayName.trim().ifBlank { "Explorer" },
                            bio = naturalStrengths.trim(),
                            skills = selectedSkills.toList(),
                            supportWanted = selectedSupport.toList(),
                            interests = selectedInterests.toList(),
                            ambitions = if (ambitionText.isNotBlank()) listOf(ambitionText.trim()) else initialProfile.ambitions,
                            collaborationStyle = collabStyle.name,
                            availability = availability.name,
                            accessibilityPrefs = selectedAccessPrefs.toList(),
                            visibility = ProfileVisibility.COMMUNITY.name,
                            onboardingCompleted = true
                        )
                        onSaveAndFinish(updated)
                    }
                },
                modifier = Modifier
                    .height(48.dp)
                    .testTag("onboarding_next_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(if (step < totalSteps - 1) "Continue" else "Approve & Enter SYMBIOSIS")
                Spacer(Modifier.width(6.dp))
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null)
            }
        }
    }
}
