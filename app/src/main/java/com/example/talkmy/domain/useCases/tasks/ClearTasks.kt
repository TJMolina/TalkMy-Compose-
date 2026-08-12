package com.example.talkmy.domain.usecases.tasks

import com.example.talkmy.domain.repositories.TasksRepository
import javax.inject.Inject

class ClearTasks @Inject constructor(
    private val repository: TasksRepository
) {
    suspend operator fun invoke() = repository.clearTasks()
}