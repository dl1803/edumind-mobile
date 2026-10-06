package com.edumind.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Primary700,
    onPrimary = White,
    primaryContainer = Primary100,
    onPrimaryContainer = Primary900,
    secondary = Primary600,
    onSecondary = White,
    background = BgCanvasMobile,
    onBackground = Neutral950,
    surface = White,
    onSurface = Neutral950,
    error = Error600,
    onError = White,
    outline = Neutral300,
    surfaceVariant = Neutral100,
    onSurfaceVariant = Neutral700
)

private val DarkColorScheme = darkColorScheme(
    primary = Primary500,
    onPrimary = White,
    primaryContainer = Primary900,
    onPrimaryContainer = Primary100,
    secondary = Primary400,
    onSecondary = Neutral950,
    background = Neutral950,
    onBackground = White,
    surface = Neutral900,
    onSurface = White,
    error = Error600,
    onError = White,
    outline = Neutral700
)

@Composable
fun EduMindTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    spacing: Spacing = Spacing(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalSpacing provides spacing) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
