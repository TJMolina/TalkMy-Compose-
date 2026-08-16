package com.example.talkmy.domain.repositories

import com.example.talkmy.domain.models.UserPreference

interface AllUserPreferencesRepository {
    fun set(type: UserPreference<*>)
    fun loadAll(): List<UserPreference<*>>
}