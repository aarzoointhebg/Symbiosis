package com.example

import com.example.data.AccessibilityPreferencesRepository
import com.example.data.AiCommunityRecommendation
import com.example.data.CommunityRecommendationType
import com.example.engine.ComplementarityEngine
import com.example.engine.IntentDiscoveryEngine
import com.example.model.AppThemeMode
import com.example.model.CollaborationInvitation
import com.example.model.CollaborationMessage
import com.example.model.CommunityPost
import com.example.model.CommunityProfile
import com.example.model.InvitationStatus
import com.example.model.MatchingMode
import com.example.model.Mission
import com.example.model.MissionMilestone
import com.example.model.MissionStatus
import com.example.model.PostBookmark
import com.example.model.PostCategory
import com.example.model.SkillProficiency
import com.example.model.UserProfile
import com.example.ui.SuggestedForYouItem
import com.example.ui.SymbiosisViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun proficiencyLevels_rankExpertHigherAndTriggerMentorshipSynergy() {
    val seeker = CommunityProfile(
      userId = "u_seeker",
      displayName = "Seeker",
      skills = listOf("Programming", "Audio engineering"),
      skillProficiencies = mapOf(
        "Programming" to SkillProficiency.ADVANCED.name,
        "Audio engineering" to SkillProficiency.BEGINNER.name
      ),
      supportWanted = listOf("Visual design"),
      availability = "FLEXIBLE"
    )

    val beginnerPeer = CommunityProfile(
      userId = "u_beginner",
      displayName = "Beginner Peer",
      skills = listOf("Visual design", "Audio engineering"),
      skillProficiencies = mapOf(
        "Visual design" to SkillProficiency.BEGINNER.name,
        "Audio engineering" to SkillProficiency.BEGINNER.name
      ),
      supportWanted = listOf("Programming"),
      availability = "FLEXIBLE"
    )

    val expertPeer = CommunityProfile(
      userId = "u_expert",
      displayName = "Expert Peer",
      skills = listOf("Visual design", "Audio engineering"),
      skillProficiencies = mapOf(
        "Visual design" to SkillProficiency.EXPERT.name,
        "Audio engineering" to SkillProficiency.EXPERT.name
      ),
      supportWanted = listOf("Programming"),
      availability = "FLEXIBLE"
    )

    val ranked = ComplementarityEngine.matchIndividuals(
      seeker = seeker,
      candidates = listOf(beginnerPeer, expertPeer),
      mode = MatchingMode.CAPABILITY
    )

    assertEquals(2, ranked.size)
    assertEquals("u_expert", ranked.first().profile.userId)
    assertTrue(ranked.first().alignmentScore > ranked.last().alignmentScore)
    assertTrue(ranked.first().explanation.contains("Expert", ignoreCase = true))
    assertTrue(ranked.first().mutualExchangeSummary.contains("peer growth pairing", ignoreCase = true))
  }

  @Test
  fun collaborationMessage_buildThreadId_isSymmetric() {
    val forward = CollaborationMessage.buildThreadId("alice_1", "bob_2")
    val reverse = CollaborationMessage.buildThreadId("bob_2", "alice_1")
    assertEquals(forward, reverse)
    assertTrue(forward.startsWith("thr_alice_1_bob_2_"))
  }

  @Test
  fun intentDiscovery_discoversRelevantCommunityPostsByPurposeAndNeededSkills() {
    val seeker = CommunityProfile(
      userId = "u_me",
      displayName = "Amina",
      skills = listOf("Programming", "Audio engineering"),
      ambitions = listOf("Make neighborhood parks accessible and greener"),
      interests = listOf("Inclusive design", "Environmental monitoring")
    )

    val complementaryPost = CommunityPost(
      id = "p_tactile",
      authorId = "u_leo",
      authorName = "Leo",
      category = PostCategory.LOOKING_FOR_COLLABORATORS.name,
      title = "Need audio narrator and coder for sensory park guide",
      body = "We are planting trees and building Braille trail markers for blind visitors.",
      capabilityTags = listOf("Tactile design", "Community gardening"),
      requestedCapabilities = listOf("Audio engineering", "Programming")
    )

    val unrelatedPost = CommunityPost(
      id = "p_unrelated",
      authorId = "u_other",
      authorName = "Sam",
      category = PostCategory.RESOURCES.name,
      title = "Vintage stamp cataloging notes",
      body = "Archiving postage albums from the 1970s.",
      capabilityTags = listOf("Archiving")
    )

    val results = IntentDiscoveryEngine.discoverRelevantPosts(
      queryOrFocus = "accessible park signage",
      posts = listOf(unrelatedPost, complementaryPost),
      seekerProfile = seeker,
      seekerMissions = emptyList()
    )

    assertTrue(results.isNotEmpty())
    assertEquals("p_tactile", results.first().post.id)
    assertTrue(results.first().complementaryNeedsYouHold.isNotEmpty())
    assertTrue(results.first().whyRelevant.isNotBlank())
    assertFalse(results.any { it.post.id == "p_unrelated" })
  }

  @Test
  fun intentDiscovery_extractMissionDraftFromPost_mergesRequestedAndInferredCapabilities() {
    val post = CommunityPost(
      id = "p_air",
      authorId = "u_1",
      authorName = "Maya",
      category = PostCategory.IDEAS.name,
      title = "Idea: Neighborhood air quality sensor stations",
      body = "Measure air pollution near bus stops and publish clear visual charts for neighbors.",
      capabilityTags = listOf("Data collection", "Community outreach"),
      requestedCapabilities = listOf("Hardware soldering", "Visual design")
    )

    val draft = IntentDiscoveryEngine.extractMissionDraftFromPost(post)
    assertEquals("Neighborhood air quality sensor stations", draft.title)
    assertTrue(draft.requiredCapabilities.contains("Hardware soldering"))
    assertTrue(draft.requiredCapabilities.contains("Visual design"))
    assertTrue(draft.objective.isNotBlank())
  }

  @Test
  fun appThemeMode_safeParse_handlesCreamWhiteDarkAndFallback() {
    assertEquals(AppThemeMode.CREAM, AppThemeMode.safeParse("CREAM"))
    assertEquals(AppThemeMode.WHITE, AppThemeMode.safeParse("white"))
    assertEquals(AppThemeMode.DARK, AppThemeMode.safeParse("Dark"))
    assertEquals(AppThemeMode.CREAM, AppThemeMode.safeParse("unknown_mode"))
  }

  @Test
  fun communityPost_toCreateMap_stripsLocationTagWhenPrivacyDisabled() {
    val privatePost = CommunityPost(
      authorId = "u_1",
      authorName = "Amina",
      category = PostCategory.IDEAS.name,
      title = "Local workshop",
      body = "Testing privacy",
      communityLocationTag = "North Ward Center",
      shareLocationTag = false
    )
    val publicPost = privatePost.copy(shareLocationTag = true)

    assertEquals("", privatePost.toCreateMap()["communityLocationTag"])
    assertEquals(false, privatePost.toCreateMap()["shareLocationTag"])
    assertEquals("North Ward Center", publicPost.toCreateMap()["communityLocationTag"])
    assertEquals(true, publicPost.toCreateMap()["shareLocationTag"])
  }

  @Test
  fun communityMemberSearch_filtersByCapabilityTagAndInterests() {
    val profiles = listOf(
      CommunityProfile(
        userId = "u_me",
        displayName = "Current User",
        skills = listOf("Assistive tech"),
        interests = listOf("Assistive tech")
      ),
      CommunityProfile(
        userId = "u_elena",
        displayName = "Elena",
        skills = listOf("Tactile prototyping", "Visual design"),
        interests = listOf("Assistive tech", "Inclusive storytelling")
      ),
      CommunityProfile(
        userId = "u_marcus",
        displayName = "Marcus",
        skills = listOf("Audio engineering"),
        interests = listOf("Sound design")
      )
    )

    val byInterest = SymbiosisViewModel.filterMembersByCapabilityOrInterest(
      profiles = profiles,
      query = "Assistive tech",
      excludeUserId = "u_me"
    )
    assertEquals(1, byInterest.size)
    assertEquals("u_elena", byInterest.first().userId)

    val byCapability = SymbiosisViewModel.filterMembersByCapabilityOrInterest(
      profiles = profiles,
      query = "Audio",
      excludeUserId = "u_me"
    )
    assertEquals(1, byCapability.size)
    assertEquals("u_marcus", byCapability.first().userId)
  }

  @Test
  fun userProfileHelpers_resolveMissionHistoryAndFirestoreBookmarks() {
    val missions = listOf(
      Mission(id = "m_own", ownerId = "u_1", memberUids = listOf("u_1"), title = "Owned Mission"),
      Mission(id = "m_joined", ownerId = "u_2", memberUids = listOf("u_2", "u_1"), title = "Joined Mission"),
      Mission(id = "m_invited", ownerId = "u_3", memberUids = listOf("u_3"), title = "Accepted Invite Mission"),
      Mission(id = "m_other", ownerId = "u_4", memberUids = listOf("u_4"), title = "Unrelated Mission")
    )
    val acceptedInvites = listOf(
      CollaborationInvitation(
        id = "inv_1",
        missionId = "m_invited",
        senderId = "u_3",
        recipientId = "u_1",
        status = InvitationStatus.ACCEPTED.name
      )
    )

    val history = SymbiosisViewModel.computeMissionHistory("u_1", missions, acceptedInvites)
    assertEquals(setOf("m_own", "m_joined", "m_invited"), history.map { it.id }.toSet())

    val bookmarks = listOf(
      PostBookmark(id = "p_1", userId = "u_1", postId = "p_1", postTitle = "Rainwater Sensors"),
      PostBookmark(id = "p_2", userId = "u_1", postId = "p_2", postTitle = "Archived Thread")
    )
    val posts = listOf(
      CommunityPost(id = "p_1", authorId = "u_2", authorName = "Amina", title = "Rainwater Sensors Updated")
    )

    val entries = SymbiosisViewModel.resolveBookmarkedEntries(bookmarks, posts)
    assertEquals(2, entries.size)
    assertEquals("Rainwater Sensors Updated", entries[0].displayTitle)
    assertEquals("Archived Thread", entries[1].displayTitle)
    assertNull(entries[1].post)
  }

  @Test
  fun activeCollaborationsProgress_countsCompletedVsTotalTasksForActiveJoinedMissions() {
    val activeMission1 = Mission(
      id = "m_active_1",
      ownerId = "u_1",
      memberUids = listOf("u_1", "u_2"),
      title = "Rainwater Node",
      status = MissionStatus.ACTIVE.name,
      progressPercent = 50L
    )
    val activeMission2 = Mission(
      id = "m_open_2",
      ownerId = "u_2",
      memberUids = listOf("u_2"),
      title = "Audio Trail",
      status = MissionStatus.OPEN.name,
      progressPercent = 33L
    )
    val completedMission = Mission(
      id = "m_done",
      ownerId = "u_1",
      memberUids = listOf("u_1"),
      title = "Finished Compost",
      status = MissionStatus.COMPLETED.name,
      progressPercent = 100L
    )
    val acceptedInvites = listOf(
      CollaborationInvitation(
        id = "inv_join",
        missionId = "m_open_2",
        senderId = "u_2",
        recipientId = "u_1",
        status = InvitationStatus.ACCEPTED.name
      )
    )
    val milestonesByMission = mapOf(
      "m_active_1" to listOf(
        MissionMilestone(id = "t1", missionId = "m_active_1", title = "Wire sensor", completed = true),
        MissionMilestone(id = "t2", missionId = "m_active_1", title = "Build UI", completed = false)
      ),
      "m_open_2" to listOf(
        MissionMilestone(id = "t3", missionId = "m_open_2", title = "Record audio", completed = true),
        MissionMilestone(id = "t4", missionId = "m_open_2", title = "Test QR", completed = true),
        MissionMilestone(id = "t5", missionId = "m_open_2", title = "Print Braille", completed = false)
      ),
      "m_done" to listOf(
        MissionMilestone(id = "t6", missionId = "m_done", title = "Celebrate", completed = true)
      )
    )

    val summary = SymbiosisViewModel.computeActiveCollaborationProgress(
      userId = "u_1",
      missions = listOf(activeMission1, activeMission2, completedMission),
      acceptedInvitations = acceptedInvites,
      milestonesByMissionId = milestonesByMission
    )

    assertEquals(2, summary.activeCollaborations.size)
    assertEquals(3, summary.totalCompletedTasks)
    assertEquals(5, summary.totalTasks)
    assertEquals(60, summary.overallPercent)

    val first = summary.activeCollaborations.first { it.mission.id == "m_active_1" }
    assertEquals(1, first.completedTasks)
    assertEquals(2, first.totalTasks)
    assertEquals(50, first.progressPercent)
  }

  @Test
  fun suggestedForYouHelpers_resolvePostsAndCollaborationsAndIgnoreUnknownTargets() {
    val post = CommunityPost(id = "p_1", authorId = "u_2", authorName = "Mira", title = "Accessible Air Quality Poster")
    val mission = Mission(id = "m_2", ownerId = "u_3", ownerName = "Samir", title = "Tactile Botanical Trail")

    val recs = listOf(
      AiCommunityRecommendation(
        targetId = "p_1",
        targetType = CommunityRecommendationType.POST,
        reason = "Your Programming skills complement Mira's Visual design.",
        matchedSkills = listOf("Programming"),
        linkedMissionTitle = "Neighborhood Sensor Network",
        relevanceSummary = "Complements Design"
      ),
      AiCommunityRecommendation(
        targetId = "m_2",
        targetType = CommunityRecommendationType.COLLABORATION,
        reason = "Builds on your accessibility interest.",
        matchedSkills = listOf("Accessibility testing"),
        linkedMissionTitle = "",
        relevanceSummary = "Collaboration Fit"
      ),
      AiCommunityRecommendation(
        targetId = "non_existent_id",
        targetType = CommunityRecommendationType.POST,
        reason = "Should be filtered out",
        matchedSkills = emptyList(),
        linkedMissionTitle = "",
        relevanceSummary = ""
      )
    )

    val resolved = SymbiosisViewModel.resolveSuggestedForYouItems(
      recommendations = recs,
      candidatePosts = listOf(post),
      candidateMissions = listOf(mission)
    )

    assertEquals(2, resolved.size)
    assertTrue(resolved[0] is SuggestedForYouItem.PostSuggestion)
    assertEquals("p_1", (resolved[0] as SuggestedForYouItem.PostSuggestion).post.id)
    assertTrue(resolved[1] is SuggestedForYouItem.CollaborationSuggestion)
    assertEquals("m_2", (resolved[1] as SuggestedForYouItem.CollaborationSuggestion).mission.id)

    val sigA = SymbiosisViewModel.buildSuggestionSignature(
      userId = "u_1",
      skills = listOf("Visual design", "Programming"),
      missionHistoryIds = listOf("m_1"),
      candidatePostIds = listOf("p_1", "p_2"),
      candidateMissionIds = listOf("m_2")
    )
    val sigB = SymbiosisViewModel.buildSuggestionSignature(
      userId = "u_1",
      skills = listOf("programming", "visual design"),
      missionHistoryIds = listOf("m_1"),
      candidatePostIds = listOf("p_2", "p_1"),
      candidateMissionIds = listOf("m_2")
    )
    assertEquals(sigA, sigB)
  }

  @Test
  fun themeToggle_switchesToDarkAndRestoresPreviousLightVariant() {
    assertEquals(
      AppThemeMode.DARK,
      AccessibilityPreferencesRepository.computeNextToggledThemeMode(
        current = AppThemeMode.CREAM,
        lastLightMode = AppThemeMode.CREAM
      )
    )
    assertEquals(
      AppThemeMode.DARK,
      AccessibilityPreferencesRepository.computeNextToggledThemeMode(
        current = AppThemeMode.WHITE,
        lastLightMode = AppThemeMode.WHITE
      )
    )
    assertEquals(
      AppThemeMode.WHITE,
      AccessibilityPreferencesRepository.computeNextToggledThemeMode(
        current = AppThemeMode.DARK,
        lastLightMode = AppThemeMode.WHITE
      )
    )
    assertEquals(
      AppThemeMode.CREAM,
      AccessibilityPreferencesRepository.computeNextToggledThemeMode(
        current = AppThemeMode.DARK,
        lastLightMode = AppThemeMode.DARK
      )
    )
  }

  @Test
  fun overlappingSkillMatching_ranksCollaboratorsBySharedSkillsAndInterests() {
    val seeker = CommunityProfile(
      userId = "u_me",
      displayName = "Amina",
      skills = listOf("Programming", "Research", "Documentation"),
      interests = listOf("Environmental technology", "Open hardware")
    )

    val highOverlap = CommunityProfile(
      userId = "u_high",
      displayName = "Devon",
      skills = listOf("Coding", "Research", "Visual design"),
      interests = listOf("Environmental technology")
    )

    val singleOverlap = CommunityProfile(
      userId = "u_single",
      displayName = "Sora",
      skills = listOf("Research", "Community gardening", "Audio engineering"),
      interests = listOf("Urban ecology")
    )

    val zeroOverlap = CommunityProfile(
      userId = "u_none",
      displayName = "NoOverlap",
      skills = listOf("Audio engineering"),
      interests = listOf("Sound design")
    )

    val blockedOverlap = CommunityProfile(
      userId = "u_blocked",
      displayName = "Blocked",
      skills = listOf("Programming", "Research", "Documentation")
    )

    val overlaps = ComplementarityEngine.matchByOverlappingSkills(
      seeker = seeker,
      candidates = listOf(seeker, singleOverlap, zeroOverlap, blockedOverlap, highOverlap),
      blockedIds = setOf("u_blocked")
    )

    assertEquals(2, overlaps.size)
    assertEquals("u_high", overlaps.first().profile.userId)
    assertEquals(2, overlaps.first().overlappingSkills.size)
    assertEquals(listOf("Environmental technology"), overlaps.first().sharedInterests)
    assertTrue(overlaps.first().overlapScore > overlaps.last().overlapScore)

    val modeResults = ComplementarityEngine.matchIndividuals(
      seeker = seeker,
      candidates = listOf(singleOverlap, zeroOverlap, highOverlap),
      mode = MatchingMode.SKILL_OVERLAP
    )
    assertEquals(2, modeResults.size)
    assertEquals("u_high", modeResults.first().profile.userId)
    assertTrue(modeResults.first().explanation.contains("overlapping skills", ignoreCase = true))
  }

  @Test
  fun userProfile_toCommunityProfile_preservesUpdatedSkillsAndInterests() {
    val existing = CommunityProfile(
      userId = "u_1",
      displayName = "Amina",
      skills = listOf("Old skill"),
      interests = listOf("Old interest")
    )
    val updated = UserProfile(
      userId = "u_1",
      displayName = "Amina",
      skills = listOf("Programming", "Basic electronics"),
      interests = listOf("Open hardware", "Clean Energy")
    )

    val merged = updated.toCommunityProfile(existing = existing)
    assertEquals(listOf("Programming", "Basic electronics"), merged.skills)
    assertEquals(listOf("Open hardware", "Clean Energy"), merged.interests)
  }
}
