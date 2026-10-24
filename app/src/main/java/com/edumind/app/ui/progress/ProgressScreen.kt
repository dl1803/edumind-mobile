package com.edumind.app.ui.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
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
import com.edumind.app.ui.progress.components.ProgressRing
import com.edumind.app.ui.progress.components.WeeklyBarChart
import com.edumind.app.ui.theme.*

@Composable
fun ProgressScreen(
    onExploreClick: () -> Unit = {},
    onCourseClick: (String) -> Unit = {},
    viewModel: ProgressViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = BgCanvasMobile,
        topBar = {
            ProgressTopBar()
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
                        onCourseClick = onCourseClick
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

@Composable
private fun ProgressTopBar() {
    Surface(
        color = Color.White,
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Primary100),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "NA",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Primary700
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Nguyễn Văn A",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Neutral950
                        )
                        Surface(
                            color = Warning100,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Học viên",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Warning700,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Text(
                        text = "Tiến độ học tập cá nhân",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            color = Neutral500
                        )
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable { },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Thông báo",
                    tint = Primary700,
                    modifier = Modifier.size(22.dp)
                )
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.TopEnd)
                        .clip(CircleShape)
                        .background(Error600),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "3",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressContent(
    state: ProgressUiState.Success,
    onEditGoalClick: () -> Unit,
    onCourseClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    ProgressRing(percent = state.overallPercent)

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp)
                    ) {
                        Text(
                            text = "Tiến độ học tập",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 16.sp,
                                lineHeight = 22.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Neutral950
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Duy trì thói quen học tập đều đặn mỗi ngày để đạt mục tiêu của bạn!",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            ),
                            color = Neutral600
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KpiCard(
                    title = "${state.data.totalHoursWatched}h",
                    subtitle = "Tổng giờ học",
                    icon = Icons.Outlined.Schedule,
                    iconBg = Color(0xFFEFF6FF),
                    iconTint = Color(0xFF2563EB),
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "${state.data.totalEnrolledCourses} khóa",
                    subtitle = "Hoàn thành",
                    icon = Icons.Outlined.School,
                    iconBg = Primary50,
                    iconTint = Primary700,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KpiCard(
                    title = "${state.data.totalExercisesPassed} / ${state.data.totalExercises} bài",
                    subtitle = "Đạt yêu cầu",
                    icon = Icons.Outlined.CheckCircle,
                    iconBg = Success100,
                    iconTint = Success600,
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "${state.data.streakDays} ngày",
                    subtitle = "Chuỗi học tập 🔥",
                    icon = Icons.Outlined.LocalFireDepartment,
                    iconBg = Color(0xFFFFF7ED),
                    iconTint = Color(0xFFEA580C),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            WeeklyBarChart(
                weeklyData = state.data.weeklyStudy,
                dailyGoalMinutes = state.data.dailyGoalMinutes,
                onEditGoalClick = onEditGoalClick
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "KHÓA HỌC ĐANG HỌC",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = Neutral700
                )
                Text(
                    text = "${state.data.courses.size} khóa đang học",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Primary700
                )
            }
        }

        items(state.data.courses, key = { it.courseId }) { course ->
            CourseProgressItem(
                course = course,
                onClick = { onCourseClick(course.courseId) }
            )
        }
    }
}

@Composable
private fun KpiCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(72.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder()
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
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Neutral950,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 10.sp,
                        color = Neutral500
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun CourseProgressItem(
    course: CourseProgress,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder()
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
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = course.courseTitle,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Neutral950,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${course.completedLessons} / ${course.totalLessons} bài học",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            color = Neutral500
                        )
                    )
                    Text(
                        text = "${course.progressPercent.toInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        ),
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
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Primary50),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Học tiếp",
                    tint = Primary700,
                    modifier = Modifier.size(18.dp)
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
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            shape = RoundedCornerShape(20.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SkeletonBox(modifier = Modifier.weight(1f).height(72.dp), shape = RoundedCornerShape(16.dp))
            SkeletonBox(modifier = Modifier.weight(1f).height(72.dp), shape = RoundedCornerShape(16.dp))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SkeletonBox(modifier = Modifier.weight(1f).height(72.dp), shape = RoundedCornerShape(16.dp))
            SkeletonBox(modifier = Modifier.weight(1f).height(72.dp), shape = RoundedCornerShape(16.dp))
        }
        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            shape = RoundedCornerShape(16.dp)
        )
        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            shape = RoundedCornerShape(16.dp)
        )
    }
}
