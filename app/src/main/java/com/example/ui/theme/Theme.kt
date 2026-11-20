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
import com.example.model.AppThemeMode

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

private val SymbiosisWhiteColorScheme = lightColorScheme(
    primary = DeepForestGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE9F2EC),
    onPrimaryContainer = ForestGreenDark,
    secondary = BotanicalGreen,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD9E6DC),
    onSecondaryContainer = ForestGreenDark,
    tertiary = ComplementaryAmber,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFF5DF),
    onTertiaryContainer = Color(0xFF4D3306),
    background = PureWhiteBackground,
    onBackground = Color(0xFF131B15),
    surface = PureWhiteSurface,
    onSurface = Color(0xFF131B15),
    surfaceVariant = PureWhiteSurfaceVariant,
    onSurfaceVariant = Color(0xFF3E4E43),
    outline = PureWhiteOutline,
    outlineVariant = Color(0xFFC2CEC4),
    error = GapTerracotta,
    onError = Color.White,
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
    background = Color.White,
    onBackground = Color(0xFF0A100C),
    surface = Color.White,
    onSurface = Color(0xFF0A100C),
    surfaceVariant = Color(0xFFE5ECE0),
    onSurfaceVariant = Color(0xFF142118),
    outline = Color(0xFF38473C),
    outlineVariant = Color(0xFF6B7C6E),
    error = Color(0xFF8C1D18),
    onError = Color.White,
    errorContainer = Color(0xFFFCE2E0),
    onErrorContainer = Color(0xFF410E0B)
)

private val SymbiosisDarkColorScheme = darkColorScheme(
    primary = DarkMintPrimary,
    onPrimary = ForestGreenDark,
    primaryContainer = DeepForestGreen,
    onPrimaryContainer = DarkCreamText,
    secondary = Color(0xFF8FBE94),
    onSecondary = ForestGreenDark,
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

private val SymbiosisHighContrastDarkColorScheme = darkColorScheme(
    primary = Color(0xFFC2F2CF),
    onPrimary = Color(0xFF04140B),
    primaryContainer = Color(0xFF1E4C34),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFFB3E5B8),
    onSecondary = Color(0xFF04140B),
    secondaryContainer = HighContrastDarkSurfaceVariant,
    onSecondaryContainer = Color.White,
    tertiary = Color(0xFFFFCF6E),
    onTertiary = Color(0xFF1F1200),
    tertiaryContainer = Color(0xFF5A420E),
    onTertiaryContainer = Color(0xFFFFF4DC),
    background = HighContrastDarkBackground,
    onBackground = Color.White,
    surface = HighContrastDarkSurface,
    onSurface = Color.White,
    surfaceVariant = HighContrastDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFE6EFE8),
    outline = HighContrastDarkOutline,
    outlineVariant = Color(0xFF5E7C69),
    error = Color(0xFFFFB4A9),
    onError = Color(0xFF3A0904),
    errorContainer = Color(0xFF6B1E15),
    onErrorContainer = Color.White
)

val SymbiosisShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

val LocalReducedMotion = staticCompositionLocalOf { false }
val LocalSymbiosisIsDark = staticCompositionLocalOf { false }
val LocalSymbiosisThemeMode = staticCompositionLocalOf { AppThemeMode.CREAM }

@Composable
fun SymbiosisTheme(
    themeMode: AppThemeMode = AppThemeMode.CREAM,
    darkTheme: Boolean = isSystemInDarkTheme(),
    highContrast: Boolean = false,
    textScale: Float = 1.0f,
    reducedMotion: Boolean = false,
    content: @Composable () -> Unit
) {
    val isDark = themeMode == AppThemeMode.DARK
    val colorScheme = when {
        themeMode == AppThemeMode.DARK && highContrast -> SymbiosisHighContrastDarkColorScheme
        themeMode == AppThemeMode.DARK -> SymbiosisDarkColorScheme
        themeMode == AppThemeMode.WHITE && highContrast -> SymbiosisHighContrastLightColorScheme
        themeMode == AppThemeMode.WHITE -> SymbiosisWhiteColorScheme
        highContrast -> SymbiosisHighContrastLightColorScheme
        else -> SymbiosisLightColorScheme
    }

    CompositionLocalProvider(
        LocalReducedMotion provides reducedMotion,
        LocalSymbiosisIsDark provides isDark,
        LocalSymbiosisThemeMode provides themeMode
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = buildSymbiosisTypography(textScale),
            shapes = SymbiosisShapes,
            content = content
        )
    }
}

