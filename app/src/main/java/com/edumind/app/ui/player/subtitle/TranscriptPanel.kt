package com.edumind.app.ui.player.subtitle

import android.os.SystemClock
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edumind.app.data.model.response.SubtitleSegment
import com.edumind.app.ui.common.SkeletonBox
import com.edumind.app.ui.player.PocPlayerState
import com.edumind.app.ui.theme.EduMindFontFamily
import com.edumind.app.ui.theme.EduMindTheme
import com.edumind.app.ui.theme.NebulaGradient
import com.edumind.app.ui.theme.Neutral100
import com.edumind.app.ui.theme.Neutral200
import com.edumind.app.ui.theme.Neutral500
import com.edumind.app.ui.theme.Neutral700
import com.edumind.app.ui.theme.Primary50
import com.edumind.app.ui.theme.Primary100
import com.edumind.app.ui.theme.Primary400
import com.edumind.app.ui.theme.Primary700
import kotlinx.coroutines.launch

private const val AUTO_SCROLL_PAUSE_MS = 4_000L
private const val AUTO_SCROLL_OFFSET_PX = -80

@Composable
fun TranscriptPanel(
    segments: List<SubtitleSegment>,
    currentIndex: Int,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    when {
        isLoading -> TranscriptSkeleton(modifier = modifier)
        segments.isEmpty() -> TranscriptEmpty(modifier = modifier)
        else -> TranscriptList(
            segments = segments,
            currentIndex = currentIndex,
            onSeek = onSeek,
            modifier = modifier
        )
    }
}

@Composable
private fun TranscriptEmpty(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Bài giảng chưa có phụ đề",
            fontFamily = EduMindFontFamily,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Normal,
            color = Neutral500
        )
    }
}

@Composable
private fun TranscriptSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(start = 16.dp, top = 12.dp, end = 16.dp)
            .semantics { contentDescription = "Đang tải phụ đề" },
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(6) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                SkeletonBox(
                    modifier = Modifier
                        .width(44.dp)
                        .height(20.dp),
                    shape = RoundedCornerShape(6.dp)
                )
                SkeletonBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(20.dp)
                )
            }
        }
    }
}

@Composable
private fun TranscriptList(
    segments: List<SubtitleSegment>,
    currentIndex: Int,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var lastDragAtMs by remember { mutableLongStateOf(0L) }
    val isCurrentOutOfView by remember(listState, currentIndex) {
        derivedStateOf {
            currentIndex >= 0 && listState.layoutInfo.visibleItemsInfo.none { it.index == currentIndex }
        }
    }
    val isCurrentAbove by remember(listState, currentIndex) {
        derivedStateOf { currentIndex < listState.firstVisibleItemIndex }
    }

    LaunchedEffect(listState) {
        listState.interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is DragInteraction.Start,
                is DragInteraction.Stop,
                is DragInteraction.Cancel -> {
                    lastDragAtMs = SystemClock.uptimeMillis()
                }
            }
        }
    }

    LaunchedEffect(currentIndex) {
        if (currentIndex >= 0 && !listState.isScrollInProgress && SystemClock.uptimeMillis() - lastDragAtMs > AUTO_SCROLL_PAUSE_MS) {
            listState.animateScrollToItem(currentIndex, AUTO_SCROLL_OFFSET_PX)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            itemsIndexed(segments, key = { index, _ -> index }) { index, segment ->
                TranscriptRow(
                    segment = segment,
                    isCurrent = index == currentIndex,
                    onClick = { onSeek(segment.startMs) }
                )
            }
        }

        AnimatedVisibility(
            visible = isCurrentOutOfView,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        ) {
            TranscriptJumpButton(
                pointsUp = isCurrentAbove,
                onClick = {
                    lastDragAtMs = 0L
                    scope.launch {
                        listState.animateScrollToItem(currentIndex, AUTO_SCROLL_OFFSET_PX)
                    }
                }
            )
        }
    }
}

@Composable
private fun TranscriptJumpButton(
    pointsUp: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .shadow(elevation = 4.dp, shape = shape)
            .clip(shape)
            .background(NebulaGradient)
            .clickable(onClick = onClick)
            .height(40.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (pointsUp) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = "Nhảy tới hiện tại",
            fontFamily = EduMindFontFamily,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}

@Composable
private fun TranscriptRow(
    segment: SubtitleSegment,
    isCurrent: Boolean,
    onClick: () -> Unit
) {
    val timeLabel = PocPlayerState.formatTime(segment.startMs)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isCurrent) Primary50 else Color.Transparent)
            .clickable(onClickLabel = "Tua đến $timeLabel", onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 1.dp)
                .background(
                    color = if (isCurrent) Primary100 else Neutral100,
                    shape = RoundedCornerShape(6.dp)
                )
                .padding(horizontal = 5.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = timeLabel,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Medium,
                color = if (isCurrent) Primary700 else Neutral500
            )
        }

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 2.dp)
                .width(if (isCurrent) 2.dp else 1.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(if (isCurrent) Primary400 else Neutral200)
        )

        Text(
            text = segment.text,
            fontFamily = EduMindFontFamily,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
            color = if (isCurrent) Primary700 else Neutral700,
            modifier = Modifier.weight(1f)
        )
    }
}

@Preview(name = "TranscriptPanel", showBackground = true)
@Composable
private fun TranscriptPanelPreview() {
    EduMindTheme {
        TranscriptPanel(
            segments = listOf(
                SubtitleSegment(0L, 6_000L, "Chào mừng các bạn đến với bài học về biến trong Python."),
                SubtitleSegment(6_000L, 12_000L, "Biến là tên gọi dùng để lưu trữ một giá trị trong bộ nhớ."),
                SubtitleSegment(12_000L, 19_000L, "Trong Python, bạn không cần khai báo kiểu dữ liệu trước khi dùng biến.")
            ),
            currentIndex = 1,
            onSeek = {}
        )
    }
}

@Preview(name = "TranscriptPanel đang tải", showBackground = true)
@Composable
private fun TranscriptPanelLoadingPreview() {
    EduMindTheme {
        TranscriptPanel(
            segments = emptyList(),
            currentIndex = -1,
            onSeek = {},
            isLoading = true
        )
    }
}

@Preview(name = "TranscriptPanel trống", showBackground = true)
@Composable
private fun TranscriptPanelEmptyPreview() {
    EduMindTheme {
        TranscriptPanel(
            segments = emptyList(),
            currentIndex = -1,
            onSeek = {}
        )
    }
}
