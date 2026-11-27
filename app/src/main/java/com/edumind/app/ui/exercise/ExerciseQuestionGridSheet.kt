package com.edumind.app.ui.exercise

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edumind.app.data.model.response.ExerciseQuestionType
import com.edumind.app.ui.theme.EduMindFontFamily
import com.edumind.app.ui.theme.Info600
import com.edumind.app.ui.theme.NebulaBrush
import com.edumind.app.ui.theme.Neutral100
import com.edumind.app.ui.theme.Neutral200
import com.edumind.app.ui.theme.Neutral300
import com.edumind.app.ui.theme.Neutral50
import com.edumind.app.ui.theme.Neutral600
import com.edumind.app.ui.theme.Neutral700
import com.edumind.app.ui.theme.Neutral950
import com.edumind.app.ui.theme.Primary100
import com.edumind.app.ui.theme.Primary50
import com.edumind.app.ui.theme.Primary600
import com.edumind.app.ui.theme.Primary900
import com.edumind.app.ui.theme.Success600

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ExerciseQuestionGridSheet(
    state: ExerciseUiState.InProgress,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = Color.White,
        dragHandle = null
    ) {
        ExerciseQuestionGridBody(
            state = state,
            onSelect = onSelect,
            onSubmit = onSubmit,
            modifier = Modifier.fillMaxHeight(0.72f)
        )
    }
}

@Composable
internal fun ExerciseQuestionGridBody(
    state: ExerciseUiState.InProgress,
    onSelect: (Int) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val total = state.questions.size
    val answered = state.answeredCount
    val unanswered = (total - answered).coerceAtLeast(0)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(width = 36.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Neutral300)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "Danh sách câu hỏi",
                fontFamily = EduMindFontFamily,
                fontSize = 18.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Neutral950
            )
        }
        HorizontalDivider(color = Neutral100)

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Neutral50)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(
                    label = "Đã làm ($answered)",
                    fill = NebulaBrush,
                    ring = false
                )
                LegendItem(
                    label = "Đang làm (1)",
                    fill = SolidColor(Primary50),
                    ring = true
                )
                LegendItem(
                    label = "Chưa làm ($unanswered)",
                    fill = SolidColor(Neutral200),
                    ring = false
                )
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(total) { i ->
                    val question = state.questions[i]
                    val isCurrent = i == state.currentIndex
                    val isAnswered = state.isAnswered(question)
                    val numberText = (i + 1).toString().padStart(2, '0')
                    val itemShape = RoundedCornerShape(12.dp)

                    val backgroundModifier = when {
                        isCurrent -> Modifier
                            .background(Primary100)
                            .border(2.dp, Primary600, itemShape)
                        isAnswered -> Modifier.background(NebulaBrush)
                        else -> Modifier
                            .background(Neutral100)
                            .border(1.dp, Neutral300, itemShape)
                    }

                    Box(
                        modifier = Modifier
                            .height(50.dp)
                            .clip(itemShape)
                            .then(backgroundModifier)
                            .clickable(
                                onClickLabel = "Đến câu ${i + 1}",
                                role = Role.Button
                            ) { onSelect(i) }
                            .padding(horizontal = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = numberText,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    isCurrent -> Primary900
                                    isAnswered -> Color.White
                                    else -> Neutral700
                                }
                            )
                            when {
                                isAnswered && !isCurrent -> Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                else -> Text(
                                    text = question.type.gridLabel(),
                                    fontFamily = EduMindFontFamily,
                                    fontSize = 8.5.sp,
                                    lineHeight = 10.sp,
                                    fontWeight = if (isCurrent || question.type == ExerciseQuestionType.ESSAY) FontWeight.Bold else FontWeight.Medium,
                                    color = when {
                                        isCurrent -> Primary900
                                        question.type == ExerciseQuestionType.ESSAY -> Info600
                                        else -> Neutral600
                                    },
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = Neutral100)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Success600)
                    .clickable(
                        onClickLabel = "Nộp bài ngay",
                        role = Role.Button,
                        onClick = onSubmit
                    )
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = if (unanswered > 0) "Nộp bài ngay (Còn $unanswered câu chưa làm)" else "Nộp bài ngay",
                    fontFamily = EduMindFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun LegendItem(label: String, fill: Brush, ring: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(fill)
                .then(
                    if (ring) {
                        Modifier.border(2.dp, Primary600, RoundedCornerShape(4.dp))
                    } else {
                        Modifier
                    }
                )
        )
        Text(
            text = label,
            fontFamily = EduMindFontFamily,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Neutral600
        )
    }
}

private fun ExerciseQuestionType.gridLabel(): String = when (this) {
    ExerciseQuestionType.SINGLE_CHOICE -> "Trắc nghiệm"
    ExerciseQuestionType.MULTIPLE_CHOICE -> "Trắc nghiệm"
    ExerciseQuestionType.TRUE_FALSE -> "Đúng/Sai"
    ExerciseQuestionType.ESSAY -> "Tự luận"
}
