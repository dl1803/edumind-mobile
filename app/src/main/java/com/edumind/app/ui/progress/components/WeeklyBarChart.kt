package com.edumind.app.ui.progress.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edumind.app.data.model.response.DailyStudyTime
import com.edumind.app.ui.theme.*

@Composable
fun WeeklyBarChart(
    weeklyData: List<DailyStudyTime>,
    dailyGoalMinutes: Int,
    onEditGoalClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Thời lượng học trong tuần",
                        fontFamily = EduMindFontFamily,
                        fontSize = 12.5.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Neutral900
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                SpanStyle(
                                    fontFamily = EduMindFontFamily,
                                    color = Neutral500,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            ) {
                                append("Mục tiêu hiện tại: ")
                            }
                            withStyle(
                                SpanStyle(
                                    fontFamily = EduMindFontFamily,
                                    color = Primary700,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append("$dailyGoalMinutes phút / ngày")
                            }
                        }
                    )
                }

                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onEditGoalClick() },
                    color = Primary50,
                    shape = RoundedCornerShape(8.dp),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Đổi mục tiêu",
                            tint = Primary700,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Đổi mục tiêu",
                            fontFamily = EduMindFontFamily,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary700
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 7 Days Chart Bar Area
            val maxMinutes = 90f
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(116.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                weeklyData.forEach { item ->
                    val barHeight = ((item.minutes / maxMinutes) * 74).coerceIn(6f, 74f).dp
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "${item.minutes}p",
                            fontFamily = EduMindFontFamily,
                            fontSize = 9.5.sp,
                            fontWeight = if (item.isToday) FontWeight.Bold else FontWeight.Normal,
                            color = if (item.isToday) Primary700 else if (item.minutes == 0) Neutral300 else Neutral400
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .width(16.dp)
                                .height(barHeight)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .then(
                                    if (item.isToday) Modifier.background(NebulaBrush)
                                    else if (item.minutes > 0) Modifier.background(Primary200)
                                    else Modifier.background(Neutral100)
                                )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (item.isToday) "${item.dayOfWeek} (19/09)" else item.dayOfWeek,
                            fontFamily = EduMindFontFamily,
                            fontSize = if (item.isToday) 9.5.sp else 10.sp,
                            fontWeight = if (item.isToday) FontWeight.Bold else FontWeight.Medium,
                            color = if (item.isToday) Primary700 else if (item.minutes == 0) Neutral400 else Neutral500,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Visible,
                            modifier = Modifier.wrapContentWidth(unbounded = true)
                        )
                    }
                }
            }
        }
    }
}
