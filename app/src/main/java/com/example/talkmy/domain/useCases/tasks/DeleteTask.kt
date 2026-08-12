package com.example.talkmy.domain.usecases.tasks

import com.example.talkmy.domain.repositories.TasksRepository
import javax.inject.Inject

class DeleteTask @Inject constructor(
    private val tasksRepository: TasksRepository
) {
    suspend operator fun invoke(id: Int) = tasksRepository.deleteTask(id)
}