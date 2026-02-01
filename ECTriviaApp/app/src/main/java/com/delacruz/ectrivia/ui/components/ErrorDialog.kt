package com.delacruz.ectrivia.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.delacruz.ectrivia.ui.theme.ECTriviaPrimary
import com.delacruz.ectrivia.ui.theme.TextPrimary

@Composable
fun ErrorDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onRetry: (() -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title, color = TextPrimary)
        },
        text = {
            Text(text = message, color = TextPrimary)
        },
        confirmButton = {
            if (onRetry != null) {
                TextButton(onClick = onRetry) {
                    Text("Retry", color = ECTriviaPrimary)
                }
            }
            TextButton(onClick = onDismiss) {
                Text("OK", color = ECTriviaPrimary)
            }
        }
    )
}
