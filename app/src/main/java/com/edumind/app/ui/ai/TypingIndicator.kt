package com.edumind.app.ui.ai

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edumind.app.ui.theme.EduMindFontFamily
import com.edumind.app.ui.theme.NebulaGradient
import com.edumind.app.ui.theme.NebulaPurple
import com.edumind.app.ui.theme.Primary700

private const val DOT_CYCLE_MS = 1200
private const val DOT_STAGGER_MS = 200
private const val DOT_JUMP_DP = -6f

@Composable
fun TypingIndicator(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "typing")

    Row(
        modifier = modifier
            .padding(vertical = 4.dp)
            .clearAndSetSemantics { contentDescription = "AI đang xử lý câu trả lời" },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(NebulaGradient),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(12.dp)
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier
                    .clip(AiBubbleShape)
                    .background(Color.White)
                    .border(2.dp, NebulaGradient, AiBubbleShape)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(3) { index ->
                    val offsetY by transition.animateFloat(
                        initialValue = 0f,
                        targetValue = 0f,
                        animationSpec = infiniteRepeatable(
                            animation = keyframes {
                                durationMillis = DOT_CYCLE_MS
                                0f at 0
                                DOT_JUMP_DP at DOT_STAGGER_MS using FastOutSlowInEasing
                                0f at DOT_STAGGER_MS * 2 using FastOutSlowInEasing
                            },
                            repeatMode = RepeatMode.Restart,
                            initialStartOffset = StartOffset(index * DOT_STAGGER_MS)
                        ),
                        label = "dot$index"
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .graphicsLayer { translationY = offsetY.dp.toPx() }
                            .clip(CircleShape)
                            .background(NebulaPurple)
                    )
                }
            }

            Text(
                text = "EduAI đang trích xuất câu trả lời từ bài giảng...",
                fontFamily = EduMindFontFamily,
                fontSize = 10.sp,
                lineHeight = 14.sp,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Medium,
                color = Primary700,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}
