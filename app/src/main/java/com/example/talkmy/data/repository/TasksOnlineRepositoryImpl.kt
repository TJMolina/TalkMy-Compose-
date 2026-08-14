package com.example.talkmy.data.repository

import com.example.talkmy.domain.repositories.TasksOnlineRepository
import com.lagradost.nicehttp.NiceResponse
import com.lagradost.nicehttp.Requests
import javax.inject.Inject

class TasksOnlineRepositoryImpl @Inject constructor(
    private val requests: Requests
) : TasksOnlineRepository {
    override suspend fun getTextFromUrls(url: String): String? {
        var response: NiceResponse? = null
        return try {
            response = requests.get(url)
            if (response.isSuccessful) {
                response.document.body().apply {
                    select("script, style, noscript, iframe, nav, footer, header").remove()
                }.html()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        } finally {
            response?.okhttpResponse?.close()
        }
    }
}