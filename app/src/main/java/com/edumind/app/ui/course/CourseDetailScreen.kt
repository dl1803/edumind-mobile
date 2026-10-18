package com.edumind.app.ui.course

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.RemoveRedEye
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.edumind.app.data.model.response.CourseDetail
import com.edumind.app.data.model.response.CourseSection
import com.edumind.app.data.model.response.LessonType
import com.edumind.app.ui.common.ErrorView
import com.edumind.app.ui.theme.EduMindFontFamily
import com.edumind.app.ui.theme.Error100
import com.edumind.app.ui.theme.Error600
import com.edumind.app.ui.theme.NebulaBrush
import com.edumind.app.ui.theme.Neutral100
import com.edumind.app.ui.theme.Neutral200
import com.edumind.app.ui.theme.Neutral400
import com.edumind.app.ui.theme.Neutral50
import com.edumind.app.ui.theme.Neutral500
import com.edumind.app.ui.theme.Neutral600
import com.edumind.app.ui.theme.Neutral700
import com.edumind.app.ui.theme.Neutral800
import com.edumind.app.ui.theme.Neutral900
import com.edumind.app.ui.theme.Neutral950
import com.edumind.app.ui.theme.Primary100
import com.edumind.app.ui.theme.Primary200
import com.edumind.app.ui.theme.Primary50
import com.edumind.app.ui.theme.Primary600
import com.edumind.app.ui.theme.Primary700
import com.edumind.app.ui.theme.Success100
import com.edumind.app.ui.theme.Success600
import com.edumind.app.ui.theme.Success700
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
            val totalLessons = course.sections.sumOf { it.lessons.size }

            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(Neutral50)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = if (course.isEnrolled) 90.dp else 80.dp)
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
                                color = Primary600
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
                                    fontSize = 14.sp,
                                    color = if (state.selectedTab == 0) Primary700 else Neutral500
                                )
                            }
                        )
                        Tab(
                            selected = state.selectedTab == 1,
                            onClick = { viewModel.onTabSelected(1) },
                            text = {
                                Text(
                                    text = "Nội dung ($totalLessons)",
                                    fontFamily = EduMindFontFamily,
                                    fontWeight = if (state.selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = if (state.selectedTab == 1) Primary700 else Neutral500
                                )
                            }
                        )
                        Tab(
                            selected = state.selectedTab == 2,
                            onClick = { viewModel.onTabSelected(2) },
                            text = {
                                Text(
                                    text = "Đánh giá (${course.reviewCount})",
                                    fontFamily = EduMindFontFamily,
                                    fontWeight = if (state.selectedTab == 2) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = if (state.selectedTab == 2) Primary700 else Neutral500
                                )
                            }
                        )
                    }

                    when (state.selectedTab) {
                        0 -> CourseIntroTab(
                            course = course,
                            onContinueLearning = {
                                val target = course.lastLessonId ?: "l_2_2"
                                onNavigateToPlayer(target)
                            }
                        )
                        1 -> CourseSyllabusTab(
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
                        2 -> CourseReviewsTab(
                            course = course,
                            onWriteReviewClick = {
                                Toast.makeText(context, "Mở giao diện viết đánh giá", Toast.LENGTH_SHORT).show()
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
                            ?: "l_2_2"
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
            .height(280.dp)
            .background(Color.Black)
    ) {
        AsyncImage(
            model = thumbnailUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.9f)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.75f),
                            Color.Black.copy(alpha = 0.20f),
                            Color.Black.copy(alpha = 0.60f)
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
                    .size(44.dp)
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
                    .size(44.dp)
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
                .size(64.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.25f))
                .border(2.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                .clickable(onClick = onPlayPreview),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(NebulaBrush),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Xem thử",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        if (isEnrolled) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 14.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Success600.copy(alpha = 0.95f))
                    .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Đã sở hữu khóa học",
                        fontFamily = EduMindFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 14.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .clickable(onClick = onPlayPreview)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.RemoveRedEye,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Xem thử video",
                        fontFamily = EduMindFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
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
                .background(Color(0xFFEFF6FF))
                .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(6.dp))
                .padding(horizontal = 10.dp, vertical = 2.dp)
        ) {
            Text(
                text = course.categoryName,
                fontFamily = EduMindFontFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1D4ED8)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = course.title,
            fontFamily = EduMindFontFamily,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Neutral950,
            lineHeight = 22.sp
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
                    modifier = Modifier.size(14.dp)
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
                    color = Neutral500
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.People,
                    contentDescription = null,
                    tint = Neutral400,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${course.studentCount} học viên",
                    fontFamily = EduMindFontFamily,
                    fontSize = 12.sp,
                    color = Neutral600
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Neutral400,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Giảng viên: ",
                    fontFamily = EduMindFontFamily,
                    fontSize = 12.sp,
                    color = Neutral600
                )
                Text(
                    text = course.instructor.name,
                    fontFamily = EduMindFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Neutral800,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = Neutral400,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${course.totalHours} giờ học",
                    fontFamily = EduMindFontFamily,
                    fontSize = 12.sp,
                    color = Neutral600
                )
            }
        }
    }
}

@Composable
private fun CourseIntroTab(
    course: CourseDetail,
    onContinueLearning: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (course.isEnrolled) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, Neutral200.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFEFF6FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Primary600,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tiến độ bài học",
                            fontFamily = EduMindFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Neutral900
                        )
                    }

                    Text(
                        text = "${course.progressPercent}% hoàn thành",
                        fontFamily = EduMindFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary700
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Neutral100)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = (course.progressPercent / 100f).coerceIn(0.05f, 1f))
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(NebulaBrush)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Primary50.copy(alpha = 0.6f))
                        .border(1.dp, Primary100, RoundedCornerShape(12.dp))
                        .clickable(onClick = onContinueLearning)
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
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(NebulaBrush),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Đang học dở:",
                                fontFamily = EduMindFontFamily,
                                fontSize = 10.sp,
                                color = Neutral400
                            )
                            Text(
                                text = course.lastLessonTitle ?: "Bài 5: Vòng lặp For và While",
                                fontFamily = EduMindFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Neutral900,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Primary700,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, Neutral200.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Primary50),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Primary600,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI Trợ giảng tương tác ngữ cảnh",
                        fontFamily = EduMindFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Neutral900
                    )
                }
                Text(
                    text = "Học viên được hỗ trợ bởi AI Trợ giảng tương tác theo ngữ cảnh bài giảng (hạn mức 50 câu hỏi/ngày và 10 lượt tóm tắt/ngày).",
                    fontFamily = EduMindFontFamily,
                    fontSize = 12.sp,
                    color = Neutral600,
                    lineHeight = 18.sp
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .border(1.dp, Neutral200.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFEFF6FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VideoLibrary,
                        contentDescription = null,
                        tint = Primary600,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Giới thiệu khóa học",
                    fontFamily = EduMindFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Neutral900
                )
            }

            Text(
                text = course.description,
                fontFamily = EduMindFontFamily,
                fontSize = 12.sp,
                color = Neutral600,
                lineHeight = 18.sp
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = Primary600,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${course.totalHours} giờ video HD",
                            fontFamily = EduMindFontFamily,
                            fontSize = 11.sp,
                            color = Neutral700
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Primary600,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "AI Trợ giảng 24/7",
                            fontFamily = EduMindFontFamily,
                            fontSize = 11.sp,
                            color = Neutral700
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VideoLibrary,
                            contentDescription = null,
                            tint = Primary600,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "24 bài giảng (4 chương)",
                            fontFamily = EduMindFontFamily,
                            fontSize = 11.sp,
                            color = Neutral700
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Laptop,
                            contentDescription = null,
                            tint = Success600,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Dự án & Thực hành",
                            fontFamily = EduMindFontFamily,
                            fontSize = 11.sp,
                            color = Neutral700
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Neutral100)
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Mục tiêu & Chuẩn đầu ra",
                    fontFamily = EduMindFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Neutral800
                )
                course.learningObjectives.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Success600,
                            modifier = Modifier
                                .size(15.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = item,
                            fontFamily = EduMindFontFamily,
                            fontSize = 12.sp,
                            color = Neutral600,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Neutral100)
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Yêu cầu tham gia",
                    fontFamily = EduMindFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Neutral800
                )
                course.requirements.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 6.dp)
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Neutral400)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = item,
                            fontFamily = EduMindFontFamily,
                            fontSize = 12.sp,
                            color = Neutral600,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .border(1.dp, Neutral200.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Primary50),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Primary600,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Giảng viên hướng dẫn",
                    fontFamily = EduMindFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Neutral900
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(NebulaBrush)
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "LN",
                            fontFamily = EduMindFontFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Primary700
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = course.instructor.name,
                        fontFamily = EduMindFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Neutral900
                    )
                    Text(
                        text = "Giảng viên Khoa CNTT — ĐHQG Hà Nội",
                        fontFamily = EduMindFontFamily,
                        fontSize = 11.sp,
                        color = Neutral500
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                SpanStyle(
                                    color = Warning600,
                                    fontWeight = FontWeight.SemiBold
                                )
                            ) {
                                append("★ ${course.instructor.rating} Đánh giá")
                            }
                            append("  •  ${course.instructor.courseCount} Khóa học  •  ${course.instructor.studentCount} học viên")
                        },
                        fontFamily = EduMindFontFamily,
                        fontSize = 10.sp,
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
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val totalSections = sections.size
        val totalLessons = sections.sumOf { it.lessons.size }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$totalSections Chương • $totalLessons Bài giảng • 14h tổng cộng",
                fontFamily = EduMindFontFamily,
                fontSize = 12.sp,
                color = Neutral500
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

        sections.forEachIndexed { index, section ->
            val isExpanded = expandedIds.contains(section.id)
            val isCurrentChapter = isEnrolled && index == 1

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(
                        width = 1.dp,
                        color = if (isCurrentChapter) Primary200 else Neutral200,
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isCurrentChapter) Primary50.copy(alpha = 0.7f) else Neutral50)
                        .clickable { onToggleSection(section.id) }
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = section.title,
                            fontFamily = EduMindFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCurrentChapter) Primary700 else Neutral900
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (index == 0 && isEnrolled) {
                                "${section.lessons.size} bài giảng • 45 phút • Đã hoàn thành 100%"
                            } else if (isCurrentChapter) {
                                "${section.lessons.size} bài giảng • 1h 20p • Đang học (1/4)"
                            } else {
                                "${section.lessons.size} bài giảng • 45 phút"
                            },
                            fontFamily = EduMindFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (index == 0 && isEnrolled) Success700 else if (isCurrentChapter) Primary700 else Neutral500
                        )
                    }

                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = if (isCurrentChapter) Primary700 else Neutral500,
                        modifier = Modifier.size(20.dp)
                    )
                }

                AnimatedVisibility(visible = isExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                    ) {
                        section.lessons.forEachIndexed { lessonIndex, lesson ->
                            val isCompleted = isEnrolled && (index == 0 || (index == 1 && lessonIndex == 0))
                            val isCurrent = isEnrolled && index == 1 && lessonIndex == 1
                            val isUnlocked = isEnrolled || lesson.isFreePreview

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (isCurrent) Primary50 else Color.White)
                                    .clickable { onLessonClick(lesson.id, isUnlocked) }
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (isCompleted) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Success600,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    } else if (isCurrent) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(NebulaBrush),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    } else if (isUnlocked) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = Primary700,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = Neutral400,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = lesson.title,
                                            fontFamily = EduMindFontFamily,
                                            fontSize = 12.sp,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isCurrent) Neutral950 else if (isUnlocked) Neutral900 else Neutral700
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (isCurrent) "Đang học dở" else "${if (lesson.type == LessonType.VIDEO) "Video" else "Quiz"} • ${lesson.durationText}",
                                            fontFamily = EduMindFontFamily,
                                            fontSize = 10.sp,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isCurrent) Primary700 else Neutral400
                                        )
                                    }
                                }

                                if (isCurrent) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(NebulaBrush)
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "Tiếp tục",
                                            fontFamily = EduMindFontFamily,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                } else if (!isEnrolled && lesson.isFreePreview) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFEFF6FF))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
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

                            if (lessonIndex < section.lessons.size - 1) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(Neutral100)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CourseReviewsTab(
    course: CourseDetail,
    onWriteReviewClick: () -> Unit
) {
    var selectedFilterTag by remember { mutableStateOf("Tất cả (${course.reviewCount})") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .border(1.dp, Neutral200.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(end = 16.dp)
                ) {
                    Text(
                        text = "${course.rating}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Neutral950
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        repeat(5) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Warning600,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Text(
                        text = "${course.reviewCount} đánh giá",
                        fontFamily = EduMindFontFamily,
                        fontSize = 10.sp,
                        color = Neutral500
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(72.dp)
                        .background(Neutral200)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    RatingDistributionBar(star = "5", percent = 78, percentText = "78%")
                    RatingDistributionBar(star = "4", percent = 16, percentText = "16%")
                    RatingDistributionBar(star = "3", percent = 4, percentText = "4%")
                    RatingDistributionBar(star = "2", percent = 1, percentText = "1%")
                    RatingDistributionBar(star = "1", percent = 1, percentText = "1%")
                }
            }
        }

        if (course.isEnrolled) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFEFF6FF), Color(0xFFFAF5FF), Color(0xFFFDF2F8))
                        )
                    )
                    .border(1.dp, Primary200.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(NebulaBrush),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Viết đánh giá của bạn",
                                fontFamily = EduMindFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Neutral950
                            )
                            Text(
                                text = "Đủ điều kiện gửi nhận xét (≥20% tiến độ)",
                                fontFamily = EduMindFontFamily,
                                fontSize = 10.sp,
                                color = Neutral500
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Success100)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Tiến độ ${course.progressPercent}%",
                            fontFamily = EduMindFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Success700
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(NebulaBrush)
                        .clickable(onClick = onWriteReviewClick),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Viết đánh giá ngay",
                        fontFamily = EduMindFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Neutral100)
                    .border(1.dp, Neutral200, RoundedCornerShape(16.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Neutral400,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Chỉ học viên đã mua khóa học và hoàn thành từ 20% bài giảng mới có thể gửi nhận xét.",
                    fontFamily = EduMindFontFamily,
                    fontSize = 11.sp,
                    color = Neutral600,
                    lineHeight = 16.sp
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val chips = listOf("Tất cả (${course.reviewCount})", "5 sao (110)", "Có nhận xét AI", "Mới nhất")
            chips.forEach { chipText ->
                val isSelected = chipText == selectedFilterTag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) Primary700 else Color.White)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) Primary700 else Neutral200,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { selectedFilterTag = chipText }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = chipText,
                        fontFamily = EduMindFontFamily,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (isSelected) Color.White else Neutral600
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ReviewCardItem(
                avatarText = "TM",
                name = "Trần Thị Mai",
                time = "2 ngày trước",
                rating = 5,
                content = "Khóa học cực kỳ chi tiết cho người mới. Điểm mình thích nhất là tính năng AI Trợ giảng: đoạn nào xem video chưa hiểu chỉ cần bấm hỏi là AI trích dẫn giải thích ngay!"
            )
            ReviewCardItem(
                avatarText = "LA",
                name = "Lê Tuấn Anh",
                time = "1 tuần trước",
                rating = 5,
                content = "Thầy Nam giảng rất thực tế, không lý thuyết suông. Bài tập chương 2 hơi thử thách một chút nhưng làm xong hiểu sâu bản chất vòng lặp luôn."
            )
            ReviewCardItem(
                avatarText = "DM",
                name = "Đặng Đức Minh",
                time = "2 tuần trước",
                rating = 5,
                content = "Video nét 1080p, phụ đề AI nhận diện tiếng Việt rất chuẩn. Mong app ra thêm khóa Django chuyên sâu sau khóa này."
            )
        }
    }
}

@Composable
private fun RatingDistributionBar(
    star: String,
    percent: Int,
    percentText: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = star,
            fontFamily = EduMindFontFamily,
            fontSize = 10.sp,
            color = Neutral500,
            modifier = Modifier.width(12.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Neutral200)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = (percent / 100f).coerceIn(0f, 1f))
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Warning600)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = percentText,
            fontFamily = EduMindFontFamily,
            fontSize = 10.sp,
            color = Neutral400,
            modifier = Modifier.width(28.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun ReviewCardItem(
    avatarText: String,
    name: String,
    time: String,
    rating: Int,
    content: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, Neutral200.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Primary100),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = avatarText,
                        fontFamily = EduMindFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary700
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = name,
                        fontFamily = EduMindFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Neutral900
                    )
                    Text(
                        text = time,
                        fontFamily = EduMindFontFamily,
                        fontSize = 10.sp,
                        color = Neutral400
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                repeat(rating) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Warning600,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        Text(
            text = content,
            fontFamily = EduMindFontFamily,
            fontSize = 12.sp,
            color = Neutral700,
            lineHeight = 18.sp
        )
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
    if (course.isEnrolled) {
        val lessonShortName = remember(course.lastLessonTitle) {
            val raw = course.lastLessonTitle ?: "Bài 5"
            val match = Regex("""Bài\s*\d+""", RegexOption.IGNORE_CASE).find(raw)
            match?.value ?: (if (raw.contains(":")) raw.substringBefore(":").trim() else raw.take(10).trim())
        }

        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(Color.White)
                .border(width = 1.dp, color = Neutral200)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(NebulaBrush)
                    .clickable(onClick = onContinueClick),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tiếp tục học ($lessonShortName)",
                        fontFamily = EduMindFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    } else {
        val isFree = course.priceVnd == 0L
        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(Color.White)
                .border(width = 1.dp, color = Neutral200)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isFree) "Miễn phí" else course.priceVnd.toVnd(),
                fontFamily = EduMindFontFamily,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                style = if (isFree) {
                    TextStyle(color = Success600)
                } else {
                    TextStyle(brush = NebulaBrush)
                }
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(NebulaBrush)
                    .clickable(
                        enabled = !isEnrolling,
                        onClick = if (isFree) onEnrollFreeClick else onBuyClick
                    )
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isFree) "Đăng ký miễn phí" else "Mua khóa học",
                        fontFamily = EduMindFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
