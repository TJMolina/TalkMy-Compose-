package com.example.talkmy.data.repository

import com.example.talkmy.domain.repositories.TasksOnlineRepository
import com.lagradost.nicehttp.Requests
import javax.inject.Inject

class TasksOnlineRepositoryImpl @Inject constructor(
    private val requests: Requests
) : TasksOnlineRepository {
    override suspend fun getTextFromUrls(url: String): String? {
        return try {
            val response = requests.get(url)
            if (response.isSuccessful) {
                response.document.body().html()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}