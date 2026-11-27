package com.edumind.app.ui.quiz

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edumind.app.ui.theme.Error100
import com.edumind.app.ui.theme.Error400
import com.edumind.app.ui.theme.Error600
import com.edumind.app.ui.theme.Error700
import com.edumind.app.ui.theme.Neutral100
import com.edumind.app.ui.theme.Neutral200
import com.edumind.app.ui.theme.Neutral300
import com.edumind.app.ui.theme.Neutral700
import com.edumind.app.ui.theme.Neutral800
import com.edumind.app.ui.theme.Primary300
import com.edumind.app.ui.theme.Primary50
import com.edumind.app.ui.theme.Primary600
import com.edumind.app.ui.theme.Primary900
import com.edumind.app.ui.theme.Success100
import com.edumind.app.ui.theme.Success600
import com.edumind.app.ui.theme.Success700

enum class QuizOptionState { Default, Selected, Correct, Wrong }

@Composable
fun QuizOptionItem(
    optionKey: String,
    content: String,
    state: QuizOptionState,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    role: Role = Role.RadioButton
) {
    val isSelectedWrong = state == QuizOptionState.Wrong
    val shakeProgress = remember { Animatable(0f) }

    LaunchedEffect(state) {
        if (isSelectedWrong) {
            shakeProgress.snapTo(0f)
            shakeProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 420, easing = LinearEasing)
            )
        } else {
            shakeProgress.snapTo(0f)
        }
    }

    val rowColor by animateColorAsState(
        targetValue = when (state) {
            QuizOptionState.Default -> Color.White
            QuizOptionState.Selected -> Primary50
            QuizOptionState.Correct -> Success100
            QuizOptionState.Wrong -> Color(0xFFFEE2E2)
        },
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "quizOptionRow"
    )
    val badgeColor by animateColorAsState(
        targetValue = when (state) {
            QuizOptionState.Default -> Neutral100
            QuizOptionState.Selected -> Primary600
            QuizOptionState.Correct -> Success600
            QuizOptionState.Wrong -> Color(0xFFDC2626)
        },
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "quizOptionBadge"
    )
    val borderColor by animateColorAsState(
        targetValue = when (state) {
            QuizOptionState.Default -> Neutral200
            QuizOptionState.Selected -> Primary300
            QuizOptionState.Correct -> Success600
            QuizOptionState.Wrong -> Color(0xFFDC2626)
        },
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "quizOptionBorder"
    )
    val scale by animateFloatAsState(
        targetValue = if (state == QuizOptionState.Correct) 1.045f else 1f,
        animationSpec = spring(
            dampingRatio = 0.65f,
            stiffness = 1800f
        ),
        label = "quizOptionScale"
    )
    val badgeTextColor = if (state == QuizOptionState.Default) Neutral700 else Color.White
    val contentTextColor = when (state) {
        QuizOptionState.Default -> Neutral800
        QuizOptionState.Selected -> Primary900
        QuizOptionState.Correct -> Success700
        QuizOptionState.Wrong -> Color(0xFF7F1D1D)
    }
    val contentFontWeight = when (state) {
        QuizOptionState.Default -> FontWeight.Medium
        QuizOptionState.Selected, QuizOptionState.Correct, QuizOptionState.Wrong -> FontWeight.Bold
    }

    val shape = RoundedCornerShape(16.dp)
    val badgeShape = RoundedCornerShape(10.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                val t = shakeProgress.value
                translationX = if (t in 0.001f..0.999f) {
                    val decay = (1f - t) * (1f - t) * (1f - t)
                    val osc = kotlin.math.sin(t * 2.25 * 2.0 * Math.PI).toFloat()
                    10f * decay * osc * density
                } else 0f
            }
            .heightIn(min = 54.dp)
            .clip(shape)
            .background(rowColor)
            .border(2.dp, borderColor, shape)
            .selectable(
                selected = state == QuizOptionState.Selected,
                enabled = enabled,
                role = role,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(badgeShape)
                .background(badgeColor)
                .then(
                    if (state == QuizOptionState.Default) {
                        Modifier.border(1.dp, Neutral300, badgeShape)
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = optionKey,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = badgeTextColor
            )
        }
        Text(
            text = content,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = contentFontWeight,
            color = contentTextColor,
            modifier = Modifier.weight(1f)
        )
        when (state) {
            QuizOptionState.Selected -> Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = Primary600,
                modifier = Modifier.size(20.dp)
            )
            QuizOptionState.Correct -> Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "Chính xác",
                tint = Success600,
                modifier = Modifier.size(20.dp)
            )
            QuizOptionState.Wrong -> Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Chưa chính xác",
                tint = Color(0xFFDC2626),
                modifier = Modifier.size(20.dp)
            )
            else -> Unit
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun QuizOptionItemPreview() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QuizOptionItem(
            optionKey = "A",
            content = "1. Trạng thái mặc định (Default)",
            state = QuizOptionState.Default,
            enabled = true,
            onClick = {}
        )
        QuizOptionItem(
            optionKey = "B",
            content = "2. Trạng thái đang được chọn (Selected)",
            state = QuizOptionState.Selected,
            enabled = true,
            onClick = {}
        )
        QuizOptionItem(
            optionKey = "C",
            content = "3. Trạng thái trả lời đúng (Correct)",
            state = QuizOptionState.Correct,
            enabled = false,
            onClick = {}
        )
        QuizOptionItem(
            optionKey = "D",
            content = "4. Trạng thái trả lời sai (Wrong)",
            state = QuizOptionState.Wrong,
            enabled = false,
            onClick = {}
        )
    }
}
