package com.example.talkmy.ui.features.edittask

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.talkmy.core.ResponseState
import com.example.talkmy.domain.interfaces.TTSManagerInterface
import com.example.talkmy.domain.models.UserPreference
import com.example.talkmy.domain.models.Task
import com.example.talkmy.domain.usecases.EditTaskScreenTasksUseCases
import com.example.talkmy.ui.core.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.reflect.KClass


sealed class EditTaskDialog {
    object Url : EditTaskDialog()
    object TextOptions : EditTaskDialog()
    object VoicePreferences : EditTaskDialog()
}
data class EditTaskState(
    val contentLoadingStatus: ResponseState<String>? = null,
    val currentTextOnTextField: String = "",
    val isPlaying: Boolean = false,
    val progress: Float = 0f,
    val activeDialog: EditTaskDialog? = null,
    val preferences: Map<KClass<out UserPreference<*>>, UserPreference<*>> =
        UserPreference.allDefaults.associateBy { it::class }
)

sealed class EditTaskAction {
    data class LoadTask(val id: String?) : EditTaskAction()
    data class SaveTask(val noteText: String) : EditTaskAction()
    data class FetchTextFromUrl(val url: String) : EditTaskAction()
    data class UpdateNoteText(val text: String) : EditTaskAction()
    data class UpdatePreference(val type: UserPreference<*>) : EditTaskAction()
    data class ShowDialog(val dialog: EditTaskDialog) : EditTaskAction()
    object DismissDialog : EditTaskAction()
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

    init {
        // Observe preferences reactively
        viewModelScope.launch {
            updateState { copy(preferences = useCases.getPreferences().associateBy { it::class }) }
        }
    }

    override fun onAction(action: EditTaskAction) {
        when (action) {
            is EditTaskAction.LoadTask -> loadTask(action.id)
            is EditTaskAction.SaveTask -> saveTask(action.noteText)
            is EditTaskAction.FetchTextFromUrl -> fetchTextFromUrl(action.url)
            is EditTaskAction.UpdateNoteText -> updateState { copy(currentTextOnTextField = action.text) }
            is EditTaskAction.UpdatePreference -> updatePreference(action.type)
            is EditTaskAction.ShowDialog -> updateState { copy(activeDialog = action.dialog) }
            EditTaskAction.DismissDialog -> updateState { copy(activeDialog = null) }
            EditTaskAction.ToggleVoice -> toggleVoice()
            EditTaskAction.StopVoice -> stopVoice()
        }
    }

    private fun updatePreference(type: UserPreference<*>) {
        updateState {
            copy(preferences = preferences + (type::class to type))
        }
        useCases.savePreference(type)
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
