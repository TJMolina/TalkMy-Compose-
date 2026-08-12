package com.example.talkmy.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.talkmy.data.database.entities.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TasksDao {
    @Query("SELECT * FROM tasks_table ORDER BY date DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks_table WHERE id = :taskId")
    suspend fun getTask(taskId: Int): TaskEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Query("DELETE FROM tasks_table")
    suspend fun deleteAllTasks()

    @Query("DELETE FROM tasks_table WHERE id = :taskId")
    suspend fun deleteTask(taskId: Int)
}