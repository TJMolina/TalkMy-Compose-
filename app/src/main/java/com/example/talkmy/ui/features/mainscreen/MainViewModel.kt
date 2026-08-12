package com.example.talkmy.ui.features.mainscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.talkmy.domain.models.Task
import com.example.talkmy.domain.usecases.MainScreenTasksUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val useCases: MainScreenTasksUseCases
) : ViewModel() {

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    init {
        loadTasks()
    }

    private fun loadTasks() {
        viewModelScope.launch {
            useCases.getTasks().collect {
                _tasks.value = it
            }
        }
    }

    fun deleteTask(id: Int) {
        viewModelScope.launch {
            useCases.deleteTask(id)
        }
    }

    fun clearAllTasks() {
        viewModelScope.launch {
            useCases.clearTasks()
        }
    }
}