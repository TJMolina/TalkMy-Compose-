package com.example.talkmy.data.repository

import com.example.talkmy.data.database.dao.TasksDao
import com.example.talkmy.data.database.entities.toDatabase
import com.example.talkmy.data.database.entities.toDomain
import com.example.talkmy.domain.models.Task
import com.example.talkmy.domain.repositories.TasksRepository
import com.orhanobut.logger.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val tasksDao: TasksDao
) : TasksRepository {
    override suspend fun getTasksFromLocal(): Flow<List<Task>> {
        return tasksDao.getAllTasks().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun uploadTaskLocal(task: Task): String? {
        return try {
            tasksDao.insertTask(task.toDatabase())
            null
        } catch (e: Exception) {
            Logger.e("TaskRepositoryImpl: Error uploading task", e)
            e.message
        }
    }

    override suspend fun clearTasks() {
        try {
            tasksDao.deleteAllTasks()
        } catch (e: Exception) {
            Logger.e("TaskRepositoryImpl: Error clearing tasks", e)
        }
    }

    override suspend fun getTask(id: Int): Task? {
        return try {
            tasksDao.getTask(id).toDomain()
        } catch (e: Exception) {
            Logger.e("TaskRepositoryImpl: Error getting task", e)
            null
        }
    }

    override suspend fun deleteTask(id: Int) {
        try {
            tasksDao.deleteTask(id)
        } catch (e: Exception) {
            Logger.e("TaskRepositoryImpl: Error deleting task", e)
        }
    }
}