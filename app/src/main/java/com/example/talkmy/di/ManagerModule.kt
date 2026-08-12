package com.example.talkmy.di

import com.example.talkmy.data.source.tts.TTSManagerImpl
import com.example.talkmy.domain.interfaces.TTSManagerInterface
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ManagerModule {

    @Binds
    @Singleton
    abstract fun bindTTSManager(impl: TTSManagerImpl): TTSManagerInterface
}