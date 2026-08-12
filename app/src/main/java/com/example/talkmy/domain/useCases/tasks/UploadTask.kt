package com.example.talkmy.domain.usecases.tasks

import com.example.talkmy.domain.models.Task
import com.example.talkmy.domain.repositories.TasksRepository
import javax.inject.Inject

class UploadTask @Inject constructor(private val tasksRepository: TasksRepository) {
    suspend operator fun invoke(task: Task) = tasksRepository.uploadTaskLocal(task)
}