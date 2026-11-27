package com.edumind.app.ui.exercise

import java.util.Locale

enum class ExerciseTimerLevel { Normal, Warning, Critical }

internal const val TIMER_WARNING_SECONDS = 300
internal const val TIMER_CRITICAL_SECONDS = 60

internal fun exerciseTimerLevel(remainingSeconds: Int): ExerciseTimerLevel = when {
    remainingSeconds <= TIMER_CRITICAL_SECONDS -> ExerciseTimerLevel.Critical
    remainingSeconds <= TIMER_WARNING_SECONDS -> ExerciseTimerLevel.Warning
    else -> ExerciseTimerLevel.Normal
}

internal fun formatTimer(totalSeconds: Int): String {
    val safe = totalSeconds.coerceAtLeast(0)
    return String.format(Locale.US, "%02d:%02d", safe / 60, safe % 60)
}

internal fun remainingSecondsUntil(expiresAtMs: Long, nowMs: Long): Int =
    ((expiresAtMs - nowMs + 999L) / 1000L).coerceIn(0L, Int.MAX_VALUE.toLong()).toInt()
