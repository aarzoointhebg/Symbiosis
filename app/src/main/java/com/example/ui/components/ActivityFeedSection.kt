package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Handshake
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.ActivityEventType
import com.example.model.ActivityFeedItem
import com.example.ui.theme.ComplementaryAmber
import com.example.ui.theme.CoverageGreen
import com.example.ui.theme.LocalSymbiosisIsDark
import com.example.ui.theme.WarmGoldenYellow

@Composable
fun ActivityFeedSection(
    items: List<ActivityFeedItem>,
    title: String = "Recent Activity & Trust Feed",
    subtitle: String = "Accepted collaborations, milestones & peer skill endorsements",
    maxItems: Int = 5,
    onOpenChatWithPeerId: ((String) -> Unit)? = null,
    onViewAllClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isDark = LocalSymbiosisIsDark.current
    val displayed = items.take(maxItems)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("activity_feed_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (onViewAllClick != null && items.size > maxItems) {
                    OutlinedButton(
                        onClick = onViewAllClick,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Text("See all (${items.size})", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            if (displayed.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "Collaborate on a mission, endorse a peer's strength, or complete a milestone to see your shared progress here.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            } else {
                displayed.forEachIndexed { idx, item ->
                    val (icon, tint) = when (item.type) {
                        ActivityEventType.COLLABORATION_ACCEPTED ->
                            Icons.Rounded.Handshake to (if (isDark) androidx.compose.ui.graphics.Color(0xFF81E0A5) else CoverageGreen)
                        ActivityEventType.ENDORSEMENT_RECEIVED,
                        ActivityEventType.ENDORSEMENT_GIVEN ->
                            Icons.Rounded.ThumbUp to (if (isDark) WarmGoldenYellow else ComplementaryAmber)
                        ActivityEventType.MISSION_MILESTONE ->
                            Icons.Rounded.CheckCircle to MaterialTheme.colorScheme.primary
                        ActivityEventType.MISSION_ACCOMPLISHED ->
                            Icons.Rounded.EmojiEvents to (if (isDark) WarmGoldenYellow else ComplementaryAmber)
                        ActivityEventType.COMMUNITY_POST ->
                            Icons.Rounded.Forum to MaterialTheme.colorScheme.secondary
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = tint.copy(alpha = 0.16f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = item.type.label,
                                tint = tint,
                                modifier = Modifier.padding(8.dp)
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = tint.copy(alpha = 0.14f)
                                ) {
                                    Text(
                                        text = item.badgeText,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = tint,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = item.type.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = item.detail,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (item.type == ActivityEventType.COLLABORATION_ACCEPTED &&
                                item.targetPeerId.isNotBlank() &&
                                onOpenChatWithPeerId != null
                            ) {
                                Spacer(Modifier.height(2.dp))
                                OutlinedButton(
                                    onClick = { onOpenChatWithPeerId(item.targetPeerId) },
                                    modifier = Modifier.testTag("activity_chat_${item.targetPeerId}"),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.primary
                                    ),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.Chat,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text("Open Chat with ${item.peerName}", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }

                    if (idx < displayed.lastIndex) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}
