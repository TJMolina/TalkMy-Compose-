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
                val html = response.document.body().apply {
                    select("script, style, noscript, iframe, nav, footer, header").remove()
                }.html()
                response.okhttpResponse.close()
                html
            } else {
                response.okhttpResponse.close()
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}