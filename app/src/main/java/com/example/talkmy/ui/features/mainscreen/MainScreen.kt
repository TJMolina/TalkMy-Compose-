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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.talkmy.R
import com.example.talkmy.ui.features.mainscreen.components.FloatingButton
import com.example.talkmy.ui.features.mainscreen.components.TaskCard
import com.example.talkmy.ui.components.TopBar
import com.example.talkmy.ui.theme.TalkMyTheme

@Composable
fun MainScreen(
    onAddTask: () -> Unit,
    onEditTask: (Int) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopBar(
                title = stringResource(R.string.app_name),
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menú",
                            tint = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { },
                            onClick = { menuExpanded = false }
                        )
                    }
                }
            )
        },
        floatingActionButton = { FloatingButton(onClick = onAddTask) }
    ) {
        innerPadding ->
        MainScreenContent(
            onEditTask = onEditTask,
            modifier = Modifier.padding(innerPadding.plus(PaddingValues(vertical = 2.dp)))
        )
    }
}

@Composable
fun MainScreenContent(
    onEditTask: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        item {
            TaskCard(
                title = "Mi Tarea Importante",
                description = "Esta es una descripción larga para probar cómo se comporta el texto cuando tiene varias líneas y debe cortarse con puntos suspensivos.",
                date = "1s",
                onCardClick = { onEditTask(1) },
                onDeleteClick = {}
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
