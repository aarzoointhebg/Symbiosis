package com.example.engine

import com.example.model.CapabilityCoverageItem
import com.example.model.CollectiveTeamRecommendation
import com.example.model.CommunityProfile
import com.example.model.IndividualMatchResult
import com.example.model.MatchingMode
import com.example.model.Mission
import com.example.model.RoleAssignment
import kotlin.math.min

/**
 * Deterministic, inspectable Complementarity Engine.
 * Never ranks human worth; transparently measures capability coverage,
 * mutual complementarity, ambition alignment, availability, and fair workload balance.
 */
object ComplementarityEngine {

    private val synonymGroups: List<Set<String>> = listOf(
        setOf("programming", "coding", "software", "software development", "python", "kotlin", "embedded coding"),
        setOf("electronics", "basic electronics", "circuits", "sensors", "microcontrollers", "arduino", "hardware"),
        setOf("visual design", "presentation design", "visual presentation", "graphic design", "ui design", "slides", "illustration"),
        setOf("research", "scientific research", "environmental research", "investigation", "literature review"),
        setOf("data collection", "field sampling", "measurements", "telemetry", "sensor logging", "data logging"),
        setOf("documentation", "technical writing", "writing", "report writing", "storytelling"),
        setOf("creative communication", "communication", "public speaking", "storytelling", "presentation"),
        setOf("logical problem-solving", "problem solving", "systems thinking", "debugging", "analysis"),
        setOf("accessibility testing", "inclusive design", "tactile prototyping", "screen reader testing", "braille"),
        setOf("community gardening", "tree planting", "botany", "composting", "urban greenery", "gardening"),
        setOf("workshop coordination", "event organizing", "facilitation", "community outreach", "education")
    )

    fun normalizeCapability(raw: String): String {
        val cleaned = raw.trim().lowercase().replace(Regex("[^a-z0-9\\s-]"), " ").replace(Regex("\\s+"), " ")
        if (cleaned.isEmpty()) return ""
        val group = synonymGroups.firstOrNull { syn ->
            syn.any { term -> cleaned == term || cleaned.contains(term) || term.contains(cleaned) }
        }
        return group?.first() ?: cleaned
    }

    fun capabilitiesMatch(a: String, b: String): Boolean {
        val na = normalizeCapability(a)
        val nb = normalizeCapability(b)
        if (na.isEmpty() || nb.isEmpty()) return false
        if (na == nb) return true
        val tokensA = na.split(" ").filter { it.length > 2 }.toSet()
        val tokensB = nb.split(" ").filter { it.length > 2 }.toSet()
        return tokensA.isNotEmpty() && tokensB.isNotEmpty() && tokensA.intersect(tokensB).isNotEmpty()
    }

    fun matchIndividuals(
        seeker: CommunityProfile,
        candidates: List<CommunityProfile>,
        mode: MatchingMode,
        blockedIds: Set<String> = emptySet(),
        activeLoadByUser: Map<String, Int> = emptyMap()
    ): List<IndividualMatchResult> {
        val seekerCaps = seeker.allCapabilities()
        val seekerSupportNeeds = seeker.supportWanted
        val seekerAmbitions = seeker.ambitions + seeker.interests

        return candidates
            .filter { it.userId != seeker.userId && it.userId !in blockedIds }
            .mapNotNull { candidate ->
                val candCaps = candidate.allCapabilities()
                val candNeeds = candidate.supportWanted
                val candAmbitions = candidate.ambitions + candidate.interests

                // Capabilities candidate brings that seeker lacks, requested support on, or holds at Beginner level
                val mentorshipSynergies = candCaps.filter { cap ->
                    val candProf = candidate.proficiencyFor(cap)
                    candProf.level >= 3 && seekerCaps.any { mine ->
                        capabilitiesMatch(cap, mine) && seeker.proficiencyFor(mine).level == 1
                    }
                }.distinct()

                val complementary = candCaps.filter { cap ->
                    seekerSupportNeeds.any { need -> capabilitiesMatch(cap, need) } ||
                        seekerCaps.none { mine -> capabilitiesMatch(cap, mine) } ||
                        cap in mentorshipSynergies
                }.distinct()

                // Reciprocal: capabilities seeker brings that candidate appreciates support on or lacks (weighted by seeker's proficiency)
                val seekerHelpsCand = seekerCaps.filter { cap ->
                    candNeeds.any { need -> capabilitiesMatch(cap, need) } ||
                        candCaps.none { theirs -> capabilitiesMatch(cap, theirs) } ||
                        (seeker.proficiencyFor(cap).level >= 3 && candCaps.any { theirs ->
                            capabilitiesMatch(cap, theirs) && candidate.proficiencyFor(theirs).level == 1
                        })
                }.distinct()

                val sharedAmbitions = findSharedConceptPhrases(seekerAmbitions, candAmbitions)
                val availabilityAligned = seeker.availability == candidate.availability ||
                    seeker.availability == "FLEXIBLE" ||
                    candidate.availability == "FLEXIBLE"

                val loadCount = activeLoadByUser[candidate.userId] ?: candidate.currentProjects.size
                val workloadFactor = (1.0f - (loadCount * 0.08f)).coerceIn(0.65f, 1.0f)

                val weightedComplementaryUnits = complementary
                    .sortedByDescending { candidate.proficiencyFor(it).matchingWeight }
                    .take(4)
                    .sumOf { candidate.proficiencyFor(it).matchingWeight.toDouble() }
                    .toFloat()
                val weightedReciprocalUnits = seekerHelpsCand
                    .sortedByDescending { seeker.proficiencyFor(it).matchingWeight }
                    .take(3)
                    .sumOf { seeker.proficiencyFor(it).matchingWeight.toDouble() }
                    .toFloat()
                val mentorshipBonus = (mentorshipSynergies.size.coerceAtMost(2) * 0.06f)

                val score = when (mode) {
                    MatchingMode.CAPABILITY -> {
                        val compRatio = (weightedComplementaryUnits / 4.2f).coerceIn(0f, 1f) * 0.60f
                        val mutualRatio = (weightedReciprocalUnits / 3.1f).coerceIn(0f, 1f) * 0.24f
                        val availBonus = if (availabilityAligned) 0.10f else 0f
                        (compRatio + mutualRatio + availBonus + mentorshipBonus) * workloadFactor
                    }
                    MatchingMode.ASSISTANCE -> {
                        val directNeedScore = candCaps.filter { cap ->
                            seekerSupportNeeds.any { need -> capabilitiesMatch(cap, need) } || cap in mentorshipSynergies
                        }.take(3).sumOf { candidate.proficiencyFor(it).matchingWeight.toDouble() }.toFloat()
                        val reciprocalScore = seekerCaps.filter { cap ->
                            candNeeds.any { need -> capabilitiesMatch(cap, need) }
                        }.take(3).sumOf { seeker.proficiencyFor(it).matchingWeight.toDouble() }.toFloat()
                        if (directNeedScore == 0f && reciprocalScore == 0f) return@mapNotNull null
                        val base = ((directNeedScore * 0.45f) + (reciprocalScore * 0.45f)) / 3.0f +
                            (if (availabilityAligned) 0.10f else 0f) + mentorshipBonus
                        base.coerceIn(0.15f, 1f) * workloadFactor
                    }
                    MatchingMode.AMBITION -> {
                        if (sharedAmbitions.isEmpty()) return@mapNotNull null
                        val ambRatio = (sharedAmbitions.size.coerceAtMost(3) / 3.0f) * 0.72f
                        val compBonus = (weightedComplementaryUnits / 4.0f).coerceIn(0f, 1f) * 0.18f
                        val availBonus = if (availabilityAligned) 0.10f else 0f
                        (ambRatio + compBonus + availBonus) * workloadFactor
                    }
                    MatchingMode.COLLECTIVE -> {
                        val compRatio = (weightedComplementaryUnits / 4.2f).coerceIn(0f, 1f) * 0.58f
                        val ambRatio = (sharedAmbitions.size.coerceAtMost(2) / 2.0f) * 0.32f
                        val availBonus = if (availabilityAligned) 0.10f else 0f
                        (compRatio + ambRatio + availBonus) * workloadFactor
                    }
                }

                val highlightedCandSkills = complementary
                    .sortedByDescending { candidate.proficiencyFor(it).level }
                    .take(3)
                    .joinToString(", ") { cap ->
                        "$cap (${candidate.proficiencyFor(cap).label})"
                    }
                val highlightedSeekerSkills = seekerHelpsCand
                    .sortedByDescending { seeker.proficiencyFor(it).level }
                    .take(2)
                    .joinToString(", ") { cap ->
                        "$cap (${seeker.proficiencyFor(cap).label})"
                    }

                val mutualSummary = buildString {
                    if (complementary.isNotEmpty()) {
                        append("${candidate.displayName} brings $highlightedCandSkills.")
                    }
                    if (mentorshipSynergies.isNotEmpty()) {
                        append(" Great peer growth pairing on ${mentorshipSynergies.take(2).joinToString(", ")}.")
                    }
                    if (seekerHelpsCand.isNotEmpty()) {
                        append(" In return, you contribute $highlightedSeekerSkills.")
                    }
                }.ifBlank { "Potential peer collaborator with compatible schedule." }

                val accessibilityNote = if (candidate.accessibilityPrefs.isNotEmpty()) {
                    "Prefers: ${candidate.accessibilityPrefs.joinToString(", ")}"
                } else {
                    "Flexible inclusive collaboration"
                }

                val explanation = when (mode) {
                    MatchingMode.CAPABILITY ->
                        "${candidate.displayName} complements your profile with ${highlightedCandSkills.ifBlank { "distinct perspectives" }}, while your ${highlightedSeekerSkills.ifBlank { "experience" }} balances the partnership."
                    MatchingMode.ASSISTANCE ->
                        "Reciprocal fit: ${candidate.displayName} brings ${highlightedCandSkills.ifBlank { "targeted support" }} and welcomes collaboration on ${candNeeds.take(2).joinToString(", ").ifBlank { "shared tasks" }}."
                    MatchingMode.AMBITION ->
                        "Shared direction around ${sharedAmbitions.take(2).joinToString(" & ")}. Combining proficiency levels expands what you can build."
                    MatchingMode.COLLECTIVE ->
                        "Strong collective contributor covering ${highlightedCandSkills.ifBlank { "key areas" }} with balanced availability."
                }

                val metricLabel = when (mode) {
                    MatchingMode.CAPABILITY -> "${(score * 100).toInt()}% capability complementarity"
                    MatchingMode.ASSISTANCE -> "${(score * 100).toInt()}% reciprocal fit"
                    MatchingMode.AMBITION -> "${(score * 100).toInt()}% ambition alignment"
                    MatchingMode.COLLECTIVE -> "${(score * 100).toInt()}% group contribution fit"
                }

                IndividualMatchResult(
                    profile = candidate,
                    mode = mode,
                    complementaryCapabilities = complementary,
                    mutualExchangeSummary = mutualSummary,
                    sharedAmbitions = sharedAmbitions,
                    availabilityAligned = availabilityAligned,
                    accessibilityNote = accessibilityNote,
                    explanation = explanation,
                    alignmentMetricLabel = metricLabel,
                    alignmentScore = score.coerceIn(0f, 1f)
                )
            }
            .sortedByDescending { it.alignmentScore }
    }

    /**
     * Weighted Set-Cover Group Optimization for Collective Matching.
     * Evaluates combinations of 2..targetSize participants across actual profiles,
     * balancing essential coverage, optional coverage, complementarity,
     * availability, accessibility compatibility, and workload distribution.
     */
    fun recommendCollectiveTeams(
        mission: Mission,
        availableProfiles: List<CommunityProfile>,
        includeUser: CommunityProfile? = null,
        maxResults: Int = 3,
        activeLoadByUser: Map<String, Int> = emptyMap()
    ): List<CollectiveTeamRecommendation> {
        val essential = mission.requiredCapabilities.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        val optional = mission.optionalCapabilities.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        if (essential.isEmpty() && optional.isEmpty()) return emptyList()

        val pool = availableProfiles.distinctBy { it.userId }
        if (pool.isEmpty()) return emptyList()

        val targetTeamSize = mission.targetParticipants.toInt().coerceIn(2, min(5, pool.size.coerceAtLeast(2)))

        // Generate candidate subsets of size 2..min(targetTeamSize, pool.size) using greedy seeds + combinations
        val candidateTeams = mutableListOf<List<CommunityProfile>>()

        // 1. Greedy weighted set-cover solution (anchor = includeUser if provided, else best first cover)
        val greedyTeam = buildGreedyCoverTeam(
            essential = essential,
            optional = optional,
            pool = pool,
            anchor = includeUser,
            maxTeamSize = targetTeamSize,
            activeLoadByUser = activeLoadByUser
        )
        if (greedyTeam.size >= 2) candidateTeams.add(greedyTeam)

        // 2. Alternative team combinations (excluding one member or starting from different anchors)
        if (pool.size <= 10) {
            val combos = generateCombinations(pool, min(targetTeamSize, pool.size))
            combos.forEach { combo ->
                if (includeUser == null || combo.any { it.userId == includeUser.userId }) {
                    candidateTeams.add(combo)
                }
            }
            if (targetTeamSize > 2 && pool.size >= 2) {
                generateCombinations(pool, 2).forEach { pair ->
                    if (includeUser == null || pair.any { it.userId == includeUser.userId }) {
                        candidateTeams.add(pair)
                    }
                }
            }
        } else {
            // For larger pools, build diverse greedy teams starting from top single contributors
            pool.take(6).forEach { seed ->
                val alt = buildGreedyCoverTeam(essential, optional, pool, seed, targetTeamSize, activeLoadByUser)
                if (alt.size >= 2) candidateTeams.add(alt)
            }
        }

        val uniqueTeams = candidateTeams
            .distinctBy { team -> team.map { it.userId }.sorted().joinToString(",") }

        val evaluated = uniqueTeams.map { team ->
            evaluateTeam(mission, essential, optional, team, activeLoadByUser)
        }

        return evaluated
            .sortedWith(
                compareByDescending<CollectiveTeamRecommendation> { it.essentialCoveragePercent }
                    .thenByDescending { it.totalCoveragePercent }
                    .thenBy { it.teamMembers.size }
            )
            .take(maxResults)
    }

    private fun buildGreedyCoverTeam(
        essential: List<String>,
        optional: List<String>,
        pool: List<CommunityProfile>,
        anchor: CommunityProfile?,
        maxTeamSize: Int,
        activeLoadByUser: Map<String, Int>
    ): List<CommunityProfile> {
        val selected = mutableListOf<CommunityProfile>()
        val coveredEssential = mutableSetOf<String>()
        val coveredOptional = mutableSetOf<String>()

        if (anchor != null && pool.any { it.userId == anchor.userId }) {
            selected.add(anchor)
            val caps = anchor.allCapabilities()
            essential.filterTo(coveredEssential) { req -> caps.any { capabilitiesMatch(it, req) } }
            optional.filterTo(coveredOptional) { opt -> caps.any { capabilitiesMatch(it, opt) } }
        }

        while (selected.size < maxTeamSize) {
            val remaining = pool.filter { cand -> selected.none { it.userId == cand.userId } }
            if (remaining.isEmpty()) break

            val bestNext = remaining.maxByOrNull { cand ->
                val caps = cand.allCapabilities()
                val newEssWeight = essential
                    .filter { req -> req !in coveredEssential }
                    .sumOf { req ->
                        val bestMatch = caps.filter { capabilitiesMatch(it, req) }
                            .maxOfOrNull { cand.proficiencyFor(it).matchingWeight } ?: 0f
                        bestMatch.toDouble()
                    }
                val newOptWeight = optional
                    .filter { opt -> opt !in coveredOptional }
                    .sumOf { opt ->
                        val bestMatch = caps.filter { capabilitiesMatch(it, opt) }
                            .maxOfOrNull { cand.proficiencyFor(it).matchingWeight } ?: 0f
                        bestMatch.toDouble()
                    }
                val load = activeLoadByUser[cand.userId] ?: cand.currentProjects.size
                val workloadBonus = (3 - load.coerceAtMost(3)) * 0.25
                (newEssWeight * 3.0) + (newOptWeight * 1.2) + workloadBonus
            } ?: break

            val bestCaps = bestNext.allCapabilities()
            val addsValue = essential.any { it !in coveredEssential && bestCaps.any { c -> capabilitiesMatch(c, it) } } ||
                optional.any { it !in coveredOptional && bestCaps.any { c -> capabilitiesMatch(c, it) } } ||
                selected.size < 2

            if (!addsValue && coveredEssential.size == essential.size) break

            selected.add(bestNext)
            essential.filterTo(coveredEssential) { req -> bestCaps.any { capabilitiesMatch(it, req) } }
            optional.filterTo(coveredOptional) { opt -> bestCaps.any { capabilitiesMatch(it, opt) } }
        }
        return selected
    }

    private fun evaluateTeam(
        mission: Mission,
        essential: List<String>,
        optional: List<String>,
        team: List<CommunityProfile>,
        activeLoadByUser: Map<String, Int>
    ): CollectiveTeamRecommendation {
        val coverageItems = mutableListOf<CapabilityCoverageItem>()
        essential.forEach { req ->
            val providers = team.filter { member ->
                member.allCapabilities().any { capabilitiesMatch(it, req) }
            }.map { it.displayName }
            coverageItems.add(CapabilityCoverageItem(req, essential = true, coveredBy = providers))
        }
        optional.forEach { opt ->
            val providers = team.filter { member ->
                member.allCapabilities().any { capabilitiesMatch(it, opt) }
            }.map { it.displayName }
            coverageItems.add(CapabilityCoverageItem(opt, essential = false, coveredBy = providers))
        }

        val essCovered = coverageItems.count { it.essential && it.isCovered }
        val essPercent = if (essential.isEmpty()) 100 else ((essCovered * 100f) / essential.size).toInt()
        val totalCovered = coverageItems.count { it.isCovered }
        val totalAll = coverageItems.size.coerceAtLeast(1)
        val totalPercent = ((totalCovered * 100f) / totalAll).toInt()

        val uncoveredEssential = coverageItems.filter { it.essential && !it.isCovered }.map { it.capability }
        val uncoveredOptional = coverageItems.filter { !it.essential && !it.isCovered }.map { it.capability }

        val roleAssignments = team.map { member ->
            val memberCaps = member.allCapabilities()
            val matchedReqs = (essential + optional).filter { req ->
                memberCaps.any { capabilitiesMatch(it, req) }
            }.distinct()
            val displayCaps = if (matchedReqs.isNotEmpty()) matchedReqs else memberCaps.take(2)
            val roleTitle = inferRoleTitle(displayCaps)
            val ambitionMatch = findSharedConceptPhrases(
                listOf(mission.title, mission.objective),
                member.ambitions + member.interests
            ).firstOrNull() ?: member.ambitions.firstOrNull() ?: "Shared mission curiosity"
            RoleAssignment(
                participant = member,
                roleTitle = roleTitle,
                contributedCapabilities = displayCaps,
                ambitionAlignment = ambitionMatch
            )
        }

        val feasibilityLabel = when {
            essPercent == 100 && uncoveredOptional.isEmpty() -> "Fully Covered & Ready"
            essPercent == 100 -> "Strong Essential Coverage"
            essPercent >= 70 -> "Promising (Needs ${uncoveredEssential.joinToString(", ")})"
            else -> "Partial Coverage (${uncoveredEssential.size} essential gaps)"
        }

        val maxLoad = team.maxOfOrNull { activeLoadByUser[it.userId] ?: it.currentProjects.size } ?: 0
        val workloadNote = if (maxLoad <= 1) {
            "Balanced workload: no member is overloaded across active missions."
        } else {
            "Fair distribution checked; consider lightweight roles for members active in $maxLoad other initiatives."
        }

        val allAccessPrefs = team.flatMap { it.accessibilityPrefs }.distinct()
        val accessibilityNote = if (allAccessPrefs.isNotEmpty()) {
            "Team accessibility plan honors: ${allAccessPrefs.take(4).joinToString(", ")}."
        } else {
            "Ensure shared documents, clear visual & audio summaries, and flexible pacing."
        }

        val contributionsSentence = roleAssignments.joinToString(", ") { ra ->
            "${ra.participant.displayName} contributes ${ra.contributedCapabilities.joinToString(" & ").ifBlank { "collaborative support" }}"
        }
        val explanation = buildString {
            append("This team covers $essCovered of ${essential.size} essential requirements ($essPercent%): $contributionsSentence. ")
            if (uncoveredEssential.isEmpty()) {
                append("Together, their combined capabilities fulfill the core requirements of \"${mission.title}\".")
            } else {
                append("Missing essential capability: ${uncoveredEssential.joinToString(", ")} — invite another contributor or adjust scope.")
            }
        }

        val plan = buildList {
            add("1. Kickoff & Accessibility Check: Align on ${team.flatMap { it.communicationPrefs }.distinct().take(2).joinToString(" & ").ifBlank { "shared chat + visual/audio notes" }}.")
            roleAssignments.forEachIndexed { index, ra ->
                add("${index + 2}. ${ra.participant.displayName} (${ra.roleTitle}): Lead ${ra.contributedCapabilities.take(2).joinToString(" & ").ifBlank { "mission tasks" }}.")
            }
            if (uncoveredEssential.isNotEmpty()) {
                add("${roleAssignments.size + 2}. Gap Strategy: Seek a collaborator or learning resource for ${uncoveredEssential.joinToString(", ")}.")
            } else {
                add("${roleAssignments.size + 2}. Integration & Launch: Combine research, build, and accessible storytelling for community release.")
            }
        }

        return CollectiveTeamRecommendation(
            teamMembers = team,
            roleAssignments = roleAssignments,
            coverageItems = coverageItems,
            essentialCoveragePercent = essPercent,
            totalCoveragePercent = totalPercent,
            uncoveredEssential = uncoveredEssential,
            uncoveredOptional = uncoveredOptional,
            feasibilityLabel = feasibilityLabel,
            workloadBalanceNote = workloadNote,
            accessibilityAlignmentNote = accessibilityNote,
            explanation = explanation,
            collaborationPlan = plan
        )
    }

    private fun inferRoleTitle(caps: List<String>): String {
        val joined = caps.joinToString(" ").lowercase()
        return when {
            joined.contains("program") || joined.contains("cod") || joined.contains("electronic") ->
                "Systems & Technical Builder"
            joined.contains("visual") || joined.contains("present") || joined.contains("design") ->
                "Visual Storytelling & Design Lead"
            joined.contains("research") || joined.contains("data") || joined.contains("document") ->
                "Research & Data Investigation Lead"
            joined.contains("accessib") || joined.contains("tactile") || joined.contains("audio") ->
                "Inclusive Experience Architect"
            joined.contains("garden") || joined.contains("tree") || joined.contains("compost") ->
                "Ecological Action Coordinator"
            else -> "Collaborative Contributor"
        }
    }

    private fun findSharedConceptPhrases(a: List<String>, b: List<String>): List<String> {
        val stopWords = setOf("the", "and", "for", "with", "want", "build", "create", "make", "project", "based", "into", "from")
        val matches = mutableListOf<String>()
        for (phraseA in a) {
            val wordsA = phraseA.lowercase().split(Regex("[^a-z0-9]+")).filter { it.length > 3 && it !in stopWords }.toSet()
            for (phraseB in b) {
                val wordsB = phraseB.lowercase().split(Regex("[^a-z0-9]+")).filter { it.length > 3 && it !in stopWords }.toSet()
                if (wordsA.intersect(wordsB).isNotEmpty()) {
                    matches.add(phraseA)
                    break
                }
            }
        }
        return matches.distinct()
    }

    private fun <T> generateCombinations(items: List<T>, k: Int): List<List<T>> {
        if (k == 0) return listOf(emptyList())
        if (items.size < k) return emptyList()
        val result = mutableListOf<List<T>>()
        fun backtrack(start: Int, current: MutableList<T>) {
            if (current.size == k) {
                result.add(current.toList())
                return
            }
            for (i in start until items.size) {
                current.add(items[i])
                backtrack(i + 1, current)
                current.removeAt(current.lastIndex)
            }
        }
        backtrack(0, mutableListOf())
        return result
    }
}
