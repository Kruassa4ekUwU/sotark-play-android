package com.sotark.play.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.sotark.play.data.SecretTheme

// Основной синий
val BluePrimary   = Color(0xFF1A73E8)
val BlueSecondary = Color(0xFF4285F4)

// Ukrainian
val UkrainianBlue   = Color(0xFF005BBB)
val UkrainianYellow = Color(0xFFFFD500)

@Suppress("DEPRECATION")
@Deprecated("Use MaterialTheme.colorScheme.primary")
val GreenPrimary = BluePrimary

// ── Секретные темы ────────────────────────────────────────────────────────────

private val MatteMetalDark = darkColorScheme(
    primary          = Color(0xFFB0BEC5),
    onPrimary        = Color(0xFF000000),
    secondary        = Color(0xFF90A4AE),
    background       = Color(0xFF1C1C1E),
    surface          = Color(0xFF2C2C2E),
    surfaceVariant   = Color(0xFF3A3A3C),
    onBackground     = Color(0xFFE5E5EA),
    onSurface        = Color(0xFFE5E5EA),
    onSurfaceVariant = Color(0xFF8E8E93)
)

private val NeonDark = darkColorScheme(
    primary          = Color(0xFF00FF88),
    onPrimary        = Color(0xFF000000),
    secondary        = Color(0xFF00E5FF),
    background       = Color(0xFF050510),
    surface          = Color(0xFF0D0D20),
    surfaceVariant   = Color(0xFF151530),
    onBackground     = Color(0xFF00FF88),
    onSurface        = Color(0xFFE0E0FF),
    onSurfaceVariant = Color(0xFF8080C0),
    outline          = Color(0xFF00FF88).copy(alpha = 0.5f)
)

private val OnyxDark = darkColorScheme(
    primary          = Color(0xFFCFB980),
    onPrimary        = Color(0xFF000000),
    secondary        = Color(0xFFB8A06A),
    background       = Color(0xFF0A0A0A),
    surface          = Color(0xFF141414),
    surfaceVariant   = Color(0xFF1E1E1E),
    onBackground     = Color(0xFFCFB980),
    onSurface        = Color(0xFFE8E8E8),
    onSurfaceVariant = Color(0xFF888888),
    outline          = Color(0xFFCFB980).copy(alpha = 0.4f)
)

private val SunsetLight = lightColorScheme(
    primary          = Color(0xFFFF6B35),
    onPrimary        = Color(0xFFFFFFFF),
    secondary        = Color(0xFFFF9A3C),
    background       = Color(0xFFFFF8F0),
    surface          = Color(0xFFFFFFFF),
    surfaceVariant   = Color(0xFFFFF0E0),
    onBackground     = Color(0xFF2D1B00),
    onSurface        = Color(0xFF2D1B00),
    onSurfaceVariant = Color(0xFF7A4A20)
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
    outline          = UkrainianYellow.copy(alpha = 0.5f),
    outlineVariant   = UkrainianYellow.copy(alpha = 0.3f),
    inverseSurface   = UkrainianYellow,
    inverseOnSurface = UkrainianBlue,
    primaryContainer    = Color(0xFF003580),
    onPrimaryContainer  = UkrainianYellow,
    secondaryContainer  = Color(0xFF004A9E),
    onSecondaryContainer = UkrainianYellow,
)

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

@Composable
fun SotarkPlayTheme(
    darkTheme: Boolean       = false,
    ukrainianTheme: Boolean  = false,
    secretTheme: SecretTheme = SecretTheme.NONE,
    content: @Composable () -> Unit
) {
    val colors = when {
        ukrainianTheme                    -> UkrainianColors
        secretTheme == SecretTheme.MATTE_METAL -> MatteMetalDark
        secretTheme == SecretTheme.NEON        -> NeonDark
        secretTheme == SecretTheme.ONYX        -> OnyxDark
        secretTheme == SecretTheme.SUNSET      -> SunsetLight
        darkTheme                         -> DarkColors
        else                              -> LightColors
    }
    MaterialTheme(colorScheme = colors, typography = Typography(), content = content)
}
