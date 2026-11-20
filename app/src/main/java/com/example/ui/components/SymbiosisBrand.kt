package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.BotanicalGreen
import com.example.ui.theme.DarkBarkText
import com.example.ui.theme.DarkForestSurfaceVariant
import com.example.ui.theme.DeepForestGreen
import com.example.ui.theme.DuckBeakOrange
import com.example.ui.theme.DuckYellow
import com.example.ui.theme.LocalSymbiosisIsDark
import com.example.ui.theme.MutedSage
import com.example.ui.theme.PlayfairDisplayFontFamily
import com.example.ui.theme.RabbitCream
import com.example.ui.theme.RabbitEarPink
import com.example.ui.theme.SoftCream
import com.example.ui.theme.SoftEarthBrown
import com.example.ui.theme.WarmGoldenYellow
import com.example.ui.theme.WarmWhite

/**
 * Primary abstract SYMBIOSIS symbol (strictly animal-free).
 * Two complete, flowing organic ribbons intertwining to form an infinity-heart emblem.
 */
@Composable
fun SymbiosisEmblem(
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    onDarkBackground: Boolean = false
) {
    val primaryRibbon = if (onDarkBackground) SoftCream else DeepForestGreen
    val secondaryRibbon = WarmGoldenYellow
    val botanicalLeaf = if (onDarkBackground) MutedSage else BotanicalGreen

    Canvas(
        modifier = modifier
            .size(size)
            .semantics {
                contentDescription = "SYMBIOSIS abstract emblem showing two intertwining organic forms"
            }
    ) {
        val w = this.size.width
        val h = this.size.height
        val strokeW = w * 0.088f

        // Left complete flowing organic loop
        val leftLoop = Path().apply {
            moveTo(w * 0.50f, h * 0.52f)
            cubicTo(w * 0.37f, h * 0.27f, w * 0.16f, h * 0.22f, w * 0.12f, h * 0.42f)
            cubicTo(w * 0.08f, h * 0.61f, w * 0.22f, h * 0.77f, w * 0.38f, h * 0.73f)
            cubicTo(w * 0.48f, h * 0.70f, w * 0.54f, h * 0.56f, w * 0.62f, h * 0.44f)
            cubicTo(w * 0.69f, h * 0.34f, w * 0.77f, h * 0.34f, w * 0.82f, h * 0.39f)
        }
        drawPath(
            path = leftLoop,
            color = primaryRibbon,
            style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Right complete flowing organic loop
        val rightLoop = Path().apply {
            moveTo(w * 0.50f, h * 0.52f)
            cubicTo(w * 0.63f, h * 0.27f, w * 0.84f, h * 0.22f, w * 0.88f, h * 0.42f)
            cubicTo(w * 0.92f, h * 0.61f, w * 0.78f, h * 0.77f, w * 0.62f, h * 0.73f)
            cubicTo(w * 0.52f, h * 0.70f, w * 0.46f, h * 0.56f, w * 0.38f, h * 0.44f)
            cubicTo(w * 0.31f, h * 0.34f, w * 0.23f, h * 0.34f, w * 0.18f, h * 0.39f)
        }
        drawPath(
            path = rightLoop,
            color = secondaryRibbon,
            style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Subtle botanical sprout at crown
        val leafPath = Path().apply {
            moveTo(w * 0.50f, h * 0.10f)
            quadraticTo(w * 0.58f, h * 0.19f, w * 0.50f, h * 0.28f)
            quadraticTo(w * 0.42f, h * 0.19f, w * 0.50f, h * 0.10f)
        }
        drawPath(path = leafPath, color = botanicalLeaf)

        // Restrained golden heart-spark at base
        val sparkPath = Path().apply {
            moveTo(w * 0.50f, h * 0.79f)
            lineTo(w * 0.535f, h * 0.855f)
            lineTo(w * 0.60f, h * 0.89f)
            lineTo(w * 0.535f, h * 0.925f)
            lineTo(w * 0.50f, h * 0.99f)
            lineTo(w * 0.465f, h * 0.925f)
            lineTo(w * 0.40f, h * 0.89f)
            lineTo(w * 0.465f, h * 0.855f)
            close()
        }
        drawPath(path = sparkPath, color = secondaryRibbon)
    }
}

@Composable
fun SymbiosisWordmark(
    modifier: Modifier = Modifier,
    onDarkBackground: Boolean = false,
    showTagline: Boolean = true,
    compact: Boolean = false
) {
    val titleColor = if (onDarkBackground) SoftCream else MaterialTheme.colorScheme.primary
    val taglineColor = if (onDarkBackground) MutedSage else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 14.dp)
    ) {
        SymbiosisEmblem(
            size = if (compact) 38.dp else 54.dp,
            onDarkBackground = onDarkBackground
        )
        Column {
            Text(
                text = "SYMBIOSIS",
                fontFamily = PlayfairDisplayFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = if (compact) 20.sp else 27.sp,
                letterSpacing = 1.6.sp,
                color = titleColor
            )
            if (showTagline) {
                Text(
                    text = "Different Abilities. Shared Possibilities.",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontStyle = FontStyle.Italic
                    ),
                    color = taglineColor
                )
            }
        }
    }
}

/**
 * Storybook illustration scene of the Duck (yellow duck, green blindfold & scarf, white cane)
 * and the Rabbit (cream rabbit, pink inner ears, rosy cheeks, wheelchair when contextually appropriate),
 * plus a Role-Exchange toggle demonstrating equal, non-fixed collaboration.
 */
@Composable
fun DuckAndRabbitStoryCard(
    modifier: Modifier = Modifier,
    showArtworkBanner: Boolean = true
) {
    var roleExchanged by remember { mutableStateOf(true) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (!roleExchanged) "The Founding Story" else "Exchanging Roles Together",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Mascot Companions • Mutual Interdependence",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                OutlinedButton(
                    onClick = { roleExchanged = !roleExchanged },
                    modifier = Modifier.testTag("toggle_mascot_roles_button"),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SwapHoriz,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(if (roleExchanged) "Founding Scene" else "Switch Roles")
                }
            }

            val isDark = LocalSymbiosisIsDark.current

            if (!roleExchanged && showArtworkBanner) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_mascots_banner),
                        contentDescription = "The Founding Story illustration: the blind yellow duck with a white cane walking beside the sighted cream rabbit in a wheelchair toward a sunlit pavilion",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(206.dp)
                            .clip(RoundedCornerShape(18.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            } else {
                // Interactive Role-Exchange Storybook Scene
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(178.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = if (isDark) {
                                    listOf(DarkForestSurfaceVariant, MaterialTheme.colorScheme.surface)
                                } else {
                                    listOf(SoftCream, Color(0xFFEDF2E4))
                                }
                            )
                        )
                ) {
                    DuckAndRabbitCanvas(
                        roleExchanged = roleExchanged,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(178.dp)
                    )
                }
            }

            AnimatedVisibility(visible = !roleExchanged, enter = fadeIn()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Two friends wanted to reach the same hilltop destination. The duck was blind (wearing a green eye-covering and carrying a white cane) and could walk with steady steps. The rabbit could see clearly ahead and used a wheelchair on rough trails.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = "The duck contributed mobility; the rabbit contributed visual guidance. Neither had to become someone else — together, different abilities created shared possibilities.",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(visible = roleExchanged, enter = fadeIn()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "When conditions change, their collaboration shifts naturally! Inside a bustling evening workshop, the duck leads acoustic & tactile navigation with confidence while the rabbit coordinates the mobile toolkit and presents visual guides.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = "Roles are never permanently fixed as helper and recipient. Every person brings something valuable depending on the mission.",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DuckAndRabbitCanvas(
    roleExchanged: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier.semantics {
            contentDescription = if (!roleExchanged) {
                "Storybook scene: The yellow duck with a green blindfold, green scarf, and white cane walks beside the cream rabbit in a wheelchair who points gently forward."
            } else {
                "Role-exchange scene: The duck guides tactile audio calibration while the rabbit coordinates the mobile toolkit."
            }
        }
    ) {
        val w = size.width
        val h = size.height

        // Soft rolling botanical ground
        val hillPath = Path().apply {
            moveTo(0f, h * 0.78f)
            quadraticTo(w * 0.35f, h * 0.66f, w * 0.68f, h * 0.76f)
            quadraticTo(w * 0.86f, h * 0.81f, w, h * 0.72f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(path = hillPath, color = MutedSage.copy(alpha = 0.42f))

        // Delicate golden sparkles
        val sparkles = listOf(Offset(w * 0.50f, h * 0.22f), Offset(w * 0.16f, h * 0.28f), Offset(w * 0.84f, h * 0.25f))
        sparkles.forEach { pt ->
            drawCircle(color = WarmGoldenYellow.copy(alpha = 0.75f), radius = 4.5f, center = pt)
        }

        // --- DUCK (left) ---
        val duckX = w * 0.31f
        val duckY = h * 0.62f

        // White cane (contextually angled forward)
        drawLine(
            color = WarmWhite,
            start = Offset(duckX + 26f, duckY + 8f),
            end = Offset(duckX + 76f, h * 0.85f),
            strokeWidth = 4.5f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color(0xFFD9534F),
            start = Offset(duckX + 66f, h * 0.78f),
            end = Offset(duckX + 76f, h * 0.85f),
            strokeWidth = 5f,
            cap = StrokeCap.Round
        )

        // Duck body & head
        drawOval(
            color = DuckYellow,
            topLeft = Offset(duckX - 34f, duckY - 12f),
            size = Size(68f, 52f)
        )
        drawCircle(
            color = DuckYellow,
            radius = 25f,
            center = Offset(duckX + 4f, duckY - 26f)
        )
        // Gentle outline
        drawCircle(
            color = SoftEarthBrown.copy(alpha = 0.45f),
            radius = 25f,
            center = Offset(duckX + 4f, duckY - 26f),
            style = Stroke(width = 2f)
        )
        // Green scarf
        drawRoundRect(
            color = DeepForestGreen,
            topLeft = Offset(duckX - 18f, duckY - 7f),
            size = Size(44f, 12f),
            cornerRadius = CornerRadius(6f, 6f)
        )
        drawRoundRect(
            color = BotanicalGreen,
            topLeft = Offset(duckX - 20f, duckY + 1f),
            size = Size(14f, 22f),
            cornerRadius = CornerRadius(5f, 5f)
        )
        // Green eye-covering representing blindness
        drawRoundRect(
            color = DeepForestGreen,
            topLeft = Offset(duckX - 18f, duckY - 35f),
            size = Size(44f, 13f),
            cornerRadius = CornerRadius(6f, 6f)
        )
        // Expressive orange beak
        val beak = Path().apply {
            moveTo(duckX + 24f, duckY - 26f)
            quadraticTo(duckX + 44f, duckY - 23f, duckX + 26f, duckY - 15f)
            close()
        }
        drawPath(beak, color = DuckBeakOrange)

        // --- RABBIT (right) ---
        val rabX = w * 0.69f
        val rabY = h * 0.56f

        // Wheelchair (when in founding story or collaborative mobility)
        val wheelCenter = Offset(rabX - 2f, h * 0.72f)
        drawCircle(
            color = DeepForestGreen,
            radius = 28f,
            center = wheelCenter,
            style = Stroke(width = 4.5f)
        )
        drawCircle(
            color = BotanicalGreen.copy(alpha = 0.5f),
            radius = 19f,
            center = wheelCenter,
            style = Stroke(width = 2f)
        )
        drawCircle(color = WarmGoldenYellow, radius = 5f, center = wheelCenter)
        // Chair seat & backrest
        drawLine(
            color = SoftEarthBrown,
            start = Offset(rabX - 22f, rabY + 10f),
            end = Offset(rabX + 22f, rabY + 10f),
            strokeWidth = 5f,
            cap = StrokeCap.Round
        )

        // Rabbit long ears with soft pink interiors
        drawOval(
            color = RabbitCream,
            topLeft = Offset(rabX - 18f, rabY - 68f),
            size = Size(14f, 38f)
        )
        drawOval(
            color = RabbitEarPink,
            topLeft = Offset(rabX - 14f, rabY - 62f),
            size = Size(7f, 26f)
        )
        drawOval(
            color = RabbitCream,
            topLeft = Offset(rabX + 4f, rabY - 66f),
            size = Size(14f, 38f)
        )
        drawOval(
            color = RabbitEarPink,
            topLeft = Offset(rabX + 7f, rabY - 60f),
            size = Size(7f, 26f)
        )

        // Rabbit body & head
        drawOval(
            color = RabbitCream,
            topLeft = Offset(rabX - 24f, rabY - 10f),
            size = Size(48f, 44f)
        )
        drawCircle(
            color = RabbitCream,
            radius = 23f,
            center = Offset(rabX, rabY - 22f)
        )
        drawCircle(
            color = SoftEarthBrown.copy(alpha = 0.4f),
            radius = 23f,
            center = Offset(rabX, rabY - 22f),
            style = Stroke(width = 2f)
        )
        // Rosy cheeks & expressive eyes
        drawCircle(color = RabbitEarPink, radius = 5f, center = Offset(rabX - 12f, rabY - 16f))
        drawCircle(color = RabbitEarPink, radius = 5f, center = Offset(rabX + 12f, rabY - 16f))
        drawCircle(color = DarkBarkText, radius = 3.2f, center = Offset(rabX - 8f, rabY - 24f))
        drawCircle(color = DarkBarkText, radius = 3.2f, center = Offset(rabX + 8f, rabY - 24f))
        // Small green botanical scarf matching friend
        drawRoundRect(
            color = BotanicalGreen,
            topLeft = Offset(rabX - 17f, rabY - 2f),
            size = Size(34f, 9f),
            cornerRadius = CornerRadius(4.5f, 4.5f)
        )

        // Mutual connection arc between Duck & Rabbit
        val arcPath = Path().apply {
            moveTo(duckX + 32f, duckY - 38f)
            quadraticTo(w * 0.50f, h * 0.14f, rabX - 30f, rabY - 34f)
        }
        drawPath(
            path = arcPath,
            color = WarmGoldenYellow.copy(alpha = 0.7f),
            style = Stroke(width = 3f, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun MascotBadgeAvatar(
    isDuck: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.size(42.dp),
        shape = CircleShape,
        color = if (isDuck) DuckYellow.copy(alpha = 0.35f) else RabbitEarPink.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, if (isDuck) WarmGoldenYellow else SoftEarthBrown.copy(alpha = 0.4f))
    ) {
        Canvas(modifier = Modifier.padding(6.dp)) {
            if (isDuck) {
                drawCircle(color = DuckYellow, radius = size.minDimension * 0.38f)
                drawRoundRect(
                    color = DeepForestGreen,
                    topLeft = Offset(size.width * 0.18f, size.height * 0.30f),
                    size = Size(size.width * 0.64f, size.height * 0.20f),
                    cornerRadius = CornerRadius(4f, 4f)
                )
            } else {
                drawCircle(color = RabbitCream, radius = size.minDimension * 0.36f)
                drawCircle(color = RabbitEarPink, radius = size.minDimension * 0.12f, center = Offset(size.width * 0.32f, size.height * 0.56f))
                drawCircle(color = RabbitEarPink, radius = size.minDimension * 0.12f, center = Offset(size.width * 0.68f, size.height * 0.56f))
            }
        }
    }
}
