package com.sotark.play.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val GreenPrimary   = Color(0xFF01875F)
val GreenSecondary = Color(0xFF00C853)
val SurfaceDark    = Color(0xFF1C1C1C)
val CardDark       = Color(0xFF2A2A2A)

private val DarkColors = darkColorScheme(
    primary          = GreenPrimary,
    onPrimary        = Color.White,
    secondary        = GreenSecondary,
    background       = Color(0xFF121212),
    surface          = SurfaceDark,
    surfaceVariant   = CardDark,
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

@Composable
fun SotarkPlayTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography  = Typography(),
        content     = content
    )
}
