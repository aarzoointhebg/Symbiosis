package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Interests
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val SUGGESTED_PROFILE_SKILLS = listOf(
    "Programming",
    "Visual design",
    "Research",
    "Data collection",
    "Documentation",
    "Basic electronics",
    "Audio engineering",
    "Accessibility testing",
    "Workshop coordination",
    "Community gardening"
)

private val SUGGESTED_PROFILE_INTERESTS = listOf(
    "Environmental technology",
    "Assistive tech",
    "Neighborhood greenery",
    "Open hardware",
    "Circular living",
    "Inclusive storytelling",
    "Urban ecology",
    "Digital literacy",
    "Sound design",
    "Civic innovation"
)

/**
 * Reusable Profile UI component that lets users input, customize, and remove
 * their skills and interests, then persist them directly to Cloud Firestore.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileSkillsAndInterestsEditor(
    initialSkills: List<String>,
    initialInterests: List<String>,
    isCloudSynced: Boolean,
    isSaving: Boolean,
    onSaveSkillsAndInterests: (skills: List<String>, interests: List<String>) -> Unit,
    onCancel: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val skills = remember(initialSkills) {
        mutableStateListOf<String>().apply {
            addAll(initialSkills.map { it.trim() }.filter { it.isNotEmpty() }.distinctBy { it.lowercase() })
        }
    }
    val interests = remember(initialInterests) {
        mutableStateListOf<String>().apply {
            addAll(initialInterests.map { it.trim() }.filter { it.isNotEmpty() }.distinctBy { it.lowercase() })
        }
    }

    var customSkillText by remember { mutableStateOf("") }
    var customInterestText by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("profile_skills_interests_editor"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Update Skills & Interests",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (isCloudSynced) {
                            "Stored in Cloud Firestore for collaborator matching"
                        } else {
                            "Saved to your local profile workspace"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CloudDone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (isCloudSynced) "Firestore" else "Local",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Skills input
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Psychology,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Skills (${skills.size})",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SUGGESTED_PROFILE_SKILLS.forEach { suggestion ->
                    val selected = skills.any { it.equals(suggestion, ignoreCase = true) }
                    FilterChip(
                        selected = selected,
                        onClick = {
                            validationError = null
                            if (selected) {
                                skills.removeAll { it.equals(suggestion, ignoreCase = true) }
                            } else if (skills.size < 20) {
                                skills.add(suggestion)
                            }
                        },
                        label = { Text(suggestion, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.testTag("profile_editor_skill_${suggestion.lowercase().replace(" ", "_")}")
                    )
                }
            }

            val customSkills = skills.filter { s ->
                SUGGESTED_PROFILE_SKILLS.none { it.equals(s, ignoreCase = true) }
            }
            if (customSkills.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    customSkills.forEach { custom ->
                        InputChip(
                            selected = true,
                            onClick = { skills.remove(custom) },
                            label = { Text(custom, style = MaterialTheme.typography.labelSmall) },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Remove skill $custom",
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = customSkillText,
                    onValueChange = { customSkillText = it },
                    label = { Text("Add a skill") },
                    placeholder = { Text("e.g., Robotics, Hydroponics") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("profile_editor_custom_skill_input")
                )
                Button(
                    onClick = {
                        val clean = customSkillText.trim().take(60)
                        if (clean.isNotEmpty() && skills.none { it.equals(clean, ignoreCase = true) } && skills.size < 20) {
                            skills.add(clean)
                            customSkillText = ""
                            validationError = null
                        }
                    },
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("profile_editor_add_skill_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add")
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Interests input
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Interests,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Interests (${interests.size})",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SUGGESTED_PROFILE_INTERESTS.forEach { suggestion ->
                    val selected = interests.any { it.equals(suggestion, ignoreCase = true) }
                    FilterChip(
                        selected = selected,
                        onClick = {
                            validationError = null
                            if (selected) {
                                interests.removeAll { it.equals(suggestion, ignoreCase = true) }
                            } else if (interests.size < 20) {
                                interests.add(suggestion)
                            }
                        },
                        label = { Text(suggestion, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.testTag("profile_editor_interest_${suggestion.lowercase().replace(" ", "_")}")
                    )
                }
            }

            val customInterests = interests.filter { i ->
                SUGGESTED_PROFILE_INTERESTS.none { it.equals(i, ignoreCase = true) }
            }
            if (customInterests.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    customInterests.forEach { custom ->
                        InputChip(
                            selected = true,
                            onClick = { interests.remove(custom) },
                            label = { Text(custom, style = MaterialTheme.typography.labelSmall) },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Remove interest $custom",
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = customInterestText,
                    onValueChange = { customInterestText = it },
                    label = { Text("Add an interest") },
                    placeholder = { Text("e.g., Clean Energy, Music Tech") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("profile_editor_custom_interest_input")
                )
                Button(
                    onClick = {
                        val clean = customInterestText.trim().take(60)
                        if (clean.isNotEmpty() && interests.none { it.equals(clean, ignoreCase = true) } && interests.size < 20) {
                            interests.add(clean)
                            customInterestText = ""
                            validationError = null
                        }
                    },
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("profile_editor_add_interest_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add")
                }
            }

            AnimatedVisibility(visible = validationError != null) {
                Text(
                    text = validationError.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (onCancel != null) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("profile_editor_cancel_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Close")
                    }
                }
                Button(
                    onClick = {
                        when {
                            skills.isEmpty() -> {
                                validationError = "Please select or add at least one skill."
                            }
                            interests.isEmpty() -> {
                                validationError = "Please select or add at least one interest."
                            }
                            else -> {
                                validationError = null
                                onSaveSkillsAndInterests(skills.toList(), interests.toList())
                            }
                        }
                    },
                    enabled = !isSaving,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("profile_skills_interests_save_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Rounded.CloudDone, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(if (isCloudSynced) "Save to Firestore" else "Save Skills & Interests")
                    }
                }
            }
        }
    }
}
