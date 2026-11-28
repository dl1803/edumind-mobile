package com.edumind.app.ui.exercise

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edumind.app.data.model.response.ExerciseQuestionDto
import com.edumind.app.data.model.response.ExerciseQuestionType
import com.edumind.app.ui.common.EduMindButton
import com.edumind.app.ui.common.EduMindButtonStyle
import com.edumind.app.ui.quiz.QuizOptionItem
import com.edumind.app.ui.quiz.QuizOptionState
import com.edumind.app.ui.theme.EduMindFontFamily
import com.edumind.app.ui.theme.Error600
import com.edumind.app.ui.theme.Info100
import com.edumind.app.ui.theme.Info600
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
import com.edumind.app.ui.theme.Primary100
import com.edumind.app.ui.theme.Primary200
import com.edumind.app.ui.theme.Primary50
import com.edumind.app.ui.theme.Primary600
import com.edumind.app.ui.theme.Primary700
import com.edumind.app.ui.theme.Primary800
import com.edumind.app.ui.theme.Success600

@Composable
internal fun ExerciseQuestionContent(
    state: ExerciseUiState.InProgress,
    onSelectSingle: (String, String) -> Unit,
    onToggleMultiple: (String, String) -> Unit,
    onEssayChange: (String, String) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onGoTo: (Int) -> Unit,
    onSubmitClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isGridVisible by rememberSaveable { mutableStateOf(false) }
    val total = state.questions.size
    val index = state.currentIndex

    LaunchedEffect(state.isLocked) {
        if (state.isLocked) isGridVisible = false
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
    ) {
        ExerciseProgressHeader(
            currentNumber = index + 1,
            total = total,
            answeredCount = state.answeredCount,
            isEssay = state.currentQuestion.type == ExerciseQuestionType.ESSAY,
            onOpenGrid = { isGridVisible = true }
        )

        AnimatedContent(
            targetState = index,
            transitionSpec = {
                val direction = if (targetState > initialState) 1 else -1
                (slideInHorizontally(animationSpec = tween(300)) { fullWidth -> fullWidth * direction } +
                    fadeIn(animationSpec = tween(300))) togetherWith
                    (slideOutHorizontally(animationSpec = tween(300)) { fullWidth -> -fullWidth * direction } +
                        fadeOut(animationSpec = tween(300)))
            },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            label = "exerciseQuestionTransition"
        ) { questionIndex ->
            val question = state.questions[questionIndex]
            ExerciseQuestionBody(
                question = question,
                selectedKeys = state.answerOf(question).selectedKeys,
                essayText = state.answerOf(question).essayText,
                onSelectSingle = { key -> onSelectSingle(question.id, key) },
                onToggleMultiple = { key -> onToggleMultiple(question.id, key) },
                onEssayChange = { text -> onEssayChange(question.id, text) },
                enabled = !state.isLocked
            )
        }

        ExerciseBottomBar(
            isFirst = index == 0,
            isLast = index == total - 1,
            onPrevious = onPrevious,
            onOpenGrid = { isGridVisible = true },
            onNext = onNext,
            onSubmit = onSubmitClick,
            enabled = !state.isLocked
        )
    }

    if (isGridVisible) {
        ExerciseQuestionGridSheet(
            state = state,
            onSelect = { target ->
                onGoTo(target)
                isGridVisible = false
            },
            onDismiss = { isGridVisible = false },
            onSubmit = {
                isGridVisible = false
                onSubmitClick()
            }
        )
    }
}

@Composable
private fun ExerciseProgressHeader(
    currentNumber: Int,
    total: Int,
    answeredCount: Int,
    isEssay: Boolean,
    onOpenGrid: () -> Unit
) {
    val currentText = currentNumber.toString().padStart(2, '0')
    val totalText = total.toString().padStart(2, '0')
    val titleSuffix = if (isEssay) " (Tự luận)" else ""
    val percent = if (total > 0) (answeredCount * 100) / total else 0
    val progressFraction = if (total > 0) (answeredCount.toFloat() / total).coerceIn(0f, 1f) else 0f
    val isCompleted = percent == 100

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .clickable(onClickLabel = "Mở danh sách câu hỏi", onClick = onOpenGrid)
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Câu $currentText / $totalText$titleSuffix",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary700
                )
                Text(
                    text = "$percent%",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCompleted) Success600 else Neutral600
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Neutral200)
            ) {
                if (progressFraction > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = progressFraction)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                            .background(NebulaBrush)
                    )
                }
            }
        }
        HorizontalDivider(color = Neutral200)
    }
}

@Composable
private fun ExerciseQuestionBody(
    question: ExerciseQuestionDto,
    selectedKeys: Set<String>,
    essayText: String,
    onSelectSingle: (String) -> Unit,
    onToggleMultiple: (String) -> Unit,
    onEssayChange: (String) -> Unit,
    enabled: Boolean = true
) {
    val isEssay = question.type == ExerciseQuestionType.ESSAY

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isEssay) Info100 else Primary100)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = question.type.label(),
                    fontFamily = EduMindFontFamily,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isEssay) Info600 else Primary800
                )
            }
            val isEssayReady = essayText.trim().length >= ESSAY_MIN_LENGTH
            Text(
                text = question.type.hint(),
                fontFamily = EduMindFontFamily,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = if (isEssay) FontWeight.Medium else FontWeight.Normal,
                color = if (isEssay) {
                    if (isEssayReady) Success600 else Error600
                } else {
                    Neutral500
                }
            )
        }

        Text(
            text = question.content,
            fontFamily = EduMindFontFamily,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Neutral950
        )

        if (isEssay) {
            ExerciseEssayInput(
                value = essayText,
                onValueChange = onEssayChange,
                enabled = enabled
            )
        } else {
            val isMultiple = question.type == ExerciseQuestionType.MULTIPLE_CHOICE
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                question.options.orEmpty().forEach { option ->
                    QuizOptionItem(
                        optionKey = option.key,
                        content = option.content,
                        state = if (option.key in selectedKeys) {
                            QuizOptionState.Selected
                        } else {
                            QuizOptionState.Default
                        },
                        enabled = enabled,
                        onClick = {
                            if (isMultiple) onToggleMultiple(option.key) else onSelectSingle(option.key)
                        },
                        role = if (isMultiple) Role.Checkbox else Role.RadioButton
                    )
                }
            }
        }
    }
}

@Composable
private fun ExerciseEssayInput(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean = true
) {
    val belowMinimum = value.trim().length < ESSAY_MIN_LENGTH
    val isTooShort = value.isNotEmpty() && belowMinimum
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val borderColor = when {
        isTooShort -> Error600
        isFocused -> Primary600
        else -> Primary200
    }
    val shape = RoundedCornerShape(16.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color.White)
            .border(1.5.dp, borderColor, shape)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Neutral50)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.EditNote,
                contentDescription = null,
                tint = Primary600,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "Phần trả lời của học viên",
                fontFamily = EduMindFontFamily,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Neutral700
            )
        }
        HorizontalDivider(color = Neutral200)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 190.dp)
                .background(Color.White)
                .padding(14.dp)
        ) {
            if (value.isEmpty()) {
                Text(
                    text = "Nhập câu trả lời chi tiết của bạn tại đây...",
                    fontFamily = EduMindFontFamily,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    color = Neutral400
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                readOnly = !enabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 162.dp),
                interactionSource = interactionSource,
                cursorBrush = SolidColor(Primary600),
                textStyle = TextStyle(
                    fontFamily = EduMindFontFamily,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    color = Neutral800
                )
            )
        }

        HorizontalDivider(color = Neutral100)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Neutral50)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${value.length} / $ESSAY_MAX_LENGTH",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Primary700
            )
        }
    }
}

@Composable
private fun ExerciseBottomBar(
    isFirst: Boolean,
    isLast: Boolean,
    onPrevious: () -> Unit,
    onOpenGrid: () -> Unit,
    onNext: () -> Unit,
    onSubmit: () -> Unit,
    enabled: Boolean = true
) {
    Column(modifier = Modifier.fillMaxWidth().background(Color.White)) {
        HorizontalDivider(color = Neutral200)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            EduMindButton(
                text = "Câu trước",
                onClick = onPrevious,
                style = EduMindButtonStyle.Outlined,
                enabled = !isFirst && enabled,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Primary50)
                    .border(1.dp, Primary200, RoundedCornerShape(12.dp))
                    .clickable(
                        enabled = enabled,
                        onClickLabel = "Bảng danh sách câu hỏi",
                        role = Role.Button,
                        onClick = onOpenGrid
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.GridView,
                    contentDescription = "Bảng danh sách câu hỏi",
                    tint = Primary700,
                    modifier = Modifier.size(22.dp)
                )
            }
            if (isLast) {
                EduMindButton(
                    text = "Nộp bài",
                    onClick = onSubmit,
                    enabled = enabled,
                    modifier = Modifier.weight(1f)
                )
            } else {
                EduMindButton(
                    text = "Câu tiếp",
                    onClick = onNext,
                    enabled = enabled,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

private fun ExerciseQuestionType.label(): String = when (this) {
    ExerciseQuestionType.SINGLE_CHOICE -> "Câu hỏi trắc nghiệm"
    ExerciseQuestionType.MULTIPLE_CHOICE -> "Nhiều lựa chọn"
    ExerciseQuestionType.TRUE_FALSE -> "Đúng / Sai"
    ExerciseQuestionType.ESSAY -> "Câu hỏi tự luận"
}

private fun ExerciseQuestionType.hint(): String = when (this) {
    ExerciseQuestionType.SINGLE_CHOICE -> "Chọn 1 đáp án đúng nhất"
    ExerciseQuestionType.MULTIPLE_CHOICE -> "Chọn một hoặc nhiều đáp án"
    ExerciseQuestionType.TRUE_FALSE -> "Chọn Đúng hoặc Sai"
    ExerciseQuestionType.ESSAY -> "Tối thiểu $ESSAY_MIN_LENGTH ký tự"
}
