package com.edumind.app.ui.common

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edumind.app.R
import com.edumind.app.ui.theme.Neutral700
import com.edumind.app.ui.theme.Warning100
import com.edumind.app.ui.theme.Warning600

@Composable
fun OfflineBanner(
    isOffline: Boolean,
    modifier: Modifier = Modifier,
    message: String = "Đang offline"
) {
    AnimatedVisibility(
        visible = isOffline,
        enter = slideInVertically { -it } + fadeIn(),
        exit = slideOutVertically { -it } + fadeOut()
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(Warning100)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_wifi_off),
                contentDescription = null,
                tint = Warning600,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = message,
                color = Neutral700,
                fontSize = 14.sp
            )
        }
    }
}
