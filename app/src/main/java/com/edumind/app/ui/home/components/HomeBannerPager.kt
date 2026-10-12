package com.edumind.app.ui.home.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.edumind.app.data.model.response.FeaturedCourse
import com.edumind.app.ui.theme.EduMindFontFamily
import com.edumind.app.ui.theme.NebulaBrush
import com.edumind.app.ui.theme.Neutral300
import com.edumind.app.ui.theme.Warning600
import com.edumind.app.util.toVnd
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeBannerPager(
    featuredCourses: List<FeaturedCourse>,
    onCourseClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (featuredCourses.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { featuredCourses.size })
    val isDragged by pagerState.interactionSource.collectIsDraggedAsState()

    // Auto-scroll mỗi 5s, dừng khi user đang kéo
    LaunchedEffect(pagerState, isDragged, featuredCourses.size) {
        if (!isDragged && featuredCourses.size > 1) {
            while (true) {
                delay(5000)
                val nextPage = (pagerState.currentPage + 1) % featuredCourses.size
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) { page ->
            val course = featuredCourses[page]
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onCourseClick(course.id) },
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Ảnh nền Banner
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(course.bannerUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = course.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Gradient phủ tối
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.4f),
                                        Color.Black.copy(alpha = 0.85f)
                                    )
                                )
                            )
                    )

                    // Nội dung thông tin trên Banner
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        // Badges
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.25f), CircleShape)
                                    .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "✦ ${course.badgeText}",
                                    fontFamily = EduMindFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = Color.White
                                )
                            }
                            if (course.hasAiAssistant) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF2E1065).copy(alpha = 0.8f), CircleShape)
                                        .border(1.dp, Color(0xFFC084FC).copy(alpha = 0.4f), CircleShape)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "AI Trợ giảng 24/7",
                                        fontFamily = EduMindFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = Color(0xFFE9D5FF)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = course.title,
                            fontFamily = EduMindFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            lineHeight = 22.sp,
                            color = Color.White,
                            maxLines = 1
                        )

                        Text(
                            text = course.subtitle,
                            fontFamily = EduMindFontFamily,
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            maxLines = 1
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Hàng giá & đánh giá
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = course.priceVnd.toVnd(),
                                    fontFamily = EduMindFontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                                course.originalPriceVnd?.let { original ->
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = original.toVnd(),
                                        fontFamily = EduMindFontFamily,
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.6f),
                                        textDecoration = TextDecoration.LineThrough
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .background(Color.Black.copy(alpha = 0.35f), CircleShape)
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = Warning600,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${course.rating} (${course.reviewCount})",
                                    fontFamily = EduMindFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Indicator Dots
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(featuredCourses.size) { index ->
                val isSelected = pagerState.currentPage == index
                val width by animateDpAsState(
                    targetValue = if (isSelected) 24.dp else 6.dp,
                    label = "dotWidth"
                )
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .height(5.dp)
                        .width(width)
                        .clip(CircleShape)
                        .background(if (isSelected) NebulaBrush else Brush.linearGradient(listOf(Neutral300, Neutral300)))
                )
            }
        }
    }
}
