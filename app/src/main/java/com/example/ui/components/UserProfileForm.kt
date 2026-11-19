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
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Interests
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Schedule
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.AvailabilityWindow
import com.example.model.CollaborationStyle
import com.example.model.SkillProficiency
import com.example.model.UserProfile

private val CURATED_SKILLS = listOf(
    "Programming",
    "Visual design",
    "Research",
    "Data collection",
    "Documentation",
    "Basic electronics",
    "Audio engineering",
    "Accessibility testing",
    "Workshop coordination",
    "Creative communication",
    "Community gardening",
    "Tactile prototyping"
)

private val CURATED_INTERESTS = listOf(
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

private val DAYS_OF_WEEK = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
private val TIME_SLOTS = listOf("Morning", "Afternoon", "Evening")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UserProfileForm(
    initialProfile: UserProfile,
    isSaving: Boolean = false,
    onSaveProfile: (UserProfile) -> Unit,
    onCancel: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var displayName by remember(initialProfile.userId, initialProfile.displayName) {
        mutableStateOf(initialProfile.displayName)
    }
    var headline by remember(initialProfile.userId, initialProfile.headline) {
        mutableStateOf(initialProfile.headline)
    }
    var bio by remember(initialProfile.userId, initialProfile.bio) {
        mutableStateOf(initialProfile.bio)
    }

    val selectedSkills = remember(initialProfile.userId, initialProfile.skills) {
        mutableStateListOf<String>().apply { addAll(initialProfile.skills) }
    }
    val skillProficiencies = remember(initialProfile.userId, initialProfile.skillProficiencies) {
        mutableStateMapOf<String, SkillProficiency>().apply {
            initialProfile.skills.forEach { skill ->
                put(skill, initialProfile.proficiencyFor(skill))
            }
        }
    }
    val selectedInterests = remember(initialProfile.userId, initialProfile.interests) {
        mutableStateListOf<String>().apply { addAll(initialProfile.interests) }
    }
    var selectedAvailability by remember(initialProfile.userId, initialProfile.availability) {
        mutableStateOf(initialProfile.availabilityWindow)
    }
    val selectedDays = remember(initialProfile.userId, initialProfile.availableDays) {
        mutableStateListOf<String>().apply { addAll(initialProfile.availableDays) }
    }
    val selectedTimeSlots = remember(initialProfile.userId, initialProfile.timeSlots) {
        mutableStateListOf<String>().apply { addAll(initialProfile.timeSlots) }
    }
    var selectedStyle by remember(initialProfile.userId, initialProfile.collaborationStyle) {
        mutableStateOf(initialProfile.preferredStyle)
    }

    var customSkillInput by remember { mutableStateOf("") }
    var customInterestInput by remember { mutableStateOf("") }
    var validationMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Card
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SymbiosisEmblem(size = 38.dp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Matching Profile Form",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Share your skills, interests, and availability so SYMBIOSIS can discover complementary collaborators.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                OutlinedTextField(
                    value = displayName,
                    onValueChange = {
                        displayName = it
                        validationMessage = null
                    },
                    label = { Text("Display Name (required)") },
                    placeholder = { Text("How collaborators address you") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("user_profile_name_input"),
                    shape = RoundedCornerShape(14.dp)
                )

                OutlinedTextField(
                    value = headline,
                    onValueChange = { headline = it },
                    label = { Text("Role or Focus (optional)") },
                    placeholder = { Text("e.g., Systems builder & community researcher") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("user_profile_headline_input"),
                    shape = RoundedCornerShape(14.dp)
                )

                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Short Intro (optional)") },
                    placeholder = { Text("What kind of collaborations energize you?") },
                    minLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("user_profile_bio_input"),
                    shape = RoundedCornerShape(14.dp)
                )
            }
        }

        // Skills Section
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Rounded.Psychology, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column {
                        Text(
                            text = "Skills & Capabilities",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Select what you can contribute (${selectedSkills.size} selected)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CURATED_SKILLS.forEach { skill ->
                        val active = selectedSkills.any { it.equals(skill, ignoreCase = true) }
                        FilterChip(
                            selected = active,
                            onClick = {
                                validationMessage = null
                                if (active) {
                                    selectedSkills.removeAll { it.equals(skill, ignoreCase = true) }
                                    skillProficiencies.keys.removeAll { it.equals(skill, ignoreCase = true) }
                                } else {
                                    selectedSkills.add(skill)
                                    if (skillProficiencies.keys.none { it.equals(skill, ignoreCase = true) }) {
                                        skillProficiencies[skill] = initialProfile.proficiencyFor(skill)
                                    }
                                }
                            },
                            modifier = Modifier.testTag("user_profile_skill_chip_${skill.lowercase().replace(" ", "_")}"),
                            label = { Text(skill) }
                        )
                    }
                }

                val customSkills = selectedSkills.filter { s ->
                    CURATED_SKILLS.none { it.equals(s, ignoreCase = true) }
                }
                if (customSkills.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        customSkills.forEach { custom ->
                            InputChip(
                                selected = true,
                                onClick = {
                                    selectedSkills.remove(custom)
                                    skillProficiencies.remove(custom)
                                },
                                label = { Text(custom) },
                                trailingIcon = {
                                    Icon(
                                        Icons.Rounded.Close,
                                        contentDescription = "Remove $custom",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = customSkillInput,
                        onValueChange = { customSkillInput = it },
                        label = { Text("Add another skill") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("user_profile_skill_input"),
                        shape = RoundedCornerShape(14.dp)
                    )
                    Button(
                        onClick = {
                            val clean = customSkillInput.trim()
                            if (clean.isNotEmpty() && selectedSkills.none { it.equals(clean, ignoreCase = true) }) {
                                selectedSkills.add(clean)
                                skillProficiencies[clean] = SkillProficiency.INTERMEDIATE
                                customSkillInput = ""
                                validationMessage = null
                            }
                        },
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("user_profile_add_skill_button")
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add")
                    }
                }

                if (selectedSkills.isNotEmpty()) {
                    HorizontalDivider()
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Self-Assess Skill Proficiency",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Rate each skill from Beginner to Expert so SYMBIOSIS weights your depth and suggests great growth pairings.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        selectedSkills.forEach { skill ->
                            val currentLevel = skillProficiencies[skill]
                                ?: skillProficiencies.entries.firstOrNull { it.key.equals(skill, ignoreCase = true) }?.value
                                ?: initialProfile.proficiencyFor(skill)
                            val slug = skill.lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_')

                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = skill,
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = currentLevel.shortLabel + " • " + currentLevel.label,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }

                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        SkillProficiency.entries.forEach { level ->
                                            FilterChip(
                                                selected = currentLevel == level,
                                                onClick = { skillProficiencies[skill] = level },
                                                modifier = Modifier.testTag("skill_level_${slug}_${level.name.lowercase()}"),
                                                label = { Text(level.label, style = MaterialTheme.typography.labelSmall) }
                                            )
                                        }
                                    }

                                    Text(
                                        text = currentLevel.description,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Interests Section
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Rounded.Interests, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column {
                        Text(
                            text = "Interests & Focus Areas",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Topics and missions you care about (${selectedInterests.size} selected)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CURATED_INTERESTS.forEach { interest ->
                        val active = selectedInterests.any { it.equals(interest, ignoreCase = true) }
                        FilterChip(
                            selected = active,
                            onClick = {
                                validationMessage = null
                                if (active) selectedInterests.removeAll { it.equals(interest, ignoreCase = true) }
                                else selectedInterests.add(interest)
                            },
                            modifier = Modifier.testTag("user_profile_interest_chip_${interest.lowercase().replace(" ", "_")}"),
                            label = { Text(interest) }
                        )
                    }
                }

                val customInterests = selectedInterests.filter { i ->
                    CURATED_INTERESTS.none { it.equals(i, ignoreCase = true) }
                }
                if (customInterests.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        customInterests.forEach { custom ->
                            InputChip(
                                selected = true,
                                onClick = { selectedInterests.remove(custom) },
                                label = { Text(custom) },
                                trailingIcon = {
                                    Icon(
                                        Icons.Rounded.Close,
                                        contentDescription = "Remove $custom",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = customInterestInput,
                        onValueChange = { customInterestInput = it },
                        label = { Text("Add another interest") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("user_profile_interest_input"),
                        shape = RoundedCornerShape(14.dp)
                    )
                    Button(
                        onClick = {
                            val clean = customInterestInput.trim()
                            if (clean.isNotEmpty() && selectedInterests.none { it.equals(clean, ignoreCase = true) }) {
                                selectedInterests.add(clean)
                                customInterestInput = ""
                                validationMessage = null
                            }
                        },
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("user_profile_add_interest_button")
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add")
                    }
                }
            }
        }

        // Availability Section
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Rounded.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column {
                        Text(
                            text = "Availability for Matching",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Used to align collaboration schedules and rhythms",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text("Primary Availability Window", style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AvailabilityWindow.entries.forEach { window ->
                        FilterChip(
                            selected = selectedAvailability == window,
                            onClick = { selectedAvailability = window },
                            modifier = Modifier.testTag("user_profile_availability_${window.name.lowercase()}"),
                            label = { Text(window.label) }
                        )
                    }
                }

                HorizontalDivider()

                Text("Specific Days (optional)", style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DAYS_OF_WEEK.forEach { day ->
                        val active = selectedDays.contains(day)
                        FilterChip(
                            selected = active,
                            onClick = {
                                if (active) selectedDays.remove(day) else selectedDays.add(day)
                            },
                            modifier = Modifier.testTag("user_profile_day_${day.lowercase()}"),
                            label = { Text(day) }
                        )
                    }
                }

                Text("Preferred Time of Day (optional)", style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TIME_SLOTS.forEach { slot ->
                        val active = selectedTimeSlots.contains(slot)
                        FilterChip(
                            selected = active,
                            onClick = {
                                if (active) selectedTimeSlots.remove(slot) else selectedTimeSlots.add(slot)
                            },
                            modifier = Modifier.testTag("user_profile_slot_${slot.lowercase()}"),
                            label = { Text(slot) }
                        )
                    }
                }

                Text("Collaboration Style", style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CollaborationStyle.entries.forEach { style ->
                        FilterChip(
                            selected = selectedStyle == style,
                            onClick = { selectedStyle = style },
                            modifier = Modifier.testTag("user_profile_style_${style.name.lowercase()}"),
                            label = { Text(style.label) }
                        )
                    }
                }
            }
        }

        // Live Matching Summary Preview
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Matching Engine Preview",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Skills (${selectedSkills.size}): ${
                        selectedSkills.joinToString(", ") { skill ->
                            val lvl = skillProficiencies[skill] ?: initialProfile.proficiencyFor(skill)
                            "$skill (${lvl.label})"
                        }.ifBlank { "Add at least one skill" }
                    }",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Interests (${selectedInterests.size}): ${selectedInterests.joinToString(", ").ifBlank { "Add at least one interest" }}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Availability: ${selectedAvailability.label}" +
                        (if (selectedDays.isNotEmpty()) " • ${selectedDays.joinToString(", ")}" else "") +
                        (if (selectedTimeSlots.isNotEmpty()) " (${selectedTimeSlots.joinToString("/")})" else "") +
                        " • ${selectedStyle.label}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        AnimatedVisibility(visible = validationMessage != null) {
            Text(
                text = validationMessage ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (onCancel != null) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .height(52.dp)
                        .testTag("user_profile_cancel_button"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Cancel")
                }
            }

            Button(
                onClick = {
                    val cleanName = displayName.trim()
                    when {
                        cleanName.length < 2 -> {
                            validationMessage = "Please enter a display name with at least 2 characters."
                        }
                        selectedSkills.isEmpty() -> {
                            validationMessage = "Select or add at least one skill for matching."
                        }
                        selectedInterests.isEmpty() -> {
                            validationMessage = "Select or add at least one interest for matching."
                        }
                        else -> {
                            validationMessage = null
                            val proficiencyMap = selectedSkills.associateWith { skill ->
                                (skillProficiencies[skill] ?: initialProfile.proficiencyFor(skill)).name
                            }
                            onSaveProfile(
                                initialProfile.copy(
                                    displayName = cleanName,
                                    headline = headline.trim(),
                                    bio = bio.trim(),
                                    skills = selectedSkills.toList(),
                                    skillProficiencies = proficiencyMap,
                                    interests = selectedInterests.toList(),
                                    availability = selectedAvailability.name,
                                    availableDays = selectedDays.toList(),
                                    timeSlots = selectedTimeSlots.toList(),
                                    collaborationStyle = selectedStyle.name,
                                    onboardingCompleted = true
                                )
                            )
                        }
                    }
                },
                enabled = !isSaving,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("user_profile_save_button"),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Rounded.Check, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Save Matching Profile")
                }
            }
        }
    }
}
