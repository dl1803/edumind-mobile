package com.edumind.app.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.edumind.app.ui.theme.EduMindFontFamily
import com.edumind.app.ui.theme.EduMindTheme
import com.edumind.app.ui.theme.NebulaGradient
import com.edumind.app.ui.theme.Neutral100
import com.edumind.app.ui.theme.Neutral200
import com.edumind.app.ui.theme.Neutral300
import com.edumind.app.ui.theme.Neutral400
import com.edumind.app.ui.theme.Neutral500
import com.edumind.app.ui.theme.Neutral600
import com.edumind.app.ui.theme.Neutral700
import com.edumind.app.ui.theme.Neutral800
import com.edumind.app.ui.theme.Neutral900
import com.edumind.app.ui.theme.Neutral950
import com.edumind.app.ui.theme.Primary100
import com.edumind.app.ui.theme.Primary300
import com.edumind.app.ui.theme.Primary50
import com.edumind.app.ui.theme.Primary600
import com.edumind.app.ui.theme.Primary700
import com.edumind.app.ui.theme.Primary900
import com.edumind.app.ui.theme.Primary950
import com.edumind.app.ui.theme.Success600

@Composable
fun PlayerScreen(
    onBackClick: () -> Unit,
    onNavigateToAiSummary: () -> Unit = {},
    onNavigateToAiChat: () -> Unit = {},
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                viewModel.pause()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    PlayerScreenContent(
        state = state,
        onBackClick = onBackClick,
        onNavigateToAiSummary = onNavigateToAiSummary,
        onNavigateToAiChat = onNavigateToAiChat,
        onTogglePlayPause = { viewModel.togglePlayPause() },
        onSeek = { targetMs -> viewModel.seekTo(targetMs) },
        onSeekRelative = { offsetMs -> viewModel.seekRelative(offsetMs) },
        onOverlayClick = { viewModel.toggleControlsVisibility() },
        videoSurface = {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        useController = false
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                        player = viewModel.exoPlayer
                    }
                },
                update = { playerView ->
                    playerView.player = viewModel.exoPlayer
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    )
}

@Composable
fun PlayerScreenContent(
    state: PocPlayerState,
    onBackClick: () -> Unit,
    onNavigateToAiSummary: () -> Unit = {},
    onNavigateToAiChat: () -> Unit = {},
    onTogglePlayPause: () -> Unit = {},
    onSeek: (Long) -> Unit = {},
    onSeekRelative: (Long) -> Unit = {},
    onOverlayClick: () -> Unit = {},
    videoSurface: @Composable () -> Unit = {}
) {
    var selectedMainTab by remember { mutableIntStateOf(0) } // 0: Mục lục, 1: Ghi chú
    var selectedSubTab by remember { mutableIntStateOf(0) }  // 0: Nội dung, 1: Giới thiệu chung

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Video Area (280dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(Color.Black)
            ) {
                videoSurface()

                PlayerControlsOverlay(
                    state = state,
                    onTogglePlayPause = onTogglePlayPause,
                    onSeek = onSeek,
                    onSeekRelative = onSeekRelative,
                    onBackClick = onBackClick,
                    onToggleFullscreen = { },
                    onOverlayClick = onOverlayClick
                )
            }

            // Thanh Tab chính dưới video: Mục lục | Ghi chú | Tóm tắt AI
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .border(1.dp, Neutral200)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Tab 1: Mục lục
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedMainTab == 0) Primary100.copy(alpha = 0.7f) else Color.Transparent)
                        .border(
                            width = 1.dp,
                            color = if (selectedMainTab == 0) Primary300 else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { selectedMainTab = 0 }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = if (selectedMainTab == 0) Primary700 else Neutral500,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Mục lục",
                            fontFamily = EduMindFontFamily,
                            fontSize = 12.sp,
                            fontWeight = if (selectedMainTab == 0) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (selectedMainTab == 0) Primary900 else Neutral600
                        )
                    }
                }

                // Tab 2: Ghi chú
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedMainTab == 1) Primary100.copy(alpha = 0.7f) else Color.Transparent)
                        .border(
                            width = 1.dp,
                            color = if (selectedMainTab == 1) Primary300 else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { selectedMainTab = 1 }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = null,
                            tint = if (selectedMainTab == 1) Primary700 else Neutral500,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Ghi chú",
                            fontFamily = EduMindFontFamily,
                            fontSize = 12.sp,
                            fontWeight = if (selectedMainTab == 1) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (selectedMainTab == 1) Primary900 else Neutral600
                        )
                    }
                }

                // Tab 3: Nút chuyển nhanh Tóm tắt AI
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Primary50)
                        .border(1.dp, Primary100, RoundedCornerShape(12.dp))
                        .clickable(onClick = onNavigateToAiSummary)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Primary600,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Tóm tắt AI",
                            fontFamily = EduMindFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Primary700
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "↗",
                            fontFamily = EduMindFontFamily,
                            fontSize = 10.sp,
                            color = Primary600
                        )
                    }
                }
            }

            // Nội dung theo Tab chính
            if (selectedMainTab == 0) {
                // THANH TAB CON: Nội dung (50%) | Giới thiệu chung (50%)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Subtab 1: Nội dung (50%)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .background(if (selectedSubTab == 0) Primary50 else Color.White)
                            .clickable { selectedSubTab = 0 },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nội dung",
                            fontFamily = EduMindFontFamily,
                            fontSize = 12.sp,
                            fontWeight = if (selectedSubTab == 0) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (selectedSubTab == 0) Primary700 else Neutral500
                        )
                        // Thanh viền dưới: nếu active thì viền tím 2dp, nếu inactive thì viền xám mờ 1dp sát mép đáy
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (selectedSubTab == 0) 2.dp else 1.dp)
                                .align(Alignment.BottomCenter)
                                .background(if (selectedSubTab == 0) Primary600 else Neutral200)
                        )
                    }

                    // Vạch ngăn cách dọc
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(16.dp)
                            .background(Neutral200)
                    )

                    // Subtab 2: Giới thiệu chung (50%)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .background(if (selectedSubTab == 1) Primary50 else Color.White)
                            .clickable { selectedSubTab = 1 },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Giới thiệu chung",
                            fontFamily = EduMindFontFamily,
                            fontSize = 12.sp,
                            fontWeight = if (selectedSubTab == 1) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (selectedSubTab == 1) Primary700 else Neutral500
                        )
                        // Thanh viền dưới: nếu active thì viền tím 2dp, nếu inactive thì viền xám mờ 1dp sát mép đáy
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (selectedSubTab == 1) 2.dp else 1.dp)
                                .align(Alignment.BottomCenter)
                                .background(if (selectedSubTab == 1) Primary600 else Neutral200)
                        )
                    }
                }

                // VÙNG NỘI DUNG CUỘN CỦA TAB MỤC LỤC
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFFAFAFA))
                ) {
                    if (selectedSubTab == 0) {
                        LessonContentTab(onSeek = onSeek)
                    } else {
                        LessonOverviewTab()
                    }
                }
            } else {
                // TAB GHI CHÚ
                LessonNotesTab()
            }
        }

        // Nút tròn nổi EduAI (Nebula Gradient) ở góc dưới phải
        FloatingActionButton(
            onClick = onNavigateToAiChat,
            shape = CircleShape,
            containerColor = Color.Transparent,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 24.dp)
                .size(56.dp)
                .clip(CircleShape)
                .background(NebulaGradient)
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "Hỏi EduAI",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun LessonContentTab(onSeek: (Long) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // PHẦN A: BÀI HỌC TRƯỚC (ĐÃ XONG)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BÀI HỌC TRƯỚC (ĐÃ XONG)",
                    fontFamily = EduMindFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = Neutral400
                )
                Text(
                    text = "08:40",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = Neutral400
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, Neutral200, RoundedCornerShape(16.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Đã xong",
                        tint = Success600,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Bài 02: Cú pháp Print & Thụt lề (Indentation)",
                            fontFamily = EduMindFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Neutral800
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Hoàn thành - 100%",
                            fontFamily = EduMindFontFamily,
                            fontSize = 10.sp,
                            color = Neutral400
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Xem lại",
                    tint = Neutral400,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // PHẦN B: BÀI HIỆN TẠI (BÀI 03)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PHÂN ĐOẠN BÀI HỌC HIỆN TẠI (BÀI 03)",
                    fontFamily = EduMindFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = Primary700
                )
                Text(
                    text = "15:30",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary700
                )
            }

            // Phân đoạn 1: 00:00
            LessonSegmentCard(
                timeBadge = "00:00",
                title = "1. Giới thiệu tổng quan bài học",
                subtitle = "Khái niệm cốt lõi & lộ trình",
                onClick = { onSeek(0L) }
            )

            // Phân đoạn 2: 02:15
            LessonSegmentCard(
                timeBadge = "02:15",
                title = "2. Quy tắc khai báo biến chuẩn PEP 8",
                subtitle = "snake_case, không bắt đầu bằng số",
                onClick = { onSeek(135000L) }
            )

            // Phân đoạn 3: 05:40 (ĐANG PHÁT TẠI 04:15 - ACTIVE CARD NỔI BẬT)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Primary50)
                    .border(1.dp, Primary300, RoundedCornerShape(16.dp))
                    .clickable { onSeek(255000L) }
            ) {
                // Vạch tím Gradient Nebula ở mép trái
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(NebulaGradient)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp, top = 12.dp, end = 14.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Badge Nebula Gradient
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(NebulaGradient)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "05:40",
                                fontFamily = EduMindFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "3. Kiểu số nguyên int & số thực float",
                                fontFamily = EduMindFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Primary950
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Đang phát tại 04:15",
                                fontFamily = EduMindFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = Primary700
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Đang phát",
                        tint = Primary600,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Phân đoạn 4: 10:20
            LessonSegmentCard(
                timeBadge = "10:20",
                title = "4. Chuỗi ký tự (str) & Kiểu Logic (bool)",
                subtitle = "Ép kiểu casting và bài tập ngắn",
                onClick = { onSeek(620000L) }
            )
        }

        // PHẦN C: BÀI TẬP SAU BÀI HỌC
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Assignment,
                        contentDescription = null,
                        tint = Neutral400,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "BÀI TẬP",
                        fontFamily = EduMindFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = Neutral400
                    )
                }
                Text(
                    text = "10 câu",
                    fontFamily = EduMindFontFamily,
                    fontSize = 11.sp,
                    color = Neutral400
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, Neutral200, RoundedCornerShape(16.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Neutral100),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Assignment,
                            contentDescription = null,
                            tint = Neutral500,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Bài tập cuối bài 03: Biến & Kiểu dữ liệu",
                            fontFamily = EduMindFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Neutral700
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Mở sau khi xem hết bài giảng",
                            fontFamily = EduMindFontFamily,
                            fontSize = 10.sp,
                            color = Neutral400
                        )
                    }
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Mở",
                    tint = Neutral400,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // PHẦN D: BÀI HỌC TIẾP THEO
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BÀI HỌC TIẾP THEO",
                    fontFamily = EduMindFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = Neutral400
                )
                Text(
                    text = "18:20",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = Neutral400
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, Neutral200, RoundedCornerShape(16.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Neutral100),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "04",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Neutral600
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Bài 04: Cấu trúc rẽ nhánh If - Elif - Else",
                            fontFamily = EduMindFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Neutral800
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Chưa bắt đầu - 0%",
                            fontFamily = EduMindFontFamily,
                            fontSize = 10.sp,
                            color = Neutral400
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Khóa",
                    tint = Neutral300,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

// Card phân đoạn bình thường
@Composable
private fun LessonSegmentCard(
    timeBadge: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, Neutral200, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Neutral100)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = timeBadge,
                    fontFamily = EduMindFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Neutral600
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    fontFamily = EduMindFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Neutral800
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontFamily = EduMindFontFamily,
                    fontSize = 10.sp,
                    color = Neutral400
                )
            }
        }
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Phát",
            tint = Neutral400,
            modifier = Modifier.size(16.dp)
        )
    }
}

// -----------------------------------------------------------------------------------------
// TAB CON 2: GIỚI THIỆU CHUNG BÀI HỌC
// -----------------------------------------------------------------------------------------
@Composable
private fun LessonOverviewTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Card 1: Tổng quan bài 03
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .border(1.dp, Neutral200, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Primary50)
                        .border(1.dp, Primary100, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "Python Core",
                        fontFamily = EduMindFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary700
                    )
                }
                Text(
                    text = "15:30",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = Neutral500
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Bài 03: Khái niệm Biến và Kiểu dữ liệu cơ bản trong Python",
                fontFamily = EduMindFontFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Neutral950
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Bài học này cung cấp kiến thức nền tảng về cơ chế cấp phát bộ nhớ, nguyên lý Dynamic Typing trong Python, cách khai báo biến tuân theo chuẩn PEP 8 và 4 kiểu dữ liệu cơ bản: int, float, str, bool.",
                fontFamily = EduMindFontFamily,
                fontSize = 12.sp,
                color = Neutral600,
                lineHeight = 18.sp
            )
        }

        // Card 2: Mục tiêu bài học
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .border(1.dp, Neutral200, RoundedCornerShape(16.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "🎯 Mục tiêu bài học",
                fontFamily = EduMindFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Neutral900
            )

            val objectives = listOf(
                "Hiểu rõ nguyên lý Dynamic Typing và cách Python tự suy luận kiểu.",
                "Thành thạo quy tắc đặt tên biến snake_case chuẩn PEP 8.",
                "Phân biệt và thực hiện ép kiểu chính xác giữa int, float và string."
            )

            objectives.forEach { obj ->
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Success600,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(top = 1.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = obj,
                        fontFamily = EduMindFontFamily,
                        fontSize = 12.sp,
                        color = Neutral700,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Card 3: Giảng viên phụ trách
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .border(1.dp, Neutral200, RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Text(
                text = "👨‍🏫 Giảng viên phụ trách",
                fontFamily = EduMindFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Neutral900
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Primary100)
                        .border(1.dp, Primary300, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Primary700,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "ThS. Nguyễn Hoàng Nam",
                        fontFamily = EduMindFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Neutral900
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Senior Python Engineer - 8+ năm giảng dạy",
                        fontFamily = EduMindFontFamily,
                        fontSize = 10.sp,
                        color = Neutral500
                    )
                }
            }
        }

        // Card 4: Tài liệu đính kèm
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .border(1.dp, Neutral200, RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Text(
                text = "📁 Tài liệu đính kèm",
                fontFamily = EduMindFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Neutral900
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Neutral100)
                    .border(1.dp, Neutral200, RoundedCornerShape(12.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Primary100),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            tint = Primary700,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Slide_Bai03_Python_Bien.pdf",
                            fontFamily = EduMindFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Neutral800
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "2.4 MB • Tài liệu bài giảng",
                            fontFamily = EduMindFontFamily,
                            fontSize = 10.sp,
                            color = Neutral500
                        )
                    }
                }
                Text(
                    text = "Tải về",
                    fontFamily = EduMindFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary700
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
private fun LessonNotesTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Thanh lọc nhãn tag
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Nhãn:",
                fontFamily = EduMindFontFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Neutral400
            )

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(NebulaGradient)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tất cả (3)",
                    fontFamily = EduMindFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Primary50)
                    .border(1.dp, Primary100, CircleShape)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Cú pháp",
                    fontFamily = EduMindFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary700
                )
            }

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Neutral100)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Lỗi thường gặp",
                    fontFamily = EduMindFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Neutral600
                )
            }
        }

        // Header ghi chú
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ghi chú trong Bài 03 (3)",
                fontFamily = EduMindFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Neutral900
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(NebulaGradient)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Thêm tại 04:15",
                        fontFamily = EduMindFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // Ghi chú 1
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .border(1.dp, Primary100, RoundedCornerShape(16.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Primary50)
                        .border(1.dp, Primary100, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Cú pháp • 02:15",
                        fontFamily = EduMindFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary700
                    )
                }
            }
            Text(
                text = "Quy tắc đặt tên biến: Luôn dùng cú pháp snake_case theo chuẩn PEP 8. Tuyệt đối không bắt đầu tên biến bằng số.",
                fontFamily = EduMindFontFamily,
                fontSize = 12.sp,
                color = Neutral800,
                lineHeight = 18.sp
            )
        }

        // Ghi chú 2
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .border(1.dp, Neutral200, RoundedCornerShape(16.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Neutral100)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "Lỗi thường gặp • 07:35",
                    fontFamily = EduMindFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Neutral600
                )
            }
            Text(
                text = "Không cộng trực tiếp chuỗi với số (str + int) vì Python là ngôn ngữ strongly typed. Bắt buộc phải ép kiểu str(so).",
                fontFamily = EduMindFontFamily,
                fontSize = 12.sp,
                color = Neutral800,
                lineHeight = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

// -----------------------------------------------------------------------------------------
// PREVIEW CHUẨN MỰC
// -----------------------------------------------------------------------------------------
@Preview(name = "M-12 PlayerScreen (Toàn bộ màn hình)", showBackground = true, showSystemUi = true)
@Composable
private fun PlayerScreenPreview() {
    EduMindTheme {
        PlayerScreenContent(
            state = PocPlayerState(
                isPlaying = true,
                positionMs = 255000L, // 04:15
                durationMs = 930000L, // 15:30
                isControlsVisible = true,
                title = "Bài 03: Biến & Kiểu dữ liệu cơ bản"
            ),
            onBackClick = {},
            videoSurface = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF1E1E2E))
                )
            }
        )
    }
}
