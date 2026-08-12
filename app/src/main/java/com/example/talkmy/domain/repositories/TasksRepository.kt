package com.example.talkmy.domain.repositories

import com.example.talkmy.domain.models.Task
import kotlinx.coroutines.flow.Flow

interface TasksRepository {
    suspend fun getTasksFromLocal(): Flow<List<Task>>
    suspend fun uploadTaskLocal(task:Task): String?
    suspend fun clearTasks()
    suspend fun getTask(id: Int): Task?
    suspend fun deleteTask(id: Int)
}