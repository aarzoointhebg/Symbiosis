package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Diversity3
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AccessibilityPreferencesRepository
import com.example.data.AiLogicService
import com.example.data.AuthSignInResult
import com.example.data.CredentialManagerFirebaseAuthService
import com.example.data.DemoSeedData
import com.example.data.FirebaseAuthService
import com.example.data.FirestoreSymbiosisRepository
import com.example.data.FirestoreUserProfileRepository
import com.example.data.LocalDemoSymbiosisRepository
import com.example.data.LocalUserProfileRepository
import com.example.data.PasswordlessEmailOtpService
import com.example.model.AccessibilitySettings
import com.example.model.CapabilityAvailabilityStatus
import com.example.model.CommunityProfile
import com.example.model.UiState
import com.example.model.toUserProfile
import com.example.ui.components.SymbiosisEmblem
import com.example.ui.components.UserProfileForm
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.DiscoverAndIntentScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MatchesScreen
import com.example.ui.screens.MissionsAndCollaborateScreen
import com.example.ui.screens.MySpaceAndCapabilitiesScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PossibilityFinderScreen
import kotlinx.coroutines.launch

private enum class TopNavTab(
    val label: String,
    val icon: ImageVector,
    val tag: String
) {
    HOME("Home", Icons.Rounded.Home, "nav_home"),
    MATCHES("Matches", Icons.Rounded.Diversity3, "nav_matches"),
    POSSIBILITIES("Possibilities", Icons.Rounded.AutoAwesome, "nav_possibilities"),
    MISSIONS("Missions", Icons.Rounded.Groups, "nav_missions"),
    MY_SPACE("My Space", Icons.Rounded.Person, "nav_myspace")
}

private enum class SecondaryRoute {
    MATCHING_PROFILE,
    INTENT_DISCOVERY,
    ONBOARDING
}

private data class LocalGuestSession(
    val userId: String,
    val displayName: String,
    val startInOnboarding: Boolean = false
)

@Composable
fun SymbiosisApp(
    accessibilityRepo: AccessibilityPreferencesRepository,
    accessibilitySettings: AccessibilitySettings
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authService: FirebaseAuthService = remember(context) {
        CredentialManagerFirebaseAuthService(context.applicationContext)
    }
    val otpService = remember { PasswordlessEmailOtpService() }

    val authStateFlow = remember(authService) { authService.observeAuthState() }
    val authenticatedAccount by authStateFlow.collectAsStateWithLifecycle(
        initialValue = authService.currentAccount
    )
    var localGuestSession by remember { mutableStateOf<LocalGuestSession?>(null) }

    // Attempt silent Credential Manager auto sign-in once on launch when signed out
    LaunchedEffect(Unit) {
        if (authService.currentAccount == null) {
            if (authService.attemptAutoSignIn(context) is AuthSignInResult.Success) {
                localGuestSession = null
            }
        }
    }

    when {
        authenticatedAccount != null -> {
            val account = authenticatedAccount!!
            key(account.uid) {
                val vm: SymbiosisViewModel = viewModel(
                    key = "cloud_${account.uid}",
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return SymbiosisViewModel(
                                repository = FirestoreSymbiosisRepository(context.applicationContext, account.uid),
                                aiLogicService = AiLogicService(),
                                currentUserId = account.uid,
                                initialDisplayName = account.displayName
                            ) as T
                        }
                    }
                )
                val userProfileVm: UserProfileViewModel = viewModel(
                    key = "cloud_user_profile_${account.uid}",
                    factory = UserProfileViewModel.provideFactory(
                        repository = FirestoreUserProfileRepository(
                            context = context.applicationContext,
                            currentUserId = account.uid,
                            authService = authService
                        ),
                        currentUserId = account.uid,
                        initialDisplayName = account.displayName,
                        accountEmail = account.email,
                        authService = authService
                    )
                )
                SymbiosisMainShell(
                    viewModel = vm,
                    userProfileViewModel = userProfileVm,
                    accessibilityRepo = accessibilityRepo,
                    accessibilitySettings = accessibilitySettings,
                    startInOnboarding = false,
                    onSignOut = {
                        scope.launch {
                            authService.signOut()
                            localGuestSession = null
                        }
                    },
                    onRequestGoogleSync = {}
                )
            }
        }

        localGuestSession != null -> {
            val guest = localGuestSession!!
            key(guest.userId) {
                val localRepo = remember(guest.userId) {
                    LocalDemoSymbiosisRepository(
                        currentUserId = guest.userId,
                        initialDisplayName = guest.displayName,
                        onboardingDone = !guest.startInOnboarding
                    )
                }
                val vm: SymbiosisViewModel = viewModel(
                    key = "local_${guest.userId}",
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return SymbiosisViewModel(
                                repository = localRepo,
                                aiLogicService = AiLogicService(),
                                currentUserId = guest.userId,
                                initialDisplayName = guest.displayName
                            ) as T
                        }
                    }
                )
                val userProfileVm: UserProfileViewModel = viewModel(
                    key = "local_user_profile_${guest.userId}",
                    factory = UserProfileViewModel.provideFactory(
                        repository = LocalUserProfileRepository(
                            currentUserId = guest.userId,
                            initialDisplayName = guest.displayName,
                            backingRepo = localRepo
                        ),
                        currentUserId = guest.userId,
                        initialDisplayName = guest.displayName
                    )
                )
                SymbiosisMainShell(
                    viewModel = vm,
                    userProfileViewModel = userProfileVm,
                    accessibilityRepo = accessibilityRepo,
                    accessibilitySettings = accessibilitySettings,
                    startInOnboarding = guest.startInOnboarding,
                    onSignOut = { localGuestSession = null },
                    onRequestGoogleSync = {
                        localGuestSession = null
                    }
                )
            }
        }

        else -> {
            AuthScreen(
                authService = authService,
                otpService = otpService,
                onGoogleAuthSuccess = { localGuestSession = null },
                onEmailOtpVerifiedDemoSession = { email, isNewAccount ->
                    val name = email.substringBefore("@").replaceFirstChar { it.uppercase() }
                    localGuestSession = LocalGuestSession(
                        userId = "otp_${email.lowercase().hashCode()}",
                        displayName = name,
                        startInOnboarding = isNewAccount
                    )
                },
                onLaunchLocalWorkspace = {
                    localGuestSession = LocalGuestSession(
                        userId = "local_guest",
                        displayName = "Guest Explorer",
                        startInOnboarding = false
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SymbiosisMainShell(
    viewModel: SymbiosisViewModel,
    userProfileViewModel: UserProfileViewModel,
    accessibilityRepo: AccessibilityPreferencesRepository,
    accessibilitySettings: AccessibilitySettings,
    startInOnboarding: Boolean,
    onSignOut: () -> Unit,
    onRequestGoogleSync: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val statusBanner by viewModel.statusBanner.collectAsStateWithLifecycle()
    val ownProfileState by viewModel.ownProfileState.collectAsStateWithLifecycle()
    val userProfileState by userProfileViewModel.profileState.collectAsStateWithLifecycle()
    val isProfileSaving by userProfileViewModel.isSaving.collectAsStateWithLifecycle()
    val profileSaveError by userProfileViewModel.saveError.collectAsStateWithLifecycle()
    val profileSaveSuccess by userProfileViewModel.saveSuccessMessage.collectAsStateWithLifecycle()

    val discoverableProfiles by viewModel.allDiscoverableProfiles.collectAsStateWithLifecycle()
    val ambitionsState by viewModel.ambitionsState.collectAsStateWithLifecycle()
    val allMissions by viewModel.allMissions.collectAsStateWithLifecycle()
    val invitations by viewModel.invitations.collectAsStateWithLifecycle()
    val savedPossibilities by viewModel.savedPossibilities.collectAsStateWithLifecycle()
    val safetyBlocks by viewModel.safetyBlocks.collectAsStateWithLifecycle()
    val privateSettings by viewModel.privateSettings.collectAsStateWithLifecycle()
    val endorsementSummaryByUser by viewModel.endorsementSummaryByUser.collectAsStateWithLifecycle()
    val messagesByPeerId by viewModel.messagesByPeerId.collectAsStateWithLifecycle()

    val selectedMode by viewModel.selectedMatchingMode.collectAsStateWithLifecycle()
    val matchesFeedState by viewModel.matchesFeedState.collectAsStateWithLifecycle()
    val matchesSearchQuery by viewModel.matchesSearchQuery.collectAsStateWithLifecycle()
    val selectedInterestCategory by viewModel.selectedInterestCategory.collectAsStateWithLifecycle()
    val firestoreOnlyMatches by viewModel.firestoreOnlyMatches.collectAsStateWithLifecycle()
    val individualMatches by viewModel.individualMatches.collectAsStateWithLifecycle()
    val collectiveRecommendations by viewModel.collectiveRecommendations.collectAsStateWithLifecycle()
    val intentSearchState by viewModel.intentSearchState.collectAsStateWithLifecycle()
    val currentInterpretedIntent by viewModel.currentInterpretedIntent.collectAsStateWithLifecycle()
    val aiTagState by viewModel.aiTagSuggestionState.collectAsStateWithLifecycle()
    val aiMissionSuggestion by viewModel.aiMissionSuggestionState.collectAsStateWithLifecycle()
    val possibilityState by viewModel.possibilityState.collectAsStateWithLifecycle()
    val activeMilestones by viewModel.activeMilestones.collectAsStateWithLifecycle()

    val ownProfile = (ownProfileState as? UiState.Success)?.data
        ?: DemoSeedData.defaultUserProfile(viewModel.currentUserId, viewModel.initialDisplayName)
    val currentUserProfile = (userProfileState as? UiState.Success)?.data
        ?: ownProfile.toUserProfile()
    val ambitions = (ambitionsState as? UiState.Success)?.data.orEmpty()

    LaunchedEffect(currentUserProfile) {
        viewModel.syncActiveUserProfile(currentUserProfile)
    }

    var currentTab by remember { mutableStateOf(TopNavTab.HOME) }
    var missionsInitialTab by remember { mutableIntStateOf(0) }
    var mySpaceInitialTab by remember { mutableIntStateOf(3) }
    var secondaryRoute by remember {
        mutableStateOf(
            if (startInOnboarding) SecondaryRoute.ONBOARDING else null
        )
    }

    LaunchedEffect(statusBanner) {
        val msg = statusBanner
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusBanner()
        }
    }

    LaunchedEffect(profileSaveSuccess, profileSaveError) {
        val msg = profileSaveSuccess ?: profileSaveError
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            userProfileViewModel.clearMessages()
        }
    }

    if (secondaryRoute != null) {
        BackHandler {
            secondaryRoute = null
        }
    }

    val expressMissionInterest: (com.example.model.Mission) -> Unit = { mission ->
        if (mission.ownerId == viewModel.currentUserId || mission.ownerId.isBlank()) {
            missionsInitialTab = 0
            currentTab = TopNavTab.MISSIONS
        } else {
            val ownerProfile = discoverableProfiles.firstOrNull { it.userId == mission.ownerId }
                ?: CommunityProfile(userId = mission.ownerId, displayName = mission.ownerName.ifBlank { "Mission Lead" })
            viewModel.sendCollaborationInvitation(
                recipient = ownerProfile,
                missionId = mission.id,
                missionTitle = mission.title,
                proposedRole = "Complementary Collaborator",
                rationale = "Shared interest in mission objective: ${mission.objective.ifBlank { mission.title }}"
            )
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            contentWindowInsets = WindowInsets.navigationBars,
            topBar = {
                if (secondaryRoute != null) {
                    TopAppBar(
                        title = {
                            Text(
                                text = when (secondaryRoute) {
                                    SecondaryRoute.MATCHING_PROFILE -> "Matching Profile"
                                    SecondaryRoute.INTENT_DISCOVERY -> "Discover & Intent Search"
                                    SecondaryRoute.ONBOARDING -> "Guided Setup"
                                    null -> "SYMBIOSIS"
                                }
                            )
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = { secondaryRoute = null },
                                modifier = Modifier.testTag("secondary_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background
                        )
                    )
                }
            },
            bottomBar = {
                if (!useRail && secondaryRoute == null) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        TopNavTab.entries.forEach { tab ->
                            NavigationBarItem(
                                selected = currentTab == tab,
                                onClick = {
                                    if (tab == TopNavTab.MISSIONS) missionsInitialTab = 0
                                    if (tab == TopNavTab.MY_SPACE) mySpaceInitialTab = 3
                                    currentTab = tab
                                },
                                icon = { Icon(tab.icon, contentDescription = tab.label) },
                                label = { Text(tab.label, maxLines = 1) },
                                modifier = Modifier.testTag(tab.tag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useRail && secondaryRoute == null) {
                    NavigationRail(
                        modifier = Modifier.fillMaxHeight(),
                        containerColor = MaterialTheme.colorScheme.surface,
                        header = {
                            SymbiosisEmblem(size = 36.dp, modifier = Modifier.padding(top = 12.dp, bottom = 8.dp))
                        }
                    ) {
                        TopNavTab.entries.forEach { tab ->
                            NavigationRailItem(
                                selected = currentTab == tab,
                                onClick = {
                                    if (tab == TopNavTab.MISSIONS) missionsInitialTab = 0
                                    if (tab == TopNavTab.MY_SPACE) mySpaceInitialTab = 3
                                    currentTab = tab
                                },
                                icon = { Icon(tab.icon, contentDescription = tab.label) },
                                label = { Text(tab.label) },
                                modifier = Modifier.testTag(tab.tag)
                            )
                        }
                    }
                    Spacer(Modifier.width(4.dp))
                }

                Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                    when (secondaryRoute) {
                        SecondaryRoute.MATCHING_PROFILE -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                UserProfileForm(
                                    initialProfile = currentUserProfile,
                                    isSaving = isProfileSaving,
                                    onSaveProfile = { updatedUserProfile ->
                                        userProfileViewModel.saveProfile(updatedUserProfile) {
                                            secondaryRoute = null
                                        }
                                    },
                                    onCancel = { secondaryRoute = null },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .widthIn(max = 720.dp)
                                )
                            }
                        }

                        SecondaryRoute.INTENT_DISCOVERY -> {
                            DiscoverAndIntentScreen(
                                selectedMode = selectedMode,
                                individualMatches = individualMatches,
                                collectiveRecommendations = collectiveRecommendations,
                                allMissions = allMissions,
                                intentSearchState = intentSearchState,
                                currentInterpretedIntent = currentInterpretedIntent,
                                onSelectMode = viewModel::selectMatchingMode,
                                onRunIntentSearch = viewModel::runIntentSearch,
                                onSendInvitation = viewModel::sendCollaborationInvitation,
                                onExpressMissionInterest = expressMissionInterest,
                                onBlockUser = viewModel::blockUser
                            )
                        }

                        SecondaryRoute.ONBOARDING -> {
                            OnboardingScreen(
                                initialProfile = ownProfile,
                                aiTagState = aiTagState,
                                onExtractAiTags = viewModel::extractCapabilitiesWithAi,
                                onClearAiTags = viewModel::clearAiTagSuggestion,
                                onSaveAndFinish = { updated ->
                                    viewModel.saveProfile(updated) {
                                        userProfileViewModel.saveProfile(updated.toUserProfile())
                                        secondaryRoute = null
                                    }
                                },
                                onSkipWithoutPressure = { skipped ->
                                    viewModel.saveProfile(skipped) {
                                        secondaryRoute = null
                                    }
                                }
                            )
                        }

                        null -> {
                            when (currentTab) {
                                TopNavTab.HOME -> {
                                    HomeScreen(
                                        userName = currentUserProfile.displayName.ifBlank { ownProfile.displayName },
                                        isCloudSynced = viewModel.isCloudBacked,
                                        featuredTeamPreview = collectiveRecommendations.firstOrNull(),
                                        onDiscoverPossibilities = { currentTab = TopNavTab.POSSIBILITIES },
                                        onOpenMatchingProfileForm = {
                                            userProfileViewModel.loadProfile()
                                            secondaryRoute = SecondaryRoute.MATCHING_PROFILE
                                        },
                                        onSearchMatches = { query ->
                                            viewModel.updateMatchesSearchQuery(query)
                                            currentTab = TopNavTab.MATCHES
                                        },
                                        onOpenDiscover = { secondaryRoute = SecondaryRoute.INTENT_DISCOVERY },
                                        onOpenAmbitionMap = {
                                            missionsInitialTab = 2
                                            currentTab = TopNavTab.MISSIONS
                                        },
                                        onStartOnboarding = { secondaryRoute = SecondaryRoute.ONBOARDING }
                                    )
                                }

                                TopNavTab.MATCHES -> {
                                    MatchesScreen(
                                        matchesFeedState = matchesFeedState,
                                        currentUserProfile = currentUserProfile,
                                        selectedMode = selectedMode,
                                        searchQuery = matchesSearchQuery,
                                        selectedInterestCategory = selectedInterestCategory,
                                        firestoreOnly = firestoreOnlyMatches,
                                        isCloudSynced = viewModel.isCloudBacked,
                                        allMissions = allMissions,
                                        endorsementSummaryByUser = endorsementSummaryByUser,
                                        messagesByPeerId = messagesByPeerId,
                                        onSearchQueryChange = viewModel::updateMatchesSearchQuery,
                                        onSelectInterestCategory = viewModel::selectInterestCategory,
                                        onSelectMode = viewModel::selectMatchingMode,
                                        onToggleFirestoreOnly = viewModel::setFirestoreOnlyMatches,
                                        onClearFilters = viewModel::clearMatchesFilters,
                                        onEditMatchingProfile = {
                                            userProfileViewModel.loadProfile()
                                            secondaryRoute = SecondaryRoute.MATCHING_PROFILE
                                        },
                                        onOpenIntentDiscovery = {
                                            secondaryRoute = SecondaryRoute.INTENT_DISCOVERY
                                        },
                                        onToggleSkillEndorsement = viewModel::toggleSkillEndorsement,
                                        onSendCollaborationMessage = viewModel::sendCollaborationMessage,
                                        onSendInvitation = viewModel::sendCollaborationInvitation,
                                        onBlockUser = viewModel::blockUser
                                    )
                                }

                                TopNavTab.POSSIBILITIES -> {
                                    PossibilityFinderScreen(
                                        possibilityState = possibilityState,
                                        onAnalyze = viewModel::analyzePossibility,
                                        onSavePossibility = viewModel::savePossibilityAnalysis,
                                        onInviteCollaborator = { peer, role, rationale ->
                                            val analysis = (possibilityState as? UiState.Success)?.data
                                            viewModel.sendCollaborationInvitation(
                                                recipient = peer,
                                                missionId = DemoSeedData.featuredCommunityMission.id,
                                                missionTitle = analysis?.objective?.take(90)
                                                    ?: DemoSeedData.featuredCommunityMission.title,
                                                proposedRole = role,
                                                rationale = rationale
                                            )
                                        },
                                        onTurnIntoMission = { analysis ->
                                            val required = analysis.capabilityBreakdown
                                                .filter { it.status != CapabilityAvailabilityStatus.OPTIONAL }
                                                .map { it.name }
                                            val optional = analysis.capabilityBreakdown
                                                .filter { it.status == CapabilityAvailabilityStatus.OPTIONAL }
                                                .map { it.name }
                                            viewModel.createMission(
                                                title = analysis.objective.take(90),
                                                description = analysis.suggestedPlan.joinToString(" • ").ifBlank {
                                                    "Complementary team formed from Possibility Finder."
                                                },
                                                objective = analysis.objective,
                                                requiredCaps = required,
                                                optionalCaps = optional,
                                                accessibilityReq = "Inclusive & Accessible",
                                                targetParticipants = 3,
                                                onCreated = {
                                                    missionsInitialTab = 0
                                                    currentTab = TopNavTab.MISSIONS
                                                }
                                            )
                                        }
                                    )
                                }

                                TopNavTab.MISSIONS -> {
                                    MissionsAndCollaborateScreen(
                                        initialTab = missionsInitialTab,
                                        currentUserId = viewModel.currentUserId,
                                        missions = allMissions,
                                        invitations = invitations,
                                        milestones = activeMilestones,
                                        profiles = discoverableProfiles,
                                        ambitions = ambitions,
                                        aiMissionSuggestion = aiMissionSuggestion,
                                        onCreateMission = viewModel::createMission,
                                        onSuggestRequirementsAi = viewModel::suggestMissionRequirementsWithAi,
                                        onClearMissionSuggestion = viewModel::clearMissionSuggestion,
                                        onRespondInvitation = viewModel::respondToInvitation,
                                        onToggleMilestone = viewModel::toggleDemoMilestone,
                                        onUpdateMissionProgress = viewModel::updateMissionProgress,
                                        onExpressMissionInterest = expressMissionInterest
                                    )
                                }

                                TopNavTab.MY_SPACE -> {
                                    MySpaceAndCapabilitiesScreen(
                                        initialTab = mySpaceInitialTab,
                                        ownProfile = ownProfile,
                                        userProfile = currentUserProfile,
                                        isProfileSaving = isProfileSaving,
                                        accountEmail = userProfileViewModel.accountEmail,
                                        ambitions = ambitions,
                                        savedPossibilities = savedPossibilities,
                                        invitations = invitations,
                                        privateSettings = privateSettings,
                                        safetyBlocks = safetyBlocks,
                                        accessibilitySettings = accessibilitySettings,
                                        aiTagState = aiTagState,
                                        ownEndorsementsBySkill = endorsementSummaryByUser[viewModel.currentUserId].orEmpty(),
                                        isCloudSynced = userProfileViewModel.isCloudBacked,
                                        onSaveUserProfile = { updatedUserProfile ->
                                            userProfileViewModel.saveProfile(updatedUserProfile)
                                        },
                                        onSaveProfile = { updated -> viewModel.saveProfile(updated) },
                                        onExtractAiTags = viewModel::extractCapabilitiesWithAi,
                                        onClearAiTags = viewModel::clearAiTagSuggestion,
                                        onSaveAmbition = viewModel::saveAmbition,
                                        onDeleteAmbition = viewModel::deleteAmbition,
                                        onSavePrivateSettings = viewModel::savePrivateSettings,
                                        onUnblockUser = viewModel::unblockUser,
                                        onSetTextScale = { scale ->
                                            scope.launch { accessibilityRepo.setTextScale(scale) }
                                        },
                                        onSetHighContrast = { enabled ->
                                            scope.launch { accessibilityRepo.setHighContrast(enabled) }
                                        },
                                        onSetReducedMotion = { enabled ->
                                            scope.launch { accessibilityRepo.setReducedMotion(enabled) }
                                        },
                                        onSetPreferredComms = { comms ->
                                            scope.launch { accessibilityRepo.setPreferredComms(comms) }
                                        },
                                        onSignOut = onSignOut,
                                        onSwitchToGoogleSignIn = onRequestGoogleSync,
                                        onStartOnboarding = { secondaryRoute = SecondaryRoute.ONBOARDING }
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
