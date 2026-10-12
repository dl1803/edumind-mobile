package com.edumind.app.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.edumind.app.ui.common.CourseCard
import com.edumind.app.ui.common.EmptyStateView
import com.edumind.app.ui.common.ErrorView
import com.edumind.app.ui.home.components.ContinueLearningSection
import com.edumind.app.ui.home.components.HomeBannerPager
import com.edumind.app.ui.home.components.HomeHeader
import com.edumind.app.ui.home.components.HomeSearchBar
import com.edumind.app.ui.home.components.HomeSkeleton
import com.edumind.app.ui.home.components.HomeStickyHeader
import com.edumind.app.ui.theme.BgCanvasMobile
import com.edumind.app.ui.theme.EduMindFontFamily
import com.edumind.app.ui.theme.Neutral950
import com.edumind.app.ui.theme.Primary700

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onCourseClick: (String) -> Unit = {},
    onContinueClick: (courseId: String, lessonId: String) -> Unit = { _, _ -> },
    onSearchClick: () -> Unit = {},
    onAiChatClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onAvatarClick: () -> Unit = {},
    onSeeAllCoursesClick: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Cấu hình Pull-to-refresh
    val pullRefreshState = rememberPullToRefreshState()
    if (pullRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            viewModel.refresh()
        }
    }
    LaunchedEffect(uiState.isRefreshing) {
        if (!uiState.isRefreshing) {
            pullRefreshState.endRefresh()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCanvasMobile)
    ) {
        // Hiệu ứng Ambient Glow nhẹ trên đầu màn hình
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

        when {
            uiState.isLoading -> {
                HomeSkeleton()
            }

            uiState.errorMessage != null && uiState.featuredCourses.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    ErrorView(
                        message = uiState.errorMessage ?: "Đã có lỗi xảy ra",
                        onRetry = { viewModel.loadHomeData() }
                    )
                }
            }

            uiState.isEmpty -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStateView(
                        title = "Chưa có nội dung",
                        message = "Hiện chưa có khóa học nào được cập nhật. Vui lòng quay lại sau!",
                        actionText = "Tải lại",
                        onAction = { viewModel.loadHomeData() }
                    )
                }
            }

            else -> {
                // Khung cuộn duy nhất: Lưới 2 cột
                val gridState = rememberLazyGridState()
                val isStickyHeaderVisible by remember {
                    derivedStateOf {
                        // Chỉ hiển thị sticky header khi đã cuộn qua hoàn toàn cả Header (index 0) và Search Bar (index 1)
                        gridState.firstVisibleItemIndex > 1
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(pullRefreshState.nestedScrollConnection)
                ) {
                    LazyVerticalGrid(
                        state = gridState,
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        // Chừa lề 16dp 2 bên và 120dp đáy để Floating Glass Dock không che khuất
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 1. Header (Span 2 cột)
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            HomeHeader(
                                userName = uiState.userDisplayName,
                                streakDays = uiState.streakDays,
                                unreadCount = uiState.unreadNotificationCount,
                                onAvatarClick = onAvatarClick,
                                onNotificationClick = onNotificationClick
                            )
                        }

                        // 2. Search Bar (Span 2 cột)
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            HomeSearchBar(
                                onSearchClick = onSearchClick,
                                onAiChatClick = onAiChatClick
                            )
                        }

                        // 3. Banner Carousel (Span 2 cột)
                        if (uiState.featuredCourses.isNotEmpty()) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                HomeBannerPager(
                                    featuredCourses = uiState.featuredCourses,
                                    onCourseClick = onCourseClick
                                )
                            }
                        }

                        // 4. Tiếp tục học (Span 2 cột)
                        if (uiState.continueLearningItems.isNotEmpty()) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                ContinueLearningSection(
                                    items = uiState.continueLearningItems,
                                    onContinueClick = onContinueClick,
                                    onSeeAllClick = onSeeAllCoursesClick
                                )
                            }
                        }

                        // 5. Section "Gợi ý cho bạn"
                        if (uiState.recommendedCourses.isNotEmpty()) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp, bottom = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Gợi ý cho bạn",
                                        fontFamily = EduMindFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Neutral950
                                    )
                                    Text(
                                        text = "Xem tất cả ›",
                                        fontFamily = EduMindFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = Primary700
                                    )
                                }
                            }

                            items(uiState.recommendedCourses, key = { "rec_${it.id}" }) { course ->
                                CourseCard(
                                    course = course,
                                    onClick = { onCourseClick(course.id) }
                                )
                            }
                        }

                        // 6. Section "Mới nhất"
                        if (uiState.newCourses.isNotEmpty()) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp, bottom = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Mới nhất",
                                        fontFamily = EduMindFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Neutral950
                                    )
                                    Text(
                                        text = "Xem tất cả ›",
                                        fontFamily = EduMindFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = Primary700
                                    )
                                }
                            }

                            items(uiState.newCourses, key = { "new_${it.id}" }) { course ->
                                CourseCard(
                                    course = course,
                                    onClick = { onCourseClick(course.id) }
                                )
                            }
                        }

                        // Khoảng đệm chân lưới
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    // Sticky Compact Header khi người dùng cuộn màn hình lên
                    AnimatedVisibility(
                        visible = isStickyHeaderVisible,
                        enter = fadeIn() + slideInVertically { -it },
                        exit = fadeOut() + slideOutVertically { -it },
                        modifier = Modifier.align(Alignment.TopCenter)
                    ) {
                        HomeStickyHeader(
                            unreadCount = uiState.unreadNotificationCount,
                            onSearchClick = onSearchClick,
                            onAiChatClick = onAiChatClick,
                            onNotificationClick = onNotificationClick
                        )
                    }

                    // Pull-to-refresh Indicator
                    PullToRefreshContainer(
                        state = pullRefreshState,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }
            }
        }
    }
}
