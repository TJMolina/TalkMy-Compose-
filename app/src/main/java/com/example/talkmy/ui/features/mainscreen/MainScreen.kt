package com.example.talkmy.ui.features.mainscreen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.talkmy.R
import com.example.talkmy.core.extensions.toRelativeTime
import com.example.talkmy.ui.components.TopBar
import com.example.talkmy.ui.features.mainscreen.components.FloatingButton
import com.example.talkmy.ui.features.mainscreen.components.TaskCard
import com.example.talkmy.ui.theme.TalkMyTheme

@Composable
fun MainScreen(
    onAddTask: () -> Unit,
    onEditTask: (Int) -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopBar(
                title = stringResource(R.string.app_name),
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.menu),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.clear_all)) },
                            onClick = { 
                                viewModel.onAction(MainAction.ClearAll)
                                menuExpanded = false 
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = { FloatingButton(onClick = onAddTask) }
    ) { innerPadding ->
        MainScreenContent(
            state = state,
            onEditTask = onEditTask,
            onDeleteTask = { viewModel.onAction(MainAction.DeleteTask(it)) },
            modifier = Modifier.padding(innerPadding.plus(PaddingValues(vertical = 2.dp)))
        )
    }
}

@Composable
fun MainScreenContent(
    state: MainState,
    onEditTask: (Int) -> Unit,
    onDeleteTask: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(state.tasks.size) { index ->
            val task = state.tasks[index]
            val dateString = remember(task.date) { task.date.toRelativeTime() }
            TaskCard(
                title = stringResource(R.string.note_title_format, index + 1),
                description = task.note.take(500),
                date = dateString,
                onCardClick = { onEditTask(task.id) },
                onDeleteClick = { onDeleteTask(task.id) }
            )
        }
    }
}

@Preview
@Composable
fun prev() {
    TalkMyTheme {
        MainScreen(
            onAddTask = {},
            onEditTask = {}
        )
    }
}
