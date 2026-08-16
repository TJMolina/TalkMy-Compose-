package com.example.talkmy.ui.features.edittask.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.talkmy.R
import com.example.talkmy.ui.components.settings.SliderPreferenceWidget
import com.example.talkmy.ui.theme.TalkMyTheme

@Composable
fun TextOptionsDialog(
    initialTextSize: Float,
    onSizeChange: (Float) -> Unit,
    onDismiss: () -> Unit,
    onApply: (Float) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        TextOptionsDialogContent(
            initialTextSize = initialTextSize,
            onSizeChange = onSizeChange,
            onDismiss = onDismiss,
            onApply = onApply
        )
    }
}

@Composable
fun TextOptionsDialogContent(
    initialTextSize: Float,
    onSizeChange: (Float) -> Unit,
    onDismiss: () -> Unit,
    onApply: (Float) -> Unit
) {
    var currentSize by remember { mutableFloatStateOf(initialTextSize) }

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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.dialog_text_config_title),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            SliderPreferenceWidget(
                title = stringResource(R.string.dialog_text_size),
                value = currentSize,
                onValueChange = { 
                    currentSize = it
                    onSizeChange(it) // For real-time preview if needed
                },
                valueRange = 15f..39f,
                steps = 47, // (39 - 15) / 0.5 = 48 intervals, 47 steps
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Preview Text
            Text(
                text = stringResource(R.string.dialog_text_preview),
                fontSize = currentSize.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.Normal
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(R.string.cancel))
                }
                TextButton(onClick = { onApply(currentSize) }) {
                    Text(text = stringResource(R.string.apply))
                }
            }
        }
    }
}

@Preview(showBackground = true, apiLevel = 36)
@Composable
fun TextOptionsDialogPreview() {
    TalkMyTheme(dynamicColor = false) {
        TextOptionsDialogContent(
            initialTextSize = 20f,
            onSizeChange = {},
            onDismiss = {},
            onApply = {}
        )
    }
}
