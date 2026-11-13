package com.edumind.app.ui.player

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edumind.app.ui.theme.EduMindFontFamily
import com.edumind.app.ui.theme.NebulaBrush
import com.edumind.app.ui.theme.Neutral950

@Composable
fun PlayerErrorOverlay(
    type: PlayerErrorType,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Neutral950)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
            .padding(horizontal = 24.dp, vertical = if (isLandscape) 16.dp else 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = if (isLandscape) 560.dp else 340.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(if (isLandscape) 12.dp else 8.dp)
        ) {
            Text(
                text = type.message(isLandscape),
                fontFamily = EduMindFontFamily,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Row(
                modifier = if (isLandscape) Modifier.widthIn(max = 300.dp) else Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (type != PlayerErrorType.NOT_ENROLLED) {
                    PlayerCompactButton(
                        text = "Thử lại",
                        onClick = onRetry,
                        isPrimary = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                PlayerCompactButton(
                    text = "Quay lại",
                    onClick = onBack,
                    isPrimary = false,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PlayerCompactButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = true
) {
    val shape = RoundedCornerShape(8.dp)
    val buttonModifier = if (isPrimary) {
        modifier
            .height(36.dp)
            .clip(shape)
            .background(NebulaBrush)
            .clickable(onClick = onClick)
    } else {
        modifier
            .height(36.dp)
            .clip(shape)
            .border(1.dp, Color.White.copy(alpha = 0.7f), shape)
            .background(Color.Transparent)
            .clickable(onClick = onClick)
    }

    Box(
        modifier = buttonModifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontFamily = EduMindFontFamily,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}

private fun PlayerErrorType.message(isLandscape: Boolean): String = if (isLandscape) {
    when (this) {
        PlayerErrorType.NETWORK -> "Mất kết nối internet. Video sẽ tự động tiếp tục khi có mạng."
        PlayerErrorType.URL_EXPIRED -> "Đường dẫn phát video đã hết hạn và chưa thể làm mới. Vui lòng thử lại."
        PlayerErrorType.NOT_ENROLLED -> "Vui lòng mua hoặc đăng ký khóa học để xem video này."
        PlayerErrorType.UNKNOWN -> "Video chưa sẵn sàng hoặc đã xảy ra lỗi."
    }
} else {
    when (this) {
        PlayerErrorType.NETWORK -> "Mất kết nối internet.\nVideo sẽ tự động tiếp tục khi có mạng."
        PlayerErrorType.URL_EXPIRED -> "Đường dẫn phát video đã hết hạn\nvà chưa thể làm mới. Vui lòng thử lại."
        PlayerErrorType.NOT_ENROLLED -> "Vui lòng mua hoặc đăng ký khóa học\nđể xem video này."
        PlayerErrorType.UNKNOWN -> "Video chưa sẵn sàng\nhoặc đã xảy ra lỗi."
    }
}
