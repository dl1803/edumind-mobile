package com.edumind.app.ui.player.subtitle

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import com.edumind.app.data.local.datastore.SubtitleFontSize
import com.edumind.app.ui.theme.EduMindFontFamily

private const val CUE_FADE_MS = 120

@Composable
fun SubtitleOverlay(
    text: String?,
    controlsVisible: Boolean,
    modifier: Modifier = Modifier,
    isLandscape: Boolean = false,
    subtitleFontSize: SubtitleFontSize = SubtitleFontSize.MEDIUM
) {
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
        AnimatedContent(
            targetState = text,
            transitionSpec = {
                (fadeIn(tween(CUE_FADE_MS)) togetherWith fadeOut(tween(CUE_FADE_MS)))
                    .using(SizeTransform(clip = false) { _, _ -> snap() })
            },
            contentAlignment = Alignment.BottomCenter,
            label = "subtitleCue"
        ) { cue ->
            if (cue != null) {
                val fontSize = if (isLandscape) (subtitleFontSize.textSp + 2).sp else subtitleFontSize.textSp.sp
                val lineHeight = if (isLandscape) (subtitleFontSize.lineHeightSp + 4).sp else subtitleFontSize.lineHeightSp.sp
                Text(
                    text = cue,
                    fontFamily = EduMindFontFamily,
                    fontSize = fontSize,
                    lineHeight = lineHeight,
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
}
