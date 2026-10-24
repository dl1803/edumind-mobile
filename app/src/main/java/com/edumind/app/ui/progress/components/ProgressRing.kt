package com.edumind.app.ui.progress.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edumind.app.ui.theme.NebulaBrush
import com.edumind.app.ui.theme.Neutral500
import com.edumind.app.ui.theme.Neutral950
import com.edumind.app.ui.theme.Primary100

@Composable
fun ProgressRing(
    percent: Int,
    modifier: Modifier = Modifier,
    size: Dp = 136.dp,
    strokeWidth: Dp = 12.dp
) {
    val animatedPercent by animateFloatAsState(
        targetValue = percent.toFloat().coerceIn(0f, 100f),
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "progress_ring_animation"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()

            drawArc(
                color = Primary100,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            if (animatedPercent > 0f) {
                drawArc(
                    brush = NebulaBrush,
                    startAngle = -90f,
                    sweepAngle = (animatedPercent / 100f) * 360f,
                    useCenter = false,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$percent%",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 28.sp,
                    lineHeight = 36.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Neutral950
            )
            Text(
                text = "Tổng tiến độ",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = Neutral500
            )
        }
    }
}
