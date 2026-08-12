package com.example.talkmy.domain.usecases

import com.example.talkmy.domain.usecases.online.GetTextFromUrl
import com.example.talkmy.domain.usecases.tasks.GetTask
import com.example.talkmy.domain.usecases.tasks.UploadTask

data class EditTaskScreenTasksUseCases(
    val getTask: GetTask,
    val uploadTask: UploadTask,
    val getTextFromUrl: GetTextFromUrl
)