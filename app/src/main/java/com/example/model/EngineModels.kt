package com.example.model

enum class MatchingMode(val title: String, val subtitle: String) {
    CAPABILITY(
        "Capability Matching",
        "Discover peers whose abilities complement yours."
    ),
    ASSISTANCE(
        "Assistance Matching",
        "Reciprocal task support where both people contribute."
    ),
    AMBITION(
        "Ambition Matching",
        "Connect with people pursuing shared or compatible goals."
    ),
    COLLECTIVE(
        "Collective Matching",
        "Form multi-person teams covering an entire objective."
    )
}

enum class DiscoveryCategory(val label: String, val description: String) {
    SAME_MISSION(
        "Same Mission",
        "Pursuing substantially the same objective despite different wording."
    ),
    RELATED_MISSION(
        "Related Missions",
        "Pursuing different but connected objectives within a shared purpose."
    ),
    COMPLEMENTARY_MISSION(
        "Complementary Missions",
        "Distinct objectives that strengthen one another through collaboration."
    )
}

data class IndividualMatchResult(
    val profile: CommunityProfile,
    val mode: MatchingMode,
    val complementaryCapabilities: List<String>,
    val mutualExchangeSummary: String,
    val sharedAmbitions: List<String>,
    val availabilityAligned: Boolean,
    val accessibilityNote: String,
    val explanation: String,
    val alignmentMetricLabel: String,
    val alignmentScore: Float // Transparently measures capability + ambition alignment (0..1), never human worth
)

data class RoleAssignment(
    val participant: CommunityProfile,
    val roleTitle: String,
    val contributedCapabilities: List<String>,
    val ambitionAlignment: String
)

data class CapabilityCoverageItem(
    val capability: String,
    val essential: Boolean,
    val coveredBy: List<String>
) {
    val isCovered: Boolean get() = coveredBy.isNotEmpty()
}

data class CollectiveTeamRecommendation(
    val teamMembers: List<CommunityProfile>,
    val roleAssignments: List<RoleAssignment>,
    val coverageItems: List<CapabilityCoverageItem>,
    val essentialCoveragePercent: Int,
    val totalCoveragePercent: Int,
    val uncoveredEssential: List<String>,
    val uncoveredOptional: List<String>,
    val feasibilityLabel: String,
    val workloadBalanceNote: String,
    val accessibilityAlignmentNote: String,
    val explanation: String,
    val collaborationPlan: List<String>
)

data class StructuredIntent(
    val originalTitle: String,
    val mainObjective: String,
    val broaderPurpose: String,
    val problemDomain: String,
    val intendedOutcome: String,
    val activities: List<String>,
    val requiredCapabilities: List<String>,
    val semanticConcepts: List<String>,
    val sourceLabel: String // e.g., "Gemini AI Logic" or "Local Semantic Engine"
)

data class IntentSearchResult(
    val mission: Mission,
    val category: DiscoveryCategory,
    val interpretedIntent: String,
    val semanticSimilarityPercent: Int,
    val relationshipExplanation: String,
    val collaborationPotential: String,
    val capabilityComplementNote: String,
    val ambitionAlignmentNote: String
)

enum class CapabilityAvailabilityStatus { AVAILABLE, MISSING, OPTIONAL }

data class AnalyzedCapability(
    val name: String,
    val status: CapabilityAvailabilityStatus,
    val note: String
)

data class PossibilityAnalysis(
    val objective: String,
    val capabilityBreakdown: List<AnalyzedCapability>,
    val potentialCollaborators: List<IndividualMatchResult>,
    val groupCombinations: List<CollectiveTeamRecommendation>,
    val alternativeApproaches: List<String>,
    val suggestedPlan: List<String>,
    val analysisSource: String
)

// Passwordless Email OTP Authentication Models (Part 11)
enum class OtpSessionStep {
    ENTER_EMAIL,
    VERIFY_CODE,
    VERIFIED
}

data class OtpAuthUiState(
    val email: String = "",
    val codeInput: String = "",
    val step: OtpSessionStep = OtpSessionStep.ENTER_EMAIL,
    val accountExists: Boolean? = null,
    val isSubmitting: Boolean = false,
    val statusBanner: String? = null,
    val errorMessage: String? = null,
    val resendCooldownSeconds: Int = 0,
    val remainingAttempts: Int = 5,
    val expiresInSeconds: Int = 0,
    val isLockedOut: Boolean = false,
    val devDemoPreviewHint: String? = null // Strictly debug/dev-only demonstration indicator
)
