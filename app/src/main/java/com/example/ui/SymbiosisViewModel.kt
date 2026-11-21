package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AiCandidateMission
import com.example.data.AiCandidatePost
import com.example.data.AiCommunityRecommendation
import com.example.data.AiLogicService
import com.example.data.CommunityRecommendationType
import com.example.data.DemoSeedData
import com.example.data.ExtractedCapabilityTags
import com.example.data.MissionRequirementSuggestion
import com.example.data.SymbiosisRepository
import com.example.model.toUserProfile
import com.example.engine.ComplementarityEngine
import com.example.engine.IntentDiscoveryEngine
import com.example.engine.PossibilityFinderEngine
import com.example.model.ActivityEventType
import com.example.model.ActivityFeedItem
import com.example.model.CollaborationInvitation
import com.example.model.CollaborationMessage
import com.example.model.CollectiveTeamRecommendation
import com.example.model.CommunityComment
import com.example.model.CommunityPost
import com.example.model.CommunityProfile
import com.example.model.FollowedTopic
import com.example.model.IndividualMatchResult
import com.example.model.IntentPostRecommendation
import com.example.model.IntentSearchResult
import com.example.model.InvitationStatus
import com.example.model.MatchingMode
import com.example.model.Mission
import com.example.model.MissionMilestone
import com.example.model.MissionStatus
import com.example.model.OverlappingSkillMatch
import com.example.model.PossibilityAnalysis
import com.example.model.PostBookmark
import com.example.model.PostCategory
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
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

    private val cloudUserMemberMissions: StateFlow<List<Mission>> = repository.observeUserMemberMissions()
        .catch { error ->
            unwrapFlowError<List<Mission>>(error, "Failed to load user mission history")
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val allMissions: StateFlow<List<Mission>> = combine(
        cloudUserMemberMissions,
        cloudMissions
    ) { userMissions, communityMissions ->
        (userMissions + communityMissions + DemoSeedData.intentDiscoveryMissions)
            .distinctBy { it.id }
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

    // Community Posts, Comments, Bookmarks, Followed Topics & Moderation Filters (Section 19)
    private val _selectedPostCategory = MutableStateFlow<PostCategory?>(null)
    val selectedPostCategory: StateFlow<PostCategory?> = _selectedPostCategory.asStateFlow()

    private val _communityFeedMode = MutableStateFlow(CommunityFeedMode.ALL)
    val communityFeedMode: StateFlow<CommunityFeedMode> = _communityFeedMode.asStateFlow()

    private val _communityIntentQuery = MutableStateFlow("")
    val communityIntentQuery: StateFlow<String> = _communityIntentQuery.asStateFlow()

    private val _reportedPostIds = MutableStateFlow<Set<String>>(emptySet())
    private val _localSeedAppreciators = MutableStateFlow<Map<String, List<String>>>(emptyMap())

    private val cloudCommunityPosts: StateFlow<List<CommunityPost>> = repository.observeCommunityPosts()
        .catch { error ->
            unwrapFlowError<List<CommunityPost>>(error, "Failed to load community posts")
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val communityPosts: StateFlow<List<CommunityPost>> = combine(
        cloudCommunityPosts,
        safetyBlocks,
        _reportedPostIds,
        _localSeedAppreciators
    ) { cloud, blocks, reportedIds, seedAppreciations ->
        val blockedSet = blocks.map { it.blockedUserId }.toSet()
        (cloud + DemoSeedData.sampleCommunityPosts)
            .distinctBy { it.id }
            .filter { it.authorId !in blockedSet && it.id !in reportedIds }
            .map { post ->
                val override = seedAppreciations[post.id]
                if (override != null) post.copy(appreciatorUids = override) else post
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), DemoSeedData.sampleCommunityPosts)

    private val cloudCommunityComments: StateFlow<List<CommunityComment>> = repository.observeCommunityComments()
        .catch { error ->
            unwrapFlowError<List<CommunityComment>>(error, "Failed to load comments")
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val commentsByPostId: StateFlow<Map<String, List<CommunityComment>>> = cloudCommunityComments
        .map { cloud ->
            (cloud + DemoSeedData.sampleCommunityComments)
                .distinctBy { it.id }
                .groupBy { it.postId }
                .mapValues { (_, items) ->
                    items.sortedBy { it.createdAt?.toDate()?.time ?: Long.MAX_VALUE }
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyMap())

    val postBookmarks: StateFlow<List<PostBookmark>> = repository.observePostBookmarks()
        .catch { error ->
            unwrapFlowError<List<PostBookmark>>(error, "Failed to load post bookmarks")
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val bookmarkedPostIds: StateFlow<Set<String>> = postBookmarks
        .map { list ->
            list.map { it.postId.ifBlank { it.id } }.filter { it.isNotBlank() }.toSet()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptySet())

    val bookmarkedPostEntries: StateFlow<List<BookmarkedPostEntry>> = combine(
        postBookmarks,
        communityPosts
    ) { bookmarks, posts ->
        resolveBookmarkedEntries(bookmarks, posts)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val followedTopics: StateFlow<List<FollowedTopic>> = repository.observeFollowedTopics()
        .catch { error ->
            unwrapFlowError<List<FollowedTopic>>(error, "Failed to load followed topics")
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private val _communityMemberSearchQuery = MutableStateFlow("")
    val communityMemberSearchQuery: StateFlow<String> = _communityMemberSearchQuery.asStateFlow()

    val communityMatchedMembers: StateFlow<List<CommunityProfile>> = combine(
        _communityMemberSearchQuery,
        allDiscoverableProfiles
    ) { query, profiles ->
        filterMembersByCapabilityOrInterest(
            profiles = profiles,
            query = query,
            excludeUserId = currentUserId
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    // Global Top Search Bar State
    private val _globalSearchQuery = MutableStateFlow("")
    val globalSearchQuery: StateFlow<String> = _globalSearchQuery.asStateFlow()

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

    private val _firestoreSkillOverlapQueryPeers = MutableStateFlow<List<CommunityProfile>>(emptyList())
    private val _isRefreshingSkillOverlap = MutableStateFlow(false)
    val isRefreshingSkillOverlap: StateFlow<Boolean> = _isRefreshingSkillOverlap.asStateFlow()

    val overlappingSkillFeedState: StateFlow<UiState<OverlappingSkillFeed>> = combine(
        effectiveSeekerProfile,
        firestorePeersState,
        _firestoreSkillOverlapQueryPeers,
        safetyBlocks,
        _firestoreOnlyMatches
    ) { me, peersState, queriedPeers, blocks, firestoreOnly ->
        val blockedSet = blocks.map { it.blockedUserId }.toSet()
        when (peersState) {
            is UiState.Loading -> UiState.Loading
            is UiState.Error -> UiState.Error(peersState.message)
            is UiState.Success -> {
                val firestorePeers = (queriedPeers + peersState.data)
                    .distinctBy { it.userId }
                    .filter { it.userId != currentUserId && it.userId !in blockedSet }
                val firestoreIds = firestorePeers.map { it.userId }.toSet()

                val candidatePool = if (isCloudBacked && firestoreOnly) {
                    firestorePeers
                } else {
                    (firestorePeers + DemoSeedData.communityCollaborators)
                        .distinctBy { it.userId }
                        .filter { it.userId != currentUserId && it.userId !in blockedSet }
                }

                val ranked = ComplementarityEngine.matchByOverlappingSkills(
                    seeker = me,
                    candidates = candidatePool,
                    blockedIds = blockedSet
                ).sortedWith(
                    compareByDescending<OverlappingSkillMatch> { it.profile.userId in firestoreIds }
                        .thenByDescending { it.overlappingSkills.size }
                        .thenByDescending { it.overlapScore }
                )

                UiState.Success(
                    OverlappingSkillFeed(
                        seekerSkills = me.allCapabilities(),
                        firestorePeerCount = firestorePeers.size,
                        items = ranked.map { match ->
                            OverlappingSkillMatchItem(
                                match = match,
                                isFromFirestore = match.profile.userId in firestoreIds
                            )
                        }
                    )
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    fun refreshOverlappingSkillMatches(userTriggered: Boolean = false) {
        val skills = effectiveSeekerProfile.value.allCapabilities()
        if (skills.isEmpty()) {
            if (userTriggered) {
                _statusBanner.value = "Add at least one skill on your profile to match overlapping collaborators."
            }
            return
        }
        viewModelScope.launch {
            _isRefreshingSkillOverlap.value = true
            repository.queryCollaboratorsByOverlappingSkills(skills)
                .onSuccess { peers ->
                    _firestoreSkillOverlapQueryPeers.value = peers
                    if (userTriggered) {
                        val overlapCount = ComplementarityEngine.matchByOverlappingSkills(
                            seeker = effectiveSeekerProfile.value,
                            candidates = peers
                        ).size
                        _statusBanner.value = if (isCloudBacked) {
                            "Queried Firestore for overlapping skills ($overlapCount ${if (overlapCount == 1) "match" else "matches"} found)."
                        } else {
                            "Updated overlapping skill collaborator suggestions ($overlapCount found)."
                        }
                    }
                }
                .onFailure { err ->
                    if (userTriggered) {
                        _statusBanner.value = err.localizedMessage ?: "Could not refresh overlapping skill matches from Firestore."
                    }
                }
            _isRefreshingSkillOverlap.value = false
        }
    }

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

    // Active Mission Milestones & Per-Mission Task Progress Tracking
    private val _localSeedMilestones = MutableStateFlow(DemoSeedData.defaultMilestones)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val cloudMilestonesByMissionId: StateFlow<Map<String, List<MissionMilestone>>> =
        cloudUserMemberMissions.flatMapLatest { missions ->
            if (missions.isEmpty()) {
                flowOf(emptyMap())
            } else {
                combine(
                    missions.map { mission ->
                        repository.observeMilestones(mission.id)
                            .map { list -> mission.id to list }
                            .catch { emit(mission.id to emptyList()) }
                    }
                ) { pairs ->
                    pairs.toMap()
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyMap())

    val milestonesByMissionId: StateFlow<Map<String, List<MissionMilestone>>> = combine(
        cloudMilestonesByMissionId,
        _localSeedMilestones
    ) { cloudMap, seedList ->
        val seedMap = seedList.groupBy { it.missionId }
        (seedMap.keys + cloudMap.keys).associateWith { missionId ->
            val cloud = cloudMap[missionId].orEmpty()
            if (cloud.isNotEmpty()) cloud else seedMap[missionId].orEmpty()
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000L),
        DemoSeedData.defaultMilestones.groupBy { it.missionId }
    )

    val activeMilestones: StateFlow<List<MissionMilestone>> = milestonesByMissionId
        .map { map ->
            map[DemoSeedData.featuredCommunityMission.id]
                ?: map.values.flatten().ifEmpty { DemoSeedData.defaultMilestones }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), DemoSeedData.defaultMilestones)

    val activeCollaborationsProgress: StateFlow<ActiveCollaborationsProgressSummary> = combine(
        allMissions,
        invitations,
        milestonesByMissionId
    ) { missions, invList, msMap ->
        computeActiveCollaborationProgress(
            userId = currentUserId,
            missions = missions,
            acceptedInvitations = invList,
            milestonesByMissionId = msMap
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000L),
        ActiveCollaborationsProgressSummary()
    )

    // Gemini AI Community Recommendations ('Suggested for You')
    private val _suggestedForYouState =
        MutableStateFlow<UiState<List<SuggestedForYouItem>>>(UiState.Loading)
    val suggestedForYouState: StateFlow<UiState<List<SuggestedForYouItem>>> =
        _suggestedForYouState.asStateFlow()
    private var suggestedForYouJob: Job? = null
    private var lastSuggestedSignature: String? = null

    val intentRecommendedPosts: StateFlow<List<IntentPostRecommendation>> = combine(
        _communityIntentQuery,
        communityPosts,
        effectiveSeekerProfile,
        allMissions,
        followedTopics
    ) { query, posts, me, missions, topics ->
        val topicNames = topics.map { it.topic }.toSet()
        IntentDiscoveryEngine.discoverRelevantPosts(
            queryOrFocus = query,
            posts = posts.filter { it.authorId != currentUserId },
            seekerProfile = me,
            seekerMissions = missions,
            followedTopics = topicNames
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val globalSearchResults: StateFlow<GlobalSearchResults> = combine(
        _globalSearchQuery,
        allDiscoverableProfiles,
        allMissions,
        communityPosts
    ) { rawQuery, peers, missions, posts ->
        val q = rawQuery.trim()
        if (q.isBlank()) {
            GlobalSearchResults()
        } else {
            val matchedPeers = peers.filter { profile ->
                profile.displayName.contains(q, ignoreCase = true) ||
                    profile.headline.contains(q, ignoreCase = true) ||
                    profile.skills.any { it.contains(q, ignoreCase = true) || ComplementarityEngine.capabilitiesMatch(it, q) } ||
                    profile.allCapabilities().any { it.contains(q, ignoreCase = true) || ComplementarityEngine.capabilitiesMatch(it, q) } ||
                    profile.interests.any { it.contains(q, ignoreCase = true) } ||
                    profile.ambitions.any { it.contains(q, ignoreCase = true) }
            }.take(6)

            val matchedMissions = missions.filter { mission ->
                mission.title.contains(q, ignoreCase = true) ||
                    mission.description.contains(q, ignoreCase = true) ||
                    mission.objective.contains(q, ignoreCase = true) ||
                    mission.intentDomain.contains(q, ignoreCase = true) ||
                    mission.requiredCapabilities.any { it.contains(q, ignoreCase = true) || ComplementarityEngine.capabilitiesMatch(it, q) } ||
                    mission.optionalCapabilities.any { it.contains(q, ignoreCase = true) }
            }.take(6)

            val matchedPosts = posts.filter { post ->
                post.title.contains(q, ignoreCase = true) ||
                    post.body.contains(q, ignoreCase = true) ||
                    post.capabilityTags.any { it.contains(q, ignoreCase = true) } ||
                    post.requestedCapabilities.any { it.contains(q, ignoreCase = true) }
            }.take(4)

            GlobalSearchResults(
                query = q,
                collaborators = matchedPeers,
                missions = matchedMissions,
                posts = matchedPosts
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), GlobalSearchResults())

    val acceptedCollaborators: StateFlow<List<CommunityProfile>> = combine(
        invitations,
        allDiscoverableProfiles
    ) { invList, peers ->
        invList.filter { InvitationStatus.safeParse(it.status) == InvitationStatus.ACCEPTED }
            .mapNotNull { inv ->
                val isCurrentUserSender = inv.senderId == currentUserId
                val peerId = if (isCurrentUserSender) inv.recipientId else inv.senderId
                val peerName = if (isCurrentUserSender) inv.recipientName else inv.senderName
                if (peerId.isBlank() || peerId == currentUserId) return@mapNotNull null
                peers.firstOrNull { it.userId == peerId } ?: CommunityProfile(
                    id = peerId,
                    userId = peerId,
                    displayName = peerName.ifBlank { "Collaborator" },
                    headline = "Accepted collaborator • ${inv.missionTitle}",
                    skills = listOf(inv.proposedRole.ifBlank { "Collaboration" })
                )
            }
            .distinctBy { it.userId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val activityFeed: StateFlow<List<ActivityFeedItem>> = combine(
        invitations,
        allMissions,
        activeMilestones,
        skillEndorsements,
        communityPosts
    ) { invList, missions, milestones, endorsements, posts ->
        val items = mutableListOf<ActivityFeedItem>()
        val now = System.currentTimeMillis()

        // 1. Collaboration successes from accepted invitations
        invList.filter { InvitationStatus.safeParse(it.status) == InvitationStatus.ACCEPTED }
            .forEachIndexed { idx, inv ->
                val isSender = inv.senderId == currentUserId
                val peerId = if (isSender) inv.recipientId else inv.senderId
                val peerName = if (isSender) inv.recipientName else inv.senderName
                val ts = inv.updatedAt?.toDate()?.time ?: inv.createdAt?.toDate()?.time ?: (now - (idx + 1) * 60_000L)
                items += ActivityFeedItem(
                    id = "act_inv_${inv.id}",
                    type = ActivityEventType.COLLABORATION_ACCEPTED,
                    title = "Collaboration active with $peerName",
                    detail = "${inv.missionTitle} • Role: ${inv.proposedRole.ifBlank { "Shared Contributor" }}",
                    peerName = peerName,
                    badgeText = "Active Chat",
                    timestampMillis = ts,
                    targetPeerId = peerId
                )
            }

        // 2. Skill endorsements received by or given by current user
        endorsements.forEachIndexed { idx, end ->
            val ts = end.createdAt?.toDate()?.time ?: (now - (idx + 3) * 180_000L)
            if (end.recipientId == currentUserId) {
                items += ActivityFeedItem(
                    id = "act_end_rx_${end.id}",
                    type = ActivityEventType.ENDORSEMENT_RECEIVED,
                    title = "${end.endorserName.ifBlank { "A peer" }} endorsed your ${end.skillName}",
                    detail = end.note.ifBlank { "Trust endorsement added to your profile badge." },
                    peerName = end.endorserName,
                    badgeText = "+1 ${end.skillName}",
                    timestampMillis = ts,
                    targetPeerId = end.endorserId
                )
            } else if (end.endorserId == currentUserId) {
                items += ActivityFeedItem(
                    id = "act_end_tx_${end.id}",
                    type = ActivityEventType.ENDORSEMENT_GIVEN,
                    title = "You endorsed ${end.recipientName} for ${end.skillName}",
                    detail = "Strengthened visible peer trust on SYMBIOSIS.",
                    peerName = end.recipientName,
                    badgeText = end.skillName,
                    timestampMillis = ts,
                    targetPeerId = end.recipientId
                )
            }
        }

        // 3. Completed milestones & accomplished/active missions
        milestones.filter { it.completed }.forEachIndexed { idx, ms ->
            val ts = ms.updatedAt?.toDate()?.time ?: (now - (idx + 5) * 300_000L)
            items += ActivityFeedItem(
                id = "act_ms_${ms.id}",
                type = ActivityEventType.MISSION_MILESTONE,
                title = "Milestone achieved: ${ms.title}",
                detail = "Shared contribution by ${ms.assignedRole}",
                peerName = ms.assignedRole,
                badgeText = "Milestone Done",
                timestampMillis = ts
            )
        }

        missions.filter {
            val status = MissionStatus.safeParse(it.status)
            (currentUserId in it.memberUids || it.ownerId == currentUserId) &&
                (status == MissionStatus.COMPLETED || it.progressPercent >= 50L)
        }.forEachIndexed { idx, m ->
            val status = MissionStatus.safeParse(m.status)
            val ts = m.updatedAt?.toDate()?.time ?: (now - (idx + 8) * 420_000L)
            items += ActivityFeedItem(
                id = "act_mis_${m.id}",
                type = ActivityEventType.MISSION_ACCOMPLISHED,
                title = if (status == MissionStatus.COMPLETED) "Mission accomplished: ${m.title}" else "${m.title} (${m.progressPercent}% complete)",
                detail = m.interpretedIntent.ifBlank { m.objective },
                peerName = m.ownerName,
                badgeText = status.label,
                timestampMillis = ts
            )
        }

        // 4. Community achievements & posts involving current user
        posts.filter { it.authorId == currentUserId || it.categoryEnum() == PostCategory.ACHIEVEMENTS }
            .take(3)
            .forEachIndexed { idx, p ->
                val ts = p.createdAt?.toDate()?.time ?: (now - (idx + 12) * 500_000L)
                items += ActivityFeedItem(
                    id = "act_post_${p.id}",
                    type = ActivityEventType.COMMUNITY_POST,
                    title = p.title,
                    detail = "${p.authorName} shared in ${p.categoryEnum().label}",
                    peerName = p.authorName,
                    badgeText = p.categoryEnum().shortLabel,
                    timestampMillis = ts
                )
            }

        items.sortedByDescending { it.timestampMillis }.take(15)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

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
        val targetEntry = milestonesByMissionId.value.entries.firstOrNull { (_, list) ->
            list.any { it.id == milestoneId }
        }
        val targetMilestone = targetEntry?.value?.firstOrNull { it.id == milestoneId }
            ?: _localSeedMilestones.value.firstOrNull { it.id == milestoneId }
            ?: return
        val mission = allMissions.value.firstOrNull { it.id == targetMilestone.missionId }
            ?: DemoSeedData.featuredCommunityMission
        toggleMissionTask(mission, targetMilestone)
    }

    fun toggleMissionTask(mission: Mission, milestone: MissionMilestone) {
        val nextCompleted = !milestone.completed
        val isCloudTask = cloudMilestonesByMissionId.value[mission.id]?.any { it.id == milestone.id } == true
        val currentTasks = milestonesByMissionId.value[mission.id].orEmpty()
        val updatedTasks = currentTasks.map { ms ->
            if (ms.id == milestone.id) ms.copy(completed = nextCompleted) else ms
        }
        val newPct = if (updatedTasks.isNotEmpty()) {
            ((updatedTasks.count { it.completed } * 100L) / updatedTasks.size).coerceIn(0L, 100L)
        } else {
            mission.progressPercent
        }

        if (!isCloudTask) {
            _localSeedMilestones.update { list ->
                list.map { ms ->
                    if (ms.id == milestone.id) ms.copy(completed = nextCompleted) else ms
                }
            }
        }

        viewModelScope.launch {
            if (isCloudTask || !repository.isCloudBacked) {
                repository.toggleMilestone(mission.id, milestone.id, nextCompleted)
                    .onFailure { err ->
                        _statusBanner.value = err.localizedMessage ?: "Failed to update task."
                        return@launch
                    }
                val isCompletedMission = MissionStatus.safeParse(mission.status) == MissionStatus.COMPLETED
                if (!isCompletedMission) {
                    if (mission.ownerId == currentUserId) {
                        val nextStatus = if (newPct >= 100L) MissionStatus.COMPLETED else MissionStatus.ACTIVE
                        repository.updateMissionProgress(mission.id, newPct, nextStatus, updateStatus = true)
                    } else if (currentUserId in mission.memberUids) {
                        repository.updateMissionProgress(mission.id, newPct, MissionStatus.ACTIVE, updateStatus = false)
                    }
                }
            }
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

    // ---- Section 19 & Global Search Actions ----

    fun updateGlobalSearchQuery(query: String) {
        _globalSearchQuery.value = query
    }

    fun clearGlobalSearch() {
        _globalSearchQuery.value = ""
    }

    fun selectPostCategory(category: PostCategory?) {
        _selectedPostCategory.value = category
    }

    fun selectCommunityFeedMode(mode: CommunityFeedMode) {
        _communityFeedMode.value = mode
    }

    fun updateCommunityIntentQuery(query: String) {
        _communityIntentQuery.value = query
    }

    fun updateCommunityMemberSearchQuery(query: String) {
        _communityMemberSearchQuery.value = query
    }

    fun clearCommunityMemberSearch() {
        _communityMemberSearchQuery.value = ""
    }

    fun ensureSuggestedForYouLoaded() {
        refreshSuggestedForYou(force = false)
    }

    fun refreshSuggestedForYou(force: Boolean = true) {
        val me = effectiveSeekerProfile.value
        val missions = allMissions.value
        val posts = communityPosts.value
        val invList = invitations.value

        val missionHistory = computeMissionHistory(
            userId = currentUserId,
            missions = missions,
            acceptedInvitations = invList
        )
        val candidatePosts = posts
            .filter { it.authorId != currentUserId }
            .ifEmpty { posts }
        val joinedMissionIds = missionHistory.map { it.id }.toSet()
        val candidateMissions = missions.filter { mission ->
            mission.id !in joinedMissionIds &&
                mission.ownerId != currentUserId &&
                currentUserId !in mission.memberUids &&
                MissionStatus.safeParse(mission.status) != MissionStatus.COMPLETED
        }

        val signature = buildSuggestionSignature(
            userId = currentUserId,
            skills = me.allCapabilities(),
            missionHistoryIds = missionHistory.map { "${it.id}:${it.progressPercent}:${it.status}" },
            candidatePostIds = candidatePosts.map { it.id },
            candidateMissionIds = candidateMissions.map { it.id }
        )

        if (!force && signature == lastSuggestedSignature && _suggestedForYouState.value is UiState.Success) {
            return
        }
        if (!force && suggestedForYouJob?.isActive == true && signature == lastSuggestedSignature) {
            return
        }

        suggestedForYouJob?.cancel()
        suggestedForYouJob = viewModelScope.launch {
            _suggestedForYouState.value = UiState.Loading
            val skillsWithProficiency = me.skills.ifEmpty { me.allCapabilities() }.distinct().map { skill ->
                val prof = me.skillProficiencies.entries
                    .firstOrNull { it.key.equals(skill, ignoreCase = true) }
                    ?.value
                if (!prof.isNullOrBlank()) "$skill ($prof)" else skill
            }
            val historySummary = missionHistory.take(8).map { m ->
                "${m.title} [${MissionStatus.safeParse(m.status).label}, ${m.progressPercent}% complete, domain: ${m.intentDomain.ifBlank { "Collaboration" }}]"
            }
            val aiPosts = candidatePosts.take(12).map { p ->
                AiCandidatePost(
                    id = p.id,
                    title = p.title,
                    category = p.categoryEnum().label,
                    authorName = p.authorName,
                    capabilityTags = p.capabilityTags,
                    requestedCapabilities = p.requestedCapabilities,
                    missionTitle = p.missionTitle,
                    summary = p.body.take(220)
                )
            }
            val aiMissions = candidateMissions.take(10).map { m ->
                AiCandidateMission(
                    id = m.id,
                    title = m.title,
                    ownerName = m.ownerName,
                    domain = m.intentDomain.ifBlank { "Community Mission" },
                    requiredCapabilities = m.requiredCapabilities,
                    description = m.objective.ifBlank { m.description }.take(220)
                )
            }

            aiLogicService.recommendCommunityOpportunities(
                userDisplayName = me.displayName.ifBlank { initialDisplayName },
                skillsWithProficiency = skillsWithProficiency,
                interests = me.interests + me.ambitions,
                supportWanted = me.supportWanted,
                missionHistorySummary = historySummary,
                candidatePosts = aiPosts,
                candidateMissions = aiMissions
            ).onSuccess { recommendations ->
                lastSuggestedSignature = signature
                val resolved = resolveSuggestedForYouItems(
                    recommendations = recommendations,
                    candidatePosts = candidatePosts,
                    candidateMissions = candidateMissions
                )
                _suggestedForYouState.value = UiState.Success(resolved)
            }.onFailure { err ->
                _suggestedForYouState.value = UiState.Error(
                    err.localizedMessage ?: "Could not load Gemini recommendations right now."
                )
            }
        }
    }

    fun createCommunityPost(
        category: PostCategory,
        title: String,
        body: String,
        capabilityTags: List<String>,
        requestedCapabilities: List<String>,
        mission: Mission?,
        communityLocationTag: String,
        shareLocationTag: Boolean,
        linkUrl: String,
        imageUri: String,
        visibility: String = "COMMUNITY",
        onCreated: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val me = effectiveSeekerProfile.value
            val post = CommunityPost(
                authorId = currentUserId,
                authorName = me.displayName.ifBlank { initialDisplayName },
                authorHeadline = me.headline,
                category = category.name,
                title = title.trim(),
                body = body.trim(),
                capabilityTags = capabilityTags,
                requestedCapabilities = requestedCapabilities,
                missionId = mission?.id.orEmpty(),
                missionTitle = mission?.title.orEmpty(),
                communityLocationTag = if (shareLocationTag) communityLocationTag.trim() else "",
                shareLocationTag = shareLocationTag,
                linkUrl = linkUrl.trim(),
                imageUri = imageUri.trim(),
                visibility = visibility
            )
            repository.createCommunityPost(post)
                .onSuccess {
                    _statusBanner.value = "Published to SYMBIOSIS Community."
                    onCreated()
                }
                .onFailure { err ->
                    _statusBanner.value = err.localizedMessage ?: "Could not publish post."
                }
        }
    }

    fun togglePostAppreciation(post: CommunityPost) {
        viewModelScope.launch {
            val isSeedInCloud = repository.isCloudBacked && cloudCommunityPosts.value.none { it.id == post.id }
            if (isSeedInCloud) {
                val current = _localSeedAppreciators.value[post.id] ?: post.appreciatorUids
                val next = if (currentUserId in current) current - currentUserId else (current + currentUserId).distinct()
                _localSeedAppreciators.update { it + (post.id to next) }
                return@launch
            }
            repository.togglePostAppreciation(post)
                .onFailure { err ->
                    if (err.message == "SEED_POST_LOCAL_ONLY") {
                        val current = _localSeedAppreciators.value[post.id] ?: post.appreciatorUids
                        val next = if (currentUserId in current) current - currentUserId else (current + currentUserId).distinct()
                        _localSeedAppreciators.update { it + (post.id to next) }
                    } else {
                        _statusBanner.value = err.localizedMessage ?: "Could not update appreciation."
                    }
                }
        }
    }

    fun addCommunityComment(post: CommunityPost, body: String, replyToAuthor: String = "") {
        val clean = body.trim()
        if (clean.isEmpty()) return
        viewModelScope.launch {
            val me = effectiveSeekerProfile.value
            val comment = CommunityComment(
                postId = post.id,
                authorId = currentUserId,
                authorName = me.displayName.ifBlank { initialDisplayName },
                body = clean,
                replyToAuthor = replyToAuthor
            )
            repository.addCommunityComment(comment)
                .onSuccess { _statusBanner.value = "Reply added." }
                .onFailure { _statusBanner.value = it.localizedMessage ?: "Could not add reply." }
        }
    }

    fun togglePostBookmark(post: CommunityPost) {
        viewModelScope.launch {
            repository.togglePostBookmark(post)
                .onSuccess { saved ->
                    _statusBanner.value = if (saved) "Saved post to bookmarks." else "Removed bookmark."
                }
                .onFailure { _statusBanner.value = it.localizedMessage ?: "Could not update bookmark." }
        }
    }

    fun sharePostToPeer(
        post: CommunityPost,
        recipient: CommunityProfile,
        personalNote: String = ""
    ) {
        if (recipient.userId.isBlank() || recipient.userId == currentUserId) return
        viewModelScope.launch {
            val senderName = effectiveSeekerProfile.value.displayName.ifBlank { initialDisplayName }
            val postLink = post.linkUrl.trim().ifBlank { "symbiosis://community/post/${post.id}" }
            val snippet = post.body.trim().take(180)
            val composedBody = buildString {
                if (personalNote.isNotBlank()) {
                    append(personalNote.trim()).append("\n\n")
                }
                append("Shared Community Post: \"${post.title}\"\n")
                append("By ${post.authorName} • ${post.categoryEnum().label}\n")
                if (snippet.isNotBlank()) append("$snippet\n")
                append("Link: $postLink")
            }
            repository.sendCollaborationMessage(
                recipientId = recipient.userId,
                recipientName = recipient.displayName,
                senderName = senderName,
                body = composedBody,
                contextLabel = "Shared Post: ${post.title.take(60)}"
            ).onSuccess {
                _statusBanner.value = "Shared \"${post.title}\" with ${recipient.displayName}."
            }.onFailure { err ->
                _statusBanner.value = err.localizedMessage ?: "Could not share post."
            }
        }
    }

    fun sharePostToMissionGroup(
        post: CommunityPost,
        mission: Mission,
        personalNote: String = ""
    ) {
        viewModelScope.launch {
            val senderName = effectiveSeekerProfile.value.displayName.ifBlank { initialDisplayName }
            val postLink = post.linkUrl.trim().ifBlank { "symbiosis://community/post/${post.id}" }
            val snippet = post.body.trim().take(180)
            val composedBody = buildString {
                if (personalNote.isNotBlank()) {
                    append(personalNote.trim()).append("\n\n")
                }
                append("Shared to Group \"${mission.title}\": \"${post.title}\"\n")
                append("By ${post.authorName} • ${post.categoryEnum().label}\n")
                if (snippet.isNotBlank()) append("$snippet\n")
                append("Link: $postLink")
            }

            val profilesById = allDiscoverableProfiles.value.associateBy { it.userId }
            val recipientIds = (mission.memberUids + mission.ownerId)
                .filter { it.isNotBlank() && it != currentUserId }
                .distinct()

            val targetProfiles = if (recipientIds.isNotEmpty()) {
                recipientIds.map { uid ->
                    profilesById[uid] ?: CommunityProfile(
                        userId = uid,
                        displayName = if (uid == mission.ownerId && mission.ownerName.isNotBlank()) {
                            mission.ownerName
                        } else {
                            "Group Collaborator"
                        }
                    )
                }
            } else {
                // Solo/open mission: share with top matching collaborators interested in this mission's capabilities
                allDiscoverableProfiles.value
                    .filter { it.userId != currentUserId }
                    .take(2)
            }

            if (targetProfiles.isEmpty()) {
                _statusBanner.value = "Posted \"${post.title}\" to ${mission.title} group space."
                return@launch
            }

            var sentCount = 0
            targetProfiles.forEach { recipient ->
                repository.sendCollaborationMessage(
                    recipientId = recipient.userId,
                    recipientName = recipient.displayName,
                    senderName = senderName,
                    body = composedBody,
                    contextLabel = "Group Share • ${mission.title.take(48)}"
                ).onSuccess { sentCount++ }
            }

            _statusBanner.value = if (sentCount > 0) {
                "Shared \"${post.title}\" with $sentCount collaborator(s) in \"${mission.title}\"."
            } else {
                "Could not share post with mission group."
            }
        }
    }

    fun toggleFollowTopic(topic: String) {
        if (topic.isBlank()) return
        viewModelScope.launch {
            repository.toggleFollowTopic(topic)
                .onSuccess { followed ->
                    _statusBanner.value = if (followed) "Following topic \"$topic\"." else "Unfollowed \"$topic\"."
                }
                .onFailure { _statusBanner.value = it.localizedMessage ?: "Could not update topic." }
        }
    }

    fun reportCommunityPost(post: CommunityPost, reason: String) {
        viewModelScope.launch {
            _reportedPostIds.update { it + post.id }
            repository.reportCommunityPost(post, reason)
                .onSuccess { _statusBanner.value = "Post hidden & flagged safely for moderation review." }
                .onFailure { _statusBanner.value = it.localizedMessage ?: "Report logged locally." }
        }
    }

    fun convertPostToMission(
        post: CommunityPost,
        useAi: Boolean,
        onCreated: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val draft = IntentDiscoveryEngine.extractMissionDraftFromPost(post)
            var requiredCaps = draft.requiredCapabilities
            var optionalCaps = draft.optionalCapabilities
            if (useAi) {
                aiLogicService.analyzeCommunityPostForMission(post.title, post.body)
                    .onSuccess { suggestion ->
                        if (suggestion.essentialCapabilities.isNotEmpty()) {
                            requiredCaps = (post.requestedCapabilities + suggestion.essentialCapabilities)
                                .distinctBy { it.lowercase() }
                                .take(5)
                        }
                        if (suggestion.optionalCapabilities.isNotEmpty()) {
                            optionalCaps = suggestion.optionalCapabilities.take(4)
                        }
                    }
            }
            createMission(
                title = draft.title,
                description = draft.description,
                objective = draft.objective,
                requiredCaps = requiredCaps,
                optionalCaps = optionalCaps,
                accessibilityReq = "Inclusive & Accessible",
                targetParticipants = 4,
                onCreated = {
                    _statusBanner.value = "Mission created from \"${draft.title}\"!"
                    onCreated()
                }
            )
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

    companion object {
        private const val TAG = "SymbiosisVM"

        fun filterMembersByCapabilityOrInterest(
            profiles: List<CommunityProfile>,
            query: String,
            excludeUserId: String? = null
        ): List<CommunityProfile> {
            val trimmed = query.trim()
            if (trimmed.isBlank()) return emptyList()
            val tokens = trimmed.split(Regex("[,;\\s]+"))
                .map { it.trim() }
                .filter { it.length >= 2 }
                .ifEmpty { listOf(trimmed) }

            return profiles
                .filter { profile -> excludeUserId == null || profile.userId != excludeUserId }
                .mapNotNull { profile ->
                    var score = 0
                    val caps = profile.allCapabilities()
                    val interests = profile.interests + profile.ambitions

                    val fullCapMatch = caps.any {
                        it.contains(trimmed, ignoreCase = true) ||
                            ComplementarityEngine.capabilitiesMatch(it, trimmed)
                    }
                    val fullInterestMatch = interests.any {
                        it.contains(trimmed, ignoreCase = true)
                    }
                    val nameOrHeadlineMatch =
                        profile.displayName.contains(trimmed, ignoreCase = true) ||
                            profile.headline.contains(trimmed, ignoreCase = true)

                    if (fullCapMatch) score += 6
                    if (fullInterestMatch) score += 5
                    if (nameOrHeadlineMatch) score += 3

                    tokens.forEach { token ->
                        if (caps.any {
                                it.contains(token, ignoreCase = true) ||
                                    ComplementarityEngine.capabilitiesMatch(it, token)
                            }
                        ) {
                            score += 3
                        }
                        if (interests.any { it.contains(token, ignoreCase = true) }) {
                            score += 2
                        }
                        if (profile.headline.contains(token, ignoreCase = true)) {
                            score += 1
                        }
                    }

                    if (score > 0) profile to score else null
                }
                .sortedByDescending { it.second }
                .map { it.first }
        }

        fun computeMissionHistory(
            userId: String,
            missions: List<Mission>,
            acceptedInvitations: List<CollaborationInvitation> = emptyList()
        ): List<Mission> {
            if (userId.isBlank()) return emptyList()
            val acceptedMissionIds = acceptedInvitations
                .filter {
                    it.status == InvitationStatus.ACCEPTED.name &&
                        (it.senderId == userId || it.recipientId == userId)
                }
                .map { it.missionId }
                .toSet()

            return missions
                .filter { mission ->
                    mission.ownerId == userId ||
                        userId in mission.memberUids ||
                        mission.id in acceptedMissionIds
                }
                .sortedWith(
                    compareByDescending<Mission> {
                        it.updatedAt?.toDate()?.time ?: it.createdAt?.toDate()?.time ?: 0L
                    }.thenBy {
                        when (MissionStatus.safeParse(it.status)) {
                            MissionStatus.ACTIVE -> 0
                            MissionStatus.OPEN -> 1
                            MissionStatus.COMPLETED -> 2
                        }
                    }
                )
        }

        fun resolveBookmarkedEntries(
            bookmarks: List<PostBookmark>,
            posts: List<CommunityPost>
        ): List<BookmarkedPostEntry> {
            val postsById = posts.associateBy { it.id }
            return bookmarks
                .distinctBy { it.postId.ifBlank { it.id } }
                .mapNotNull { bookmark ->
                    val targetId = bookmark.postId.ifBlank { bookmark.id }
                    if (targetId.isBlank()) return@mapNotNull null
                    BookmarkedPostEntry(
                        bookmark = bookmark,
                        post = postsById[targetId]
                    )
                }
        }

        fun computeActiveCollaborationProgress(
            userId: String,
            missions: List<Mission>,
            acceptedInvitations: List<CollaborationInvitation> = emptyList(),
            milestonesByMissionId: Map<String, List<MissionMilestone>>
        ): ActiveCollaborationsProgressSummary {
            val history = computeMissionHistory(
                userId = userId,
                missions = missions,
                acceptedInvitations = acceptedInvitations
            )
            val activeMissions = history.filter {
                MissionStatus.safeParse(it.status) != MissionStatus.COMPLETED
            }
            val items = activeMissions.map { mission ->
                val tasks = milestonesByMissionId[mission.id].orEmpty()
                val completed = tasks.count { it.completed }
                MissionCollaborationProgress(
                    mission = mission,
                    tasks = tasks,
                    completedTasks = completed,
                    totalTasks = tasks.size,
                    hasExplicitTasks = tasks.isNotEmpty()
                )
            }
            return ActiveCollaborationsProgressSummary(
                activeCollaborations = items,
                totalCompletedTasks = items.sumOf { it.completedTasks },
                totalTasks = items.sumOf { it.totalTasks }
            )
        }

        fun buildSuggestionSignature(
            userId: String,
            skills: List<String>,
            missionHistoryIds: List<String>,
            candidatePostIds: List<String>,
            candidateMissionIds: List<String>
        ): String = listOf(
            userId,
            skills.map { it.trim().lowercase() }.sorted().joinToString(","),
            missionHistoryIds.sorted().joinToString(","),
            candidatePostIds.sorted().joinToString(","),
            candidateMissionIds.sorted().joinToString(",")
        ).joinToString("|")

        fun resolveSuggestedForYouItems(
            recommendations: List<AiCommunityRecommendation>,
            candidatePosts: List<CommunityPost>,
            candidateMissions: List<Mission>
        ): List<SuggestedForYouItem> {
            val postsById = candidatePosts.associateBy { it.id }
            val missionsById = candidateMissions.associateBy { it.id }
            return recommendations.mapNotNull { rec ->
                when (rec.targetType) {
                    CommunityRecommendationType.POST -> {
                        val post = postsById[rec.targetId] ?: return@mapNotNull null
                        SuggestedForYouItem.PostSuggestion(rec, post)
                    }
                    CommunityRecommendationType.COLLABORATION -> {
                        val mission = missionsById[rec.targetId] ?: return@mapNotNull null
                        SuggestedForYouItem.CollaborationSuggestion(rec, mission)
                    }
                }
            }
        }
    }
}

data class BookmarkedPostEntry(
    val bookmark: PostBookmark,
    val post: CommunityPost?
) {
    val postId: String get() = bookmark.postId.ifBlank { bookmark.id }
    val displayTitle: String get() = post?.title?.takeIf { it.isNotBlank() }
        ?: bookmark.postTitle.ifBlank { "Saved Community Thread" }
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

data class OverlappingSkillMatchItem(
    val match: OverlappingSkillMatch,
    val isFromFirestore: Boolean
)

data class OverlappingSkillFeed(
    val seekerSkills: List<String> = emptyList(),
    val firestorePeerCount: Int = 0,
    val items: List<OverlappingSkillMatchItem> = emptyList()
)

enum class CommunityFeedMode(val label: String) {
    ALL("All Posts"),
    INTENT_DISCOVERY("Intent Picks"),
    FOLLOWED_TOPICS("Following"),
    BOOKMARKS("Saved")
}

data class GlobalSearchResults(
    val query: String = "",
    val collaborators: List<CommunityProfile> = emptyList(),
    val missions: List<Mission> = emptyList(),
    val posts: List<CommunityPost> = emptyList()
) {
    val isEmpty: Boolean
        get() = collaborators.isEmpty() && missions.isEmpty() && posts.isEmpty()
}

data class MissionCollaborationProgress(
    val mission: Mission,
    val tasks: List<MissionMilestone>,
    val completedTasks: Int,
    val totalTasks: Int,
    val hasExplicitTasks: Boolean
) {
    val completionRatio: Float
        get() = if (totalTasks > 0) {
            (completedTasks.toFloat() / totalTasks.toFloat()).coerceIn(0f, 1f)
        } else {
            (mission.progressPercent.coerceIn(0L, 100L) / 100f)
        }

    val progressPercent: Int
        get() = (completionRatio * 100f).toInt().coerceIn(0, 100)
}

data class ActiveCollaborationsProgressSummary(
    val activeCollaborations: List<MissionCollaborationProgress> = emptyList(),
    val totalCompletedTasks: Int = 0,
    val totalTasks: Int = 0
) {
    val overallRatio: Float
        get() = when {
            totalTasks > 0 -> (totalCompletedTasks.toFloat() / totalTasks.toFloat()).coerceIn(0f, 1f)
            activeCollaborations.isNotEmpty() ->
                activeCollaborations.map { it.completionRatio }.average().toFloat().coerceIn(0f, 1f)
            else -> 0f
        }

    val overallPercent: Int
        get() = (overallRatio * 100f).toInt().coerceIn(0, 100)
}

sealed class SuggestedForYouItem {
    abstract val recommendation: AiCommunityRecommendation

    data class PostSuggestion(
        override val recommendation: AiCommunityRecommendation,
        val post: CommunityPost
    ) : SuggestedForYouItem()

    data class CollaborationSuggestion(
        override val recommendation: AiCommunityRecommendation,
        val mission: Mission
    ) : SuggestedForYouItem()
}
