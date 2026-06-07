package com.sotark.play.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Основной синий
val BluePrimary    = Color(0xFF1A73E8)
val BlueSecondary  = Color(0xFF4285F4)
val BlueDark       = Color(0xFF0D47A1)

// Ukrainian
val UkrainianBlue   = Color(0xFF005BBB)
val UkrainianYellow = Color(0xFFFFD500)

// Liquid Glass colours
val GlassSurface   = Color(0x40FFFFFF)  // белый 25% прозрачность
val GlassBorder    = Color(0x60FFFFFF)  // белый 37%
val GlassDark      = Color(0x40000000)  // чёрный 25%

private val DarkColors = darkColorScheme(
    primary          = BluePrimary,
    onPrimary        = Color.White,
    secondary        = BlueSecondary,
    background       = Color(0xFF0A0A0F),
    surface          = Color(0xFF1A1A2E),
    surfaceVariant   = Color(0xFF252540),
    onBackground     = Color.White,
    onSurface        = Color.White,
    onSurfaceVariant = Color(0xFFB0B0CC)
)

private val LightColors = lightColorScheme(
    primary          = BluePrimary,
    onPrimary        = Color.White,
    secondary        = BlueSecondary,
    background       = Color(0xFFF0F4FF),
    surface          = Color.White,
    surfaceVariant   = Color(0xFFE8EEFF),
    onBackground     = Color(0xFF1A1A2E),
    onSurface        = Color(0xFF1A1A2E),
    onSurfaceVariant = Color(0xFF4A4A6A)
)

private val UkrainianColors = darkColorScheme(
    primary          = UkrainianYellow,
    onPrimary        = Color(0xFF1A1A1A),
    secondary        = Color(0xFFFFE135),
    background       = UkrainianBlue,
    surface          = Color(0xFF004A9E),
    surfaceVariant   = Color(0xFF003D85),
    onBackground     = UkrainianYellow,
    onSurface        = UkrainianYellow,
    onSurfaceVariant = Color(0xFFFFEB80),
    // Фикс проплешин — все элементы должны быть видны
    outline          = UkrainianYellow.copy(alpha = 0.5f),
    outlineVariant   = UkrainianYellow.copy(alpha = 0.3f),
    inverseSurface   = UkrainianYellow,
    inverseOnSurface = UkrainianBlue,
    primaryContainer = Color(0xFF003580),
    onPrimaryContainer = UkrainianYellow,
    secondaryContainer = Color(0xFF004A9E),
    onSecondaryContainer = UkrainianYellow,
    tertiaryContainer  = Color(0xFF003070),
    onTertiaryContainer  = UkrainianYellow,
)

@Composable
fun SotarkPlayTheme(
    darkTheme: Boolean      = false,
    ukrainianTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = when {
        ukrainianTheme -> UkrainianColors
        darkTheme      -> DarkColors
        else           -> LightColors
    }
    MaterialTheme(
        colorScheme = colors,
        typography  = Typography(),
        content     = content
    )
}
