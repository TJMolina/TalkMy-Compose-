package com.example.talkmy.domain.usecases.tasks

import com.example.talkmy.domain.models.Task
import com.example.talkmy.domain.repositories.TasksRepository
import javax.inject.Inject

class GetTask @Inject constructor(
    private val tasksRepository: TasksRepository
) {
    suspend operator fun invoke(id: Int): Task? {
        return tasksRepository.getTask(id)
    }
}