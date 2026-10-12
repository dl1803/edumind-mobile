package com.edumind.app.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edumind.app.ui.theme.EduMindFontFamily
import com.edumind.app.ui.theme.Error600
import com.edumind.app.ui.theme.NebulaBrush
import com.edumind.app.ui.theme.Neutral200
import com.edumind.app.ui.theme.Neutral400
import com.edumind.app.ui.theme.Primary600
import com.edumind.app.ui.theme.Primary700

/**
 * Sticky Compact Header hiển thị khi người dùng cuộn màn hình lên.
 * Gồm:
 * 1. Thanh tìm kiếm 60-65% width
 * 2. Nút compact "+ Hỏi AI"
 * 3. Icon chuông gradient với badge số thông báo
 */
@Composable
fun HomeStickyHeader(
    unreadCount: Int,
    onSearchClick: () -> Unit,
    onAiChatClick: () -> Unit,
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.98f),
        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 1. Thanh tìm kiếm nhỏ gọn (60% - 65% width)
            Box(
                modifier = Modifier
                    .weight(0.65f)
                    .height(38.dp)
                    .clip(RoundedCornerShape(19.dp))
                    .background(Color(0xFFFAFAFA))
                    .border(1.dp, Neutral200, RoundedCornerShape(19.dp))
                    .clickable(onClick = onSearchClick)
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = Primary600,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Tìm kiếm khóa học...",
                        fontFamily = EduMindFontFamily,
                        fontSize = 12.sp,
                        color = Neutral400,
                        maxLines = 1
                    )
                }
            }

            // 2. Nút "+ Hỏi AI" thu nhỏ nhẹ
            Box(
                modifier = Modifier
                    .height(36.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFFEFF6FF),
                                Color(0xFFFAF5FF),
                                Color(0xFFFDF2F8)
                            )
                        )
                    )
                    .border(1.dp, Color(0xFFE9D5FF), RoundedCornerShape(18.dp))
                    .clickable(onClick = onAiChatClick)
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "✦",
                        fontFamily = EduMindFontFamily,
                        fontSize = 12.sp,
                        color = Primary600
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Hỏi AI",
                        fontFamily = EduMindFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Primary700
                    )
                }
            }

            // 3. Chuông thông báo nhỏ màu tím với chấm đỏ tròn chuẩn trên vai phải chuông
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onNotificationClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.size(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = "Thông báo",
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer(alpha = 0.99f)
                            .drawWithCache {
                                onDrawWithContent {
                                    drawContent()
                                    drawRect(NebulaBrush, blendMode = BlendMode.SrcIn)
                                }
                            }
                    )

                    if (unreadCount > 0) {
                        val displayCount = if (unreadCount > 99) "99+" else unreadCount.toString()
                        val badgeSize = if (displayCount.length > 2) 18.dp else 16.dp
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 3.dp, y = (-2).dp)
                                .size(badgeSize)
                                .background(Error600, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = displayCount,
                                style = TextStyle(
                                    fontFamily = EduMindFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = if (displayCount.length > 2) 7.5.sp else 10.sp,
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                    lineHeight = if (displayCount.length > 2) 7.5.sp else 10.sp,
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
    }
}
