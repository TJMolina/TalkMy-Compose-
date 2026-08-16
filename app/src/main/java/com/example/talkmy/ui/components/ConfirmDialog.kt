package com.example.talkmy.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.talkmy.R
import com.example.talkmy.ui.theme.TalkMyTheme

@Composable
fun ConfirmDialog(
    title: String,
    description: String,
    confirmText: String = stringResource(R.string.delete),
    cancelText: String = stringResource(R.string.cancel),
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        ConfirmDialogContent(
            title = title,
            description = description,
            confirmText = confirmText,
            cancelText = cancelText,
            onDismiss = onDismiss,
            onConfirm = onConfirm
        )
    }
}

@Composable
fun ConfirmDialogContent(
    title: String,
    description: String,
    confirmText: String,
    cancelText: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(300.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Title and Description Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                )
            }

            // Buttons Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(text = cancelText, fontSize = 14.sp)
                }
                TextButton(
                    onClick = onConfirm,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(text = confirmText, fontSize = 14.sp)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ConfirmDialogPreview() {
    TalkMyTheme {
        ConfirmDialogContent(
            title = stringResource(R.string.dialog_delete_note_title),
            description = stringResource(R.string.delete_note_confirmation),
            confirmText = stringResource(R.string.delete),
            cancelText = stringResource(R.string.cancel),
            onDismiss = {},
            onConfirm = {}
        )
    }
}
