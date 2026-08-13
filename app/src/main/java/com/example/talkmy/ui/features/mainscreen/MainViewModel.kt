package com.example.talkmy.ui.features.mainscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.talkmy.domain.models.Task
import com.example.talkmy.domain.usecases.MainScreenTasksUseCases
import com.example.talkmy.ui.core.ActionHandler
import com.example.talkmy.ui.core.DefaultStateContainer
import com.example.talkmy.ui.core.StateContainer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainState(
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = false
)

sealed class MainAction {
    object LoadTasks : MainAction()
    data class DeleteTask(val id: Int) : MainAction()
    object ClearAll : MainAction()
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val useCases: MainScreenTasksUseCases
) : ViewModel(),
    ActionHandler<MainAction>,
    StateContainer<MainState> by DefaultStateContainer(MainState()) {

    init {
        onAction(MainAction.LoadTasks)
    }

    override fun onAction(action: MainAction) {
        when (action) {
            MainAction.LoadTasks -> loadTasks()
            is MainAction.DeleteTask -> deleteTask(action.id)
            MainAction.ClearAll -> clearAllTasks()
        }
    }

    private fun loadTasks() {
        viewModelScope.launch {
            updateState { copy(isLoading = true) }
            useCases.getTasks().collect { taskList ->
                updateState { copy(tasks = taskList, isLoading = false) }
            }
        }
    }

    private fun deleteTask(id: Int) {
        viewModelScope.launch {
            useCases.deleteTask(id)
        }
    }

    private fun clearAllTasks() {
        viewModelScope.launch {
            useCases.clearTasks()
        }
    }
}
