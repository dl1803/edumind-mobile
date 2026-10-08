package com.edumind.app.ui.common

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.edumind.app.ui.theme.Neutral100

fun Modifier.shimmer(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val shimmerColors = listOf(
        Neutral100,
        Color.White.copy(alpha = 0.7f),
        Neutral100,
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 200f, translateAnim - 200f),
        end = Offset(translateAnim, translateAnim)
    )

    background(brush)
}

@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp)
) {
    Box(
        modifier = modifier
            .clip(shape)
            .shimmer()
    )
}

@Composable
fun SkeletonLines(
    lines: Int = 3,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.semantics { contentDescription = "Đang tải nội dung" },
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(lines) { index ->
            val widthFraction = if (index == lines - 1) 0.6f else 1f
            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth(widthFraction)
                    .height(12.dp)
            )
        }
    }
}
