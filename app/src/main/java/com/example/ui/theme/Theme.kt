package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val SymbiosisLightColorScheme = lightColorScheme(
    primary = DeepForestGreen,
    onPrimary = WarmWhite,
    primaryContainer = SageSurface,
    onPrimaryContainer = DeepForestGreen,
    secondary = BotanicalGreen,
    onSecondary = WarmWhite,
    secondaryContainer = MutedSage,
    onSecondaryContainer = ForestGreenDark,
    tertiary = WarmGoldenYellow,
    onTertiary = DarkBarkText,
    tertiaryContainer = GoldenSoftSurface,
    onTertiaryContainer = SoftEarthBrown,
    background = SoftCream,
    onBackground = DarkBarkText,
    surface = WarmWhite,
    onSurface = DarkBarkText,
    surfaceVariant = SageSurface,
    onSurfaceVariant = MutedMossText,
    outline = WarmSandBorder,
    outlineVariant = MutedSage,
    error = GapTerracotta,
    onError = WarmWhite,
    errorContainer = GapSoftSurface,
    onErrorContainer = GapTerracotta
)

private val SymbiosisHighContrastLightColorScheme = lightColorScheme(
    primary = ForestGreenDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD5E2CF),
    onPrimaryContainer = Color(0xFF0A1F14),
    secondary = DeepForestGreen,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC5D5BE),
    onSecondaryContainer = Color(0xFF0A1F14),
    tertiary = Color(0xFF8C5B00),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFBE5B6),
    onTertiaryContainer = Color(0xFF2D1C00),
    background = Color(0xFFFFFDF9),
    onBackground = Color(0xFF0E1510),
    surface = Color.White,
    onSurface = Color(0xFF0E1510),
    surfaceVariant = Color(0xFFE5ECE0),
    onSurfaceVariant = Color(0xFF1D2920),
    outline = Color(0xFF47574B),
    outlineVariant = Color(0xFF7B8C7E),
    error = Color(0xFF8C1D18),
    onError = Color.White,
    errorContainer = Color(0xFFFCE2E0),
    onErrorContainer = Color(0xFF410E0B)
)

private val SymbiosisDarkColorScheme = darkColorScheme(
    primary = MutedSage,
    onPrimary = ForestGreenDark,
    primaryContainer = DeepForestGreen,
    onPrimaryContainer = SoftCream,
    secondary = BotanicalGreen,
    onSecondary = WarmWhite,
    secondaryContainer = DarkForestSurfaceVariant,
    onSecondaryContainer = DarkSageText,
    tertiary = WarmGoldenYellow,
    onTertiary = ForestGreenDark,
    tertiaryContainer = Color(0xFF4E3D1A),
    onTertiaryContainer = GoldenSoftSurface,
    background = DarkForestBackground,
    onBackground = DarkCreamText,
    surface = DarkForestSurface,
    onSurface = DarkCreamText,
    surfaceVariant = DarkForestSurfaceVariant,
    onSurfaceVariant = DarkSageText,
    outline = Color(0xFF44594B),
    outlineVariant = Color(0xFF314438),
    error = Color(0xFFE58C7D),
    onError = Color(0xFF3E100A),
    errorContainer = Color(0xFF5C2118),
    onErrorContainer = Color(0xFFFFDAD4)
)

val SymbiosisShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

val LocalReducedMotion = staticCompositionLocalOf { false }

@Composable
fun SymbiosisTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    highContrast: Boolean = false,
    textScale: Float = 1.0f,
    reducedMotion: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        highContrast && !darkTheme -> SymbiosisHighContrastLightColorScheme
        darkTheme -> SymbiosisDarkColorScheme
        else -> SymbiosisLightColorScheme
    }

    CompositionLocalProvider(LocalReducedMotion provides reducedMotion) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = buildSymbiosisTypography(textScale),
            shapes = SymbiosisShapes,
            content = content
        )
    }
}
