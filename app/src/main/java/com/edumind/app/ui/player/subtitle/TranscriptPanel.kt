package com.edumind.app.ui.player.subtitle

import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edumind.app.data.model.response.SubtitleSegment
import com.edumind.app.ui.player.PocPlayerState
import com.edumind.app.ui.theme.EduMindFontFamily
import com.edumind.app.ui.theme.EduMindTheme
import com.edumind.app.ui.theme.Neutral100
import com.edumind.app.ui.theme.Neutral200
import com.edumind.app.ui.theme.Neutral500
import com.edumind.app.ui.theme.Neutral700
import com.edumind.app.ui.theme.Primary50
import com.edumind.app.ui.theme.Primary100
import com.edumind.app.ui.theme.Primary400
import com.edumind.app.ui.theme.Primary700

private const val AUTO_SCROLL_PAUSE_MS = 5_000L

@Composable
fun TranscriptPanel(
    segments: List<SubtitleSegment>,
    currentIndex: Int,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (segments.isEmpty()) {
        TranscriptEmpty(modifier = modifier)
    } else {
        TranscriptList(
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
            text = "Chưa có phụ đề",
            fontFamily = EduMindFontFamily,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Normal,
            color = Neutral500
        )
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
    var lastDragAtMs by remember { mutableLongStateOf(0L) }

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
            listState.animateScrollToItem(currentIndex)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        itemsIndexed(segments, key = { _, segment -> segment.startMs }) { index, segment ->
            TranscriptRow(
                segment = segment,
                isCurrent = index == currentIndex,
                onClick = { onSeek(segment.startMs) }
            )
        }
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
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Mốc thời gian đặt trong thẻ card bo góc nhạt
        Box(
            modifier = Modifier
                .padding(top = 1.dp)
                .background(
                    color = if (isCurrent) Primary100 else Neutral100,
                    shape = RoundedCornerShape(6.dp)
                )
                .padding(horizontal = 6.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = timeLabel,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Medium,
                color = if (isCurrent) Primary700 else Neutral500
            )
        }

        // Thanh dọc ngăn cách phân định rõ ràng giữa mốc thời gian và nội dung
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 2.dp)
                .width(2.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(if (isCurrent) Primary400 else Neutral200)
        )

        // Nội dung câu phụ đề
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
