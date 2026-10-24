package com.edumind.app.ui.progress.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        GoalOption(45, "🎯", "45 phút / ngày", "Cân bằng hoàn hảo giữa lý thuyết và thực hành", badge = "Khuyến nghị"),
        GoalOption(60, "🚀", "60 phút / ngày", "Tăng tốc nhanh chóng cho người học tập trung"),
        GoalOption(90, "🔥", "90 phút / ngày", "Chinh phục khóa học cấp tốc trong thời gian ngắn")
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
                .padding(horizontal = 24.dp, vertical = 6.dp)
        ) {
            // Header with title and close X button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Thiết lập mục tiêu ngày",
                        fontFamily = EduMindFontFamily,
                        fontSize = 16.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Neutral900
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Tùy chỉnh thời lượng học tập phù hợp lịch trình",
                        fontFamily = EduMindFontFamily,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = Neutral500
                    )
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF5F5F5))
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Đóng",
                        tint = Neutral500,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Options List
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                options.forEach { option ->
                    val isSelected = selectedGoal == option.minutes
                    val borderColor = if (isSelected) Color(0xFF8B5CF6) else Color(0xFFE5E5E5)
                    val containerBg = if (isSelected) Color(0xFFF5F3FF) else Color.White

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(if (isSelected) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(16.dp))
                            .background(containerBg)
                            .clickable { selectedGoal = option.minutes }
                            .padding(horizontal = 14.dp, vertical = 14.dp),
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
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) Color(0xFF8B5CF6) else Color(0xFFF5F3FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = option.emoji,
                                    fontSize = 16.sp
                                )
                            }

                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = option.title,
                                        fontFamily = EduMindFontFamily,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Primary950 else Neutral900
                                    )
                                    if (option.badge != null) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFFDDD6FE))
                                                .padding(horizontal = 6.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = option.badge,
                                                fontFamily = EduMindFontFamily,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF5B21B6)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = option.description,
                                    fontFamily = EduMindFontFamily,
                                    fontSize = 10.sp,
                                    color = if (isSelected) Primary700 else Neutral500
                                )
                            }
                        }

                        // Right Indicator: Checked circle with white tick OR unselected gray ring
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF8B5CF6)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = "Đã chọn",
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, Color(0xFFD4D4D4), CircleShape)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons: Hủy bỏ & Lưu mục tiêu mới
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 28.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hủy bỏ button (màu nền xám nổi bật rõ nét, viền nhẹ định hình khối)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE2E8F0))
                        .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Hủy bỏ",
                        fontFamily = EduMindFontFamily,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                }

                // Lưu mục tiêu mới button (NebulaBrush gradient background, white text + check icon)
                Box(
                    modifier = Modifier
                        .weight(1.8f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(NebulaBrush)
                        .clickable { onConfirm(selectedGoal) },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Lưu mục tiêu mới",
                            fontFamily = EduMindFontFamily,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

private data class GoalOption(
    val minutes: Int,
    val emoji: String,
    val title: String,
    val description: String,
    val badge: String? = null
)
