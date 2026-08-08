package com.example.talkmy.ui.features.mainscreen.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.talkmy.ui.theme.TalkMyTheme

@Composable
fun FloatingButton(onClick: () -> Unit) {
    FloatingActionButton(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        onClick = onClick
    ){
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Agregar",
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }
}


@Preview
@Composable
fun prevv(){
    TalkMyTheme{
        FloatingButton({})
    }
}
