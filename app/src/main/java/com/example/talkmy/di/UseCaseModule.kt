package com.example.talkmy.di

import com.example.talkmy.domain.usecases.*
import com.example.talkmy.domain.usecases.online.GetTextFromUrl
import com.example.talkmy.domain.usecases.tasks.ClearTasks
import com.example.talkmy.domain.usecases.tasks.DeleteTask
import com.example.talkmy.domain.usecases.tasks.GetTask
import com.example.talkmy.domain.usecases.tasks.GetTasks
import com.example.talkmy.domain.usecases.tasks.UploadTask
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideMainScreenTasksUseCases(
        getTasks: GetTasks,
        getTask: GetTask,
        deleteTask: DeleteTask,
        clearTasks: ClearTasks,
    ): MainScreenTasksUseCases {
        return MainScreenTasksUseCases(
            getTasks = getTasks,
            getTask = getTask,
            deleteTask = deleteTask,
            clearTasks = clearTasks,
        )
    }

    @Provides
    @Singleton
    fun provideEditTaskScreenTasksUseCases(
        getTask: GetTask,
        uploadTask: UploadTask,
        getTextFromUrl: GetTextFromUrl
    ): EditTaskScreenTasksUseCases {
        return EditTaskScreenTasksUseCases(
            getTask = getTask,
            uploadTask = uploadTask,
            getTextFromUrl = getTextFromUrl
        )
    }
}