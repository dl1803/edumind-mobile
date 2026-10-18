package com.edumind.app.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.edumind.app.data.model.response.ContinueLearningItem
import com.edumind.app.data.model.response.CourseSummary
import com.edumind.app.data.model.response.MyCourse
import com.edumind.app.ui.common.CourseCard
import com.edumind.app.ui.common.ErrorView
import com.edumind.app.ui.theme.EduMindFontFamily
import com.edumind.app.ui.theme.NebulaGradient
import com.edumind.app.ui.theme.Neutral100
import com.edumind.app.ui.theme.Neutral200
import com.edumind.app.ui.theme.Neutral500
import com.edumind.app.ui.theme.Neutral700
import com.edumind.app.ui.theme.Neutral900
import com.edumind.app.ui.theme.Primary100
import com.edumind.app.ui.theme.Primary700
import com.edumind.app.ui.theme.Success600

@Composable
fun MyCoursesScreen(
    onCourseClick: (String) -> Unit = {},
    onResumeLearningClick: (courseId: String, lessonId: String) -> Unit = { _, _ -> },
    onExploreClick: () -> Unit = {},
    viewModel: MyCoursesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
    ) {
        TopAppBarHeader()

        when (val state = uiState) {
            is MyCoursesUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary700)
                }
            }

            is MyCoursesUiState.Error -> {
                ErrorView(
                    message = state.message,
                    onRetry = { viewModel.loadMyCourses() },
                    modifier = Modifier.fillMaxSize()
                )
            }

            is MyCoursesUiState.Empty -> {
                EmptyAllCoursesView(onExploreClick = onExploreClick)
            }

            is MyCoursesUiState.Success -> {
                SuccessContent(
                    state = state,
                    onFilterSelect = { viewModel.onFilterChanged(it) },
                    onCourseClick = onCourseClick,
                    onResumeLearningClick = onResumeLearningClick
                )
            }
        }
    }
}

@Composable
private fun TopAppBarHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Primary100),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = Primary700,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = "Khóa học của tôi",
                    fontFamily = EduMindFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Neutral900
                )
                Text(
                    text = "Thư viện học tập cá nhân",
                    fontFamily = EduMindFontFamily,
                    fontSize = 11.sp,
                    color = Neutral500
                )
            }
        }

        IconButton(onClick = { }) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Tìm kiếm",
                tint = Neutral700
            )
        }
    }
}

@Composable
private fun SuccessContent(
    state: MyCoursesUiState.Success,
    onFilterSelect: (MyCoursesFilter) -> Unit,
    onCourseClick: (String) -> Unit,
    onResumeLearningClick: (courseId: String, lessonId: String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 80.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(span = { GridItemSpan(2) }) {
            FilterChipsRow(
                state = state,
                onFilterSelect = onFilterSelect
            )
        }

        state.quickResumeItem?.let { resumeItem ->
            if (state.selectedFilter != MyCoursesFilter.COMPLETED) {
                item(span = { GridItemSpan(2) }) {
                    QuickResumeCard(
                        item = resumeItem,
                        onResumeClick = {
                            onResumeLearningClick(resumeItem.courseId, resumeItem.lessonId)
                        }
                    )
                }
            }
        }

        item(span = { GridItemSpan(2) }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DANH SÁCH KHÓA HỌC (${state.displayedCourses.size})",
                    fontFamily = EduMindFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Neutral900,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Truy cập gần nhất",
                    fontFamily = EduMindFontFamily,
                    fontSize = 11.sp,
                    color = Primary700,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        if (state.displayedCourses.isEmpty()) {
            item(span = { GridItemSpan(2) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Không có khóa học nào ở mục này.",
                        fontFamily = EduMindFontFamily,
                        fontSize = 13.sp,
                        color = Neutral500
                    )
                }
            }
        } else {
            items(state.displayedCourses, key = { it.id }) { myCourse ->
                val summary = CourseSummary(
                    id = myCourse.id,
                    title = myCourse.title,
                    thumbnailUrl = myCourse.thumbnailUrl,
                    instructorName = myCourse.instructorName,
                    rating = myCourse.rating,
                    reviewCount = myCourse.reviewCount,
                    priceVnd = 0L
                )

                CourseCard(
                    course = summary,
                    onClick = { onCourseClick(myCourse.id) },
                    progress = myCourse.progressPercent
                )
            }
        }
    }
}

@Composable
private fun FilterChipsRow(
    state: MyCoursesUiState.Success,
    onFilterSelect: (MyCoursesFilter) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        item {
            FilterChipItem(
                label = "Tất cả",
                count = state.allCount,
                isSelected = state.selectedFilter == MyCoursesFilter.ALL,
                onClick = { onFilterSelect(MyCoursesFilter.ALL) }
            )
        }
        item {
            FilterChipItem(
                label = "Đang học",
                count = state.inProgressCount,
                isSelected = state.selectedFilter == MyCoursesFilter.IN_PROGRESS,
                onClick = { onFilterSelect(MyCoursesFilter.IN_PROGRESS) }
            )
        }
        item {
            FilterChipItem(
                label = "Hoàn thành",
                count = state.completedCount,
                isSelected = state.selectedFilter == MyCoursesFilter.COMPLETED,
                onClick = { onFilterSelect(MyCoursesFilter.COMPLETED) }
            )
        }
    }
}

@Composable
private fun FilterChipItem(
    label: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundModifier = if (isSelected) {
        Modifier.background(NebulaGradient)
    } else {
        Modifier
            .background(Color.White)
            .border(1.dp, Neutral200, RoundedCornerShape(9999.dp))
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(9999.dp))
            .then(backgroundModifier)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            fontFamily = EduMindFontFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isSelected) Color.White else Neutral700
        )
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(if (isSelected) Color.White.copy(alpha = 0.25f) else Neutral100)
                .padding(horizontal = 6.dp, vertical = 1.dp)
        ) {
            Text(
                text = count.toString(),
                fontFamily = EduMindFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else Neutral700
            )
        }
    }
}

@Composable
private fun QuickResumeCard(
    item: ContinueLearningItem,
    onResumeClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF3B0764),
                        Color(0xFF1E1B4B),
                        Color(0xFF0F172A)
                    )
                )
            )
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF8B5CF6).copy(alpha = 0.3f))
                        .border(1.dp, Color(0xFFC084FC).copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "● Đang học dở",
                        fontFamily = EduMindFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE9D5FF)
                    )
                }

                Text(
                    text = "${item.progressPercent}%",
                    fontFamily = EduMindFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD8B4FE)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.currentLessonTitle,
                fontFamily = EduMindFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "Khóa: ${item.courseTitle}",
                fontFamily = EduMindFontFamily,
                fontSize = 11.sp,
                color = Color(0xFFCBD5E1),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { item.progressPercent.coerceIn(0, 100) / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(9999.dp)),
                color = Color(0xFF8B5CF6),
                trackColor = Color.White.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.remainingTimeText,
                    fontFamily = EduMindFontFamily,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(NebulaGradient)
                        .clickable(onClick = onResumeClick)
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Tiếp tục học",
                        fontFamily = EduMindFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyAllCoursesView(
    onExploreClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(Color.White)
                .border(2.dp, Color(0xFFE9D5FF), RoundedCornerShape(32.dp)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Primary100),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = Primary700,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Bạn chưa mua khóa học nào",
            fontFamily = EduMindFontFamily,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Neutral900
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Bắt đầu hành trình chinh phục kiến thức công nghệ cùng lộ trình bài bản và Trợ giảng AI đồng hành 24/7!",
            fontFamily = EduMindFontFamily,
            fontSize = 13.sp,
            color = Neutral500,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(NebulaGradient)
                .clickable(onClick = onExploreClick)
                .padding(horizontal = 28.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Explore,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "Khám phá khóa học",
                fontFamily = EduMindFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun QuickResumeCardPreview() {
    QuickResumeCard(
        item = ContinueLearningItem(
            courseId = "1",
            lessonId = "1",
            courseTitle = "Lập trình Python cơ bản",
            currentLessonTitle = "Bài 04: Cấu trúc điều kiện If-Else",
            thumbnailUrl = "",
            progressPercent = 45,
            remainingTimeText = "12:45 / 24:30"
        ),
        onResumeClick = {}
    )
}
