package com.edumind.app.ui.exercise

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edumind.app.data.model.response.ExerciseEntry
import com.edumind.app.ui.theme.EduMindFontFamily
import com.edumind.app.ui.theme.Neutral100
import com.edumind.app.ui.theme.Neutral200
import com.edumind.app.ui.theme.Neutral400
import com.edumind.app.ui.theme.Neutral500
import com.edumind.app.ui.theme.Neutral700
import com.edumind.app.ui.theme.Neutral950
import com.edumind.app.ui.theme.Primary300
import com.edumind.app.ui.theme.Primary50
import com.edumind.app.ui.theme.Primary600
import com.edumind.app.ui.theme.Primary700

@Composable
fun ExerciseEntryCard(
    entry: ExerciseEntry?,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    isLocked: Boolean = false
) {
    if (entry == null) return

    val displayTitle = remember(entry.title) {
        val trimmed = entry.title.trimStart()
        val detail = if (trimmed.startsWith("Bài tập cuối bài", ignoreCase = true)) {
            trimmed.drop("Bài tập cuối bài".length)
                .trimStart()
                .removePrefix(":")
                .trim()
        } else {
            entry.title.trim()
        }
        detail.ifEmpty { entry.title }
    }

    val headerColor = if (isLocked) Neutral400 else Primary700
    val cardBorderColor = if (isLocked) Neutral200 else Primary300
    val iconContainerColor = if (isLocked) Neutral100 else Primary50
    val iconTint = if (isLocked) Neutral400 else Primary700
    val titleColor = if (isLocked) Neutral700 else Neutral950

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Assignment,
                    contentDescription = null,
                    tint = headerColor,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = "BÀI TẬP SAU BÀI HỌC",
                    fontFamily = EduMindFontFamily,
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = headerColor
                )
            }
            if (entry.questionCount > 0) {
                Text(
                    text = "${entry.questionCount} câu",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = headerColor
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .border(1.dp, cardBorderColor, RoundedCornerShape(16.dp))
                .clickable(
                    enabled = !isLocked,
                    role = Role.Button,
                    onClick = { onClick(entry.exerciseId) }
                )
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconContainerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Quiz,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = displayTitle,
                    fontFamily = EduMindFontFamily,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = titleColor
                )
                Text(
                    text = if (isLocked) "Mở sau khi xem hết bài giảng" else "Đã mở khóa",
                    fontFamily = EduMindFontFamily,
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    fontWeight = if (isLocked) FontWeight.Normal else FontWeight.Medium,
                    color = if (isLocked) Neutral500 else Primary600
                )
            }
            Row(
                modifier = Modifier.padding(end = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (!isLocked) {
                    Text(
                        text = "Làm bài",
                        fontFamily = EduMindFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary700
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = if (isLocked) Neutral400 else Primary700,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ExerciseEntryCardLockedPreview() {
    Box(modifier = Modifier.padding(16.dp)) {
        ExerciseEntryCard(
            entry = ExerciseEntry(
                exerciseId = "exercise-mock-01",
                title = "Bài tập cuối bài: Biến và kiểu dữ liệu",
                questionCount = 5
            ),
            isLocked = true,
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExerciseEntryCardUnlockedPreview() {
    Box(modifier = Modifier.padding(16.dp)) {
        ExerciseEntryCard(
            entry = ExerciseEntry(
                exerciseId = "exercise-mock-01",
                title = "Bài tập cuối bài: Biến và kiểu dữ liệu",
                questionCount = 5
            ),
            isLocked = false,
            onClick = {}
        )
    }
}
