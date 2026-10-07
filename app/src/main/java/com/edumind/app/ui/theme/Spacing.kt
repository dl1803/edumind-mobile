package com.edumind.app.ui.theme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/* Hệ thống Spacing chuẩn Base-4px theo Design System */
@Immutable
data class Spacing(
    val default: Dp = 0.dp,
    val xs: Dp = 4.dp,       // 4dp
    val sm: Dp = 8.dp,       // 8dp
    val md: Dp = 12.dp,      // 12dp
    val base: Dp = 16.dp,    // 16dp - Base spacing
    val lg: Dp = 24.dp,      // 24dp
    val xl: Dp = 32.dp,      // 32dp
    val space1: Dp = 4.dp,   // space-1 (4dp)
    val space2: Dp = 8.dp,   // space-2 (8dp)
    val space3: Dp = 12.dp,  // space-3 (12dp)
    val space4: Dp = 16.dp,  // space-4 (16dp)
    val space5: Dp = 20.dp,  // space-5 (20dp)
    val space6: Dp = 24.dp,  // space-6 (24dp)
    val space8: Dp = 32.dp,  // space-8 (32dp)
    val space10: Dp = 40.dp, // space-10 (40dp)
    val space12: Dp = 48.dp, // space-12 (48dp)
    val space16: Dp = 64.dp, // space-16 (64dp)
    val space20: Dp = 80.dp  // space-20 (80dp)
)

val LocalSpacing = staticCompositionLocalOf { Spacing() }

val MaterialTheme.spacing: Spacing
    @Composable
    @ReadOnlyComposable
    get() = LocalSpacing.current