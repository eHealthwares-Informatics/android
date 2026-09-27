package com.rxsoft.mobile.ui.designsystem.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import com.rxsoft.mobile.ui.designsystem.token.ShapeTokens

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    leadingIconDescription: String? = null,
    trailingIcon: ImageVector? = null,
    trailingIconDescription: String? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    supportingText: String? = null,
    singleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {},
    visualTransformation: VisualTransformation = VisualTransformation.None,
    enabled: Boolean = true,
    readOnly: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = label ?: placeholder ?: "Text field" },
        enabled = enabled,
        readOnly = readOnly,
        label = if (label != null) {{ Text(text = label) }} else null,
        placeholder = if (placeholder != null) {{ Text(text = placeholder) }} else null,
        leadingIcon = if (leadingIcon != null) {
            {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = leadingIconDescription,
                )
            }
        } else null,
        trailingIcon = if (trailingIcon != null) {
            {
                IconButton(
                    onClick = onTrailingIconClick ?: {},
                    enabled = onTrailingIconClick != null,
                ) {
                    Icon(
                        imageVector = trailingIcon,
                        contentDescription = trailingIconDescription,
                    )
                }
            }
        } else null,
        isError = isError,
        singleLine = singleLine,
        shape = ShapeTokens.textField,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction,
        ),
        keyboardActions = KeyboardActions(
            onDone = { onImeAction() },
            onNext = { onImeAction() },
        ),
        visualTransformation = visualTransformation,
        supportingText = when {
            isError && errorMessage != null -> {{ Text(text = errorMessage, color = androidx.compose.material3.MaterialTheme.colorScheme.error) }}
            supportingText != null -> {{ Text(text = supportingText) }}
            else -> null
        },
    )
}
