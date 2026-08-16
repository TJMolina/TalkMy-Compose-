package com.example.talkmy.ui.components.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.talkmy.R
import com.example.talkmy.ui.theme.TalkMyTheme

@Composable
fun SliderPreferenceWidget(
    modifier: Modifier = Modifier,
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    icon: Painter? = null,
    enabled: Boolean = true,
    valueDisplay: @Composable ((Float) -> String)? = null
) {
    BasePreferenceWidget(
        modifier = modifier,
        title = title,
        icon = if (icon != null) {
            {
                androidx.compose.material3.Icon(
                    painter = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        } else null,
        subcomponent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PrefsHorizontalPadding)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Slider(
                        value = value,
                        onValueChange = onValueChange,
                        valueRange = valueRange,
                        steps = steps,
                        enabled = enabled,
                        modifier = Modifier.weight(1f)
                    )
                    if (valueDisplay != null) {
                        Text(
                            text = valueDisplay(value),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun SliderPreferenceWidgetPreview() {
    TalkMyTheme {
        Surface {
            Column {
                SliderPreferenceWidget(
                    title = stringResource(R.string.volume),
                    value = 0.5f,
                    onValueChange = {},
                    valueDisplay = { "${(it * 100).toInt()}%" }
                )
                SliderPreferenceWidget(
                    title = stringResource(R.string.speed),
                    value = 1.0f,
                    valueRange = 0.5f..2.0f,
                    onValueChange = {},
                    valueDisplay = { "x$it" }
                )
            }
        }
    }
}
