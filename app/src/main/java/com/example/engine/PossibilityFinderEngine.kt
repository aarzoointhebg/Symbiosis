package com.example.engine

import com.example.model.AnalyzedCapability
import com.example.model.CapabilityAvailabilityStatus
import com.example.model.CommunityProfile
import com.example.model.MatchingMode
import com.example.model.Mission
import com.example.model.PossibilityAnalysis

/**
 * Possibility Finder Engine:
 * Reveals what a person can accomplish by combining what they already bring
 * with complementary collaborators and alternative execution pathways.
 */
object PossibilityFinderEngine {

    fun analyzePossibility(
        objective: String,
        userContributions: List<String>,
        learningGoals: List<String>,
        supportWanted: List<String>,
        inferredEssentialReqs: List<String>,
        inferredOptionalReqs: List<String>,
        userProfile: CommunityProfile,
        communityProfiles: List<CommunityProfile>,
        blockedIds: Set<String> = emptySet(),
        analysisSource: String = "Complementarity Engine + Local Semantic Analysis"
    ): PossibilityAnalysis {
        val activeUser = userProfile.copy(
            skills = (userProfile.skills + userContributions).distinct(),
            supportWanted = (userProfile.supportWanted + supportWanted).distinct()
        )
        val userAllCaps = activeUser.allCapabilities()

        val essentialReqs = inferredEssentialReqs.ifEmpty {
            inferRequiredCapabilitiesFromText(objective)
        }
        val optionalReqs = inferredOptionalReqs.ifEmpty {
            listOf("Presentation design", "Documentation", "Accessibility testing")
                .filterNot { opt -> essentialReqs.any { ComplementarityEngine.capabilitiesMatch(it, opt) } }
        }

        val breakdown = mutableListOf<AnalyzedCapability>()
        essentialReqs.forEach { req ->
            val hasIt = userAllCaps.any { ComplementarityEngine.capabilitiesMatch(it, req) }
            breakdown.add(
                AnalyzedCapability(
                    name = req,
                    status = if (hasIt) CapabilityAvailabilityStatus.AVAILABLE else CapabilityAvailabilityStatus.MISSING,
                    note = if (hasIt) "Available in your capabilities" else "Missing — seek a complementary collaborator"
                )
            )
        }
        optionalReqs.forEach { opt ->
            val hasIt = userAllCaps.any { ComplementarityEngine.capabilitiesMatch(it, opt) }
            breakdown.add(
                AnalyzedCapability(
                    name = opt,
                    status = if (hasIt) CapabilityAvailabilityStatus.AVAILABLE else CapabilityAvailabilityStatus.OPTIONAL,
                    note = if (hasIt) "Available bonus strength" else "Optional — elevates polish & reach"
                )
            )
        }

        val missing = breakdown.filter { it.status == CapabilityAvailabilityStatus.MISSING }.map { it.name }
        val seekerWithNeeds = activeUser.copy(supportWanted = (activeUser.supportWanted + missing).distinct())

        val potentialCollaborators = ComplementarityEngine.matchIndividuals(
            seeker = seekerWithNeeds,
            candidates = communityProfiles,
            mode = MatchingMode.CAPABILITY,
            blockedIds = blockedIds
        ).take(5)

        val syntheticMission = Mission(
            id = "possibility_preview",
            ownerId = activeUser.userId,
            ownerName = activeUser.displayName,
            title = objective,
            description = objective,
            objective = objective,
            requiredCapabilities = essentialReqs,
            optionalCapabilities = optionalReqs,
            targetParticipants = 3L
        )

        val eligiblePool = listOf(activeUser) + communityProfiles.filter { it.userId != activeUser.userId && it.userId !in blockedIds }
        val groupCombinations = ComplementarityEngine.recommendCollectiveTeams(
            mission = syntheticMission,
            availableProfiles = eligiblePool,
            includeUser = activeUser,
            maxResults = 3
        )

        val alternatives = buildList {
            if (learningGoals.isNotEmpty()) {
                add("Co-Learning Path: Pair with a peer experienced in ${learningGoals.first()} to co-build while learning hands-on.")
            }
            if (missing.isNotEmpty()) {
                add("Modular Scope Path: Launch a v1 prototype using your ${userContributions.take(2).joinToString(" & ").ifBlank { "core strengths" }} first, then invite collaborators for ${missing.joinToString(", ")}.")
            }
            add("Community Showcase Path: Present an open concept brief in Shared Missions so complementary peers can express interest.")
        }

        val suggestedPlan = groupCombinations.firstOrNull()?.collaborationPlan ?: buildList {
            add("1. Anchor Your Contribution: Lead with ${userContributions.take(2).joinToString(" & ").ifBlank { "your existing strengths" }}.")
            if (missing.isNotEmpty()) {
                add("2. Bridge Missing Capabilities: Send consent-based invitations for ${missing.joinToString(", ")}.")
            }
            add("3. Co-Create Accessible Milestones: Split tasks fairly and review progress together.")
        }

        return PossibilityAnalysis(
            objective = objective,
            capabilityBreakdown = breakdown,
            potentialCollaborators = potentialCollaborators,
            groupCombinations = groupCombinations,
            alternativeApproaches = alternatives,
            suggestedPlan = suggestedPlan,
            analysisSource = analysisSource
        )
    }

    fun inferRequiredCapabilitiesFromText(text: String): List<String> {
        val intent = IntentDiscoveryEngine.extractLocalStructuredIntent(text)
        return intent.requiredCapabilities.ifEmpty {
            listOf("Programming", "Research", "Visual presentation")
        }
    }
}
