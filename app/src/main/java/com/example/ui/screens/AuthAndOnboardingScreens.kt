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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.KeyOff
import androidx.compose.material.icons.rounded.LockOpen
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.AuthSignInResult
import com.example.data.ExtractedCapabilityTags
import com.example.data.FirebaseAuthService
import com.example.data.OtpDispatchOutcome
import com.example.data.OtpVerifyOutcome
import com.example.data.PasswordlessEmailOtpService
import com.example.model.AvailabilityWindow
import com.example.model.CollaborationStyle
import com.example.model.CommunityProfile
import com.example.model.OtpAuthUiState
import com.example.model.OtpSessionStep
import com.example.model.ProfileVisibility
import com.example.model.UiState
import com.example.ui.components.AiBadge
import com.example.ui.components.InfoBannerCard
import com.example.ui.components.MascotBadgeAvatar
import com.example.ui.components.SafetyAndDignityNotice
import com.example.ui.components.SymbiosisWordmark
import com.example.ui.theme.SoftCream
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    authService: FirebaseAuthService,
    otpService: PasswordlessEmailOtpService,
    onGoogleAuthSuccess: () -> Unit,
    onEmailOtpVerifiedDemoSession: (email: String, isNewAccount: Boolean) -> Unit,
    onLaunchLocalWorkspace: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Google Cloud Sign-In, 1 = Passwordless Email OTP
    var googleLoading by remember { mutableStateOf(false) }
    var googleError by remember { mutableStateOf<String?>(null) }

    var otpState by remember { mutableStateOf(OtpAuthUiState()) }

    // Cooldown & expiry countdown ticker for OTP
    LaunchedEffect(otpState.step, otpState.resendCooldownSeconds, otpState.expiresInSeconds) {
        if (otpState.step == OtpSessionStep.VERIFY_CODE && (otpState.resendCooldownSeconds > 0 || otpState.expiresInSeconds > 0)) {
            delay(1000L)
            otpState = otpState.copy(
                resendCooldownSeconds = (otpState.resendCooldownSeconds - 1).coerceAtLeast(0),
                expiresInSeconds = (otpState.expiresInSeconds - 1).coerceAtLeast(0)
            )
        }
    }

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
                // Brand Hero
                SymbiosisWordmark(showTagline = true)

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
                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.primary
                        ) {
                            Tab(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                modifier = Modifier.testTag("tab_google_signin"),
                                text = { Text("Google Cloud Sign-In") }
                            )
                            Tab(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                modifier = Modifier.testTag("tab_email_otp"),
                                text = { Text("Passwordless Email") }
                            )
                        }

                        if (selectedTab == 0) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = "Cloud Sync & Gemini AI Logic",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Sign in with Google via Jetpack Credential Manager to persist your capability profile, shared missions, and consent-based invitations in Cloud Firestore.",
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
                        } else {
                            // Part 11: Complete Passwordless Email Registration & Authentication
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.KeyOff,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Passwordless Email Verification",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                InfoBannerCard(
                                    title = if (otpService.isLiveEmailConfigured()) "Transactional Email Ready" else "Development Demonstration Mode",
                                    body = otpService.gatewayNotice(),
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                                )

                                when (otpState.step) {
                                    OtpSessionStep.ENTER_EMAIL -> {
                                        OutlinedTextField(
                                            value = otpState.email,
                                            onValueChange = { otpState = otpState.copy(email = it, errorMessage = null) },
                                            label = { Text("Email address (no password needed)") },
                                            placeholder = { Text("you@domain.com") },
                                            leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null) },
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("otp_email_input"),
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        if (otpState.errorMessage != null) {
                                            Text(
                                                text = otpState.errorMessage!!,
                                                color = MaterialTheme.colorScheme.error,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                        Button(
                                            onClick = {
                                                scope.launch {
                                                    otpState = otpState.copy(isSubmitting = true, errorMessage = null)
                                                    when (val res = otpService.requestOtp(otpState.email)) {
                                                        is OtpDispatchOutcome.Dispatched -> {
                                                            otpState = otpState.copy(
                                                                isSubmitting = false,
                                                                step = OtpSessionStep.VERIFY_CODE,
                                                                accountExists = res.accountExists,
                                                                statusBanner = res.statusMessage,
                                                                resendCooldownSeconds = res.cooldownSeconds,
                                                                expiresInSeconds = res.expiresInSeconds,
                                                                remainingAttempts = PasswordlessEmailOtpService.MAX_ATTEMPTS,
                                                                devDemoPreviewHint = res.debugDemoHint,
                                                                codeInput = ""
                                                            )
                                                        }
                                                        is OtpDispatchOutcome.Rejected -> {
                                                            otpState = otpState.copy(
                                                                isSubmitting = false,
                                                                errorMessage = res.reason
                                                            )
                                                        }
                                                    }
                                                }
                                            },
                                            enabled = !otpState.isSubmitting && otpState.email.isNotBlank(),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(50.dp)
                                                .testTag("send_otp_button"),
                                            shape = RoundedCornerShape(14.dp)
                                        ) {
                                            Text("Send 6-Digit Verification Code")
                                        }
                                    }

                                    OtpSessionStep.VERIFY_CODE -> {
                                        if (otpState.accountExists == true) {
                                            Text(
                                                text = "Existing account recognized — signing you in without a password.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        } else {
                                            Text(
                                                text = "New email — verifying will create your account and start welcoming onboarding.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        }

                                        if (otpState.statusBanner != null) {
                                            Text(
                                                text = otpState.statusBanner!!,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        // Debug-only demo helper (never rendered in release builds)
                                        if (otpState.devDemoPreviewHint != null) {
                                            Surface(
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(10.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "Dev Simulator Only (no email server): Test code ready",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    TextButton(
                                                        onClick = {
                                                            otpState = otpState.copy(codeInput = otpState.devDemoPreviewHint!!)
                                                        },
                                                        modifier = Modifier.testTag("autofill_dev_otp_button")
                                                    ) {
                                                        Text("Fill Test Code")
                                                    }
                                                }
                                            }
                                        }

                                        OutlinedTextField(
                                            value = otpState.codeInput,
                                            onValueChange = { v ->
                                                val digits = v.filter { it.isDigit() }.take(6)
                                                otpState = otpState.copy(codeInput = digits, errorMessage = null)
                                            },
                                            label = { Text("6-digit verification code") },
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("otp_code_input"),
                                            shape = RoundedCornerShape(14.dp)
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Expires in ${otpState.expiresInSeconds / 60}m ${otpState.expiresInSeconds % 60}s",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "${otpState.remainingAttempts} attempts left",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        if (otpState.errorMessage != null) {
                                            Text(
                                                text = otpState.errorMessage!!,
                                                color = MaterialTheme.colorScheme.error,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }

                                        Button(
                                            onClick = {
                                                when (val outcome = otpService.verifyOtp(otpState.email, otpState.codeInput)) {
                                                    is OtpVerifyOutcome.Verified -> {
                                                        otpState = otpState.copy(
                                                            step = OtpSessionStep.VERIFIED,
                                                            errorMessage = null
                                                        )
                                                        onEmailOtpVerifiedDemoSession(outcome.normalizedEmail, outcome.isNewAccount)
                                                    }
                                                    is OtpVerifyOutcome.Failed -> {
                                                        otpState = otpState.copy(
                                                            errorMessage = outcome.reason,
                                                            remainingAttempts = outcome.remainingAttempts,
                                                            isLockedOut = outcome.lockedOut
                                                        )
                                                    }
                                                }
                                            },
                                            enabled = otpState.codeInput.length == 6 && !otpState.isLockedOut,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(50.dp)
                                                .testTag("verify_otp_button"),
                                            shape = RoundedCornerShape(14.dp)
                                        ) {
                                            Icon(Icons.Rounded.LockOpen, contentDescription = null)
                                            Spacer(Modifier.width(8.dp))
                                            Text("Verify & Continue")
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            TextButton(
                                                onClick = {
                                                    otpState = OtpAuthUiState(email = otpState.email)
                                                }
                                            ) {
                                                Text("Change Email")
                                            }
                                            TextButton(
                                                enabled = otpState.resendCooldownSeconds == 0,
                                                onClick = {
                                                    scope.launch {
                                                        when (val res = otpService.requestOtp(otpState.email)) {
                                                            is OtpDispatchOutcome.Dispatched -> {
                                                                otpState = otpState.copy(
                                                                    statusBanner = res.statusMessage,
                                                                    resendCooldownSeconds = res.cooldownSeconds,
                                                                    expiresInSeconds = res.expiresInSeconds,
                                                                    remainingAttempts = PasswordlessEmailOtpService.MAX_ATTEMPTS,
                                                                    devDemoPreviewHint = res.debugDemoHint,
                                                                    errorMessage = null
                                                                )
                                                            }
                                                            is OtpDispatchOutcome.Rejected -> {
                                                                otpState = otpState.copy(errorMessage = res.reason)
                                                            }
                                                        }
                                                    }
                                                }
                                            ) {
                                                Text(
                                                    if (otpState.resendCooldownSeconds > 0)
                                                        "Resend in ${otpState.resendCooldownSeconds}s"
                                                    else "Resend Code"
                                                )
                                            }
                                        }
                                    }

                                    OtpSessionStep.VERIFIED -> {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                            Text("Email verified! Opening SYMBIOSIS...")
                                        }
                                    }
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
