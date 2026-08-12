package com.example.talkmy.ui.features.mainscreen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.talkmy.R
import com.example.talkmy.domain.models.Task
import com.example.talkmy.ui.components.TopBar
import com.example.talkmy.ui.features.mainscreen.components.FloatingButton
import com.example.talkmy.ui.features.mainscreen.components.TaskCard
import com.example.talkmy.ui.theme.TalkMyTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MainScreen(
    onAddTask: () -> Unit,
    onEditTask: (Int) -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val tasks by viewModel.tasks.collectAsState()
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopBar(
                title = stringResource(R.string.app_name),
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Clear All") },
                            onClick = { 
                                // viewModel.clearTasks()
                                menuExpanded = false 
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = { FloatingButton(onClick = onAddTask) }
    ) {
        innerPadding ->
        MainScreenContent(
            tasks = tasks,
            onEditTask = onEditTask,
            onDeleteTask = { viewModel.deleteTask(it) },
            modifier = Modifier.padding(innerPadding.plus(PaddingValues(vertical = 2.dp)))
        )
    }
}

@Composable
fun MainScreenContent(
    tasks: List<Task>,
    onEditTask: (Int) -> Unit,
    onDeleteTask: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault()) }

    LazyColumn(modifier = modifier) {
        items(tasks.size) { index ->
            val task = tasks[index]
            val dateString = remember(task.date) { dateFormat.format(Date(task.date)) }
            TaskCard(
                title = "Note ${index + 1}",
                description = task.note,
                date = dateString,
                onCardClick = { onEditTask(task.id) },
                onDeleteClick = { onDeleteTask(task.id) }
            )
        }
    }
}

@Preview
@Composable
fun prev(){
    TalkMyTheme{
        MainScreen(
            onAddTask = {},
            onEditTask = {}
        )
    }
}
