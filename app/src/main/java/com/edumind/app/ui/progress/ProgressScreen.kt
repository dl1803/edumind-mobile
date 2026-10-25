package com.edumind.app.ui.progress

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.edumind.app.data.model.response.CourseProgress
import com.edumind.app.ui.common.EmptyStateView
import com.edumind.app.ui.common.ErrorView
import com.edumind.app.ui.common.SkeletonBox
import com.edumind.app.ui.progress.components.GoalAdjustmentBottomSheet
import com.edumind.app.ui.progress.components.WeeklyBarChart
import com.edumind.app.ui.theme.*

@Composable
fun ProgressScreen(
    userName: String = "Nguyễn Văn Minh",
    onExploreClick: () -> Unit = {},
    onCourseClick: (String) -> Unit = {},
    onPlayClick: (String) -> Unit = onCourseClick,
    viewModel: ProgressViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCanvasMobile)
    ) {
        // Hiệu ứng Ambient Glow nhẹ trên đầu màn hình đồng bộ với HomeScreen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x1A8B5CF6),
                            Color(0x0DE0E7FF),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            if (uiState is ProgressUiState.Loading) {
                ProgressTopBarSkeleton()
            } else {
                ProgressTopBar(userName = userName)
            }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (val state = uiState) {
                is ProgressUiState.Loading -> {
                    ProgressLoadingSkeleton()
                }

                is ProgressUiState.Empty -> {
                    EmptyStateView(
                        title = "Chưa có dữ liệu tiến độ",
                        message = "Bắt đầu bài giảng đầu tiên để kích hoạt biểu đồ thời lượng học tập và chuỗi ngày học tập của bạn!",
                        actionText = "Khám phá khóa học ngay",
                        onAction = onExploreClick,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is ProgressUiState.Error -> {
                    ErrorView(
                        message = state.message,
                        onRetry = { viewModel.retry() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is ProgressUiState.Success -> {
                    ProgressContent(
                        state = state,
                        onEditGoalClick = { viewModel.toggleGoalSheet(true) },
                        onCourseClick = onCourseClick,
                        onPlayClick = onPlayClick
                    )

                    if (state.isGoalSheetVisible) {
                        GoalAdjustmentBottomSheet(
                            currentGoal = state.data.dailyGoalMinutes,
                            onDismiss = { viewModel.toggleGoalSheet(false) },
                            onConfirm = { newGoal -> viewModel.updateDailyGoal(newGoal) }
                        )
                    }
                }
            }
        }
    }
}
}

@Composable
private fun ProgressTopBar(
    userName: String = "Nguyễn Văn Minh"
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar viền Nebula đồng bộ trang Home
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(NebulaBrush, shape = CircleShape)
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.White, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userName.take(1).uppercase(),
                            fontFamily = EduMindFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = Primary700
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = userName,
                        fontFamily = EduMindFontFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Neutral900
                    )
                    // Card Học viên kích cỡ tương đương card chuỗi trang Home, padding gọn bao trọn
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFEF3C7), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(12.dp))
                            .padding(horizontal = 6.dp, vertical = 1.5.dp)
                    ) {
                        Text(
                            text = "Học viên",
                            fontFamily = EduMindFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = Color(0xFFB45309)
                        )
                    }
                }
                Text(
                    text = "Tiến độ học tập cá nhân",
                    fontFamily = EduMindFontFamily,
                    fontSize = 12.sp,
                    color = Neutral500
                )
            }
        }

        // Icon Chuông đồng bộ trang Home
        Box(
            modifier = Modifier
                .size(40.dp)
                .clickable { },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier.size(28.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = "Thông báo",
                    modifier = Modifier
                        .size(28.dp)
                        .graphicsLayer(alpha = 0.99f)
                        .drawWithCache {
                            onDrawWithContent {
                                drawContent()
                                drawRect(NebulaBrush, blendMode = BlendMode.SrcIn)
                            }
                        }
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 3.dp, y = (-2).dp)
                        .size(16.dp)
                        .background(Error600, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "3",
                        style = TextStyle(
                            fontFamily = EduMindFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            lineHeight = 10.sp,
                            platformStyle = @Suppress("DEPRECATION") PlatformTextStyle(
                                includeFontPadding = false
                            ),
                            lineHeightStyle = LineHeightStyle(
                                alignment = LineHeightStyle.Alignment.Center,
                                trim = LineHeightStyle.Trim.Both
                            )
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressTopBarSkeleton() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SkeletonBox(
                modifier = Modifier.size(42.dp),
                shape = CircleShape
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SkeletonBox(
                        modifier = Modifier
                            .width(100.dp)
                            .height(16.dp),
                        shape = RoundedCornerShape(4.dp)
                    )
                    SkeletonBox(
                        modifier = Modifier
                            .width(52.dp)
                            .height(18.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                SkeletonBox(
                    modifier = Modifier
                        .width(130.dp)
                        .height(12.dp),
                    shape = RoundedCornerShape(4.dp)
                )
            }
        }
        SkeletonBox(
            modifier = Modifier.size(36.dp),
            shape = CircleShape
        )
    }
}

@Composable
private fun ProgressContent(
    state: ProgressUiState.Success,
    onEditGoalClick: () -> Unit,
    onCourseClick: (String) -> Unit,
    onPlayClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // KPI row 1: Tổng giờ học & Hoàn thành
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KpiCard(
                    title = "${state.data.totalHoursWatched}h",
                    subtitle = "Tổng giờ học",
                    customIcon = { SolidClockIcon(tint = Color(0xFF2563EB)) },
                    iconBg = Color(0xFFEFF6FF),
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "${state.data.totalEnrolledCourses} khóa",
                    subtitle = "Hoàn thành",
                    icon = Icons.Filled.School,
                    iconBg = Color(0xFFF5F3FF),
                    iconTint = Color(0xFF8B5CF6),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // KPI row 2: Bài tập đạt yêu cầu & Chuỗi học tập
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KpiCard(
                    title = "${state.data.totalExercisesPassed} / ${state.data.totalExercises} bài",
                    subtitle = "Đạt yêu cầu",
                    customIcon = { SolidCheckCircleIcon(tint = Color(0xFF16A34A)) },
                    iconBg = Color(0xFFF0FDF4),
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "${state.data.streakDays} ngày",
                    subtitle = "Chuỗi học tập 🔥",
                    icon = Icons.Filled.LocalFireDepartment,
                    iconBg = Color(0xFFFFF7ED),
                    iconTint = Color(0xFFEA580C),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Biểu đồ thời lượng tuần & Mục tiêu
        item {
            WeeklyBarChart(
                weeklyData = state.data.weeklyStudy,
                dailyGoalMinutes = state.data.dailyGoalMinutes,
                onEditGoalClick = onEditGoalClick
            )
        }

        // Tiêu đề section Khóa học đang học
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "KHÓA HỌC ĐANG HỌC",
                    fontFamily = EduMindFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = Neutral900
                )
                Text(
                    text = "${state.data.courses.size} khóa đang học",
                    fontFamily = EduMindFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary700
                )
            }
        }

        // Danh sách khóa học
        items(state.data.courses, key = { it.courseId }) { course ->
            CourseProgressItem(
                course = course,
                onClick = { onCourseClick(course.courseId) },
                onPlayClick = { onPlayClick(course.courseId) }
            )
        }
    }
}

@Composable
private fun KpiCard(
    title: String,
    subtitle: String,
    icon: ImageVector? = null,
    iconBg: Color,
    iconTint: Color = Color.Unspecified,
    customIcon: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(72.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5E5E5).copy(alpha = 0.8f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                if (customIcon != null) {
                    customIcon()
                } else if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    fontFamily = EduMindFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Neutral900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    fontFamily = EduMindFontFamily,
                    fontSize = 10.sp,
                    color = Neutral500,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SolidClockIcon(
    tint: Color = Color(0xFF2563EB),
    modifier: Modifier = Modifier.size(20.dp)
) {
    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2f
        // Solid colored circle
        drawCircle(
            color = tint,
            radius = radius,
            center = center
        )
        // White clock hands
        val strokeWidth = 2.dp.toPx()
        // Vertical hand from 12 o'clock to center
        drawLine(
            color = Color.White,
            start = Offset(center.x, center.y - radius * 0.52f),
            end = Offset(center.x, center.y),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        // Horizontal hand from center to 3 o'clock
        drawLine(
            color = Color.White,
            start = Offset(center.x, center.y),
            end = Offset(center.x + radius * 0.42f, center.y),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun SolidCheckCircleIcon(
    tint: Color = Color(0xFF16A34A),
    modifier: Modifier = Modifier.size(20.dp)
) {
    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2f
        // Solid colored circle
        drawCircle(
            color = tint,
            radius = radius,
            center = center
        )
        // White checkmark
        val strokeWidth = 2.dp.toPx()
        val start = Offset(center.x - radius * 0.45f, center.y)
        val pivot = Offset(center.x - radius * 0.1f, center.y + radius * 0.35f)
        val end = Offset(center.x + radius * 0.45f, center.y - radius * 0.35f)

        drawLine(Color.White, start, pivot, strokeWidth, StrokeCap.Round)
        drawLine(Color.White, pivot, end, strokeWidth, StrokeCap.Round)
    }
}

@Composable
private fun CourseProgressItem(
    course: CourseProgress,
    onClick: () -> Unit,
    onPlayClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5E5E5).copy(alpha = 0.8f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                model = course.thumbnailUrl,
                contentDescription = course.courseTitle,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = course.courseTitle,
                    fontFamily = EduMindFontFamily,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Neutral900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(5.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${course.completedLessons} / ${course.totalLessons} bài học",
                        fontFamily = EduMindFontFamily,
                        fontSize = 10.sp,
                        color = Neutral500
                    )
                    Text(
                        text = "${course.progressPercent.toInt()}%",
                        fontFamily = EduMindFontFamily,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary700
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Neutral100)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = (course.progressPercent / 100f).coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(3.dp))
                            .background(NebulaBrush)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF5F3FF))
                    .clickable { onPlayClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Học tiếp",
                    tint = Primary700,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun ProgressLoadingSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 4 KPI Cards Grid Skeleton
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiCardSkeleton(modifier = Modifier.weight(1f))
            KpiCardSkeleton(modifier = Modifier.weight(1f))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiCardSkeleton(modifier = Modifier.weight(1f))
            KpiCardSkeleton(modifier = Modifier.weight(1f))
        }

        // Weekly Chart Card Skeleton
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE5E5E5).copy(alpha = 0.8f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        SkeletonBox(
                            modifier = Modifier
                                .width(140.dp)
                                .height(14.dp),
                            shape = RoundedCornerShape(4.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        SkeletonBox(
                            modifier = Modifier
                                .width(176.dp)
                                .height(10.dp),
                            shape = RoundedCornerShape(4.dp)
                        )
                    }
                    SkeletonBox(
                        modifier = Modifier
                            .width(80.dp)
                            .height(28.dp),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 7 Chart bars
                val barHeights = listOf(42.dp, 38.dp, 58.dp, 26.dp, 50.dp, 74.dp, 10.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(116.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    barHeights.forEach { height ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            SkeletonBox(
                                modifier = Modifier
                                    .width(16.dp)
                                    .height(height),
                                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            SkeletonBox(
                                modifier = Modifier
                                    .width(14.dp)
                                    .height(10.dp),
                                shape = RoundedCornerShape(2.dp)
                            )
                        }
                    }
                }
            }
        }

        // Section header skeleton
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SkeletonBox(
                modifier = Modifier
                    .width(128.dp)
                    .height(12.dp),
                shape = RoundedCornerShape(4.dp)
            )
            SkeletonBox(
                modifier = Modifier
                    .width(80.dp)
                    .height(12.dp),
                shape = RoundedCornerShape(4.dp)
            )
        }

        // 2 Course Cards Skeleton
        repeat(2) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE5E5E5).copy(alpha = 0.8f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SkeletonBox(
                        modifier = Modifier.size(64.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        SkeletonBox(
                            modifier = Modifier
                                .fillMaxWidth(0.75f)
                                .height(14.dp),
                            shape = RoundedCornerShape(4.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        SkeletonBox(
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .height(10.dp),
                            shape = RoundedCornerShape(4.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        SkeletonBox(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp),
                            shape = RoundedCornerShape(3.dp)
                        )
                    }
                    SkeletonBox(
                        modifier = Modifier.size(32.dp),
                        shape = CircleShape
                    )
                }
            }
        }
    }
}

@Composable
private fun KpiCardSkeleton(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(72.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5E5E5).copy(alpha = 0.8f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SkeletonBox(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(12.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                SkeletonBox(
                    modifier = Modifier
                        .width(48.dp)
                        .height(14.dp),
                    shape = RoundedCornerShape(4.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                SkeletonBox(
                    modifier = Modifier
                        .width(64.dp)
                        .height(10.dp),
                    shape = RoundedCornerShape(4.dp)
                )
            }
        }
    }
}
