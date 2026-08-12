package com.example.talkmy.di

import android.content.Context
import com.example.talkmy.data.source.preferences.AndroidPreferenceStore
import com.example.talkmy.domain.repositories.PreferenceStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PreferencesModule {

    @Provides
    @Singleton
    fun providePreferenceStore(@ApplicationContext context: Context): PreferenceStore {
        return AndroidPreferenceStore(context)
    }
}
