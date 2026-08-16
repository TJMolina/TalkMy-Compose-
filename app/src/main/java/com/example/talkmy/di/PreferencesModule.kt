package com.example.talkmy.di

import android.content.Context
import com.example.talkmy.data.repository.AllUserPreferencesRepositoryImp
import com.example.talkmy.data.source.preferences.PreferenceStoreImp
import com.example.talkmy.domain.repositories.AllUserPreferencesRepository
import com.example.talkmy.domain.interfaces.PreferenceStore
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
        return PreferenceStoreImp(context)
    }

    @Provides
    @Singleton
    fun provideUserPreferences(preferenceStore: PreferenceStore): AllUserPreferencesRepository {
        return AllUserPreferencesRepositoryImp(preferenceStore)
    }
}
