package com.edumind.app.ui.player

import androidx.media3.common.Player

data class PocPlayerState(
    val isPlaying: Boolean = false,
    val playbackState: Int = Player.STATE_IDLE,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isBuffering: Boolean = false,
    val isControlsVisible: Boolean = true,
    val errorMessage: String? = null,
    val title: String = "Bài 03: Biến & Kiểu dữ liệu cơ bản",
    val isHlsManifest: Boolean = false
) {
    val progress: Float
        get() = if (durationMs > 0L) {
            (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }

    val positionFormatted: String
        get() = formatTime(positionMs)

    val durationFormatted: String
        get() = formatTime(durationMs)

    companion object {
        fun formatTime(ms: Long): String {
            val totalSeconds = (ms.coerceAtLeast(0L) / 1000).toInt()
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format("%02d:%02d", minutes, seconds)
        }
    }
}
