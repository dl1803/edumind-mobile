package com.edumind.app.ui.course

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.edumind.app.data.model.response.CourseDetail
import com.edumind.app.data.model.response.CourseSection
import com.edumind.app.ui.common.EduMindButton
import com.edumind.app.ui.common.EduMindButtonStyle
import com.edumind.app.ui.common.ErrorView
import com.edumind.app.ui.theme.EduMindFontFamily
import com.edumind.app.ui.theme.Error100
import com.edumind.app.ui.theme.Error600
import com.edumind.app.ui.theme.NebulaBrush
import com.edumind.app.ui.theme.Neutral100
import com.edumind.app.ui.theme.Neutral200
import com.edumind.app.ui.theme.Neutral400
import com.edumind.app.ui.theme.Neutral50
import com.edumind.app.ui.theme.Neutral700
import com.edumind.app.ui.theme.Neutral900
import com.edumind.app.ui.theme.Primary100
import com.edumind.app.ui.theme.Primary50
import com.edumind.app.ui.theme.Primary600
import com.edumind.app.ui.theme.Primary700
import com.edumind.app.ui.theme.Success600
import com.edumind.app.ui.theme.Warning600
import com.edumind.app.util.toVnd

@Composable
fun CourseDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPlayer: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CourseDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    when (val state = uiState) {
        is CourseDetailUiState.Loading -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary700)
            }
        }

        is CourseDetailUiState.Error -> {
            ErrorView(
                message = state.message,
                onRetry = viewModel::retry,
                modifier = modifier.fillMaxSize()
            )
        }

        is CourseDetailUiState.Success -> {
            LaunchedEffect(state.userMessage) {
                state.userMessage?.let {
                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                    viewModel.clearUserMessage()
                }
            }

            val course = state.course

            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(Neutral50)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 96.dp)
                ) {
                    CourseHeaderMedia(
                        thumbnailUrl = course.thumbnailUrl,
                        isEnrolled = course.isEnrolled,
                        onBack = onNavigateBack,
                        onShare = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, course.title)
                                putExtra(Intent.EXTRA_TEXT, "Tham gia khóa học \"${course.title}\" trên EduMind: https://edumind.app/courses/${course.id}")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Chia sẻ khóa học"))
                        },
                        onPlayPreview = {
                            val firstLesson = course.sections.firstOrNull()?.lessons?.firstOrNull()?.id ?: "les_01"
                            onNavigateToPlayer(firstLesson)
                        }
                    )

                    CourseInfoSection(course = course)

                    TabRow(
                        selectedTabIndex = state.selectedTab,
                        containerColor = Color.White,
                        contentColor = Primary700,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[state.selectedTab]),
                                color = Primary700
                            )
                        }
                    ) {
                        Tab(
                            selected = state.selectedTab == 0,
                            onClick = { viewModel.onTabSelected(0) },
                            text = {
                                Text(
                                    text = "Giới thiệu",
                                    fontFamily = EduMindFontFamily,
                                    fontWeight = if (state.selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                            }
                        )
                        Tab(
                            selected = state.selectedTab == 1,
                            onClick = { viewModel.onTabSelected(1) },
                            text = {
                                val totalLessons = course.sections.sumOf { it.lessons.size }
                                Text(
                                    text = "Nội dung ($totalLessons)",
                                    fontFamily = EduMindFontFamily,
                                    fontWeight = if (state.selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                            }
                        )
                    }

                    if (state.selectedTab == 0) {
                        CourseIntroTab(course = course)
                    } else {
                        CourseSyllabusTab(
                            sections = course.sections,
                            isEnrolled = course.isEnrolled,
                            expandedIds = state.expandedSectionIds,
                            onToggleSection = viewModel::onToggleSection,
                            onToggleExpandAll = viewModel::onToggleExpandAll,
                            onLessonClick = { lessonId, isUnlocked ->
                                if (isUnlocked) {
                                    onNavigateToPlayer(lessonId)
                                } else {
                                    Toast.makeText(context, "Vui lòng mua khóa học để mở khóa bài học này", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }

                CourseStickyCtaBar(
                    course = course,
                    isEnrolling = state.isEnrolling,
                    onBuyClick = {
                        Toast.makeText(context, "Chuyển tới cổng thanh toán...", Toast.LENGTH_SHORT).show()
                    },
                    onEnrollFreeClick = {
                        viewModel.onEnrollFreeCourse {
                            Toast.makeText(context, "Đăng ký khóa học thành công!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onContinueClick = {
                        val targetLesson = course.lastLessonId
                            ?: course.sections.firstOrNull()?.lessons?.firstOrNull()?.id
                            ?: "les_01"
                        onNavigateToPlayer(targetLesson)
                    },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

@Composable
private fun CourseHeaderMedia(
    thumbnailUrl: String,
    isEnrolled: Boolean,
    onBack: () -> Unit,
    onShare: () -> Unit,
    onPlayPreview: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .background(Color.Black)
    ) {
        AsyncImage(
            model = thumbnailUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.85f)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.6f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.7f)
                        )
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = Color.White
                )
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(onClick = onShare),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Chia sẻ",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(60.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.3f))
                .clickable(onClick = onPlayPreview),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(NebulaBrush),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Phát video",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        if (isEnrolled) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Success600.copy(alpha = 0.9f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "✓ Đã sở hữu khóa học",
                    fontFamily = EduMindFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun CourseInfoSection(course: CourseDetail) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Primary50)
                .border(1.dp, Primary600.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = course.categoryName,
                fontFamily = EduMindFontFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Primary700
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = course.title,
            fontFamily = EduMindFontFamily,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 26.sp,
            color = Neutral900
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Warning600,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${course.rating}",
                    fontFamily = EduMindFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Neutral900
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "(${course.reviewCount} đánh giá)",
                    fontFamily = EduMindFontFamily,
                    fontSize = 12.sp,
                    color = Neutral700
                )
            }

            Text(
                text = "${course.studentCount} học viên",
                fontFamily = EduMindFontFamily,
                fontSize = 12.sp,
                color = Neutral700
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Giảng viên: ${course.instructor.name}",
                fontFamily = EduMindFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Neutral700
            )

            Text(
                text = "${course.totalHours} giờ học",
                fontFamily = EduMindFontFamily,
                fontSize = 12.sp,
                color = Neutral700
            )
        }
    }
}

@Composable
private fun CourseIntroTab(course: CourseDetail) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .border(1.dp, Neutral200, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "Giới thiệu khóa học",
                fontFamily = EduMindFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Neutral900
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = course.description,
                fontFamily = EduMindFontFamily,
                fontSize = 13.sp,
                lineHeight = 20.sp,
                color = Neutral700
            )

            if (course.keyFeatures.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Neutral100)
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Đặc điểm nổi bật:",
                    fontFamily = EduMindFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Neutral900
                )
                Spacer(modifier = Modifier.height(6.dp))
                course.keyFeatures.forEach { feature ->
                    Text(
                        text = "• $feature",
                        fontFamily = EduMindFontFamily,
                        fontSize = 12.sp,
                        color = Neutral700,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }

            if (course.learningObjectives.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Mục tiêu & Chuẩn đầu ra:",
                    fontFamily = EduMindFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Neutral900
                )
                Spacer(modifier = Modifier.height(6.dp))
                course.learningObjectives.forEach { obj ->
                    Text(
                        text = "✓ $obj",
                        fontFamily = EduMindFontFamily,
                        fontSize = 12.sp,
                        color = Success600,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .border(1.dp, Neutral200, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "Giảng viên hướng dẫn",
                fontFamily = EduMindFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Neutral900
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(NebulaBrush)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = course.instructor.name.split(" ").lastOrNull()?.take(2)?.uppercase() ?: "GV",
                            fontFamily = EduMindFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = Primary700,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = course.instructor.name,
                        fontFamily = EduMindFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Neutral900
                    )
                    Text(
                        text = course.instructor.title,
                        fontFamily = EduMindFontFamily,
                        fontSize = 12.sp,
                        color = Neutral700
                    )
                    Text(
                        text = "★ ${course.instructor.rating} • ${course.instructor.studentCount} học viên",
                        fontFamily = EduMindFontFamily,
                        fontSize = 11.sp,
                        color = Neutral400
                    )
                }
            }
        }
    }
}

@Composable
private fun CourseSyllabusTab(
    sections: List<CourseSection>,
    isEnrolled: Boolean,
    expandedIds: Set<String>,
    onToggleSection: (String) -> Unit,
    onToggleExpandAll: () -> Unit,
    onLessonClick: (String, Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val totalSections = sections.size
            val totalLessons = sections.sumOf { it.lessons.size }
            Text(
                text = "$totalSections Chương • $totalLessons Bài giảng",
                fontFamily = EduMindFontFamily,
                fontSize = 12.sp,
                color = Neutral700
            )

            Text(
                text = if (expandedIds.size == sections.size) "Thu gọn tất cả" else "Mở rộng tất cả",
                fontFamily = EduMindFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Primary700,
                modifier = Modifier.clickable(onClick = onToggleExpandAll)
            )
        }

        sections.forEach { section ->
            val isExpanded = expandedIds.contains(section.id)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, Neutral200, RoundedCornerShape(12.dp))
                    .animateContentSize()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggleSection(section.id) }
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = section.title,
                            fontFamily = EduMindFontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Neutral900
                        )
                        Text(
                            text = section.durationSummary,
                            fontFamily = EduMindFontFamily,
                            fontSize = 11.sp,
                            color = Neutral400
                        )
                    }

                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = Neutral400
                    )
                }

                AnimatedVisibility(visible = isExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Neutral50)
                    ) {
                        section.lessons.forEach { lesson ->
                            val isUnlocked = isEnrolled || lesson.isFreePreview

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onLessonClick(lesson.id, isUnlocked) }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = when {
                                            lesson.isCompleted -> Icons.Default.CheckCircle
                                            isUnlocked -> Icons.Default.PlayArrow
                                            else -> Icons.Default.Lock
                                        },
                                        contentDescription = null,
                                        tint = when {
                                            lesson.isCompleted -> Success600
                                            isUnlocked -> Primary700
                                            else -> Neutral400
                                        },
                                        modifier = Modifier.size(16.dp)
                                    )

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column {
                                        Text(
                                            text = lesson.title,
                                            fontFamily = EduMindFontFamily,
                                            fontSize = 12.sp,
                                            fontWeight = if (lesson.isCurrent) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isUnlocked) Neutral900 else Neutral400,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = lesson.durationText,
                                            fontFamily = EduMindFontFamily,
                                            fontSize = 10.sp,
                                            color = Neutral400
                                        )
                                    }
                                }

                                if (lesson.isFreePreview && !isEnrolled) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Primary100)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Học thử",
                                            fontFamily = EduMindFontFamily,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Primary700
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CourseStickyCtaBar(
    course: CourseDetail,
    isEnrolling: Boolean,
    onBuyClick: () -> Unit,
    onEnrollFreeClick: () -> Unit,
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .border(1.dp, Neutral200)
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (course.isEnrolled) {
            EduMindButton(
                text = "Tiếp tục học",
                onClick = onContinueClick,
                style = EduMindButtonStyle.Primary,
                modifier = Modifier.fillMaxWidth()
            )
        } else if (course.priceVnd <= 0L) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Miễn phí",
                    fontFamily = EduMindFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Success600
                )
            }
            EduMindButton(
                text = if (isEnrolling) "Đang đăng ký..." else "Đăng ký miễn phí",
                onClick = onEnrollFreeClick,
                enabled = !isEnrolling,
                loading = isEnrolling,
                style = EduMindButtonStyle.Primary
            )
        } else {
            Column(modifier = Modifier.weight(1f)) {
                course.originalPriceVnd?.let { originalPrice ->
                    Text(
                        text = originalPrice.toVnd(),
                        fontFamily = EduMindFontFamily,
                        fontSize = 11.sp,
                        color = Neutral400,
                        textDecoration = TextDecoration.LineThrough
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = course.priceVnd.toVnd(),
                        fontFamily = EduMindFontFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary700
                    )
                    course.originalPriceVnd?.let { originalPrice ->
                        val discountPercent = ((originalPrice - course.priceVnd) * 100 / originalPrice).toInt()
                        if (discountPercent > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Error100)
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "-$discountPercent%",
                                    fontFamily = EduMindFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Error600
                                )
                            }
                        }
                    }
                }
            }

            EduMindButton(
                text = "Mua khóa học",
                onClick = onBuyClick,
                style = EduMindButtonStyle.Primary
            )
        }
    }
}
