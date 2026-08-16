package com.example.talkmy.ui.features.edittask

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.talkmy.R
import com.example.talkmy.core.ResponseState
import com.example.talkmy.domain.models.UserPreference
import com.example.talkmy.ui.components.InputDialog
import com.example.talkmy.ui.features.edittask.components.TextOptionsDialog
import com.example.talkmy.ui.features.edittask.components.VoiceOptionsDialog
import com.example.talkmy.ui.components.TopBar
import com.example.talkmy.ui.core.ObserveEffect
import com.example.talkmy.ui.theme.TalkMyTheme
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun EditTaskScreen(
    taskId: Int? = null, onBackClick: () -> Unit, viewModel: EditTaskViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val textFieldState = rememberTextFieldState(state.currentTextOnTextField)

    var mainMenuExpanded by remember { mutableStateOf(false) }
    var voiceMenuExpanded by remember { mutableStateOf(false) }

    val currentTextSize =
        (state.preferences[UserPreference.TextSize::class] as? UserPreference.TextSize)?.value
            ?: 20.0f

    LaunchedEffect(taskId) {
        viewModel.onAction(EditTaskAction.LoadTask(taskId?.toString()))
    }

    // Normal Synchronization: ViewModel -> Editor
    LaunchedEffect(state.currentTextOnTextField) {
        if (textFieldState.text.toString() != state.currentTextOnTextField) {
            textFieldState.setTextAndPlaceCursorAtEnd(state.currentTextOnTextField)
        }
    }

    // Debounced sync: Editor -> ViewModel
    LaunchedEffect(textFieldState.text) {
        val currentText = textFieldState.text.toString()
        if (currentText != state.currentTextOnTextField) {
            delay(500.milliseconds)
            viewModel.onAction(EditTaskAction.UpdateNoteText(currentText))
        }
    }

    // Observe Side Effects
    ObserveEffect(viewModel.effect) { effect ->
        when (effect) {
            EditTaskEffect.NavigateBack -> onBackClick()
            is EditTaskEffect.ShowMessage -> {
                // TODO: Toast
            }
        }
    }

    state.activeDialog?.let { dialog ->
        when (dialog) {
            is EditTaskDialog.Url -> {
                InputDialog(
                    title = stringResource(R.string.dialog_url_title),
                    placeholder = stringResource(R.string.dialog_url_placeholder),
                    confirmText = stringResource(R.string.dialog_url_confirm),
                    onDismiss = { viewModel.onAction(EditTaskAction.DismissDialog) },
                    onConfirm = { url ->
                        viewModel.onAction(EditTaskAction.FetchTextFromUrl(url))
                        viewModel.onAction(EditTaskAction.DismissDialog)
                    })
            }

            is EditTaskDialog.TextOptions -> {
                TextOptionsDialog(
                    initialTextSize = currentTextSize,
                    onSizeChange = { },
                    onDismiss = { viewModel.onAction(EditTaskAction.DismissDialog) },
                    onApply = { newSize ->
                        viewModel.onAction(
                            EditTaskAction.UpdatePreference(
                                UserPreference.TextSize(
                                    newSize
                                )
                            )
                        )
                        viewModel.onAction(EditTaskAction.DismissDialog)
                    })
            }

            is EditTaskDialog.VoicePreferences -> {
                VoiceOptionsDialog(preferences = state.preferences, onPreferenceChange = { pref ->
                    viewModel.onAction(EditTaskAction.UpdatePreference(pref))
                }, onDismiss = { viewModel.onAction(EditTaskAction.DismissDialog) })
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(), topBar = {
            TopBar(
                title = if (taskId == null) stringResource(R.string.create_note) else stringResource(
                    R.string.edit_note
                ), onBackClick = onBackClick, actions = {
                    IconButton(onClick = {
                        viewModel.onAction(
                            EditTaskAction.ShowDialog(
                                EditTaskDialog.Url
                            )
                        )
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_baseline_link),
                            contentDescription = stringResource(R.string.paste_link),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    IconButton(onClick = {
                        viewModel.onAction(EditTaskAction.SaveTask(textFieldState.text.toString()))
                    }) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = stringResource(R.string.save),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    IconButton(onClick = { mainMenuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.menu),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    DropdownMenu(
                        expanded = mainMenuExpanded,
                        onDismissRequest = { mainMenuExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_text)) },
                            onClick = {
                                mainMenuExpanded = false
                                viewModel.onAction(EditTaskAction.ShowDialog(EditTaskDialog.TextOptions))
                            })
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_voice_options)) },
                            onClick = {
                                mainMenuExpanded = false
                                viewModel.onAction(EditTaskAction.ShowDialog(EditTaskDialog.VoicePreferences))
                            })
                    }
                    DropdownMenu(
                        expanded = voiceMenuExpanded,
                        onDismissRequest = { voiceMenuExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_voice_settings)) },
                            onClick = { voiceMenuExpanded = false })
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_voice_selector)) },
                            onClick = { voiceMenuExpanded = false })
                    }
                })
        }) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                BasicTextField(
                    state = textFieldState,
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = currentTextSize.sp
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorator = { innerTextField ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Top breathing room
                            Spacer(modifier = Modifier.height(innerPadding.calculateTopPadding() + 16.dp))

                            Box(modifier = Modifier.fillMaxWidth()) {
                                if (textFieldState.text.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.write_note_placeholder),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                            alpha = 0.5f
                                        ),
                                        fontSize = currentTextSize.sp
                                    )
                                }
                                innerTextField()
                            }

                            // Bottom breathing room
                            Spacer(modifier = Modifier.height(140.dp))
                        }
                    })
            }

            if (state.contentLoadingStatus is ResponseState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    .padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Slider(
                    value = state.progress,
                    onValueChange = { /* Update progress */ },
                    valueRange = 0f..100f,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = { viewModel.onAction(EditTaskAction.ToggleVoice) },
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (state.isPlaying) stringResource(R.string.pause) else stringResource(
                            R.string.play
                        ),
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditTaskScreenPreview() {
    TalkMyTheme {
        EditTaskScreen(onBackClick = {})
    }
}
