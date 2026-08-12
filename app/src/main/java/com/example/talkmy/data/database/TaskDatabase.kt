package com.example.talkmy.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.talkmy.data.database.dao.TasksDao
import com.example.talkmy.data.database.entities.TaskEntity

@Database(entities = [TaskEntity::class], version = 1, exportSchema = false)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun getTaskDao(): TasksDao
}