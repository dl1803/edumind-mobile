package com.edumind.app.ui.progress.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edumind.app.ui.common.EduMindButton
import com.edumind.app.ui.common.EduMindButtonStyle
import com.edumind.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalAdjustmentBottomSheet(
    currentGoal: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var selectedGoal by remember { mutableIntStateOf(currentGoal) }

    val options = listOf(
        GoalOption(30, "☕", "30 phút / ngày", "Khởi động nhẹ nhàng, dễ duy trì thói quen"),
        GoalOption(45, "🎯", "45 phút / ngày", "Cân bằng lý tưởng (Khuyên dùng)"),
        GoalOption(60, "🚀", "60 phút / ngày", "Tăng tốc tập trung cho người học chủ động"),
        GoalOption(90, "🔥", "90 phút / ngày", "Chinh phục bứt phá trong thời gian ngắn")
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Thiết lập mục tiêu ngày",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = Neutral950
            )
            Text(
                text = "Tùy chỉnh thời lượng học tập phù hợp lịch trình của bạn",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Normal
                ),
                color = Neutral500,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEach { option ->
                    val isSelected = selectedGoal == option.minutes
                    val borderColor = if (isSelected) Primary600 else Neutral200
                    val containerBg = if (isSelected) Primary50 else Color.White

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(14.dp))
                            .background(containerBg)
                            .clickable { selectedGoal = option.minutes }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) Primary600 else Primary50),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = option.emoji, fontSize = 16.sp)
                            }

                            Column {
                                Text(
                                    text = option.title,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                                    ),
                                    color = if (isSelected) Primary950 else Neutral950
                                )
                                Text(
                                    text = option.description,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = if (isSelected) Primary700 else Neutral500
                                )
                            }
                        }

                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedGoal = option.minutes },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = Primary600,
                                unselectedColor = Neutral400
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                EduMindButton(
                    text = "Hủy bỏ",
                    onClick = onDismiss,
                    style = EduMindButtonStyle.Outlined,
                    modifier = Modifier.weight(1f)
                )
                EduMindButton(
                    text = "Lưu mục tiêu",
                    onClick = { onConfirm(selectedGoal) },
                    style = EduMindButtonStyle.Primary,
                    modifier = Modifier.weight(1.5f)
                )
            }
        }
    }
}

private data class GoalOption(
    val minutes: Int,
    val emoji: String,
    val title: String,
    val description: String
)
