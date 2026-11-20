package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Diversity3
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Hub
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.ActivityFeedItem
import com.example.model.CollectiveTeamRecommendation
import com.example.ui.components.ActivityFeedSection
import com.example.ui.components.CoverageItemRow
import com.example.ui.components.CoverageMeterBar
import com.example.ui.components.DuckAndRabbitStoryCard
import com.example.ui.components.SectionHeading
import com.example.ui.components.SymbiosisEmblem
import com.example.ui.components.SymbiosisWordmark
import com.example.ui.theme.BotanicalGreen
import com.example.ui.theme.DeepForestGreen
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.MutedSage
import com.example.ui.theme.SoftCream
import com.example.ui.theme.WarmGoldenYellow

@Composable
fun HomeScreen(
    userName: String,
    isCloudSynced: Boolean,
    featuredTeamPreview: CollectiveTeamRecommendation?,
    recentActivityItems: List<ActivityFeedItem> = emptyList(),
    onDiscoverPossibilities: () -> Unit,
    onOpenMatchingProfileForm: () -> Unit,
    onOpenUserProfile: () -> Unit = {},
    onSearchMatches: (String) -> Unit,
    onOpenDiscover: () -> Unit,
    onOpenCommunity: () -> Unit,
    onOpenAmbitionMap: () -> Unit,
    onOpenChatWithPeerId: (String) -> Unit = {},
    onViewFullActivity: () -> Unit = {},
    onStartOnboarding: () -> Unit
) {
    var showHowItWorks by remember { mutableStateOf(false) }
    var homeSearchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 760.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Hero Editorial Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_symbiosis_hero),
                    contentDescription = "Intertwined forest paths and golden botanical ribbons",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    DeepForestGreen.copy(alpha = 0.88f),
                                    ForestGreenDark.copy(alpha = 0.78f),
                                    BotanicalGreen.copy(alpha = 0.68f)
                                )
                            )
                        )
                )
                Column(
                    modifier = Modifier.padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SymbiosisWordmark(onDarkBackground = true, compact = true)
                        Surface(
                            color = WarmGoldenYellow.copy(alpha = 0.22f),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = if (isCloudSynced) "Cloud Synced" else "Local Mode",
                                style = MaterialTheme.typography.labelSmall,
                                color = SoftCream,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Text(
                        text = "Welcome, $userName",
                        style = MaterialTheme.typography.labelLarge,
                        color = MutedSage
                    )

                    Text(
                        text = "You don't have to do it all alone.",
                        style = MaterialTheme.typography.displayMedium,
                        color = SoftCream
                    )

                    Text(
                        text = "Discover people whose abilities, ambitions, and ideas complement yours. Together, you can make more possible.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = SoftCream.copy(alpha = 0.9f)
                    )

                    Text(
                        text = "\"What can we accomplish together that neither of us could accomplish alone?\"",
                        style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                        color = WarmGoldenYellow
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onDiscoverPossibilities,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("home_discover_possibilities_button"),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = WarmGoldenYellow,
                                contentColor = ForestGreenDark
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Discover Possibilities", maxLines = 1)
                        }

                        OutlinedButton(
                            onClick = { showHowItWorks = !showHowItWorks },
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("home_how_it_works_button"),
                            border = BorderStroke(1.dp, SoftCream.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                text = if (showHowItWorks) "Hide Guide" else "How It Works",
                                color = SoftCream
                            )
                        }
                    }
                }
            }

            if (showHowItWorks) {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Why SYMBIOSIS Matters",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "1. The Problem: People often hold bold ambitions yet lack specific skills, tools, or teammates — and keyword directories fail to reveal complementary possibilities.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "2. What SYMBIOSIS Does: It pairs AI semantic interpretation with a transparent, deterministic Complementarity Engine to match capabilities, ambitions, and multi-person groups.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "3. The Principle: You don't have to be everything. You just have to bring something — and together, we make something extraordinary possible.",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(onClick = onStartOnboarding) {
                                Text("Guided Profile Setup")
                            }
                            OutlinedButton(onClick = onOpenAmbitionMap) {
                                Icon(Icons.Rounded.Hub, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Ambition Map")
                            }
                        }
                    }
                }
            }

            // Matching Profile Callout (UserProfileForm)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                border = BorderStroke(1.dp, WarmGoldenYellow.copy(alpha = 0.55f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        color = DeepForestGreen,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.Tune, contentDescription = null, tint = SoftCream)
                        }
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Tailor Your Matching Profile",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            text = "Update your skills, interests, and schedule availability to sharpen match quality.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f)
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = onOpenUserProfile,
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("home_open_user_profile_button")
                        ) {
                            Text("View Profile")
                        }
                        OutlinedButton(
                            onClick = onOpenMatchingProfileForm,
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("home_open_matching_form_button")
                        ) {
                            Text("Edit Skills")
                        }
                    }
                }
            }

            // Quick Skill & Interest Search Bar -> Matches Screen
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Find Complementary Collaborators",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = homeSearchQuery,
                            onValueChange = { homeSearchQuery = it },
                            placeholder = { Text("Skill or interest (e.g. Electronics, Design)") },
                            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("home_search_bar")
                        )
                        Button(
                            onClick = { onSearchMatches(homeSearchQuery.trim()) },
                            modifier = Modifier
                                .height(52.dp)
                                .testTag("home_search_go_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Matches")
                        }
                    }
                }
            }

            // Editorial 2x2 Quick-Action Grid (Section 20)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HomeQuickActionTile(
                        icon = Icons.Rounded.Diversity3,
                        title = "Skill Matches",
                        subtitle = "Find complementary peers",
                        onClick = { onSearchMatches("") },
                        modifier = Modifier.weight(1f).testTag("home_quick_matches")
                    )
                    HomeQuickActionTile(
                        icon = Icons.Rounded.Forum,
                        title = "Community",
                        subtitle = "Ideas, updates & help",
                        onClick = onOpenCommunity,
                        modifier = Modifier.weight(1f).testTag("home_quick_community")
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HomeQuickActionTile(
                        icon = Icons.Rounded.AutoAwesome,
                        title = "Possibilities",
                        subtitle = "Assemble full teams",
                        onClick = onDiscoverPossibilities,
                        modifier = Modifier.weight(1f).testTag("home_quick_possibilities")
                    )
                    HomeQuickActionTile(
                        icon = Icons.Rounded.Explore,
                        title = "Intent Search",
                        subtitle = "Match by purpose",
                        onClick = onOpenDiscover,
                        modifier = Modifier.weight(1f).testTag("home_quick_intent")
                    )
                }
            }

            // Recent Activity & Trust Endorsements Feed
            ActivityFeedSection(
                items = recentActivityItems,
                title = "Recent Activity & Collaborations",
                subtitle = "Accepted partnerships, milestones & skill endorsements",
                maxItems = 3,
                onOpenChatWithPeerId = onOpenChatWithPeerId,
                onViewAllClick = onViewFullActivity
            )

            // Founding Story & Mascots (Duck & Rabbit — opens on Founding Story illustration by default)
            DuckAndRabbitStoryCard(showArtworkBanner = true)

            // The Three Dimensions (Part 4)
            SectionHeading(
                title = "The Three Dimensions",
                subtitle = "How SYMBIOSIS turns individual strengths into shared possibilities"
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DimensionCard(
                    icon = Icons.Rounded.Psychology,
                    title = "1. Complementary Capabilities",
                    example = "Research + Design + Programming",
                    description = "Discover collaborators whose strengths cover what you don't carry alone — without fixed helper/recipient labels."
                )
                DimensionCard(
                    icon = Icons.Rounded.Lightbulb,
                    title = "2. Shared Ambitions",
                    example = "Teaching & Learning • Local Stewardship • Publications",
                    description = "Connect with people pursuing the same or compatible goals, even when they describe them in completely different words."
                )
                DimensionCard(
                    icon = Icons.Rounded.Groups,
                    title = "3. Collective Possibilities",
                    example = "Multi-person teams covering complete objectives",
                    description = "Our set-cover optimization identifies groups of 2 or more collaborators whose combined strengths unlock bigger missions."
                )
            }

            // Complementarity Engine Preview
            if (featuredTeamPreview != null) {
                SectionHeading(
                    title = "Complementarity Engine Preview",
                    subtitle = "Transparent, inspectable matching grounded in real profiles"
                )
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
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SymbiosisEmblem(size = 34.dp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Neighborhood Environmental Sensor Network",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = featuredTeamPreview.teamMembers.joinToString(" + ") { it.displayName },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }

                        CoverageMeterBar(
                            percent = featuredTeamPreview.essentialCoveragePercent,
                            label = "Essential Capability Coverage"
                        )

                        Text(
                            text = featuredTeamPreview.explanation,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        featuredTeamPreview.coverageItems.take(4).forEach { item ->
                            CoverageItemRow(item)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onOpenDiscover,
                                modifier = Modifier.weight(1f).height(48.dp)
                            ) {
                                Icon(Icons.Rounded.Explore, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Intent Search", maxLines = 1)
                            }
                            Button(
                                onClick = { onSearchMatches("") },
                                modifier = Modifier.weight(1f).height(48.dp)
                            ) {
                                Icon(Icons.Rounded.Diversity3, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Open Matches", maxLines = 1)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DimensionCard(
    icon: ImageVector,
    title: String,
    example: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Text(example, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun HomeQuickActionTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
