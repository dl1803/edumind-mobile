package com.edumind.app.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.edumind.app.ui.theme.Primary100
import com.edumind.app.ui.theme.Primary400

@Composable
fun CircularIllustration(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    iconTint: Color = Primary400,
    size: Dp = 120.dp,
    iconSize: Dp = 60.dp,
    gradientColors: List<Color> = listOf(
        Color(0xFFEFF6FF),
        Color(0xFFF5F3FF),
        Color(0xFFFDF2F8)
    ),
    borderColor: Color = Primary100.copy(alpha = 0.6f),
    borderWidth: Dp = 6.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.sweepGradient(gradientColors))
            .border(borderWidth, borderColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(iconSize)
        )
    }
}
