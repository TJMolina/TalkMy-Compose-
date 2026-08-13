package com.example.talkmy.ui.features.edittask

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.talkmy.core.ResponseState
import com.example.talkmy.domain.interfaces.TTSManagerInterface
import com.example.talkmy.domain.models.Task
import com.example.talkmy.domain.usecases.EditTaskScreenTasksUseCases
import com.example.talkmy.ui.core.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditTaskState(
    val contentLoadingStatus: ResponseState<String>? = null,
    val currentTextOnTextField: String = "",
    val isPlaying: Boolean = false,
    val progress: Float = 0f
)

sealed class EditTaskAction {
    data class LoadTask(val id: String?) : EditTaskAction()
    data class SaveTask(val noteText: String) : EditTaskAction()
    data class FetchTextFromUrl(val url: String) : EditTaskAction()
    data class UpdateNoteText(val text: String) : EditTaskAction()
    object ToggleVoice : EditTaskAction()
    object StopVoice : EditTaskAction()
}

sealed class EditTaskEffect {
    object NavigateBack : EditTaskEffect()
    data class ShowMessage(val message: String) : EditTaskEffect()
}

@HiltViewModel
class EditTaskViewModel @Inject constructor(
    private val useCases: EditTaskScreenTasksUseCases,
    private val ttsManager: TTSManagerInterface
) : ViewModel(),
    ActionHandler<EditTaskAction>,
    StateContainer<EditTaskState> by DefaultStateContainer(EditTaskState()),
    EffectContainer<EditTaskEffect> by DefaultEffectContainer() {

    private val fetchQuery = SingleActiveQuery()
    private var task: Task? = null
    override fun onAction(action: EditTaskAction) {
        when (action) {
            is EditTaskAction.LoadTask -> loadTask(action.id)
            is EditTaskAction.SaveTask -> saveTask(action.noteText)
            is EditTaskAction.FetchTextFromUrl -> fetchTextFromUrl(action.url)
            is EditTaskAction.UpdateNoteText -> updateState { copy(currentTextOnTextField = action.text) }
            EditTaskAction.ToggleVoice -> toggleVoice()
            EditTaskAction.StopVoice -> stopVoice()
        }
    }

    private fun loadTask(id: String?) {
        val taskId = id?.toIntOrNull() ?: return
        viewModelScope.launch {
            val loadedTask = useCases.getTask(taskId)
            task = loadedTask
            updateState { copy(currentTextOnTextField = loadedTask?.note ?: "") }
        }
    }

    private fun saveTask(noteText: String) {
        viewModelScope.launch {
            val currentTask = task?.copy(note = noteText) ?: Task(note = noteText)
            useCases.uploadTask(currentTask)
            postEffect { EditTaskEffect.NavigateBack }
        }
    }

    private fun fetchTextFromUrl(url: String) {
        viewModelScope.launch {
            fetchQuery.launch {
                useCases.getTextFromUrl(url).collect { res ->
                    updateState { copy(contentLoadingStatus = res) }
                    if (res is ResponseState.Success) {
                        updateState { copy(currentTextOnTextField = res.data ?: "") }
                    }
                }
            }
        }
    }

    private fun toggleVoice() {
        val isPlaying = !state.value.isPlaying
        updateState { copy(isPlaying = isPlaying) }
        if (isPlaying) {
            ttsManager.speak(state.value.currentTextOnTextField)
        } else {
            ttsManager.stop()
        }
    }

    private fun stopVoice() {
        updateState { copy(isPlaying = false) }
        ttsManager.stop()
    }

    override fun onCleared() {
        ttsManager.release()
    }
}
