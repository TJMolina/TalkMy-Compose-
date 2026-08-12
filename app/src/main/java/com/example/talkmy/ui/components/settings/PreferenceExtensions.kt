package com.example.talkmy.ui.components.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import com.example.talkmy.domain.models.PreferenceData
import kotlinx.coroutines.flow.Flow

@Composable
fun <T> PreferenceData<T>.collectAsState(): androidx.compose.runtime.State<T> {
    val flow: Flow<T> = remember(this) { changes() }
    return flow.collectAsState(initial = this.get())
}