package com.edumind.app.ui.exercise

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edumind.app.ui.common.EduMindButton
import com.edumind.app.ui.theme.EduMindFontFamily
import com.edumind.app.ui.theme.Error100
import com.edumind.app.ui.theme.Error50
import com.edumind.app.ui.theme.Error600
import com.edumind.app.ui.theme.Error700
import com.edumind.app.ui.theme.Neutral100
import com.edumind.app.ui.theme.Neutral600
import com.edumind.app.ui.theme.Neutral700
import com.edumind.app.ui.theme.Neutral900
import com.edumind.app.ui.theme.Neutral950
import com.edumind.app.ui.theme.Warning100
import com.edumind.app.ui.theme.Warning600

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ExerciseSubmitConfirmDialog(
    unansweredCount: Int,
    unansweredListText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        ExerciseSubmitConfirmCard(
            unansweredCount = unansweredCount,
            unansweredListText = unansweredListText,
            onConfirm = onConfirm,
            onDismiss = onDismiss
        )
    }
}

@Composable
internal fun ExerciseSubmitConfirmCard(
    unansweredCount: Int,
    unansweredListText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Warning100),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = Warning600,
                modifier = Modifier.size(26.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Bạn có chắc muốn nộp bài?",
            fontFamily = EduMindFontFamily,
            fontSize = 17.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Neutral950,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (unansweredCount > 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Bạn vẫn còn ",
                    fontFamily = EduMindFontFamily,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = Neutral600
                )
                Text(
                    text = "$unansweredCount câu chưa trả lời",
                    fontFamily = EduMindFontFamily,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Error600
                )
            }
            if (unansweredListText.isNotEmpty()) {
                Text(
                    text = "($unansweredListText). Các câu chưa làm sẽ không được ghi nhận kết quả.",
                    fontFamily = EduMindFontFamily,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = Neutral600,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Text(
                text = "Bạn đã hoàn thành tất cả các câu hỏi. Nhấn xác nhận để nộp bài và xem kết quả.",
                fontFamily = EduMindFontFamily,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = Neutral600,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            EduMindButton(
                text = "Xác nhận nộp bài",
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth()
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Neutral100)
                    .clickable(role = androidx.compose.ui.semantics.Role.Button, onClick = onDismiss),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tiếp tục làm bài",
                    fontFamily = EduMindFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Neutral700
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ExerciseTimeOutDialog(
    onViewResult: () -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = {}
    ) {
        ExerciseTimeOutCard(
            onViewResult = onViewResult
        )
    }
}

@Composable
internal fun ExerciseTimeOutCard(
    onViewResult: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Error100),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.HourglassBottom,
                contentDescription = null,
                tint = Error600,
                modifier = Modifier.size(30.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Đã hết giờ làm bài",
            fontFamily = EduMindFontFamily,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Error700,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Đã hết thời gian, bài đã được nộp tự động.",
            fontFamily = EduMindFontFamily,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            color = Neutral600,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(22.dp))

        EduMindButton(
            text = "Xem kết quả nộp bài",
            onClick = onViewResult,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ExerciseLeaveConfirmDialog(
    onConfirmLeave: () -> Unit,
    onDismiss: () -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        ExerciseLeaveConfirmCard(
            onConfirmLeave = onConfirmLeave,
            onDismiss = onDismiss
        )
    }
}

@Composable
internal fun ExerciseLeaveConfirmCard(
    onConfirmLeave: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Error100),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = null,
                tint = Error600,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Bạn muốn thoát?",
            fontFamily = EduMindFontFamily,
            fontSize = 17.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Neutral950,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Bài làm hiện tại sẽ không được lưu nếu bạn rời khỏi lúc này.",
            fontFamily = EduMindFontFamily,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            color = Neutral600,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            EduMindButton(
                text = "Tiếp tục làm bài",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Error50)
                    .clickable(role = Role.Button, onClick = onConfirmLeave),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Thoát bài làm",
                    fontFamily = EduMindFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Error600
                )
            }
        }
    }
}
