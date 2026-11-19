package com.example

import com.example.engine.ComplementarityEngine
import com.example.model.CollaborationMessage
import com.example.model.CommunityProfile
import com.example.model.MatchingMode
import com.example.model.SkillProficiency
import org.junit.Assert.assertEquals
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
}
