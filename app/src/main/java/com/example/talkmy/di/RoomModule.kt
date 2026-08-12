package com.example.talkmy.di

import android.content.Context
import androidx.room.Room
import com.example.talkmy.data.database.TaskDatabase
import com.example.talkmy.data.database.dao.TasksDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RoomModule {
    private const val TASK_DATABASE_NAME = "task_Database"

    @Singleton
    @Provides
    fun provideRoom(@ApplicationContext context: Context): TaskDatabase =
        Room.databaseBuilder(context, TaskDatabase::class.java, TASK_DATABASE_NAME).build()

    @Singleton
    @Provides
    fun provideTaskDao(db: TaskDatabase): TasksDao = db.getTaskDao()
}