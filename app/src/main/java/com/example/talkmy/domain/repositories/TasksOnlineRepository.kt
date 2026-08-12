package com.example.talkmy.domain.repositories

interface TasksOnlineRepository {
    suspend fun getTextFromUrls(url: String): String?
}