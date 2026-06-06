package com.sotark.play.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val GreenPrimary   = Color(0xFF01875F)
val GreenSecondary = Color(0xFF00C853)

// Ukrainian theme colors
val UkrainianBlue   = Color(0xFF005BBB)
val UkrainianYellow = Color(0xFFFFD500)

private val DarkColors = darkColorScheme(
    primary          = GreenPrimary,
    onPrimary        = Color.White,
    secondary        = GreenSecondary,
    background       = Color(0xFF121212),
    surface          = Color(0xFF1C1C1C),
    surfaceVariant   = Color(0xFF2A2A2A),
    onBackground     = Color.White,
    onSurface        = Color.White,
    onSurfaceVariant = Color(0xFFB0B0B0)
)

private val LightColors = lightColorScheme(
    primary          = GreenPrimary,
    onPrimary        = Color.White,
    secondary        = GreenSecondary,
    background       = Color(0xFFF6F6F6),
    surface          = Color.White,
    surfaceVariant   = Color(0xFFEEEEEE),
    onBackground     = Color(0xFF1C1C1C),
    onSurface        = Color(0xFF1C1C1C),
    onSurfaceVariant = Color(0xFF666666)
)

private val UkrainianColors = darkColorScheme(
    primary          = UkrainianYellow,
    onPrimary        = Color(0xFF1A1A1A),
    secondary        = UkrainianBlue,
    background       = UkrainianBlue,
    surface          = Color(0xFF004A9E),
    surfaceVariant   = Color(0xFF003D85),
    onBackground     = UkrainianYellow,
    onSurface        = UkrainianYellow,
    onSurfaceVariant = Color(0xFFFFEB80)
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
