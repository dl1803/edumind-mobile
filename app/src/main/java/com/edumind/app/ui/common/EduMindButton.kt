package com.edumind.app.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edumind.app.ui.theme.*

enum class EduMindButtonStyle { Primary, Outlined, Text, Destructive }

@Composable
fun EduMindButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: EduMindButtonStyle = EduMindButtonStyle.Primary,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val shape = RoundedCornerShape(12.dp)

    val contentColor = when {
        !enabled -> Neutral400
        style == EduMindButtonStyle.Primary || style == EduMindButtonStyle.Destructive -> Color.White
        else -> Primary600
    }

    var baseModifier = modifier
        .height(48.dp)
        .clip(shape)

    baseModifier = when {
        !enabled -> baseModifier.background(if (style == EduMindButtonStyle.Text) Color.Transparent else Neutral100)
        style == EduMindButtonStyle.Primary -> baseModifier.background(NebulaBrush)
        style == EduMindButtonStyle.Destructive -> baseModifier.background(Error600)
        style == EduMindButtonStyle.Outlined -> baseModifier
            .background(if (isPressed) Primary50 else Color.Transparent)
            .border(1.dp, Primary600, shape)
        style == EduMindButtonStyle.Text -> baseModifier.background(if (isPressed) Primary50 else Color.Transparent)
        else -> baseModifier
    }

    Box(
        modifier = baseModifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled && !loading,
                role = Role.Button,
                onClick = onClick
            )
            .semantics { if (loading) stateDescription = "Đang tải" }
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        if (enabled && isPressed && (style == EduMindButtonStyle.Primary || style == EduMindButtonStyle.Destructive)) {
            Box(modifier = Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.12f)))
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.alpha(if (loading) 0f else 1f)
        ) {
            leadingIcon?.let {
                it()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp),
                color = contentColor
            )
        }

        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = contentColor,
                strokeWidth = 2.dp
            )
        }
    }
}
