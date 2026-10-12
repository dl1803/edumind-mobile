package com.edumind.app.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edumind.app.ui.theme.EduMindFontFamily
import com.edumind.app.ui.theme.Error600
import com.edumind.app.ui.theme.NebulaBrush
import com.edumind.app.ui.theme.Neutral500
import com.edumind.app.ui.theme.Neutral700
import com.edumind.app.ui.theme.Neutral900
import com.edumind.app.ui.theme.Primary700
import com.edumind.app.ui.theme.Success600

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
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar với viền Nebula + Chấm Online xanh
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
            // Chấm xanh online
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .align(Alignment.BottomEnd)
                    .background(Success600, CircleShape)
                    .border(2.dp, Color.White, CircleShape)
            )
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

        // Chuông thông báo với Badge đếm
        IconButton(
            onClick = onNotificationClick,
            modifier = Modifier.size(40.dp)
        ) {
            BadgedBox(
                badge = {
                    if (unreadCount > 0) {
                        val displayCount = if (unreadCount > 99) "99+" else unreadCount.toString()
                        Box(
                            modifier = Modifier
                                .background(Error600, CircleShape)
                                .border(1.5.dp, Color.White, CircleShape)
                                .padding(horizontal = 4.dp, vertical = 1.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = displayCount,
                                fontFamily = EduMindFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Thông báo",
                    tint = Neutral700,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
