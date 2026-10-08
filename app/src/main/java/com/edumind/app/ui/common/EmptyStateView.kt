package com.edumind.app.ui.common

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edumind.app.R
import com.edumind.app.ui.theme.Neutral700

@Composable
fun EmptyStateView(
    title: String,
    modifier: Modifier = Modifier,
    message: String? = null,
    @DrawableRes illustration: Int = R.drawable.ic_empty_state,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = illustration),
            contentDescription = null,
            modifier = Modifier.size(160.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        if (!message.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = message, fontSize = 14.sp, color = Neutral700, textAlign = TextAlign.Center)
        }
        if (actionText != null && onAction != null) {
            Spacer(modifier = Modifier.height(20.dp))
            EduMindButton(
                text = actionText,
                onClick = onAction,
                style = EduMindButtonStyle.Primary
            )
        }
    }
}
