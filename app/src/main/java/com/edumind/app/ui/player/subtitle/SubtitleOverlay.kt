package com.edumind.app.ui.player.subtitle

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edumind.app.ui.theme.EduMindFontFamily

@Composable
fun SubtitleOverlay(
    text: String?,
    controlsVisible: Boolean,
    modifier: Modifier = Modifier,
    isLandscape: Boolean = false
) {
    if (text != null) {
        val targetPadding = when {
            isLandscape && controlsVisible -> 64.dp
            isLandscape && !controlsVisible -> 16.dp
            !isLandscape && controlsVisible -> 60.dp
            else -> 8.dp
        }
        val bottomPadding by animateDpAsState(
            targetValue = targetPadding,
            label = "subtitleBottomPadding"
        )
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = if (isLandscape) 32.dp else 12.dp)
                .padding(bottom = bottomPadding),
            contentAlignment = Alignment.BottomCenter
        ) {
            Text(
                text = text,
                fontFamily = EduMindFontFamily,
                fontSize = if (isLandscape) 14.sp else 12.sp,
                lineHeight = if (isLandscape) 20.sp else 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(4.dp))
                    .padding(
                        horizontal = if (isLandscape) 10.dp else 8.dp,
                        vertical = if (isLandscape) 4.dp else 3.dp
                    )
            )
        }
    }
}
