package com.edumind.app.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.edumind.app.ui.theme.EduMindFontFamily
import com.edumind.app.ui.theme.Primary50
import com.edumind.app.ui.theme.Primary700
import com.edumind.app.ui.theme.Primary800
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Màn hình Onboarding (Walkthrough) giới thiệu các tính năng chính của ứng dụng.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinishOnboarding: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val pages = onboardingPages
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(viewModel.navigateToLogin) {
        viewModel.navigateToLogin.collectLatest {
            onFinishOnboarding()
        }
    }

    val isLastPage = pagerState.currentPage == pages.size - 1

    OnboardingScreenContent(
        pages = pages,
        currentPage = pagerState.currentPage,
        isLastPage = isLastPage,
        onSkipClick = { viewModel.completeOnboarding() },
        onNextClick = {
            if (isLastPage) {
                viewModel.completeOnboarding()
            } else {
                coroutineScope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                }
            }
        },
        pagerContent = {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { pageIndex ->
                OnboardingSlideItem(
                    page = pages[pageIndex],
                    pageIndex = pageIndex
                )
            }
        }
    )
}

@Composable
fun OnboardingScreenContent(
    pages: List<OnboardingPage>,
    currentPage: Int,
    isLastPage: Boolean,
    onSkipClick: () -> Unit,
    onNextClick: () -> Unit,
    pagerContent: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top Bar chứa nút "Bỏ qua"
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            this@Column.AnimatedVisibility(
                visible = !isLastPage,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                val skipInteraction = remember { MutableInteractionSource() }
                val isSkipPressed by skipInteraction.collectIsPressedAsState()

                Text(
                    text = "Bỏ qua",
                    color = if (isSkipPressed) Primary800 else Primary700,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = EduMindFontFamily,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSkipPressed) Primary50 else Color.Transparent)
                        .clickable(
                            interactionSource = skipInteraction,
                            indication = null,
                            onClick = onSkipClick
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }

        // 2. Nội dung Slides: HorizontalPager
        pagerContent()

        // 3. Vùng Điều Khiển Dưới Đáy (Dots Indicator + Action Button)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hàng Page Indicator Dots (gap-2 = 8dp, mb-6 = 24dp)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                repeat(pages.size) { index ->
                    val isSelected = currentPage == index
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(width = 24.dp, height = 8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF3B82F6),
                                            Color(0xFF8B5CF6),
                                            Color(0xFFEC4899)
                                        )
                                    )
                                )
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD4D4D4))
                        )
                    }
                }
            }

            // Nút "Tiếp theo" (slides 1..3) hoặc "Bắt đầu" (slide 4)
            // Đổ bóng tím phát sáng shadow-nebula (spotColor tím, loại bỏ bóng đen đậm)
            val btnInteraction = remember { MutableInteractionSource() }
            val isBtnPressed by btnInteraction.collectIsPressedAsState()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .shadow(
                        elevation = if (isBtnPressed) 4.dp else 10.dp,
                        shape = RoundedCornerShape(12.dp),
                        clip = false,
                        ambientColor = Color(0x608B5CF6),
                        spotColor = Color(0x808B5CF6)
                    )
                    .background(
                        brush = if (isBtnPressed) {
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF2563EB),
                                    Color(0xFF7C3AED),
                                    Color(0xFFDB2777)
                                )
                            )
                        } else {
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF3B82F6),
                                    Color(0xFF8B5CF6),
                                    Color(0xFFEC4899)
                                )
                            )
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable(
                        interactionSource = btnInteraction,
                        indication = null,
                        onClick = onNextClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isLastPage) "Bắt đầu" else "Tiếp theo",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = EduMindFontFamily
                )
            }
        }
    }
}

/**
 * Slide đơn lẻ gồm khu vực minh họa Nebula Ambient + Tiêu đề & Mô tả
 */
@Composable
fun OnboardingSlideItem(
    page: OnboardingPage,
    pageIndex: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Khối minh họa:
        // 1. Nền base sáng #F8FAFC
        // 2. Dải gradient nebula-ambient đa sắc
        // 3. Vầng loang hào quang tròn mềm ở trung tâm bao bọc ảnh minh họa
        // 4. Hai dải fade làm mờ ở đỉnh và đáy hòa vào nền trắng
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
                .background(Color(0xFFF8FAFC))
                .background(
                    Brush.linearGradient(
                        colorStops = arrayOf(
                            0.00f to Color(0x283B82F6), // ~16% blue
                            0.38f to Color(0x328B5CF6), // ~20% purple
                            0.72f to Color(0x2BEC4899), // ~17% pink
                            1.00f to Color(0x208B5CF6)  // ~13% purple
                        )
                    )
                )
                .drawBehind {
                    // Vầng loang hào quang tròn tỏa sáng ở trung tâm quanh ảnh
                    val centerOffset = Offset(size.width / 2f, size.height / 2f)
                    val glowRadius = size.width * 0.46f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(
                                0.00f to Color(0x458B5CF6), // 27% tím rực rỡ
                                0.40f to Color(0x28EC4899), // 16% hồng tươi
                                0.72f to Color(0x123B82F6), // 7% xanh dương
                                1.00f to Color.Transparent
                            ),
                            center = centerOffset,
                            radius = glowRadius
                        ),
                        center = centerOffset,
                        radius = glowRadius
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            // Hình minh họa chi tiết theo từng slide
            when (pageIndex) {
                0 -> OnboardingIllustration1()
                1 -> OnboardingIllustration2()
                2 -> OnboardingIllustration3()
                3 -> OnboardingIllustration4()
            }

            // Top edge blend fade (48dp White -> Transparent)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.White, Color.Transparent)
                        )
                    )
            )

            // Bottom edge blend fade (48dp Transparent -> White)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.White)
                        )
                    )
            )
        }

        // Phần Tiêu đề và Mô tả (px-8 = 32dp, pt-6 = 24dp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .padding(top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = page.title,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF0A0A0A),
                textAlign = TextAlign.Center,
                lineHeight = 28.sp,
                fontFamily = EduMindFontFamily
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = page.description,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF525252),
                textAlign = TextAlign.Center,
                lineHeight = 21.sp,
                fontFamily = EduMindFontFamily
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 917)
@Composable
private fun OnboardingSlide1Preview() {
    OnboardingScreenContent(
        pages = onboardingPages,
        currentPage = 0,
        isLastPage = false,
        onSkipClick = {},
        onNextClick = {},
        pagerContent = {
            OnboardingSlideItem(
                page = onboardingPages[0],
                pageIndex = 0
            )
        }
    )
}

@Preview(showBackground = true, widthDp = 412, heightDp = 917)
@Composable
private fun OnboardingSlide4Preview() {
    OnboardingScreenContent(
        pages = onboardingPages,
        currentPage = 3,
        isLastPage = true,
        onSkipClick = {},
        onNextClick = {},
        pagerContent = {
            OnboardingSlideItem(
                page = onboardingPages[3],
                pageIndex = 3
            )
        }
    )
}
