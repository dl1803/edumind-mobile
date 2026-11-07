package com.edumind.app.ui.player

import android.app.Activity
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.edumind.app.util.findActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.alpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.edumind.app.data.model.response.PlayerChapter
import com.edumind.app.ui.player.subtitle.SubtitleOverlay
import com.edumind.app.ui.player.subtitle.SubtitleViewModel
import com.edumind.app.ui.player.subtitle.TranscriptPanel
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
    viewModel: PlayerViewModel = hiltViewModel(),
    subtitleViewModel: SubtitleViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val loadState by viewModel.loadState.collectAsState()
    val availableQualities by viewModel.availableQualities.collectAsState()
    val selectedQuality by viewModel.selectedQuality.collectAsState()
    val resumePromptPositionMs by viewModel.resumePromptPositionMs.collectAsState()
    val chapters by viewModel.chapters.collectAsState()
    val currentChapterIndex by viewModel.currentChapterIndex.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()
    val isFullscreen by viewModel.isFullscreen.collectAsState()
    val subtitleState by subtitleViewModel.uiState.collectAsState()
    var isQualitySheetVisible by remember { mutableStateOf(false) }
    var isChapterSheetVisible by rememberSaveable { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val activity = remember(context) { context.findActivity() }

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearUserMessage()
        }
    }

    LaunchedEffect(isFullscreen) {
        activity?.let { act ->
            val window = act.window
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            if (isFullscreen) {
                insetsController.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            } else {
                insetsController.show(WindowInsetsCompat.Type.systemBars())
                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }
    }

    BackHandler(enabled = isFullscreen) {
        viewModel.exitFullscreen()
    }

    LaunchedEffect(loadState) {
        if (loadState is LessonLoadState.Ready) {
            val lesson = (loadState as LessonLoadState.Ready).lesson
            subtitleViewModel.initLessonId(lesson.id)
        }
    }

    LaunchedEffect(state.positionMs) {
        subtitleViewModel.onPositionChanged(state.positionMs)
    }

    DisposableEffect(Unit) {
        onDispose {
            activity?.let { act ->
                val window = act.window
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.show(WindowInsetsCompat.Type.systemBars())
                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }
    }

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

    // Xử lý các trạng thái phân quyền, video xử lý hoặc lỗi tải bài giảng
    when (val currentLoad = loadState) {
        is LessonLoadState.Forbidden -> {
            PlayerStateCard(
                title = "Nội dung có tính phí",
                message = currentLoad.message,
                buttonText = "Quay lại khóa học",
                onButtonClick = onBackClick
            )
        }
        is LessonLoadState.Processing -> {
            PlayerStateCard(
                title = "Video đang được xử lý",
                message = currentLoad.message,
                buttonText = "Quay lại",
                onButtonClick = onBackClick
            )
        }
        is LessonLoadState.Error -> {
            PlayerStateCard(
                title = "Không thể tải bài giảng",
                message = currentLoad.message,
                buttonText = "Thử lại",
                onButtonClick = { viewModel.retry() },
                secondaryButtonText = "Quay lại",
                onSecondaryButtonClick = onBackClick
            )
        }
        LessonLoadState.Loading,
        is LessonLoadState.Ready -> {
            PlayerScreenContent(
                state = state,
                onBackClick = {
                    if (isFullscreen) {
                        viewModel.exitFullscreen()
                    } else {
                        onBackClick()
                    }
                },
                onNavigateToAiSummary = onNavigateToAiSummary,
                onNavigateToAiChat = onNavigateToAiChat,
                onTogglePlayPause = { viewModel.togglePlayPause() },
                onSeek = { targetMs -> viewModel.seekTo(targetMs) },
                onSeekRelative = { offsetMs -> viewModel.seekRelative(offsetMs) },
                onOverlayClick = { viewModel.toggleControlsVisibility() },
                onToggleFullscreen = {
                    viewModel.toggleFullscreen()
                },
                isLandscape = isFullscreen || isLandscape,
                availableQualities = availableQualities,
                onQualityClick = { isQualitySheetVisible = true },
                onChapterClick = { isChapterSheetVisible = true },
                chapters = chapters,
                currentChapterIndex = currentChapterIndex,
                onChapterSelect = { chapter -> viewModel.onChapterSelected(chapter) },
                transcriptContent = {
                    TranscriptPanel(
                        segments = subtitleState.segments,
                        currentIndex = subtitleState.currentIndex,
                        onSeek = { targetMs -> viewModel.seekTo(targetMs) }
                    )
                },
                subtitleText = if (subtitleState.enabled) subtitleState.currentText else null,
                hasSubtitles = subtitleState.hasSubtitles,
                subtitleEnabled = subtitleState.enabled,
                onToggleSubtitle = { subtitleViewModel.toggleEnabled() },
                videoSurface = {
                    if (loadState is LessonLoadState.Loading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.material3.CircularProgressIndicator(
                                color = Primary600,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    } else {
                        var playerViewRef by remember { mutableStateOf<PlayerView?>(null) }

                        DisposableEffect(Unit) {
                            onDispose {
                                playerViewRef?.apply {
                                    visibility = android.view.View.GONE
                                    player = null
                                }
                            }
                        }

                        AndroidView(
                            factory = { ctx ->
                                PlayerView(ctx).apply {
                                    useController = false
                                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                                    player = viewModel.exoPlayer
                                }.also { playerViewRef = it }
                            },
                            update = { playerView ->
                                playerView.player = viewModel.exoPlayer
                                playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                                playerViewRef = playerView
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            )

            // Hộp thoại xác nhận tiếp tục xem bài học từ mốc dừng cũ
            resumePromptPositionMs?.let { posMs ->
                ResumePlaybackDialog(
                    positionMs = posMs,
                    onConfirmResume = { viewModel.confirmResume() },
                    onRestart = { viewModel.restartFromBeginning() }
                )
            }

            // Bảng trượt đáy chọn chất lượng video
            if (isQualitySheetVisible) {
                QualitySelectionBottomSheet(
                    availableQualities = availableQualities,
                    selectedQuality = selectedQuality,
                    onQualitySelected = { quality ->
                        viewModel.setQuality(quality)
                    },
                    onDismiss = { isQualitySheetVisible = false }
                )
            }

            // Bảng trượt đáy danh sách chương và phân đoạn
            if (isChapterSheetVisible) {
                ChapterSheet(
                    chapters = chapters,
                    currentIndex = currentChapterIndex,
                    onChapterClick = { chapter ->
                        viewModel.onChapterSelected(chapter)
                    },
                    onDismiss = { isChapterSheetVisible = false }
                )
            }
        }
    }
}

@Composable
private fun PlayerStateCard(
    title: String,
    message: String,
    buttonText: String,
    onButtonClick: () -> Unit,
    secondaryButtonText: String? = null,
    onSecondaryButtonClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Neutral950)
            .statusBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .padding(24.dp)
        ) {
            Text(
                text = title,
                fontFamily = EduMindFontFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Neutral900,
                textAlign = TextAlign.Center
            )
            Text(
                text = message,
                fontFamily = EduMindFontFamily,
                fontSize = 14.sp,
                color = Neutral600,
                textAlign = TextAlign.Center
            )
            androidx.compose.material3.Button(
                onClick = onButtonClick,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = Primary700
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                Text(
                    text = buttonText,
                    fontFamily = EduMindFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
            if (secondaryButtonText != null && onSecondaryButtonClick != null) {
                androidx.compose.material3.OutlinedButton(
                    onClick = onSecondaryButtonClick,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, com.edumind.app.ui.theme.Neutral300),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text(
                        text = secondaryButtonText,
                        fontFamily = EduMindFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Neutral700
                    )
                }
            }
        }
    }
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
    onToggleFullscreen: () -> Unit = {},
    isLandscape: Boolean = false,
    availableQualities: List<QualityOption> = emptyList(),
    onQualityClick: () -> Unit = {},
    onChapterClick: () -> Unit = {},
    chapters: List<PlayerChapter> = emptyList(),
    currentChapterIndex: Int = 0,
    onChapterSelect: (PlayerChapter) -> Unit = {},
    transcriptContent: @Composable () -> Unit = {},
    subtitleText: String? = null,
    hasSubtitles: Boolean = false,
    subtitleEnabled: Boolean = true,
    onToggleSubtitle: () -> Unit = {},
    videoSurface: @Composable () -> Unit = {}
) {
    var selectedMainTab by remember { mutableIntStateOf(0) }
    var selectedSubTab by remember { mutableIntStateOf(0) }

    if (isLandscape) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            videoSurface()

            PlayerControlsOverlay(
                state = state,
                onTogglePlayPause = onTogglePlayPause,
                onSeek = onSeek,
                onSeekRelative = onSeekRelative,
                onBackClick = onBackClick,
                onToggleFullscreen = onToggleFullscreen,
                onOverlayClick = onOverlayClick,
                isLandscape = true,
                availableQualities = availableQualities,
                onQualityClick = onQualityClick,
                onChapterClick = onChapterClick,
                chapters = chapters,
                hasSubtitles = hasSubtitles,
                subtitleEnabled = subtitleEnabled,
                onToggleSubtitle = onToggleSubtitle
            )

            SubtitleOverlay(
                text = subtitleText,
                controlsVisible = state.isControlsVisible,
                isLandscape = true
            )
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFAFAFA))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .statusBarsPadding()
                    .background(Color(0xFFFAFAFA))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .background(Color.Black)
                ) {
                    videoSurface()

                    PlayerControlsOverlay(
                        state = state,
                        onTogglePlayPause = onTogglePlayPause,
                        onSeek = onSeek,
                        onSeekRelative = onSeekRelative,
                        onBackClick = onBackClick,
                        onToggleFullscreen = onToggleFullscreen,
                        onOverlayClick = onOverlayClick,
                        isLandscape = false,
                        availableQualities = availableQualities,
                        onQualityClick = onQualityClick,
                        onChapterClick = onChapterClick,
                        chapters = chapters,
                        hasSubtitles = hasSubtitles,
                        subtitleEnabled = subtitleEnabled,
                        onToggleSubtitle = onToggleSubtitle
                    )

                    SubtitleOverlay(
                        text = subtitleText,
                        controlsVisible = state.isControlsVisible,
                        isLandscape = false
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

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(16.dp)
                            .background(Neutral200)
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .background(if (selectedSubTab == 2) Primary50 else Color.White)
                            .clickable { selectedSubTab = 2 },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Phụ đề",
                            fontFamily = EduMindFontFamily,
                            fontSize = 12.sp,
                            fontWeight = if (selectedSubTab == 2) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (selectedSubTab == 2) Primary700 else Neutral500
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (selectedSubTab == 2) 2.dp else 1.dp)
                                .align(Alignment.BottomCenter)
                                .background(if (selectedSubTab == 2) Primary600 else Neutral200)
                        )
                    }
                }

                // VÙNG NỘI DUNG CUỘN CỦA TAB MỤC LỤC
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFFAFAFA))
                ) {
                    when (selectedSubTab) {
                        0 -> LessonContentTab(
                            chapters = chapters,
                            currentIndex = currentChapterIndex,
                            onChapterSelect = onChapterSelect
                        )
                        1 -> LessonOverviewTab()
                        else -> transcriptContent()
                    }
                }
            } else {
                // TAB GHI CHÚ
                LessonNotesTab()
            }
        }

        // Nút tròn nổi EduAI (Nebula Gradient) ở góc dưới phải
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 24.dp)
                .size(56.dp)
                .clip(CircleShape)
                .background(NebulaGradient)
                .clickable(onClick = onNavigateToAiChat),
            contentAlignment = Alignment.Center
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
}

@Composable
private fun LessonContentTab(
    chapters: List<PlayerChapter>,
    currentIndex: Int,
    onChapterSelect: (PlayerChapter) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "MỤC LỤC BÀI HỌC & PHÂN ĐOẠN",
                fontFamily = EduMindFontFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = Primary700
            )
            Text(
                text = "${chapters.size} mục",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Primary700
            )
        }

        if (chapters.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Bài giảng này không có phân đoạn riêng",
                    fontFamily = EduMindFontFamily,
                    fontSize = 13.sp,
                    color = Neutral500
                )
            }
        } else {
            chapters.forEachIndexed { index, chapter ->
                val isCurrent = index == currentIndex
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isCurrent) Primary50.copy(alpha = 0.7f) else Color.White)
                        .border(
                            width = 1.dp,
                            color = if (isCurrent) Primary300 else Neutral200,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onChapterSelect(chapter) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isCurrent) Primary100 else Neutral100)
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = chapter.durationText,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrent) Primary700 else Neutral600
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = chapter.title,
                            fontFamily = EduMindFontFamily,
                            fontSize = 12.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (isCurrent) Primary900 else Neutral800
                        )
                    }

                    if (isCurrent) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Đang phát",
                            tint = Primary600,
                            modifier = Modifier.size(16.dp)
                        )
                    } else if (chapter.isCompleted) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Đã xong",
                            tint = Success600,
                            modifier = Modifier.size(16.dp)
                        )
                    } else if (chapter.isLocked) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = "Bị khóa",
                            tint = Neutral400,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
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
