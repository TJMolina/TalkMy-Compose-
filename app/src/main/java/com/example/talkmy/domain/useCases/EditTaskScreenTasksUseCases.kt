package com.example.talkmy.domain.usecases

import com.example.talkmy.domain.usecases.online.GetTextFromUrl
import com.example.talkmy.domain.usecases.preferences.GetPreferences
import com.example.talkmy.domain.usecases.preferences.SavePreference
import com.example.talkmy.domain.usecases.tasks.GetTask
import com.example.talkmy.domain.usecases.tasks.UploadTask

data class EditTaskScreenTasksUseCases(
    val getTask: GetTask,
    val uploadTask: UploadTask,
    val getTextFromUrl: GetTextFromUrl,
    val getPreferences: GetPreferences,
    val savePreference: SavePreference
)
