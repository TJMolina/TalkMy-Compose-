package com.example.talkmy.ui.features.edittask.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.talkmy.R
import com.example.talkmy.domain.models.UserPreference
import com.example.talkmy.ui.components.settings.SliderPreferenceWidget
import com.example.talkmy.ui.components.settings.TextPreferenceWidget
import java.util.Locale
import kotlin.reflect.KClass

@Composable
fun VoiceOptionsDialog(
    preferences: Map<KClass<out UserPreference<*>>, UserPreference<*>>,
    onPreferenceChange: (UserPreference<*>) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(R.string.dialog_voice_options_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                // VOLUME
                val volumePref = preferences[UserPreference.Volume::class] as? UserPreference.Volume
                    ?: UserPreference.Volume()
                SliderPreferenceWidget(
                    title = stringResource(R.string.volume),
                    value = volumePref.value.toFloat(),
                    valueRange = 0f..100f,
                    onValueChange = { onPreferenceChange(UserPreference.Volume(it.toInt())) },
                    valueDisplay = { "${it.toInt()}%" }
                )

                // Speech (Pitch)
                val speechPref = preferences[UserPreference.Speech::class] as? UserPreference.Speech
                    ?: UserPreference.Speech()
                SliderPreferenceWidget(
                    title = stringResource(R.string.pitch),
                    value = speechPref.value,
                    valueRange = 0.5f..2.0f,
                    onValueChange = { onPreferenceChange(UserPreference.Speech(it)) },
                    valueDisplay = { String.format(Locale.ROOT, "%.1fx", it) }
                )

                // VELOCITY (Speed)
                val velocityPref = preferences[UserPreference.Velocity::class] as? UserPreference.Velocity
                    ?: UserPreference.Velocity()
                SliderPreferenceWidget(
                    title = stringResource(R.string.speed),
                    value = velocityPref.value,
                    valueRange = 0.5f..2.0f,
                    onValueChange = { onPreferenceChange(UserPreference.Velocity(it)) },
                    valueDisplay = { String.format(Locale.ROOT, "%.1fx", it) }
                )

                // Voice Selection
                val voicePref = preferences[UserPreference.Voice::class] as? UserPreference.Voice
                    ?: UserPreference.Voice()
                TextPreferenceWidget(
                    title = stringResource(R.string.voice),
                    subtitle = if (voicePref.value.isEmpty()) stringResource(R.string.default_voice) else voicePref.value,
                    onPreferenceClick = { /* TODO: Show Voice selector dialog */ }
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End).padding(horizontal = 16.dp)
                ) {
                    Text(stringResource(R.string.close))
                }
            }
        }
    }
}
