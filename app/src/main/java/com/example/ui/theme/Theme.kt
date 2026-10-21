package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.data.local.ThemeMode

data class CustomThemeColors(
    val bgMain: Color,
    val bgUpper: Color,
    val surfaceCard: Color,
    val surfaceHighlight: Color,
    val borderLavender: Color,
    val accentGold: Color,
    val accentCyan: Color,
    val orbPrimary: Color,
    val orbSecondary: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color
)

val LocalCustomColors = staticCompositionLocalOf {
    CustomThemeColors(
        bgMain = DarkBgMain,
        bgUpper = DarkBgUpper,
        surfaceCard = DarkSurfaceCard,
        surfaceHighlight = DarkSurfaceCardHighlight,
        borderLavender = DarkBorderLavender,
        accentGold = AccentGold,
        accentCyan = AccentCyan,
        orbPrimary = AccentPurpleGlow,
        orbSecondary = AccentIndigoGlow,
        textPrimary = TextWhite,
        textSecondary = TextLavender,
        textMuted = TextMuted
    )
}

@Composable
fun NovaTheme(
    themeMode: ThemeMode = ThemeMode.DARK_PURPLE,
    content: @Composable () -> Unit
) {
    val customColors = when (themeMode) {
        ThemeMode.DARK_PURPLE -> CustomThemeColors(
            bgMain = DarkBgMain,
            bgUpper = DarkBgUpper,
            surfaceCard = DarkSurfaceCard,
            surfaceHighlight = DarkSurfaceCardHighlight,
            borderLavender = DarkBorderLavender,
            accentGold = AccentGold,
            accentCyan = AccentCyan,
            orbPrimary = AccentPurpleGlow,
            orbSecondary = AccentIndigoGlow,
            textPrimary = TextWhite,
            textSecondary = TextLavender,
            textMuted = TextMuted
        )
        ThemeMode.BLACK_GOLD -> CustomThemeColors(
            bgMain = BgBlackGold,
            bgUpper = Color(0xFF120E05),
            surfaceCard = SurfaceBlackGold,
            surfaceHighlight = Color(0xFF221F17),
            borderLavender = BorderGoldMuted,
            accentGold = AccentGoldBright,
            accentCyan = Color(0xFFFFD700),
            orbPrimary = Color(0xFFD4AF37),
            orbSecondary = Color(0xFFAA7C11),
            textPrimary = TextWhite,
            textSecondary = Color(0xFFE2D6B5),
            textMuted = Color(0xFF8C7D54)
        )
        ThemeMode.MIDNIGHT_BLUE -> CustomThemeColors(
            bgMain = BgMidnight,
            bgUpper = Color(0xFF07142E),
            surfaceCard = SurfaceMidnight,
            surfaceHighlight = Color(0xFF142442),
            borderLavender = BorderMidnight,
            accentGold = AccentSky,
            accentCyan = Color(0xFF00E5FF),
            orbPrimary = Color(0xFF2563EB),
            orbSecondary = Color(0xFF06B6D4),
            textPrimary = TextWhite,
            textSecondary = Color(0xFF94A3B8),
            textMuted = Color(0xFF64748B)
        )
    }

    val materialColorScheme = darkColorScheme(
        primary = customColors.accentGold,
        onPrimary = Color.Black,
        secondary = customColors.accentCyan,
        onSecondary = Color.Black,
        tertiary = customColors.orbPrimary,
        background = customColors.bgMain,
        onBackground = customColors.textPrimary,
        surface = customColors.surfaceCard,
        onSurface = customColors.textPrimary,
        surfaceVariant = customColors.surfaceHighlight,
        outline = customColors.borderLavender
    )

    CompositionLocalProvider(LocalCustomColors provides customColors) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = Typography,
            content = content
        )
    }
}
