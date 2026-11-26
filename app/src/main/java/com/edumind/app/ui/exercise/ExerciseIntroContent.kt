package com.edumind.app.ui.exercise

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edumind.app.data.model.response.ExerciseDto
import com.edumind.app.ui.common.EduMindButton
import com.edumind.app.ui.theme.EduMindFontFamily
import com.edumind.app.ui.theme.Error600
import com.edumind.app.ui.theme.Neutral100
import com.edumind.app.ui.theme.Neutral200
import com.edumind.app.ui.theme.Neutral50
import com.edumind.app.ui.theme.Neutral500
import com.edumind.app.ui.theme.Neutral700
import com.edumind.app.ui.theme.Neutral950
import com.edumind.app.ui.theme.Primary200
import com.edumind.app.ui.theme.Primary50
import com.edumind.app.ui.theme.Primary600
import com.edumind.app.ui.theme.Primary700
import com.edumind.app.ui.theme.Primary800

@Composable
internal fun ExerciseIntroContent(
    exercise: ExerciseDto,
    isOutOfAttempts: Boolean,
    onStart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Neutral50)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Primary50)
                    .border(2.dp, Primary200, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Assignment,
                    contentDescription = null,
                    tint = Primary700,
                    modifier = Modifier.size(56.dp)
                )
            }

            val displayTitle = remember(exercise.title) {
                val detail = when {
                    exercise.title.contains(":") -> exercise.title.substringAfter(":").trim()
                    exercise.title.startsWith("Bài tập cuối bài", ignoreCase = true) ->
                        exercise.title.removePrefix("Bài tập cuối bài").trim().removePrefix(":").trim()
                    else -> exercise.title.trim()
                }
                if (detail.isNotEmpty()) {
                    "Bài tập cuối bài :\n$detail"
                } else {
                    exercise.title
                }
            }

            Text(
                text = displayTitle,
                fontFamily = EduMindFontFamily,
                fontSize = 20.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Neutral950,
                textAlign = TextAlign.Center
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, Neutral200, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                IntroMetricRow(
                    icon = Icons.Filled.FormatListNumbered,
                    label = "Số câu hỏi",
                    value = "${exercise.questions.size} câu"
                )
                HorizontalDivider(color = Neutral100)
                IntroMetricRow(
                    icon = Icons.Filled.Timer,
                    label = "Thời gian làm bài",
                    value = formatTimeLimit(exercise.timeLimit)
                )
                if (exercise.maxAttempts > 0) {
                    HorizontalDivider(color = Neutral100)
                    IntroMetricRow(
                        icon = Icons.Filled.Replay,
                        label = "Số lần làm bài",
                        value = "${exercise.attemptsUsed}/${exercise.maxAttempts} lần"
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Primary50)
                    .border(1.dp, Primary200, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Lightbulb,
                    contentDescription = null,
                    tint = Primary600,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Bạn có thể tự do chuyển đổi giữa các câu hỏi và mở bảng danh sách câu hỏi bất kỳ lúc nào.",
                    fontFamily = EduMindFontFamily,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = Primary800
                )
            }

            if (isOutOfAttempts) {
                Text(
                    text = "Đã hết số lần làm bài quy định cho bài tập này.",
                    fontFamily = EduMindFontFamily,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Error600,
                    textAlign = TextAlign.Center
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            EduMindButton(
                text = "Bắt đầu",
                onClick = onStart,
                enabled = !isOutOfAttempts,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun IntroMetricRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Primary50),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Primary700,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = label,
                fontFamily = EduMindFontFamily,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = Neutral700
            )
        }
        Text(
            text = value,
            fontFamily = EduMindFontFamily,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Neutral950
        )
    }
}
