package com.edumind.app.ui.common

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
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
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp),
                color = Neutral700,
                modifier = Modifier.padding(bottom = 4.dp)
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
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 16.sp,
                color = if (enabled) Neutral950 else Neutral400
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
                        { Text(text = it, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp), color = Neutral400) }
                    },
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    container = {
                        OutlinedTextFieldDefaults.ContainerBox(
                            enabled = enabled,
                            isError = isError,
                            interactionSource = interactionSource,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary600,
                                unfocusedBorderColor = Neutral400,
                                errorBorderColor = Error600,
                                disabledBorderColor = Neutral300,
                                disabledContainerColor = Neutral100
                            ),
                            shape = RoundedCornerShape(8.dp),
                            focusedBorderThickness = 2.dp,
                            unfocusedBorderThickness = 1.dp
                        )
                    }
                )
            }
        )

        if (errorText != null) {
            Text(
                text = errorText,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = Error600,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
