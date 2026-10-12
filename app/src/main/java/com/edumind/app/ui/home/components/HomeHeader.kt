package com.edumind.app.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
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
import com.edumind.app.ui.theme.Neutral500
import com.edumind.app.ui.theme.Neutral900
import com.edumind.app.ui.theme.Primary700

@Composable
fun HomeHeader(
    userName: String,
    streakDays: Int,
    unreadCount: Int,
    onAvatarClick: () -> Unit,
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar với viền Nebula (Đã xóa chấm xanh online)
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .clickable(onClick = onAvatarClick)
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

        Spacer(modifier = Modifier.width(12.dp))

        // Thông tin người dùng & Streak
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Xin chào,",
                    fontFamily = EduMindFontFamily,
                    fontSize = 12.sp,
                    color = Neutral500
                )
                Spacer(modifier = Modifier.width(6.dp))
                // Badge Streak
                Box(
                    modifier = Modifier
                        .background(Color(0xFFFEF3C7), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(12.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "🔥 $streakDays ngày",
                        fontFamily = EduMindFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = Color(0xFFB45309)
                    )
                }
            }
            Text(
                text = userName,
                fontFamily = EduMindFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Neutral900
            )
        }

        // Icon Chuông: Icon chuông tô màu Gradient Nebula + Badge đỏ tròn chuẩn trên vai phải chuông
        Box(
            modifier = Modifier
                .size(40.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onNotificationClick
                ),
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
