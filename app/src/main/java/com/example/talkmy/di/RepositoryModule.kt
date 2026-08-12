package com.example.talkmy.di

import com.example.talkmy.data.repository.TaskRepositoryImpl
import com.example.talkmy.data.repository.TasksOnlineRepositoryImpl
import com.example.talkmy.domain.repositories.TasksOnlineRepository
import com.example.talkmy.domain.repositories.TasksRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTasksRepository(impl: TaskRepositoryImpl): TasksRepository

    @Binds
    @Singleton
    abstract fun bindTasksOnlineRepository(impl: TasksOnlineRepositoryImpl): TasksOnlineRepository
}