package com.example.talkmy.domain.usecases.online

import com.example.talkmy.core.ResponseState
import com.example.talkmy.core.extensions.separateSentencesInsertPTagWeb
import com.example.talkmy.core.extensions.translateHTMLtoPlain
import com.example.talkmy.domain.repositories.TasksOnlineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetTextFromUrl @Inject constructor(
    private val tasksOnlineRepository: TasksOnlineRepository
) {
    suspend operator fun invoke(url: String): Flow<ResponseState<String>> = flow {
        emit(ResponseState.Loading())
        val textHTML = tasksOnlineRepository.getTextFromUrls(url)
        if (!textHTML.isNullOrEmpty()) {
            emit(ResponseState.Success(textHTML.translateHTMLtoPlain().separateSentencesInsertPTagWeb()))
        } else {
            emit(ResponseState.Error("Error receiving text."))
        }
    }
}