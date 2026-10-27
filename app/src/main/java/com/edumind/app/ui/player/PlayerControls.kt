package com.edumind.app.ui.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edumind.app.ui.theme.EduMindFontFamily
import com.edumind.app.ui.theme.EduMindTheme
import com.edumind.app.ui.theme.Primary600
import com.edumind.app.ui.theme.Primary700

@Composable
fun PlayerControlsOverlay(
    state: PocPlayerState,
    onTogglePlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onSeekRelative: (Long) -> Unit,
    onBackClick: () -> Unit,
    onToggleFullscreen: () -> Unit,
    onOverlayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onOverlayClick
            )
    ) {
        if (state.isBuffering) {
            CircularProgressIndicator(
                color = Primary600,
                strokeWidth = 3.dp,
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.Center)
            )
        }

        AnimatedVisibility(
            visible = state.isControlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.75f),
                                Color.Black.copy(alpha = 0.25f),
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            ) {
                // Top Bar: Back button, Title, Fullscreen
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .align(Alignment.TopCenter),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.4f))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = state.title,
                        fontFamily = EduMindFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.95f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp)
                    )

                    IconButton(
                        onClick = onToggleFullscreen,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.4f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = "Toàn màn hình",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Center Controls: Tua lùi 10s | Play/Pause 56dp | Tua tới 10s
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(28.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.45f))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                            .clickable { onSeekRelative(-10000L) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Replay10,
                            contentDescription = "Lùi 10 giây",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.25f))
                            .border(2.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                            .clickable(onClick = onTogglePlayPause),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (state.isPlaying) "Tạm dừng" else "Phát",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.45f))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                            .clickable { onSeekRelative(10000L) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Forward10,
                            contentDescription = "Tới 10 giây",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                // Bottom Controls: Timecode + Badges + Gear Icon + Nebula Gradient Seekbar with Chapter Markers
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = state.positionFormatted,
                                fontFamily = EduMindFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = (-0.3).sp,
                                color = Color.White
                            )
                            Text(
                                text = " / ${state.durationFormatted}",
                                fontFamily = EduMindFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                letterSpacing = (-0.3).sp,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Primary700)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "CC",
                                    fontFamily = EduMindFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "1.0x",
                                    fontFamily = EduMindFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.3).sp,
                                    color = Color.White
                                )
                            }

                            IconButton(
                                onClick = { },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Cài đặt",
                                    tint = Color.White,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    NebulaVideoSeekbar(
                        progress = state.progress,
                        chapterMarkers = listOf(0.0f, 0.35f, 0.65f, 0.85f),
                        onSeek = { fraction ->
                            val targetMs = (fraction * state.durationMs).toLong()
                            onSeek(targetMs)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun NebulaVideoSeekbar(
    progress: Float,
    chapterMarkers: List<Float> = listOf(0.0f, 0.35f, 0.65f, 0.85f),
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(0f) }
    val displayProgress = (if (isDragging) dragProgress else progress).coerceIn(0f, 1f)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(20.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val newProgress = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                    onSeek(newProgress)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        dragProgress = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        dragProgress = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                    },
                    onDragEnd = {
                        isDragging = false
                        onSeek(dragProgress)
                    },
                    onDragCancel = {
                        isDragging = false
                    }
                )
            },
        contentAlignment = Alignment.CenterStart
    ) {
        val totalWidth = maxWidth
        val trackHeight = 6.dp
        val thumbSize = 14.dp
        val markerSize = 6.dp

        // Inactive background track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(trackHeight)
                .clip(RoundedCornerShape(3.dp))
                .background(Color.White.copy(alpha = 0.3f))
        )

        // Active progress track with 3-color Nebula gradient
        val activeWidth = totalWidth * displayProgress
        if (displayProgress > 0f) {
            Box(
                modifier = Modifier
                    .width(activeWidth)
                    .height(trackHeight)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF3B82F6), // Nebula Blue
                                Color(0xFF8B5CF6), // Nebula Purple
                                Color(0xFFEC4899)  // Nebula Pink
                            )
                        )
                    )
            )
        }

        // Chapter markers (dots)
        chapterMarkers.forEach { markerFraction ->
            val markerOffset = (totalWidth * markerFraction) - (markerSize / 2)
            Box(
                modifier = Modifier
                    .padding(start = markerOffset.coerceIn(0.dp, totalWidth - markerSize))
                    .size(markerSize)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color.Black.copy(alpha = 0.4f), CircleShape)
            )
        }

        // Draggable Thumb with white center and purple border
        val thumbOffset = (totalWidth * displayProgress) - (thumbSize / 2)
        Box(
            modifier = Modifier
                .padding(start = thumbOffset.coerceIn(0.dp, totalWidth - thumbSize))
                .size(thumbSize)
                .clip(CircleShape)
                .background(Color.White)
                .border(2.dp, Primary600, CircleShape)
        )
    }
}

@Preview(name = "1. Đang phát (Playing - 04:15/15:30)", showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun PlayerControlsPlayingPreview() {
    EduMindTheme {
        Box(modifier = Modifier.size(width = 412.dp, height = 280.dp)) {
            PlayerControlsOverlay(
                state = PocPlayerState(
                    isPlaying = true,
                    positionMs = 255000L, // 04:15
                    durationMs = 930000L, // 15:30
                    isControlsVisible = true,
                    title = "Bài 03: Biến & Kiểu dữ liệu cơ bản"
                ),
                onTogglePlayPause = {},
                onSeek = {},
                onSeekRelative = {},
                onBackClick = {},
                onToggleFullscreen = {},
                onOverlayClick = {}
            )
        }
    }
}

@Preview(name = "2. Đang tạm dừng (Paused)", showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun PlayerControlsPausedPreview() {
    EduMindTheme {
        Box(modifier = Modifier.size(width = 412.dp, height = 280.dp)) {
            PlayerControlsOverlay(
                state = PocPlayerState(
                    isPlaying = false,
                    positionMs = 120000L, // 02:00
                    durationMs = 930000L,
                    isControlsVisible = true
                ),
                onTogglePlayPause = {},
                onSeek = {},
                onSeekRelative = {},
                onBackClick = {},
                onToggleFullscreen = {},
                onOverlayClick = {}
            )
        }
    }
}

@Preview(name = "3. Đang tải đệm (Buffering)", showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun PlayerControlsBufferingPreview() {
    EduMindTheme {
        Box(modifier = Modifier.size(width = 412.dp, height = 280.dp)) {
            PlayerControlsOverlay(
                state = PocPlayerState(
                    isPlaying = true,
                    isBuffering = true,
                    isControlsVisible = false
                ),
                onTogglePlayPause = {},
                onSeek = {},
                onSeekRelative = {},
                onBackClick = {},
                onToggleFullscreen = {},
                onOverlayClick = {}
            )
        }
    }
}
