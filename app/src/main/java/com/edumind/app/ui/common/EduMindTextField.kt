package com.edumind.app.ui.common

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edumind.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EduMindTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    errorText: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isError = errorText != null

    Column(modifier = modifier) {
        if (!label.isNullOrBlank()) {
            Text(
                text = label,
                fontFamily = EduMindFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Neutral700,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .semantics { if (errorText != null) error(errorText) },
            enabled = enabled,
            singleLine = singleLine,
            textStyle = TextStyle(
                fontFamily = EduMindFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = if (enabled) Neutral950 else Neutral500
            ),
            cursorBrush = SolidColor(Primary600),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            interactionSource = interactionSource,
            decorationBox = { innerTextField ->
                OutlinedTextFieldDefaults.DecorationBox(
                    value = value,
                    innerTextField = innerTextField,
                    enabled = enabled,
                    singleLine = singleLine,
                    visualTransformation = visualTransformation,
                    interactionSource = interactionSource,
                    isError = isError,
                    placeholder = placeholder?.let {
                        {
                            Text(
                                text = it,
                                style = TextStyle(
                                    fontFamily = EduMindFontFamily,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal
                                ),
                                color = Neutral400
                            )
                        }
                    },
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                    container = {
                        OutlinedTextFieldDefaults.ContainerBox(
                            enabled = enabled,
                            isError = isError,
                            interactionSource = interactionSource,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFA78BFA),
                                unfocusedBorderColor = Color(0xFFE5E7EB),
                                errorBorderColor = Error600,
                                disabledBorderColor = Color(0xFFE5E7EB),
                                disabledContainerColor = Color(0xFFF9FAFB)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            focusedBorderThickness = 1.2.dp,
                            unfocusedBorderThickness = 1.dp
                        )
                    }
                )
            }
        )

        if (!errorText.isNullOrBlank()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Error,
                    contentDescription = null,
                    tint = Error600,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = errorText,
                    fontFamily = EduMindFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = Error600
                )
            }
        }
    }
}
