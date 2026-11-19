package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessibilityNew
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Biotech
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material.icons.rounded.ThumbUpOffAlt
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.SkillEndorsement
import com.example.model.SkillEndorsementSummary
import com.example.model.SkillProficiency
import com.example.ui.theme.BotanicalGreen
import com.example.ui.theme.ComplementaryAmber
import com.example.ui.theme.CoverageGreen
import com.example.ui.theme.DeepForestGreen
import com.example.ui.theme.MutedSage
import com.example.ui.theme.SoftEarthBrown
import com.example.ui.theme.WarmGoldenYellow

private data class ExpertiseVisualStyle(
    val icon: ImageVector,
    val accentColor: Color,
    val containerColor: Color
)

private fun styleForSkill(skill: String): ExpertiseVisualStyle {
    val s = skill.lowercase()
    return when {
        s.contains("program") || s.contains("code") || s.contains("kotlin") || s.contains("python") || s.contains("software") ->
            ExpertiseVisualStyle(Icons.Rounded.Code, DeepForestGreen, Color(0xFFE3EFE7))

        s.contains("electron") || s.contains("hardware") || s.contains("sensor") || s.contains("arduino") || s.contains("robot") ->
            ExpertiseVisualStyle(Icons.Rounded.Memory, BotanicalGreen, Color(0xFFE6F0E6))

        s.contains("design") || s.contains("visual") || s.contains("illustrat") || s.contains("ui") || s.contains("slide") ->
            ExpertiseVisualStyle(Icons.Rounded.Palette, ComplementaryAmber, WarmGoldenYellow.copy(alpha = 0.22f))

        s.contains("story") || s.contains("writ") || s.contains("doc") || s.contains("communicat") ->
            ExpertiseVisualStyle(Icons.Rounded.AutoStories, SoftEarthBrown, SoftEarthBrown.copy(alpha = 0.14f))

        s.contains("access") || s.contains("tactile") || s.contains("braille") || s.contains("inclusive") ->
            ExpertiseVisualStyle(Icons.Rounded.AccessibilityNew, CoverageGreen, CoverageGreen.copy(alpha = 0.14f))

        s.contains("audio") || s.contains("sound") || s.contains("voice") ->
            ExpertiseVisualStyle(Icons.Rounded.GraphicEq, DeepForestGreen, MutedSage.copy(alpha = 0.30f))

        s.contains("garden") || s.contains("botany") || s.contains("plant") || s.contains("ecolog") ->
            ExpertiseVisualStyle(Icons.Rounded.Spa, BotanicalGreen, MutedSage.copy(alpha = 0.28f))

        s.contains("research") || s.contains("data") || s.contains("sampl") || s.contains("analy") ->
            ExpertiseVisualStyle(Icons.Rounded.Biotech, DeepForestGreen, Color(0xFFE5EEE8))

        s.contains("workshop") || s.contains("coordinat") || s.contains("outreach") || s.contains("facil") ->
            ExpertiseVisualStyle(Icons.Rounded.Groups, SoftEarthBrown, WarmGoldenYellow.copy(alpha = 0.18f))

        else ->
            ExpertiseVisualStyle(Icons.Rounded.Psychology, DeepForestGreen, MutedSage.copy(alpha = 0.25f))
    }
}

/**
 * High-contrast, scannable visual skill badge displaying an expertise icon,
 * complementary indicator, peer endorsement count pill, and optional one-tap endorsement toggle.
 */
@Composable
fun VisualSkillBadge(
    skillName: String,
    proficiency: SkillProficiency? = null,
    isComplementary: Boolean = false,
    endorsementCount: Int = 0,
    isEndorsedByMe: Boolean = false,
    onBadgeClick: (() -> Unit)? = null,
    onToggleEndorse: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val style = styleForSkill(skillName)
    val borderColor = when {
        isEndorsedByMe -> CoverageGreen
        isComplementary -> style.accentColor.copy(alpha = 0.55f)
        else -> style.accentColor.copy(alpha = 0.28f)
    }

    Surface(
        modifier = modifier
            .then(
                if (onBadgeClick != null) Modifier.clickable(onClick = onBadgeClick) else Modifier
            ),
        shape = RoundedCornerShape(14.dp),
        color = if (isEndorsedByMe) CoverageGreen.copy(alpha = 0.12f) else style.containerColor,
        border = BorderStroke(if (isEndorsedByMe || isComplementary) 1.25.dp else 1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(start = 10.dp, end = if (onToggleEndorse != null) 6.dp else 10.dp, top = 7.dp, bottom = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = style.icon,
                contentDescription = null,
                tint = style.accentColor,
                modifier = Modifier.size(16.dp)
            )

            Text(
                text = skillName,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isComplementary || isEndorsedByMe) FontWeight.SemiBold else FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            if (proficiency != null) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = style.accentColor.copy(alpha = 0.14f)
                ) {
                    Text(
                        text = proficiency.label,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = style.accentColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            if (isComplementary) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = "Complements your profile",
                    tint = ComplementaryAmber,
                    modifier = Modifier.size(14.dp)
                )
            }

            if (endorsementCount > 0 && onToggleEndorse == null) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = CoverageGreen.copy(alpha = 0.16f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Verified,
                            contentDescription = "$endorsementCount peer endorsements",
                            tint = CoverageGreen,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "$endorsementCount",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = CoverageGreen
                        )
                    }
                }
            }

            if (onToggleEndorse != null) {
                val slug = skillName.lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_')
                Surface(
                    onClick = onToggleEndorse,
                    shape = RoundedCornerShape(50),
                    color = if (isEndorsedByMe) CoverageGreen else MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                    border = BorderStroke(
                        1.dp,
                        if (isEndorsedByMe) CoverageGreen else MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.testTag("endorse_skill_${slug}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isEndorsedByMe) Icons.Rounded.ThumbUp else Icons.Rounded.ThumbUpOffAlt,
                            contentDescription = if (isEndorsedByMe) "Remove endorsement for $skillName" else "Endorse $skillName",
                            tint = if (isEndorsedByMe) Color.White else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = when {
                                isEndorsedByMe && endorsementCount > 0 -> "Endorsed • $endorsementCount"
                                endorsementCount > 0 -> "Endorse • $endorsementCount"
                                else -> "Endorse"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = if (isEndorsedByMe) Color.White else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Reusable scannable expertise badges layout for user profiles.
 * Highlights complementary skills, expertise icons, and Firestore-backed peer endorsements.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileSkillBadgesSection(
    skills: List<String>,
    skillProficiencies: Map<String, String> = emptyMap(),
    complementarySkills: Set<String> = emptySet(),
    endorsementsBySkill: Map<String, SkillEndorsementSummary> = emptyMap(),
    title: String = "Visual Skill & Expertise Badges",
    subtitle: String? = null,
    onTapSkillFilter: ((String) -> Unit)? = null,
    onToggleEndorseSkill: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val cleanSkills = skills.map { it.trim() }.filter { it.isNotEmpty() }.distinctBy { it.lowercase() }
    if (cleanSkills.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            cleanSkills.forEach { skill ->
                val normalized = SkillEndorsement.normalizeSkillKey(skill)
                val summary = endorsementsBySkill[normalized]
                val isComp = complementarySkills.any { it.equals(skill, ignoreCase = true) }
                val rawLevel = skillProficiencies[skill]
                    ?: skillProficiencies.entries.firstOrNull { it.key.equals(skill, ignoreCase = true) }?.value
                val proficiency = rawLevel?.let { SkillProficiency.safeParse(it) }
                VisualSkillBadge(
                    skillName = skill,
                    proficiency = proficiency,
                    isComplementary = isComp,
                    endorsementCount = summary?.count ?: 0,
                    isEndorsedByMe = summary?.endorsedByCurrentUser ?: false,
                    onBadgeClick = onTapSkillFilter?.let { cb -> { cb(skill) } },
                    onToggleEndorse = onToggleEndorseSkill?.let { cb -> { cb(skill) } }
                )
            }
        }
    }
}
