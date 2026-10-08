package com.edumind.app.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edumind.app.ui.theme.Primary600

@Composable
fun LoadingOverlay(
    visible: Boolean,
    modifier: Modifier = Modifier,
    message: String? = null
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(200)),
        exit = fadeOut(tween(200))
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .pointerInput(Unit) {},
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp),
                    color = Primary600,
                    strokeWidth = 3.dp
                )
                if (!message.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = message, color = Color.White, fontSize = 14.sp)
                }
            }
        }
    }
}
