package com.edumind.app.ui.exercise

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edumind.app.ui.theme.Error100
import com.edumind.app.ui.theme.Error600
import com.edumind.app.ui.theme.Primary50
import com.edumind.app.ui.theme.Primary700
import com.edumind.app.ui.theme.Warning100
import com.edumind.app.ui.theme.Warning600

@Composable
internal fun ExerciseTimerBadge(
    remainingSeconds: Int,
    modifier: Modifier = Modifier
) {
    val level = exerciseTimerLevel(remainingSeconds)
    val haptic = LocalHapticFeedback.current
    val pulseAlpha = rememberTimerPulseAlpha(active = level == ExerciseTimerLevel.Critical)

    LaunchedEffect(level) {
        if (level == ExerciseTimerLevel.Critical) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    val contentColor = when (level) {
        ExerciseTimerLevel.Normal -> Primary700
        ExerciseTimerLevel.Warning -> Warning600
        ExerciseTimerLevel.Critical -> Error600
    }
    val containerColor = when (level) {
        ExerciseTimerLevel.Normal -> Primary50
        ExerciseTimerLevel.Warning -> Warning100
        ExerciseTimerLevel.Critical -> Error100
    }
    val shape = RoundedCornerShape(12.dp)

    Row(
        modifier = modifier
            .alpha(pulseAlpha)
            .clip(shape)
            .background(containerColor)
            .border(1.dp, contentColor.copy(alpha = 0.35f), shape)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Schedule,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = formatTimer(remainingSeconds),
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
    }
}

@Composable
private fun rememberTimerPulseAlpha(active: Boolean): Float {
    if (!active) return 1f
    val transition = rememberInfiniteTransition(label = "exerciseTimerPulse")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "exerciseTimerPulseAlpha"
    )
    return alpha
}
