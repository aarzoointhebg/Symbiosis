package com.example.engine

import com.example.model.CommunityPost
import com.example.model.CommunityProfile
import com.example.model.DiscoveryCategory
import com.example.model.IntentPostRecommendation
import com.example.model.IntentSearchResult
import com.example.model.Mission
import com.example.model.StructuredIntent
import kotlin.math.sqrt

data class MissionDraftFromPost(
    val title: String,
    val description: String,
    val objective: String,
    val requiredCapabilities: List<String>,
    val optionalCapabilities: List<String>
)

/**
 * Intent-Based Semantic Mission Discovery Engine.
 * Searches for what people MEAN rather than exact keyword overlap,
 * classifying matches strictly into:
 * - SAME_MISSION
 * - RELATED_MISSION
 * - COMPLEMENTARY_MISSION
 */
object IntentDiscoveryEngine {

    private data class SemanticConceptCluster(
        val conceptId: String,
        val domain: String,
        val intentSummary: String,
        val triggers: Set<String>,
        val complementaryClusters: Set<String>
    )

    private val conceptClusters = listOf(
        SemanticConceptCluster(
            conceptId = "urban_greenery",
            domain = "Local Environmental Stewardship",
            intentSummary = "Increase neighborhood greenery, plant trees, and restore local green spaces.",
            triggers = setOf(
                "greener", "green", "tree", "trees", "planting", "plantation", "sapling", "forest",
                "park", "parks", "garden", "gardens", "flora", "foliage", "canopy", "restore",
                "vegetation", "spaces", "shade", "botanical", "neighbourhood", "neighborhood", "locality"
            ),
            complementaryClusters = setOf("waste_composting", "environmental_education", "env_monitoring")
        ),
        SemanticConceptCluster(
            conceptId = "waste_composting",
            domain = "Local Environmental Stewardship",
            intentSummary = "Reduce neighborhood waste and turn organic waste into soil for local greenery.",
            triggers = setOf(
                "waste", "compost", "composting", "recycle", "recycling", "plastic", "landfill",
                "organic", "mulch", "soil", "trash", "litter", "zero-waste", "sustainability"
            ),
            complementaryClusters = setOf("urban_greenery", "environmental_education")
        ),
        SemanticConceptCluster(
            conceptId = "environmental_education",
            domain = "Local Environmental Stewardship",
            intentSummary = "Educate and inspire residents and communities around sustainability and waste reduction.",
            triggers = setOf(
                "educate", "education", "awareness", "residents", "workshop", "campaign", "teach",
                "outreach", "guide", "neighbors", "reduce", "reduction", "habits"
            ),
            complementaryClusters = setOf("urban_greenery", "waste_composting", "env_monitoring")
        ),
        SemanticConceptCluster(
            conceptId = "env_monitoring",
            domain = "Environmental Science & Technology",
            intentSummary = "Measure, monitor, and visualize environmental conditions using sensors and field research.",
            triggers = setOf(
                "monitor", "monitoring", "sensor", "sensors", "air", "water", "quality", "temperature",
                "humidity", "pollution", "measurement", "telemetry", "scientific", "network", "interactive"
            ),
            complementaryClusters = setOf("urban_greenery", "environmental_education")
        ),
        SemanticConceptCluster(
            conceptId = "inclusive_accessibility",
            domain = "Inclusive Design & Accessibility",
            intentSummary = "Make spaces, learning tools, and experiences accessible across diverse abilities.",
            triggers = setOf(
                "accessible", "accessibility", "blind", "tactile", "braille", "audio", "wheelchair",
                "mobility", "captions", "inclusive", "assistive", "sensory", "screen", "reader"
            ),
            complementaryClusters = setOf("env_monitoring", "coding_mentorship", "creative_publishing")
        ),
        SemanticConceptCluster(
            conceptId = "coding_mentorship",
            domain = "Technology Learning & Co-Creation",
            intentSummary = "Learn, teach, or build software and coding skills collaboratively.",
            triggers = setOf(
                "programming", "code", "coding", "python", "kotlin", "software", "app", "website",
                "algorithm", "robotics", "learn", "mentor", "teach", "developer"
            ),
            complementaryClusters = setOf("env_monitoring", "inclusive_accessibility", "creative_publishing")
        ),
        SemanticConceptCluster(
            conceptId = "creative_publishing",
            domain = "Storytelling & Community Media",
            intentSummary = "Produce visual presentations, zines, publications, and inclusive community storytelling.",
            triggers = setOf(
                "publication", "magazine", "zine", "write", "writing", "story", "stories",
                "presentation", "showcase", "visual", "design", "art", "editorial"
            ),
            complementaryClusters = setOf("environmental_education", "inclusive_accessibility", "coding_mentorship")
        )
    )

    fun extractLocalStructuredIntent(
        title: String,
        description: String = "",
        objective: String = "",
        intendedOutcome: String = "",
        problemAddressed: String = ""
    ): StructuredIntent {
        val fullText = listOf(title, description, objective, intendedOutcome, problemAddressed)
            .joinToString(" ")
        val tokens = tokenize(fullText)

        val clusterScores = conceptClusters.map { cluster ->
            val hits = tokens.count { it in cluster.triggers }
            cluster to hits
        }.filter { it.second > 0 }.sortedByDescending { it.second }

        val topCluster = clusterScores.firstOrNull()?.first
        val concepts = clusterScores.map { it.first.conceptId }

        val interpretedObjective = when (topCluster?.conceptId) {
            "urban_greenery" -> "Improve the local environment by increasing greenery, planting trees, and restoring community green spaces."
            "waste_composting" -> "Reduce local waste and convert organics into nutrient-rich compost for community spaces."
            "environmental_education" -> "Empower residents and peers with practical knowledge on sustainability and waste reduction."
            "env_monitoring" -> "Build an interactive scientific prototype to collect and communicate environmental health data."
            "inclusive_accessibility" -> "Co-design accessible tools and multi-sensory guides so everyone can participate equally."
            "coding_mentorship" -> "Collaboratively build technical fluency through peer programming and hands-on prototypes."
            "creative_publishing" -> "Craft compelling visual and written storytelling to share ideas with a wider audience."
            else -> objective.ifBlank { description }.ifBlank { title }
        }

        val suggestedCaps = when (topCluster?.conceptId) {
            "urban_greenery" -> listOf("Community gardening", "Workshop coordination", "Visual design")
            "waste_composting" -> listOf("Composting", "Logistics", "Community outreach")
            "environmental_education" -> listOf("Creative communication", "Research", "Presentation design")
            "env_monitoring" -> listOf("Programming", "Research", "Data collection", "Visual presentation")
            "inclusive_accessibility" -> listOf("Accessibility testing", "Tactile prototyping", "Audio engineering")
            "coding_mentorship" -> listOf("Programming", "Logical problem-solving", "Documentation")
            "creative_publishing" -> listOf("Visual design", "Documentation", "Creative communication")
            else -> listOf("Research", "Creative communication", "Coordination")
        }

        return StructuredIntent(
            originalTitle = title,
            mainObjective = interpretedObjective,
            broaderPurpose = topCluster?.domain ?: "Collaborative Community Impact",
            problemDomain = topCluster?.domain ?: "Cross-Disciplinary Collaboration",
            intendedOutcome = intendedOutcome.ifBlank { topCluster?.intentSummary ?: title },
            activities = tokens.distinct().take(5),
            requiredCapabilities = suggestedCaps,
            semanticConcepts = concepts.ifEmpty { listOf("general_collaboration") },
            sourceLabel = "Local Semantic Concept Engine"
        )
    }

    fun searchByIntent(
        queryTitle: String,
        queryDescription: String,
        missions: List<Mission>,
        profiles: List<CommunityProfile>,
        seekerProfile: CommunityProfile? = null,
        aiExtractedIntent: StructuredIntent? = null,
        excludeMissionId: String? = null
    ): List<IntentSearchResult> {
        val queryIntent = aiExtractedIntent ?: extractLocalStructuredIntent(queryTitle, queryDescription)
        val queryText = listOf(
            queryTitle,
            queryDescription,
            queryIntent.mainObjective,
            queryIntent.broaderPurpose,
            queryIntent.intendedOutcome
        ).joinToString(" ")
        val queryVector = buildConceptVector(queryText)

        return missions
            .filter { it.id != excludeMissionId }
            .mapNotNull { mission ->
                val missionText = listOf(
                    mission.title,
                    mission.description,
                    mission.objective,
                    mission.intendedOutcome,
                    mission.problemAddressed,
                    mission.interpretedIntent
                ).joinToString(" ")

                val missionIntent = extractLocalStructuredIntent(
                    title = mission.title,
                    description = mission.description,
                    objective = mission.objective,
                    intendedOutcome = mission.intendedOutcome,
                    problemAddressed = mission.problemAddressed
                )

                val missionVector = buildConceptVector(missionText)
                val conceptSim = cosineSimilarity(queryVector.conceptWeights, missionVector.conceptWeights)
                val tokenSim = cosineSimilarity(queryVector.tokenWeights, missionVector.tokenWeights)

                val queryPrimary = queryVector.dominantConcept
                val missionPrimary = missionVector.dominantConcept

                val isComplementaryCluster = queryPrimary != null && missionPrimary != null &&
                    queryPrimary != missionPrimary &&
                    conceptClusters.firstOrNull { it.conceptId == queryPrimary }
                        ?.complementaryClusters?.contains(missionPrimary) == true

                val isSameDomain = queryIntent.problemDomain == missionIntent.problemDomain &&
                    queryIntent.problemDomain != "Cross-Disciplinary Collaboration"

                // Classify strictly into the three meaningful categories
                val category: DiscoveryCategory?
                val similarityScore: Float

                when {
                    // SAME MISSION: same core semantic cluster OR very high semantic vector alignment
                    (queryPrimary != null && queryPrimary == missionPrimary && conceptSim >= 0.55f) ||
                        (conceptSim >= 0.72f) ||
                        (tokenSim >= 0.62f) -> {
                        category = DiscoveryCategory.SAME_MISSION
                        similarityScore = maxOf(conceptSim, 0.78f)
                    }
                    // COMPLEMENTARY MISSION: distinct primary objective that strengthens the user's objective
                    isComplementaryCluster -> {
                        category = DiscoveryCategory.COMPLEMENTARY_MISSION
                        similarityScore = (0.64f + tokenSim * 0.25f).coerceIn(0.60f, 0.89f)
                    }
                    // RELATED MISSION: same broader domain or meaningful partial semantic overlap
                    isSameDomain || conceptSim >= 0.25f || tokenSim >= 0.18f -> {
                        category = DiscoveryCategory.RELATED_MISSION
                        similarityScore = maxOf(conceptSim, tokenSim, 0.45f).coerceAtMost(0.76f)
                    }
                    else -> {
                        category = null
                        similarityScore = 0f
                    }
                }

                if (category == null) return@mapNotNull null

                val ownerProfile = profiles.firstOrNull { it.userId == mission.ownerId }
                val capComplement = if (seekerProfile != null && ownerProfile != null) {
                    val theyHave = ownerProfile.allCapabilities().filter { c ->
                        seekerProfile.allCapabilities().none { ComplementarityEngine.capabilitiesMatch(c, it) }
                    }
                    if (theyHave.isNotEmpty()) {
                        "${ownerProfile.displayName} brings ${theyHave.take(3).joinToString(", ")}."
                    } else {
                        "Your capabilities reinforce shared execution."
                    }
                } else {
                    "Requires: ${mission.requiredCapabilities.take(3).joinToString(", ").ifBlank { "collaborative effort" }}."
                }

                val relationshipExplanation = when (category) {
                    DiscoveryCategory.SAME_MISSION ->
                        "Even though the wording differs (\"${mission.title}\"), both missions share the same core objective: ${missionIntent.mainObjective}"
                    DiscoveryCategory.COMPLEMENTARY_MISSION ->
                        "Different objective that strengthens yours: pairing \"${queryTitle}\" with \"${mission.title}\" creates a broader impact across ${missionIntent.problemDomain}."
                    DiscoveryCategory.RELATED_MISSION ->
                        "Connected within ${missionIntent.problemDomain}, pursuing an adjacent goal that could share insights or participants."
                }

                val collabPotential = when (category) {
                    DiscoveryCategory.SAME_MISSION ->
                        "Combine efforts into a single stronger team or coordinate milestones side by side (only after mutual consent)."
                    DiscoveryCategory.COMPLEMENTARY_MISSION ->
                        "Form a multi-part initiative where one team leads ${queryPrimary?.replace('_', ' ') ?: "the primary goal"} and the other leads ${missionPrimary?.replace('_', ' ') ?: "supporting action"}."
                    DiscoveryCategory.RELATED_MISSION ->
                        "Cross-pollinate research, tools, and lessons learned."
                }

                IntentSearchResult(
                    mission = mission,
                    category = category,
                    interpretedIntent = mission.interpretedIntent.ifBlank { missionIntent.mainObjective },
                    semanticSimilarityPercent = (similarityScore * 100).toInt().coerceIn(25, 99),
                    relationshipExplanation = relationshipExplanation,
                    collaborationPotential = collabPotential,
                    capabilityComplementNote = capComplement,
                    ambitionAlignmentNote = if (isSameDomain) {
                        "Aligned under ${missionIntent.problemDomain}"
                    } else {
                        "Cross-domain bridge into ${missionIntent.problemDomain}"
                    }
                )
            }
            .sortedWith(
                compareBy<IntentSearchResult> { it.category.ordinal }
                    .thenByDescending { it.semanticSimilarityPercent }
            )
    }

    private data class DualSemanticVector(
        val conceptWeights: Map<String, Float>,
        val tokenWeights: Map<String, Float>,
        val dominantConcept: String?
    )

    private fun buildConceptVector(text: String): DualSemanticVector {
        val tokens = tokenize(text)
        val tokenMap = mutableMapOf<String, Float>()
        tokens.forEach { t -> tokenMap[t] = (tokenMap[t] ?: 0f) + 1f }

        val conceptMap = mutableMapOf<String, Float>()
        conceptClusters.forEach { cluster ->
            var score = 0f
            tokens.forEach { token ->
                if (token in cluster.triggers) score += 1.0f
            }
            if (score > 0f) {
                conceptMap[cluster.conceptId] = score
            }
        }
        val dominant = conceptMap.maxByOrNull { it.value }?.key
        return DualSemanticVector(conceptMap, tokenMap, dominant)
    }

    private fun tokenize(text: String): List<String> {
        val stop = setOf("a", "an", "the", "our", "in", "on", "to", "for", "of", "and", "or", "with", "about", "make", "start", "build")
        return text.lowercase()
            .split(Regex("[^a-z0-9-]+"))
            .filter { it.length >= 3 && it !in stop }
    }

    private fun cosineSimilarity(a: Map<String, Float>, b: Map<String, Float>): Float {
        if (a.isEmpty() || b.isEmpty()) return 0f
        var dot = 0f
        a.forEach { (k, v) -> dot += v * (b[k] ?: 0f) }
        val magA = sqrt(a.values.sumOf { (it * it).toDouble() }.toFloat())
        val magB = sqrt(b.values.sumOf { (it * it).toDouble() }.toFloat())
        return if (magA == 0f || magB == 0f) 0f else (dot / (magA * magB)).coerceIn(0f, 1f)
    }

    /**
     * Section 19.3: Intent-Aware Community Discovery.
     * Interprets underlying mission & problem purpose rather than shallow keyword overlap,
     * surfacing posts where someone discusses the same problem, pursues a complementary goal,
     * or requests capabilities the seeker brings.
     */
    fun discoverRelevantPosts(
        queryOrFocus: String,
        posts: List<CommunityPost>,
        seekerProfile: CommunityProfile?,
        seekerMissions: List<Mission>,
        followedTopics: Set<String> = emptySet(),
        aiIntent: StructuredIntent? = null
    ): List<IntentPostRecommendation> {
        val seekerCaps = seekerProfile?.allCapabilities().orEmpty()
        val seekerNeeds = seekerProfile?.supportWanted.orEmpty()
        val seekerAmbitionText = buildString {
            if (queryOrFocus.isNotBlank()) {
                append(queryOrFocus).append(' ')
            }
            seekerProfile?.let { p ->
                append(p.ambitions.joinToString(" ")).append(' ')
                append(p.interests.joinToString(" ")).append(' ')
                append(p.headline).append(' ')
            }
            seekerMissions.take(3).forEach { m ->
                append(m.title).append(' ').append(m.objective).append(' ')
            }
            aiIntent?.let {
                append(it.mainObjective).append(' ')
                append(it.broaderPurpose).append(' ')
            }
        }.trim()

        val focusVector = buildConceptVector(seekerAmbitionText)
        val focusIntent = aiIntent ?: extractLocalStructuredIntent(
            title = queryOrFocus.ifBlank { seekerProfile?.ambitions?.firstOrNull().orEmpty() },
            description = seekerAmbitionText
        )

        return posts.mapNotNull { post ->
            val postFullText = listOf(
                post.title,
                post.body,
                post.missionTitle,
                post.capabilityTags.joinToString(" "),
                post.requestedCapabilities.joinToString(" ")
            ).joinToString(" ")

            val postVector = buildConceptVector(postFullText)
            val postIntent = extractLocalStructuredIntent(post.title, post.body)

            val conceptSim = cosineSimilarity(focusVector.conceptWeights, postVector.conceptWeights)
            val tokenSim = cosineSimilarity(focusVector.tokenWeights, postVector.tokenWeights)

            val focusPrimary = focusVector.dominantConcept
            val postPrimary = postVector.dominantConcept
            val isComplementaryCluster = focusPrimary != null && postPrimary != null &&
                focusPrimary != postPrimary &&
                conceptClusters.firstOrNull { it.conceptId == focusPrimary }
                    ?.complementaryClusters?.contains(postPrimary) == true

            // Capabilities you hold that this post explicitly asks for
            val needsYouHold = post.requestedCapabilities.filter { req ->
                seekerCaps.any { mine -> ComplementarityEngine.capabilitiesMatch(req, mine) }
            }.distinct()

            // Capabilities the post offers that match what you're seeking/interested in
            val matchingCaps = post.capabilityTags.filter { tag ->
                seekerNeeds.any { need -> ComplementarityEngine.capabilitiesMatch(tag, need) } ||
                    seekerCaps.any { mine -> ComplementarityEngine.capabilitiesMatch(tag, mine) } ||
                    followedTopics.any { topic ->
                        tag.contains(topic, ignoreCase = true) || topic.contains(tag, ignoreCase = true)
                    }
            }.distinct()

            val sameMission = post.missionId.isNotBlank() &&
                seekerMissions.any { it.id == post.missionId || it.title.equals(post.missionTitle, ignoreCase = true) }

            val topicHit = followedTopics.any { topic ->
                post.capabilityTags.any { it.equals(topic, ignoreCase = true) } ||
                    post.title.contains(topic, ignoreCase = true) ||
                    post.body.contains(topic, ignoreCase = true)
            }

            var score = (conceptSim * 0.42f + tokenSim * 0.28f)
            if (focusPrimary != null && focusPrimary == postPrimary) score += 0.20f
            if (isComplementaryCluster) score += 0.22f
            if (needsYouHold.isNotEmpty()) score += 0.25f
            if (matchingCaps.isNotEmpty()) score += 0.14f
            if (sameMission) score += 0.18f
            if (topicHit) score += 0.15f
            if (post.categoryEnum() == com.example.model.PostCategory.LOOKING_FOR_COLLABORATORS && needsYouHold.isNotEmpty()) {
                score += 0.10f
            }

            val finalScore = (score * 100).toInt().coerceIn(0, 98)
            if (queryOrFocus.isNotBlank() && finalScore < 22 && needsYouHold.isEmpty() && !topicHit) {
                return@mapNotNull null
            }
            if (queryOrFocus.isBlank() && finalScore < 26 && needsYouHold.isEmpty() && !sameMission && !topicHit) {
                return@mapNotNull null
            }

            val whyRelevant = buildString {
                when {
                    needsYouHold.isNotEmpty() -> {
                        append("${post.authorName} is looking for ${needsYouHold.joinToString(", ")}, which aligns directly with strengths you bring.")
                    }
                    isComplementaryCluster -> {
                        append("Complementary purpose in ${postIntent.problemDomain}: pairing this work strengthens ${focusIntent.problemDomain}.")
                    }
                    sameMission -> {
                        append("Directly connected to your mission \"${post.missionTitle}\".")
                    }
                    focusPrimary != null && focusPrimary == postPrimary -> {
                        append("Addresses the same core goal: ${postIntent.mainObjective}")
                    }
                    topicHit -> {
                        append("Matches a capability topic you follow (${matchingCaps.ifEmpty { followedTopics.take(2) }.joinToString(", ")}).")
                    }
                    else -> {
                        append("Aligned across ${postIntent.problemDomain} with shared interest in ${matchingCaps.ifEmpty { post.capabilityTags.take(2) }.joinToString(", ").ifBlank { "collaborative impact" }}.")
                    }
                }
            }

            IntentPostRecommendation(
                post = post,
                relevancePercent = finalScore.coerceAtLeast(35),
                whyRelevant = whyRelevant,
                matchingCapabilities = matchingCaps,
                complementaryNeedsYouHold = needsYouHold
            )
        }.sortedByDescending { it.relevancePercent }
    }

    /**
     * Section 19.5: Extract a structured Mission draft from a Community Post
     * (either directly or merged with Gemini AI suggestions).
     */
    fun extractMissionDraftFromPost(post: CommunityPost): MissionDraftFromPost {
        val localIntent = extractLocalStructuredIntent(
            title = post.title,
            description = post.body,
            objective = post.missionTitle
        )
        val combinedRequired = (post.requestedCapabilities + localIntent.requiredCapabilities)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }
            .take(5)

        val combinedOptional = (post.capabilityTags - combinedRequired.toSet())
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }
            .take(4)

        val cleanTitle = post.title
            .removePrefix("Idea:")
            .removePrefix("Update:")
            .trim()
            .take(110)

        return MissionDraftFromPost(
            title = cleanTitle.ifBlank { "Community Collaboration Mission" },
            description = post.body.trim().take(800),
            objective = localIntent.mainObjective.take(280),
            requiredCapabilities = combinedRequired.ifEmpty { listOf("Collaborative research", "Creative design") },
            optionalCapabilities = combinedOptional
        )
    }
}
