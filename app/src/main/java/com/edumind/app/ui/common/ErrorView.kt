package com.edumind.app.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edumind.app.ui.theme.Error600
import com.edumind.app.ui.theme.Neutral700

@Composable
fun ErrorView(
    message: String,
    modifier: Modifier = Modifier,
    title: String = "Đã xảy ra lỗi",
    onRetry: (() -> Unit)? = null,
    retryText: String = "Thử lại",
) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = Error600,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message,
                    fontSize = 14.sp,
                    color = Neutral700,
                    textAlign = TextAlign.Center,
                    maxLines = 4
                )
                if (onRetry != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    EduMindButton(
                        text = retryText,
                        onClick = onRetry,
                        style = EduMindButtonStyle.Outlined
                    )
                }
            }
        }
    }
}
