package com.example.talkmy.ui.features.edittask

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.talkmy.core.ResponseState
import com.example.talkmy.domain.interfaces.TTSManagerInterface
import com.example.talkmy.domain.models.Task
import com.example.talkmy.domain.usecases.EditTaskScreenTasksUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditTaskViewModel @Inject constructor(
    private val useCases: EditTaskScreenTasksUseCases,
    private val ttsManager: TTSManagerInterface
) : ViewModel() {

    private val _task = MutableStateFlow<Task?>(null)
    val task: StateFlow<Task?> = _task.asStateFlow()

    private val _urlTextState = MutableStateFlow<ResponseState<String>>(ResponseState.Loading())
    val urlTextState: StateFlow<ResponseState<String>> = _urlTextState.asStateFlow()

    fun loadTask(id: String?) {
        val taskId = id?.toIntOrNull() ?: return
        viewModelScope.launch {
            _task.value = useCases.getTask(taskId)
        }
    }

    fun saveTask(noteText: String) {
        viewModelScope.launch {
            val currentTask = _task.value?.copy(note = noteText) ?: Task(note = noteText)
            useCases.uploadTask(currentTask)
        }
    }

    fun fetchTextFromUrl(url: String) {
        viewModelScope.launch {
            useCases.getTextFromUrl(url).collect {
                _urlTextState.value = it
            }
        }
    }

    fun speak(text: String) {
        ttsManager.speak(text)
    }

    fun stopSpeaking() {
        ttsManager.stop()
    }

    override fun onCleared() {
        ttsManager.release()
    }
}