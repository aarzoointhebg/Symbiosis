package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AiLogicService
import com.example.data.DemoSeedData
import com.example.data.ExtractedCapabilityTags
import com.example.data.MissionRequirementSuggestion
import com.example.data.SymbiosisRepository
import com.example.model.toUserProfile
import com.example.engine.ComplementarityEngine
import com.example.engine.IntentDiscoveryEngine
import com.example.engine.PossibilityFinderEngine
import com.example.model.CollaborationInvitation
import com.example.model.CollaborationMessage
import com.example.model.CollectiveTeamRecommendation
import com.example.model.CommunityProfile
import com.example.model.IndividualMatchResult
import com.example.model.IntentSearchResult
import com.example.model.InvitationStatus
import com.example.model.MatchingMode
import com.example.model.Mission
import com.example.model.MissionMilestone
import com.example.model.MissionStatus
import com.example.model.PossibilityAnalysis
import com.example.model.PrivateSettings
import com.example.model.SafetyBlock
import com.example.model.SavedPossibility
import com.example.model.SkillEndorsement
import com.example.model.SkillEndorsementSummary
import com.example.model.StructuredIntent
import com.example.model.UiState
import com.example.model.UserAmbition
import com.example.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SymbiosisViewModel(
    private val repository: SymbiosisRepository,
    private val aiLogicService: AiLogicService,
    val currentUserId: String,
    val initialDisplayName: String
) : ViewModel() {

    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    val isCloudBacked: Boolean get() = repository.isCloudBacked

    private val _activeUserProfile = MutableStateFlow<UserProfile?>(null)

    private val _matchesSearchQuery = MutableStateFlow("")
    val matchesSearchQuery: StateFlow<String> = _matchesSearchQuery.asStateFlow()

    private val _selectedInterestCategory = MutableStateFlow<String?>(null)
    val selectedInterestCategory: StateFlow<String?> = _selectedInterestCategory.asStateFlow()

    private val _firestoreOnlyMatches = MutableStateFlow(false)
    val firestoreOnlyMatches: StateFlow<Boolean> = _firestoreOnlyMatches.asStateFlow()

    val ownProfileState: StateFlow<UiState<CommunityProfile>> = repository.observeOwnProfile()
        .map<CommunityProfile?, UiState<CommunityProfile>> { profile ->
            UiState.Success(profile ?: DemoSeedData.defaultUserProfile(currentUserId, initialDisplayName))
        }
        .catch { error -> emit(unwrapFlowError(error, "Failed to load profile")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val firestorePeersState: StateFlow<UiState<List<CommunityProfile>>> = repository.observeCommunityProfiles()
        .map<List<CommunityProfile>, UiState<List<CommunityProfile>>> { list ->
            UiState.Success(list.filter { it.userId != currentUserId })
        }
        .catch { error -> emit(unwrapFlowError(error, "Failed to load profiles from Firestore")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    private val cloudCommunityProfiles: StateFlow<List<CommunityProfile>> = firestorePeersState
        .map { state -> (state as? UiState.Success)?.data.orEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val safetyBlocks: StateFlow<List<SafetyBlock>> = repository.observeSafetyBlocks()
        .catch { error ->
            unwrapFlowError<List<SafetyBlock>>(error, "Failed to load safety settings")
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    // Combine live Firestore profiles with curated community profiles (deduplicated & respecting blocks)
    val allDiscoverableProfiles: StateFlow<List<CommunityProfile>> = combine(
        cloudCommunityProfiles,
        safetyBlocks
    ) { cloud, blocks ->
        val blockedSet = blocks.map { it.blockedUserId }.toSet()
        (cloud + DemoSeedData.communityCollaborators)
            .distinctBy { it.userId }
            .filter { it.userId != currentUserId && it.userId !in blockedSet }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), DemoSeedData.communityCollaborators)

    val ambitionsState: StateFlow<UiState<List<UserAmbition>>> = repository.observeAmbitions()
        .map<List<UserAmbition>, UiState<List<UserAmbition>>> { UiState.Success(it) }
        .catch { error -> emit(unwrapFlowError(error, "Failed to load ambitions")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    private val cloudMissions: StateFlow<List<Mission>> = repository.observeCommunityMissions()
        .catch { error ->
            unwrapFlowError<List<Mission>>(error, "Failed to load missions")
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val allMissions: StateFlow<List<Mission>> = cloudMissions.map { cloud ->
        (cloud + DemoSeedData.intentDiscoveryMissions).distinctBy { it.id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), DemoSeedData.intentDiscoveryMissions)

    val invitations: StateFlow<List<CollaborationInvitation>> = repository.observeInvitations()
        .catch { error ->
            unwrapFlowError<List<CollaborationInvitation>>(error, "Failed to load invitations")
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val savedPossibilities: StateFlow<List<SavedPossibility>> = repository.observeSavedPossibilities()
        .catch { error ->
            unwrapFlowError<List<SavedPossibility>>(error, "Failed to load saved possibilities")
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val privateSettings: StateFlow<PrivateSettings> = repository.observePrivateSettings()
        .map { it ?: PrivateSettings(userId = currentUserId) }
        .catch { error ->
            unwrapFlowError<PrivateSettings>(error, "Failed to load privacy settings")
            emit(PrivateSettings(userId = currentUserId))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), PrivateSettings(userId = currentUserId))

    val skillEndorsements: StateFlow<List<SkillEndorsement>> = repository.observeSkillEndorsements()
        .catch { error ->
            unwrapFlowError<List<SkillEndorsement>>(error, "Failed to load skill endorsements")
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val endorsementSummaryByUser: StateFlow<Map<String, Map<String, SkillEndorsementSummary>>> =
        skillEndorsements.map { list ->
            list.groupBy { it.recipientId }.mapValues { (_, userEndorsements) ->
                userEndorsements.groupBy { it.normalizedSkill.ifBlank { SkillEndorsement.normalizeSkillKey(it.skillName) } }
                    .mapValues { (skillKey, skillList) ->
                        SkillEndorsementSummary(
                            skillKey = skillKey,
                            count = skillList.size,
                            endorsedByCurrentUser = skillList.any { it.endorserId == currentUserId },
                            endorserNames = skillList.mapNotNull { it.endorserName.takeIf { n -> n.isNotBlank() } }.distinct()
                        )
                    }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyMap())

    val collaborationMessages: StateFlow<List<CollaborationMessage>> = repository.observeCollaborationMessages()
        .catch { error ->
            unwrapFlowError<List<CollaborationMessage>>(error, "Failed to load collaboration messages")
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val messagesByPeerId: StateFlow<Map<String, List<CollaborationMessage>>> =
        collaborationMessages.map { list ->
            list.groupBy { msg ->
                if (msg.senderId == currentUserId) msg.recipientId else msg.senderId
            }.mapValues { (_, thread) ->
                thread.sortedBy { it.createdAt?.toDate()?.time ?: Long.MAX_VALUE }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyMap())

    // Matching & Discovery States
    private val _selectedMatchingMode = MutableStateFlow(MatchingMode.CAPABILITY)
    val selectedMatchingMode: StateFlow<MatchingMode> = _selectedMatchingMode.asStateFlow()

    private val effectiveSeekerProfile: StateFlow<CommunityProfile> = combine(
        ownProfileState,
        _activeUserProfile
    ) { ownState, activeUserProfile ->
        val base = (ownState as? UiState.Success)?.data
            ?: DemoSeedData.defaultUserProfile(currentUserId, initialDisplayName)
        activeUserProfile?.toCommunityProfile(existing = base) ?: base
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000L),
        DemoSeedData.defaultUserProfile(currentUserId, initialDisplayName)
    )

    val individualMatches: StateFlow<List<IndividualMatchResult>> = combine(
        effectiveSeekerProfile,
        allDiscoverableProfiles,
        _selectedMatchingMode,
        safetyBlocks
    ) { me, peers, mode, blocks ->
        ComplementarityEngine.matchIndividuals(
            seeker = me,
            candidates = peers,
            mode = mode,
            blockedIds = blocks.map { it.blockedUserId }.toSet()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val matchesFeedState: StateFlow<UiState<MatchesFeed>> = combine(
        effectiveSeekerProfile,
        firestorePeersState,
        safetyBlocks,
        _selectedMatchingMode,
        combine(_matchesSearchQuery, _selectedInterestCategory, _firestoreOnlyMatches) { q, cat, cloudOnly ->
            Triple(q, cat, cloudOnly)
        }
    ) { me, peersState, blocks, mode, filterTriple ->
        val (query, selectedInterest, firestoreOnly) = filterTriple
        val blockedSet = blocks.map { it.blockedUserId }.toSet()

        when (peersState) {
            is UiState.Loading -> UiState.Loading
            is UiState.Error -> UiState.Error(peersState.message)
            is UiState.Success -> {
                val firestorePeers = peersState.data
                    .filter { it.userId != currentUserId && it.userId !in blockedSet }
                val firestoreIds = firestorePeers.map { it.userId }.toSet()

                val candidatePool = if (isCloudBacked && firestoreOnly) {
                    firestorePeers
                } else {
                    (firestorePeers + DemoSeedData.communityCollaborators)
                        .distinctBy { it.userId }
                        .filter { it.userId != currentUserId && it.userId !in blockedSet }
                }

                val matchMode = if (mode == MatchingMode.COLLECTIVE) MatchingMode.CAPABILITY else mode
                val ranked = ComplementarityEngine.matchIndividuals(
                    seeker = me,
                    candidates = candidatePool,
                    mode = matchMode,
                    blockedIds = blockedSet
                )

                val interestCategories = candidatePool
                    .flatMap { it.interests }
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .distinctBy { it.lowercase() }
                    .sorted()

                val trimmedQuery = query.trim()
                val filteredMatches = ranked.filter { item ->
                    val profile = item.profile
                    val matchesInterest = selectedInterest.isNullOrBlank() ||
                        profile.interests.any {
                            it.equals(selectedInterest, ignoreCase = true) ||
                                it.contains(selectedInterest, ignoreCase = true)
                        } ||
                        item.sharedAmbitions.any { it.contains(selectedInterest, ignoreCase = true) }

                    val matchesKeyword = trimmedQuery.isBlank() ||
                        profile.displayName.contains(trimmedQuery, ignoreCase = true) ||
                        profile.headline.contains(trimmedQuery, ignoreCase = true) ||
                        profile.bio.contains(trimmedQuery, ignoreCase = true) ||
                        profile.skills.any {
                            it.contains(trimmedQuery, ignoreCase = true) ||
                                ComplementarityEngine.capabilitiesMatch(it, trimmedQuery)
                        } ||
                        profile.allCapabilities().any {
                            it.contains(trimmedQuery, ignoreCase = true) ||
                                ComplementarityEngine.capabilitiesMatch(it, trimmedQuery)
                        } ||
                        profile.interests.any { it.contains(trimmedQuery, ignoreCase = true) } ||
                        profile.ambitions.any { it.contains(trimmedQuery, ignoreCase = true) } ||
                        item.complementaryCapabilities.any { it.contains(trimmedQuery, ignoreCase = true) }

                    matchesInterest && matchesKeyword
                }.sortedWith(
                    compareByDescending<IndividualMatchResult> { it.profile.userId in firestoreIds }
                        .thenByDescending { it.alignmentScore }
                )

                UiState.Success(
                    MatchesFeed(
                        seekerProfile = me.toUserProfile(),
                        firestorePeerCount = firestorePeers.size,
                        totalCandidateCount = candidatePool.size,
                        availableInterestCategories = interestCategories,
                        items = filteredMatches.map { result ->
                            MatchCardItem(
                                match = result,
                                isFromFirestore = result.profile.userId in firestoreIds
                            )
                        }
                    )
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val collectiveRecommendations: StateFlow<List<CollectiveTeamRecommendation>> = combine(
        effectiveSeekerProfile,
        allDiscoverableProfiles,
        allMissions
    ) { me, peers, missions ->
        val targetMission = missions.firstOrNull() ?: DemoSeedData.featuredCommunityMission
        ComplementarityEngine.recommendCollectiveTeams(
            mission = targetMission,
            availableProfiles = listOf(me) + peers,
            includeUser = me,
            maxResults = 3
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    // Intent-Based Search State
    private val _intentSearchState = MutableStateFlow<UiState<List<IntentSearchResult>>>(UiState.Success(emptyList()))
    val intentSearchState: StateFlow<UiState<List<IntentSearchResult>>> = _intentSearchState.asStateFlow()

    private val _currentInterpretedIntent = MutableStateFlow<StructuredIntent?>(null)
    val currentInterpretedIntent: StateFlow<StructuredIntent?> = _currentInterpretedIntent.asStateFlow()

    // AI Capability Extraction State
    private val _aiTagSuggestionState = MutableStateFlow<UiState<ExtractedCapabilityTags>?>(null)
    val aiTagSuggestionState: StateFlow<UiState<ExtractedCapabilityTags>?> = _aiTagSuggestionState.asStateFlow()

    // AI Mission Suggestion State
    private val _aiMissionSuggestionState = MutableStateFlow<UiState<MissionRequirementSuggestion>?>(null)
    val aiMissionSuggestionState: StateFlow<UiState<MissionRequirementSuggestion>?> = _aiMissionSuggestionState.asStateFlow()

    // Possibility Finder State
    private val _possibilityState = MutableStateFlow<UiState<PossibilityAnalysis>?>(null)
    val possibilityState: StateFlow<UiState<PossibilityAnalysis>?> = _possibilityState.asStateFlow()

    // Active Mission Milestones Map
    private val _activeMilestones = MutableStateFlow<List<MissionMilestone>>(DemoSeedData.defaultMilestones)
    val activeMilestones: StateFlow<List<MissionMilestone>> = _activeMilestones.asStateFlow()

    init {
        runIntentSearch(
            title = "Make our neighbourhood greener",
            description = "Plant trees and create vibrant green corners with neighbors.",
            useAi = false
        )
    }

    fun clearStatusBanner() {
        _statusBanner.value = null
    }

    fun selectMatchingMode(mode: MatchingMode) {
        _selectedMatchingMode.value = mode
    }

    fun syncActiveUserProfile(userProfile: UserProfile) {
        _activeUserProfile.value = userProfile
    }

    fun updateMatchesSearchQuery(query: String) {
        _matchesSearchQuery.value = query
    }

    fun selectInterestCategory(category: String?) {
        _selectedInterestCategory.value = category
    }

    fun setFirestoreOnlyMatches(firestoreOnly: Boolean) {
        _firestoreOnlyMatches.value = firestoreOnly
    }

    fun clearMatchesFilters() {
        _matchesSearchQuery.value = ""
        _selectedInterestCategory.value = null
    }

    fun saveUserProfile(userProfile: UserProfile, onComplete: () -> Unit = {}) {
        _activeUserProfile.value = userProfile
        val current = (ownProfileState.value as? UiState.Success)?.data
        val merged = userProfile.toCommunityProfile(existing = current)
        saveProfile(merged, onComplete)
    }

    fun saveProfile(profile: CommunityProfile, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val result = repository.saveProfile(profile)
            result.onSuccess {
                _statusBanner.value = "Matching profile saved."
                onComplete()
            }.onFailure { err ->
                _statusBanner.value = err.localizedMessage ?: "Failed to save profile."
            }
        }
    }

    fun savePrivateSettings(settings: PrivateSettings) {
        viewModelScope.launch {
            repository.savePrivateSettings(settings)
                .onSuccess { _statusBanner.value = "Privacy & safety preferences updated." }
                .onFailure { _statusBanner.value = it.localizedMessage ?: "Failed to update settings." }
        }
    }

    fun saveAmbition(ambition: UserAmbition) {
        viewModelScope.launch {
            repository.upsertAmbition(ambition)
                .onSuccess { _statusBanner.value = "Ambition saved." }
                .onFailure { _statusBanner.value = it.localizedMessage ?: "Failed to save ambition." }
        }
    }

    fun deleteAmbition(ambitionId: String) {
        viewModelScope.launch {
            repository.deleteAmbition(ambitionId)
                .onFailure { _statusBanner.value = it.localizedMessage ?: "Failed to delete ambition." }
        }
    }

    fun extractCapabilitiesWithAi(naturalText: String) {
        if (naturalText.isBlank()) return
        viewModelScope.launch {
            _aiTagSuggestionState.value = UiState.Loading
            val res = aiLogicService.extractCapabilityTags(naturalText)
            res.onSuccess { tags ->
                _aiTagSuggestionState.value = UiState.Success(tags)
            }.onFailure { err ->
                _aiTagSuggestionState.value = UiState.Error(
                    err.localizedMessage ?: "AI tag suggestion unavailable."
                )
            }
        }
    }

    fun clearAiTagSuggestion() {
        _aiTagSuggestionState.value = null
    }

    fun suggestMissionRequirementsWithAi(title: String, description: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            _aiMissionSuggestionState.value = UiState.Loading
            val res = aiLogicService.suggestMissionRequirements(title, description)
            res.onSuccess { suggestion ->
                _aiMissionSuggestionState.value = UiState.Success(suggestion)
            }.onFailure { err ->
                _aiMissionSuggestionState.value = UiState.Error(
                    err.localizedMessage ?: "AI mission suggestion unavailable."
                )
            }
        }
    }

    fun clearMissionSuggestion() {
        _aiMissionSuggestionState.value = null
    }

    fun runIntentSearch(title: String, description: String, useAi: Boolean) {
        if (title.isBlank() && description.isBlank()) return
        viewModelScope.launch {
            if (useAi) _intentSearchState.value = UiState.Loading
            val me = (ownProfileState.value as? UiState.Success)?.data
            val missions = allMissions.value
            val profiles = allDiscoverableProfiles.value + listOfNotNull(me)

            var aiIntent: StructuredIntent? = null
            if (useAi) {
                aiLogicService.interpretMissionIntent(title, description)
                    .onSuccess { aiIntent = it }
                    .onFailure { err ->
                        _statusBanner.value = "Gemini AI unavailable (${err.localizedMessage ?: "error"}); using Local Semantic Engine."
                    }
            }

            val resolvedIntent = aiIntent ?: IntentDiscoveryEngine.extractLocalStructuredIntent(title, description)
            _currentInterpretedIntent.value = resolvedIntent

            val results = IntentDiscoveryEngine.searchByIntent(
                queryTitle = title,
                queryDescription = description,
                missions = missions,
                profiles = profiles,
                seekerProfile = me,
                aiExtractedIntent = resolvedIntent
            )
            _intentSearchState.value = UiState.Success(results)
        }
    }

    fun analyzePossibility(
        objective: String,
        contributions: List<String>,
        learningGoals: List<String>,
        supportWanted: List<String>,
        useAi: Boolean
    ) {
        if (objective.isBlank()) return
        viewModelScope.launch {
            _possibilityState.value = UiState.Loading
            val me = (ownProfileState.value as? UiState.Success)?.data
                ?: DemoSeedData.defaultUserProfile(currentUserId, initialDisplayName)
            val peers = allDiscoverableProfiles.value
            val blocked = safetyBlocks.value.map { it.blockedUserId }.toSet()

            var essentialReqs: List<String> = emptyList()
            var optionalReqs: List<String> = emptyList()
            var sourceLabel = "Complementarity Engine + Local Semantic Analysis"

            if (useAi) {
                aiLogicService.suggestMissionRequirements(objective, "Contributions: ${contributions.joinToString(", ")}. Learning: ${learningGoals.joinToString(", ")}.")
                    .onSuccess { suggestion ->
                        essentialReqs = suggestion.essentialCapabilities
                        optionalReqs = suggestion.optionalCapabilities
                        sourceLabel = "Complementarity Engine + Gemini AI Logic (gemini-3.8-flash)"
                    }
                    .onFailure { err ->
                        _statusBanner.value = "Gemini AI error: ${err.localizedMessage}. Fell back to Local Semantic Engine."
                    }
            }

            val analysis = PossibilityFinderEngine.analyzePossibility(
                objective = objective,
                userContributions = contributions,
                learningGoals = learningGoals,
                supportWanted = supportWanted,
                inferredEssentialReqs = essentialReqs,
                inferredOptionalReqs = optionalReqs,
                userProfile = me,
                communityProfiles = peers,
                blockedIds = blocked,
                analysisSource = sourceLabel
            )
            _possibilityState.value = UiState.Success(analysis)
        }
    }

    fun savePossibilityAnalysis(analysis: PossibilityAnalysis) {
        viewModelScope.launch {
            val missing = analysis.capabilityBreakdown
                .filter { it.status.name == "MISSING" }
                .map { it.name }
            val suggested = analysis.potentialCollaborators.map { it.profile.displayName }
            val summary = analysis.groupCombinations.firstOrNull()?.explanation
                ?: analysis.alternativeApproaches.firstOrNull()
                ?: "Saved possibility exploration."

            repository.savePossibility(
                SavedPossibility(
                    userId = currentUserId,
                    objective = analysis.objective,
                    summary = summary,
                    missingCapabilities = missing,
                    suggestedCollaborators = suggested
                )
            ).onSuccess {
                _statusBanner.value = "Saved to My Space -> Saved Possibilities."
            }.onFailure {
                _statusBanner.value = it.localizedMessage ?: "Could not save possibility."
            }
        }
    }

    fun createMission(
        title: String,
        description: String,
        objective: String,
        requiredCaps: List<String>,
        optionalCaps: List<String>,
        accessibilityReq: String,
        targetParticipants: Int,
        onCreated: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val me = (ownProfileState.value as? UiState.Success)?.data
            val intent = IntentDiscoveryEngine.extractLocalStructuredIntent(title, description, objective)
            val mission = Mission(
                ownerId = currentUserId,
                ownerName = me?.displayName ?: initialDisplayName,
                title = title.trim(),
                description = description.trim(),
                objective = objective.ifBlank { title }.trim(),
                intendedOutcome = intent.intendedOutcome,
                problemAddressed = description.take(200),
                interpretedIntent = intent.mainObjective,
                intentDomain = intent.problemDomain,
                requiredCapabilities = requiredCaps.ifEmpty { intent.requiredCapabilities },
                optionalCapabilities = optionalCaps,
                activities = intent.activities,
                accessibilityReq = accessibilityReq.ifBlank { "Inclusive & Accessible" },
                targetParticipants = targetParticipants.toLong().coerceIn(2L, 12L),
                memberUids = listOf(currentUserId),
                status = MissionStatus.OPEN.name,
                visibility = "COMMUNITY",
                progressPercent = 10L
            )
            val initialMilestones = listOf(
                "Align on accessibility & communication norms" to "All Collaborators",
                "Complete initial capability build & research" to (requiredCaps.firstOrNull() ?: "Core Lead"),
                "Integrate & share demonstration" to "Collective Team"
            )
            repository.createMission(mission, initialMilestones)
                .onSuccess {
                    _statusBanner.value = "Shared mission published."
                    onCreated()
                }
                .onFailure {
                    _statusBanner.value = it.localizedMessage ?: "Failed to create mission."
                }
        }
    }

    fun sendCollaborationInvitation(
        recipient: CommunityProfile,
        missionId: String,
        missionTitle: String,
        proposedRole: String,
        rationale: String
    ) {
        viewModelScope.launch {
            val me = (ownProfileState.value as? UiState.Success)?.data
            val inv = CollaborationInvitation(
                missionId = missionId,
                missionTitle = missionTitle,
                senderId = currentUserId,
                senderName = me?.displayName ?: initialDisplayName,
                recipientId = recipient.userId,
                recipientName = recipient.displayName,
                participantUids = listOf(currentUserId, recipient.userId),
                proposedRole = proposedRole.ifBlank { "Complementary Collaborator" },
                rationale = rationale.take(380),
                status = InvitationStatus.PENDING.name
            )
            repository.sendInvitation(inv)
                .onSuccess {
                    _statusBanner.value = "Consent-based invitation sent to ${recipient.displayName}."
                }
                .onFailure {
                    _statusBanner.value = it.localizedMessage ?: "Could not send invitation."
                }
        }
    }

    fun respondToInvitation(invitation: CollaborationInvitation, status: InvitationStatus) {
        viewModelScope.launch {
            repository.respondToInvitation(invitation.id, invitation.missionId, status)
                .onSuccess {
                    _statusBanner.value = when (status) {
                        InvitationStatus.ACCEPTED -> "Joined collaboration on \"${invitation.missionTitle}\"."
                        InvitationStatus.DECLINED -> "Invitation declined respectfully."
                        InvitationStatus.CANCELLED -> "Invitation withdrawn."
                        else -> "Updated invitation."
                    }
                }
                .onFailure {
                    _statusBanner.value = it.localizedMessage ?: "Failed to update invitation."
                }
        }
    }

    fun toggleDemoMilestone(milestoneId: String) {
        _activeMilestones.value = _activeMilestones.value.map { ms ->
            if (ms.id == milestoneId) ms.copy(completed = !ms.completed) else ms
        }
    }

    fun updateMissionProgress(mission: Mission, progressPercent: Long) {
        viewModelScope.launch {
            val nextStatus = if (progressPercent >= 100L) MissionStatus.COMPLETED else MissionStatus.ACTIVE
            repository.updateMissionProgress(mission.id, progressPercent, nextStatus)
                .onFailure { _statusBanner.value = it.localizedMessage ?: "Failed to update progress." }
        }
    }

    fun blockUser(profile: CommunityProfile, reason: String) {
        viewModelScope.launch {
            repository.blockUser(profile.userId, reason.ifBlank { "User safety boundary" })
                .onSuccess { _statusBanner.value = "${profile.displayName} blocked & report logged safely." }
                .onFailure { _statusBanner.value = it.localizedMessage ?: "Failed to block user." }
        }
    }

    fun unblockUser(blockedUserId: String) {
        viewModelScope.launch {
            repository.unblockUser(blockedUserId)
                .onSuccess { _statusBanner.value = "Safety block removed." }
        }
    }

    fun toggleSkillEndorsement(recipient: CommunityProfile, skillName: String) {
        if (recipient.userId.isBlank() || recipient.userId == currentUserId) return
        viewModelScope.launch {
            val endorserName = effectiveSeekerProfile.value.displayName.ifBlank { initialDisplayName }
            repository.toggleSkillEndorsement(
                recipientId = recipient.userId,
                recipientName = recipient.displayName,
                endorserName = endorserName,
                skillName = skillName
            ).onSuccess { endorsed ->
                _statusBanner.value = if (endorsed) {
                    "Endorsed ${recipient.displayName} for $skillName."
                } else {
                    "Removed endorsement for $skillName."
                }
            }.onFailure { err ->
                _statusBanner.value = err.localizedMessage ?: "Could not update endorsement."
            }
        }
    }

    fun sendCollaborationMessage(
        recipient: CommunityProfile,
        body: String,
        contextLabel: String = ""
    ) {
        val cleanBody = body.trim()
        if (cleanBody.isEmpty() || recipient.userId.isBlank() || recipient.userId == currentUserId) return
        viewModelScope.launch {
            val senderName = effectiveSeekerProfile.value.displayName.ifBlank { initialDisplayName }
            repository.sendCollaborationMessage(
                recipientId = recipient.userId,
                recipientName = recipient.displayName,
                senderName = senderName,
                body = cleanBody,
                contextLabel = contextLabel
            ).onFailure { err ->
                _statusBanner.value = err.localizedMessage ?: "Could not send message."
            }
        }
    }

    private fun <T> unwrapFlowError(error: Throwable, fallback: String): UiState<T> {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val rootError = firestoreError ?: error
        Log.w(TAG, fallback, rootError)
        return UiState.Error(rootError.message ?: fallback)
    }

    private companion object {
        const val TAG = "SymbiosisVM"
    }
}

data class MatchCardItem(
    val match: IndividualMatchResult,
    val isFromFirestore: Boolean
)

data class MatchesFeed(
    val seekerProfile: UserProfile,
    val firestorePeerCount: Int,
    val totalCandidateCount: Int,
    val availableInterestCategories: List<String>,
    val items: List<MatchCardItem>
)
