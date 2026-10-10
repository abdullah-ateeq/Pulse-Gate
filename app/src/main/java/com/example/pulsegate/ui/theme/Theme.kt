package com.example.pulsegate.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.pulsegate.data.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = DarkBgVoid,
    primaryContainer = Color(0xFF00384D),
    onPrimaryContainer = NeonCyan,
    secondary = NeonAqua,
    onSecondary = DarkBgVoid,
    secondaryContainer = Color(0xFF003833),
    onSecondaryContainer = NeonAqua,
    tertiary = NeonViolet,
    onTertiary = Color.White,
    background = DarkBgVoid,
    onBackground = DarkTextPrimary,
    surface = DarkBgNavy,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkBgIndigo,
    onSurfaceVariant = DarkTextSecondary,
    error = DangerRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = LightCyanAccent,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = LightVioletAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF3E8FF),
    onSecondaryContainer = Color(0xFF6B21A8),
    tertiary = NeonAqua,
    onTertiary = DarkBgVoid,
    background = LightBgOffWhite,
    onBackground = LightTextPrimary,
    surface = LightBgSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = LightTextSecondary,
    error = DangerRed,
    onError = Color.White
)

@Composable
fun PulseGateTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}