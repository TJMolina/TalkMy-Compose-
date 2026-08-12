package com.example.talkmy.domain.usecases.tasks

import com.example.talkmy.domain.models.Task
import com.example.talkmy.domain.repositories.TasksRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTasks @Inject constructor(
    private val tasksRepository: TasksRepository
) {
    suspend operator fun invoke(): Flow<List<Task>> {
        return tasksRepository.getTasksFromLocal()
    }
}