package com.edumind.app.ui.exercise

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.edumind.app.ui.common.EduMindButton
import com.edumind.app.ui.common.ErrorView
import com.edumind.app.ui.theme.EduMindFontFamily
import com.edumind.app.ui.theme.Error100
import com.edumind.app.ui.theme.Error200
import com.edumind.app.ui.theme.Error600
import com.edumind.app.ui.theme.Error700
import com.edumind.app.ui.theme.NebulaBrush
import com.edumind.app.ui.theme.Neutral100
import com.edumind.app.ui.theme.Neutral200
import com.edumind.app.ui.theme.Neutral400
import com.edumind.app.ui.theme.Neutral50
import com.edumind.app.ui.theme.Neutral500
import com.edumind.app.ui.theme.Neutral600
import com.edumind.app.ui.theme.Neutral700
import com.edumind.app.ui.theme.Neutral800
import com.edumind.app.ui.theme.Neutral950
import com.edumind.app.ui.theme.Primary700
import com.edumind.app.ui.theme.Success100
import com.edumind.app.ui.theme.Success600
import com.edumind.app.ui.theme.Success700
import com.edumind.app.ui.theme.Warning100
import com.edumind.app.ui.theme.Warning600
import com.edumind.app.ui.theme.Warning700
import kotlin.math.roundToInt

@Composable
fun ExerciseResultScreen(
    onBackClick: () -> Unit,
    onRetryClick: (String) -> Unit,
    viewModel: ExerciseResultViewModel = hiltViewModel()
) {
    ExerciseResultScreenContent(
        state = viewModel.uiState,
        onBackClick = onBackClick,
        onRetryClick = onRetryClick
    )
}

@Composable
fun ExerciseResultScreenContent(
    state: ExerciseResultUiState,
    onBackClick: () -> Unit,
    onRetryClick: (String) -> Unit,
    initialShowResultDetail: Boolean = false
) {
    var hasNavigated by remember { mutableStateOf(false) }
    var showResultDetail by rememberSaveable { mutableStateOf(initialShowResultDetail) }

    val safeBackClick: () -> Unit = {
        if (!hasNavigated) {
            hasNavigated = true
            onBackClick()
        }
    }
    val safeRetryClick: (String) -> Unit = { exerciseId ->
        if (!hasNavigated) {
            hasNavigated = true
            onRetryClick(exerciseId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Neutral50)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = safeBackClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Đóng",
                    tint = Neutral700,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        when (state) {
            ExerciseResultUiState.Missing -> ErrorView(
                message = "Không tìm thấy kết quả bài làm.",
                title = "Không thể hiển thị kết quả",
                onClose = safeBackClick,
                modifier = Modifier.weight(1f)
            )

            is ExerciseResultUiState.Content -> if (!showResultDetail) {
                ExerciseSubmissionSuccessContent(
                    content = state.content,
                    onViewResult = { showResultDetail = true },
                    modifier = Modifier.weight(1f)
                )
            } else {
                ExerciseResultBody(
                    content = state.content,
                    onBackClick = safeBackClick,
                    onRetryClick = safeRetryClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ExerciseSubmissionSuccessContent(
    content: ExerciseResultContent,
    onViewResult: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPending = content.outcome == ExerciseOutcome.PENDING
    val badgeShape = RoundedCornerShape(24.dp)
    val cardShape = RoundedCornerShape(16.dp)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(badgeShape)
                .background(Success100.copy(alpha = 0.5f))
                .border(2.dp, Success100, badgeShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = Success600,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Nộp bài thành công!",
            fontFamily = EduMindFontFamily,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = Neutral950,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Bài làm của bạn đã được ghi nhận an toàn trên hệ thống.",
            fontFamily = EduMindFontFamily,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = Neutral500,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(cardShape)
                .background(Color.White)
                .border(1.dp, Neutral200, cardShape)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SubmissionDetailRow(
                label = "Bài tập:",
                value = content.exerciseTitle,
                valueColor = Neutral950
            )
            HorizontalDivider(color = Neutral100)
            SubmissionDetailRow(
                label = "Số câu trắc nghiệm:",
                value = "${content.mcqAnsweredCount} / ${content.mcqTotalCount} câu",
                valueColor = Primary700
            )
            HorizontalDivider(color = Neutral100)
            SubmissionDetailRow(
                label = "Số câu tự luận:",
                value = "${content.essayAnsweredCount} / ${content.essayTotalCount} câu",
                valueColor = Primary700
            )
            HorizontalDivider(color = Neutral100)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Trạng thái:",
                    fontFamily = EduMindFontFamily,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = Neutral500
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (isPending) Icons.Filled.Schedule else Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = if (isPending) Warning700 else Success600,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (isPending) "Chờ chấm tự luận" else "Đã chấm tự động",
                        fontFamily = EduMindFontFamily,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isPending) Warning700 else Success600,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
            HorizontalDivider(color = Neutral100)
            SubmissionDetailRow(
                label = "Thời gian làm bài:",
                value = formatDurationText(content.timeSpentSeconds),
                valueColor = Neutral800
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        EduMindButton(
            text = "Xem kết quả bài tập",
            onClick = onViewResult,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SubmissionDetailRow(
    label: String,
    value: String,
    valueColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontFamily = EduMindFontFamily,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            color = Neutral500,
            maxLines = 1,
            softWrap = false
        )
        Text(
            text = value,
            fontFamily = EduMindFontFamily,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Medium,
            color = valueColor,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ExerciseResultBody(
    content: ExerciseResultContent,
    onBackClick: () -> Unit,
    onRetryClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "hero") { ExerciseResultHero(content = content) }
            if (content.reviewItems.isNotEmpty()) {
                item(key = "review-title") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Chi tiết bài làm (${content.reviewItems.size} câu)",
                            fontFamily = EduMindFontFamily,
                            fontSize = 16.sp,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Neutral950
                        )
                        Text(
                            text = "Tất cả",
                            fontFamily = EduMindFontFamily,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Primary700
                        )
                    }
                }
                items(content.reviewItems, key = { it.id }) { item ->
                    ExerciseReviewCard(item = item)
                }
            }
        }
        HorizontalDivider(color = Neutral200)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (content.canRetry) {
                SecondaryActionButton(
                    text = "Về bài học",
                    onClick = onBackClick,
                    modifier = Modifier.weight(1f)
                )
                EduMindButton(
                    text = "Làm lại bài tập",
                    onClick = { onRetryClick(content.exerciseId) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    modifier = Modifier.weight(1.3f)
                )
            } else {
                EduMindButton(
                    text = "Về bài học",
                    onClick = onBackClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun SecondaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Neutral100)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontFamily = EduMindFontFamily,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Medium,
            color = Neutral700,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun ExerciseResultHero(content: ExerciseResultContent) {
    val shape = RoundedCornerShape(24.dp)
    val borderColor = when (content.outcome) {
        ExerciseOutcome.PASSED -> Neutral200
        ExerciseOutcome.FAILED -> Error200
        ExerciseOutcome.PENDING -> Warning600.copy(alpha = 0.35f)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color.White)
            .border(1.dp, borderColor, shape),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (content.outcome) {
            ExerciseOutcome.PASSED -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(NebulaBrush)
            )
            ExerciseOutcome.FAILED -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(Error600)
            )
            ExerciseOutcome.PENDING -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(Warning600)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (content.outcome == ExerciseOutcome.PENDING) {
                ExercisePendingHeader()
            } else {
                ExerciseScoredHeader(content = content)
            }
            HorizontalDivider(color = Neutral100)
            ExerciseResultStats(content = content)
        }
    }
}

@Composable
private fun ExerciseScoredHeader(content: ExerciseResultContent) {
    val isPassed = content.outcome == ExerciseOutcome.PASSED
    val ringColor = if (isPassed) Success600 else Error600
    val trackColor = if (isPassed) Neutral200 else Error100
    val scoreColor = if (isPassed) Neutral950 else Error600
    val badgeBg = if (isPassed) Success100 else Error100
    val badgeTextColor = if (isPassed) Success700 else Error700

    ExerciseScoreRing(
        scorePercent = content.scorePercent ?: 0,
        color = ringColor,
        trackColor = trackColor,
        scoreColor = scoreColor
    )

    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(badgeBg)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = if (isPassed) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
            contentDescription = null,
            tint = ringColor,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = if (isPassed) "ĐẠT YÊU CẦU BÀI HỌC" else "CHƯA ĐẠT YÊU CẦU",
            fontFamily = EduMindFontFamily,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Medium,
            color = badgeTextColor
        )
    }

    Text(
        text = "Mức đạt chuẩn tối thiểu: ${content.passingScore}% / 100%",
        fontFamily = EduMindFontFamily,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = Neutral500,
        textAlign = TextAlign.Center
    )

    if (!isPassed && !content.canRetry) {
        Text(
            text = "Đã hết số lần làm bài.",
            fontFamily = EduMindFontFamily,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Error600,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ExercisePendingHeader() {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(Warning100),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.HourglassBottom,
            contentDescription = null,
            tint = Warning600,
            modifier = Modifier.size(48.dp)
        )
    }
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(Warning100)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Schedule,
            contentDescription = null,
            tint = Warning600,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = "CHỜ CHẤM TỰ LUẬN",
            fontFamily = EduMindFontFamily,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Warning700
        )
    }
    Text(
        text = "Bài đã được gửi. Kết quả sẽ được cập nhật sau.",
        fontFamily = EduMindFontFamily,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = Neutral600,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun ExerciseScoreRing(
    scorePercent: Int,
    color: Color,
    trackColor: Color,
    scoreColor: Color,
    modifier: Modifier = Modifier
) {
    val target = scorePercent.coerceIn(0, 100) / 100f
    val isInspection = LocalInspectionMode.current
    val progress = remember(target, isInspection) {
        Animatable(if (isInspection) target else 0f)
    }

    LaunchedEffect(target, isInspection) {
        if (!isInspection) {
            progress.animateTo(
                targetValue = target,
                animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
            )
        }
    }

    Box(
        modifier = modifier.size(144.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(144.dp)) {
            val strokePx = 12.dp.toPx()
            val inset = strokePx / 2f
            val arcSize = Size(size.width - strokePx, size.height - strokePx)
            val topLeft = Offset(inset, inset)
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
            if (progress.value > 0f) {
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = 360f * progress.value,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${(progress.value * 100f).roundToInt()}%",
                fontFamily = EduMindFontFamily,
                fontSize = 28.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.Bold,
                color = scoreColor
            )
            Text(
                text = "/ 100%",
                fontFamily = EduMindFontFamily,
                fontSize = 10.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Neutral400
            )
        }
    }
}

@Composable
private fun ExerciseResultStats(content: ExerciseResultContent) {
    val time = formatTimer(content.timeSpentSeconds)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (content.outcome == ExerciseOutcome.PENDING) {
            ExerciseStatCell(
                label = "Đã trả lời",
                value = "${content.answeredCount} câu",
                valueColor = Neutral950,
                modifier = Modifier.weight(1f)
            )
            ExerciseStatCell(
                label = "Bỏ qua",
                value = "${content.skippedCount} câu",
                valueColor = Neutral600,
                modifier = Modifier.weight(1f)
            )
            ExerciseStatCell(
                label = "Thời gian",
                value = time,
                valueColor = Primary700,
                isMonospace = true,
                modifier = Modifier.weight(1f)
            )
        } else {
            ExerciseStatCell(
                label = "Đúng",
                value = "${content.correctCount} câu",
                valueColor = Success700,
                modifier = Modifier.weight(1f)
            )
            ExerciseStatCell(
                label = "Sai",
                value = "${content.wrongCount} câu",
                valueColor = Error600,
                modifier = Modifier.weight(1f)
            )
            if (content.skippedCount > 0) {
                ExerciseStatCell(
                    label = "Bỏ qua",
                    value = "${content.skippedCount} câu",
                    valueColor = Neutral600,
                    modifier = Modifier.weight(1f)
                )
            }
            ExerciseStatCell(
                label = "Thời gian",
                value = time,
                valueColor = Primary700,
                isMonospace = true,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ExerciseStatCell(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
    isMonospace: Boolean = false
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Neutral50)
            .padding(horizontal = 4.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            fontFamily = if (isMonospace) FontFamily.Monospace else EduMindFontFamily,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            maxLines = 1,
            softWrap = false
        )
        Text(
            text = label,
            fontFamily = EduMindFontFamily,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            color = Neutral500,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun ExerciseReviewCard(item: ExerciseReviewItem) {
    var expanded by rememberSaveable {
        mutableStateOf(item.verdict == ExerciseVerdict.WRONG || item.verdict == ExerciseVerdict.SKIPPED)
    }
    val verdictColor = item.verdict.color()
    val isWrong = item.verdict == ExerciseVerdict.WRONG
    val shape = RoundedCornerShape(16.dp)
    val borderColor = if (isWrong) Error200 else Neutral200
    val borderWidth = if (isWrong) 2.dp else 1.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color.White)
            .border(borderWidth, borderColor, shape)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    onClickLabel = if (expanded) "Thu gọn" else "Mở rộng",
                    role = Role.Button
                ) { expanded = !expanded }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(item.verdict.badgeBackground()),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.verdict.icon(),
                    contentDescription = null,
                    tint = verdictColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = "Câu ${item.number.toString().padStart(2, '0')}: ${item.content}",
                fontFamily = EduMindFontFamily,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Neutral950,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = item.verdict.label(),
                fontFamily = EduMindFontFamily,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Medium,
                color = verdictColor
            )
            Icon(
                imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = null,
                tint = Neutral500,
                modifier = Modifier.size(20.dp)
            )
        }

        if (expanded) {
            HorizontalDivider(color = Neutral100)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = item.content,
                    fontFamily = EduMindFontFamily,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = Neutral800
                )
                if (item.verdict == ExerciseVerdict.SKIPPED) {
                    ExerciseAnswerHighlightBox(
                        label = "Đáp án bạn chọn:",
                        lines = listOf("Bạn chưa trả lời câu này."),
                        containerColor = Neutral100,
                        textColor = Neutral600,
                        icon = Icons.Filled.Remove,
                        iconTint = Neutral500
                    )
                } else {
                    val isCorrect = item.verdict == ExerciseVerdict.CORRECT
                    ExerciseAnswerHighlightBox(
                        label = "Đáp án bạn chọn:",
                        lines = item.yourAnswerLines,
                        containerColor = when {
                            isCorrect -> Success100.copy(alpha = 0.55f)
                            isWrong -> Error100.copy(alpha = 0.55f)
                            else -> Neutral100
                        },
                        textColor = when {
                            isCorrect -> Success700
                            isWrong -> Error700
                            else -> Neutral800
                        },
                        icon = when {
                            isCorrect -> Icons.Filled.CheckCircle
                            isWrong -> Icons.Filled.Cancel
                            else -> Icons.Filled.HourglassBottom
                        },
                        iconTint = when {
                            isCorrect -> Success600
                            isWrong -> Error600
                            else -> Warning600
                        }
                    )
                }
                if (item.correctAnswerLines.isNotEmpty()) {
                    ExerciseAnswerHighlightBox(
                        label = "Đáp án đúng:",
                        lines = item.correctAnswerLines,
                        containerColor = Success100.copy(alpha = 0.55f),
                        textColor = Success700,
                        icon = Icons.Filled.CheckCircle,
                        iconTint = Success600
                    )
                }
                if (item.explanation != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Neutral50)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(color = Primary700, fontWeight = FontWeight.SemiBold)) {
                                    append("Giải thích: ")
                                }
                                withStyle(SpanStyle(color = Neutral600)) {
                                    append(item.explanation)
                                }
                            },
                            fontFamily = EduMindFontFamily,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseAnswerHighlightBox(
    label: String,
    lines: List<String>,
    containerColor: Color,
    textColor: Color,
    icon: ImageVector,
    iconTint: Color
) {
    if (lines.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            fontFamily = EduMindFontFamily,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            color = Neutral600
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(containerColor)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            lines.forEach { line ->
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(16.dp)
                    )
                    Text(
                        text = line,
                        fontFamily = EduMindFontFamily,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

private fun formatDurationText(totalSeconds: Int): String {
    val safe = totalSeconds.coerceAtLeast(0)
    val minutes = safe / 60
    val seconds = safe % 60
    return if (minutes > 0) "$minutes phút $seconds giây" else "$seconds giây"
}

private fun ExerciseVerdict.label(): String = when (this) {
    ExerciseVerdict.CORRECT -> "Đúng"
    ExerciseVerdict.WRONG -> "Sai"
    ExerciseVerdict.SKIPPED -> "Bỏ qua"
    ExerciseVerdict.UNGRADED -> "Chờ chấm"
}

private fun ExerciseVerdict.color(): Color = when (this) {
    ExerciseVerdict.CORRECT -> Success600
    ExerciseVerdict.WRONG -> Error600
    ExerciseVerdict.SKIPPED -> Neutral600
    ExerciseVerdict.UNGRADED -> Warning700
}

private fun ExerciseVerdict.badgeBackground(): Color = when (this) {
    ExerciseVerdict.CORRECT -> Success100
    ExerciseVerdict.WRONG -> Error100
    ExerciseVerdict.SKIPPED -> Neutral100
    ExerciseVerdict.UNGRADED -> Warning100
}

private fun ExerciseVerdict.icon(): ImageVector = when (this) {
    ExerciseVerdict.CORRECT -> Icons.Filled.Check
    ExerciseVerdict.WRONG -> Icons.Filled.Close
    ExerciseVerdict.SKIPPED -> Icons.Filled.Remove
    ExerciseVerdict.UNGRADED -> Icons.Filled.HourglassBottom
}

private fun previewContent(outcome: ExerciseOutcome, canRetry: Boolean) = ExerciseResultContent(
    exerciseId = "exercise-preview",
    exerciseTitle = "Bài 03: Biến & Kiểu dữ liệu",
    mcqAnsweredCount = 9,
    mcqTotalCount = 9,
    essayAnsweredCount = 1,
    essayTotalCount = 1,
    outcome = outcome,
    scorePercent = if (outcome == ExerciseOutcome.PASSED) 85 else 40,
    passingScore = 70,
    correctCount = 8,
    wrongCount = 2,
    answeredCount = 10,
    skippedCount = 0,
    timeSpentSeconds = 680,
    canRetry = canRetry,
    reviewItems = if (outcome == ExerciseOutcome.PENDING) {
        emptyList()
    } else {
        listOf(
            ExerciseReviewItem(
                id = "q1",
                number = 1,
                content = "Kiểu dữ liệu nào dưới đây là bất biến trong Python?",
                verdict = ExerciseVerdict.CORRECT,
                yourAnswerLines = listOf("B. Tuple"),
                correctAnswerLines = emptyList(),
                explanation = "Tuple là kiểu dữ liệu bất biến."
            ),
            ExerciseReviewItem(
                id = "q2",
                number = 2,
                content = "Đặc điểm kiểu dữ liệu Set",
                verdict = ExerciseVerdict.WRONG,
                yourAnswerLines = listOf("D. Cho phép truy xuất phần tử qua index"),
                correctAnswerLines = listOf("B. Set không có thứ tự và không hỗ trợ đánh chỉ mục"),
                explanation = "Trong Python, Set là tập hợp các phần tử duy nhất không có thứ tự (unordered), nên không thể truy xuất bằng chỉ mục (index)."
            )
        )
    }
)

@Preview(showBackground = true, widthDp = 412, heightDp = 890)
@Composable
private fun ExerciseSubmissionSuccessPreview() {
    ExerciseResultScreenContent(
        state = ExerciseResultUiState.Content(previewContent(ExerciseOutcome.PASSED, canRetry = false)),
        onBackClick = {},
        onRetryClick = {},
        initialShowResultDetail = false
    )
}

@Preview(showBackground = true, widthDp = 412, heightDp = 890)
@Composable
private fun ExerciseResultPassedPreview() {
    ExerciseResultScreenContent(
        state = ExerciseResultUiState.Content(previewContent(ExerciseOutcome.PASSED, canRetry = false)),
        onBackClick = {},
        onRetryClick = {},
        initialShowResultDetail = true
    )
}

@Preview(showBackground = true, widthDp = 412, heightDp = 890)
@Composable
private fun ExerciseResultFailedCanRetryPreview() {
    ExerciseResultScreenContent(
        state = ExerciseResultUiState.Content(previewContent(ExerciseOutcome.FAILED, canRetry = true)),
        onBackClick = {},
        onRetryClick = {},
        initialShowResultDetail = true
    )
}

@Preview(showBackground = true, widthDp = 412, heightDp = 890)
@Composable
private fun ExerciseResultPendingPreview() {
    ExerciseResultScreenContent(
        state = ExerciseResultUiState.Content(previewContent(ExerciseOutcome.PENDING, canRetry = false)),
        onBackClick = {},
        onRetryClick = {},
        initialShowResultDetail = true
    )
}
