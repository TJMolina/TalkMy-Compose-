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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.talkmy.R
import com.example.talkmy.core.ResponseState
import com.example.talkmy.ui.components.InputDialog
import com.example.talkmy.ui.components.TopBar
import com.example.talkmy.ui.core.ObserveEffect
import com.example.talkmy.ui.theme.TalkMyTheme
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun EditTaskScreen(
    taskId: Int? = null,
    onBackClick: () -> Unit,
    viewModel: EditTaskViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var menuExpanded by remember { mutableStateOf(false) }
    var showUrlDialog by remember { mutableStateOf(false) }

    val textFieldState = rememberTextFieldState(state.currentTextOnTextField)

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

    if (showUrlDialog) {
        InputDialog(
            title = stringResource(R.string.dialog_url_title),
            placeholder = stringResource(R.string.dialog_url_placeholder),
            confirmText = stringResource(R.string.dialog_url_confirm),
            onDismiss = { showUrlDialog = false },
            onConfirm = { url ->
                viewModel.onAction(EditTaskAction.FetchTextFromUrl(url))
                showUrlDialog = false
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopBar(
                title = if (taskId == null) "Create Note" else "Edit Note",
                onBackClick = onBackClick,
                actions = {
                    IconButton(onClick = { showUrlDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.CloudQueue,
                            contentDescription = "Paste Link",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    IconButton(onClick = { 
                        viewModel.onAction(EditTaskAction.SaveTask(textFieldState.text.toString()))
                    }) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Save",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Settings") },
                            onClick = { menuExpanded = false }
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
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
                        fontSize = 18.sp
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorator = { innerTextField ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Top breathing room
                            Spacer(modifier = Modifier.height(innerPadding.calculateTopPadding() + 16.dp))
                            
                            Box(modifier = Modifier.fillMaxWidth()) {
                                if (textFieldState.text.isEmpty()) {
                                    Text(
                                        text = "Write your note here...",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                }
                                innerTextField()
                            }
                            
                            // Bottom breathing room
                            Spacer(modifier = Modifier.height(140.dp))
                        }
                    }
                )
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
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
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
                        contentDescription = if (state.isPlaying) "Pause" else "Play",
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
