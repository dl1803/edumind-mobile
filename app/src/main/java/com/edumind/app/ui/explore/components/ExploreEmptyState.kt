package com.edumind.app.ui.explore.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edumind.app.ui.theme.EduMindFontFamily
import com.edumind.app.ui.theme.Neutral400
import com.edumind.app.ui.theme.Neutral500
import com.edumind.app.ui.theme.Neutral900
import com.edumind.app.ui.theme.Primary400
import com.edumind.app.ui.theme.Primary600
import com.edumind.app.ui.theme.Primary700

@Composable
fun ExploreEmptyState(
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(180.dp, 160.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(136.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFFEFF6FF),
                                Color(0xFFF5F3FF),
                                Color(0xFFFDF2F8)
                            )
                        )
                    )
                    .border(7.dp, Color(0x99E9D5FF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.size(72.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = Primary400,
                        modifier = Modifier.size(56.dp)
                    )

                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.BottomEnd)
                            .offset(x = (-4).dp, y = (-4).dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF5F5F5))
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = Neutral400,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-12).dp, y = 14.dp)
                    .rotate(12f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFFE0E7FF),
                                Color(0xFFC7D2FE)
                            )
                        )
                    )
                    .padding(horizontal = 7.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = Color(0xFF4F46E5),
                    modifier = Modifier.size(14.dp)
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = 12.dp, y = (-12).dp)
                    .rotate(-12f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFCE7F3))
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.QuestionMark,
                    contentDescription = null,
                    tint = Color(0xFFEC4899),
                    modifier = Modifier.size(13.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Không tìm thấy khóa học nào",
            fontFamily = EduMindFontFamily,
            fontSize = 16.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Neutral900,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Không có khóa học phù hợp với bộ lọc hiện tại. Thử thay đổi bộ lọc hoặc danh mục khác.",
            fontFamily = EduMindFontFamily,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = Neutral500,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onClearFilters,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            border = BorderStroke(1.5.dp, Primary600),
            modifier = Modifier.height(40.dp)
        ) {
            Text(
                text = "Xóa bộ lọc",
                fontFamily = EduMindFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Primary700
            )
        }
    }
}
