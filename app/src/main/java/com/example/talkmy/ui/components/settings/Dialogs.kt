package com.example.talkmy.ui.components.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

@Composable
fun <T> SingleSelectDialog(
    dismiss: () -> Unit,
    title: String,
    entries: Map<out T, String>,
    selectedKey: T,
    confirm: (T) -> Unit,
    confirmText: String,
    dismissText: String,
    iconProvider: (@Composable (key: T, value: String) -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = dismiss,
        title = { Text(text = title) },
        text = {
            LazyColumn {
                items(entries.toList()) { (key, value) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { confirm(key) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (key == selectedKey),
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        if (iconProvider != null) {
                            iconProvider(key, value)
                            Spacer(modifier = Modifier.width(16.dp))
                        }
                        Text(text = value)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = dismiss) {
                Text(text = dismissText)
            }
        }
    )
}

@Composable
fun MultiSelectDialog(
    title: String,
    entries: Map<String, String>,
    selectedKeys: Set<String>,
    confirm: (Set<String>) -> Unit,
    confirmText: String,
    dismiss: () -> Unit,
    dismissText: String,
    properties: DialogProperties = DialogProperties()
) {
    var currentSelection by remember { mutableStateOf(selectedKeys) }

    AlertDialog(
        onDismissRequest = dismiss,
        properties = properties,
        title = { Text(text = title) },
        text = {
            LazyColumn {
                items(entries.toList()) { (key, value) ->
                    val isSelected = currentSelection.contains(key)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                currentSelection = if (isSelected) {
                                    currentSelection - key
                                } else {
                                    currentSelection + key
                                }
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = null
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = value)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { confirm(currentSelection) }) {
                Text(text = confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = dismiss) {
                Text(text = dismissText)
            }
        }
    )
}