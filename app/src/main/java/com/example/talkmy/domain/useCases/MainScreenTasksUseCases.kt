package com.example.talkmy.domain.usecases

import com.example.talkmy.domain.usecases.tasks.ClearTasks
import com.example.talkmy.domain.usecases.tasks.DeleteTask
import com.example.talkmy.domain.usecases.tasks.GetTask
import com.example.talkmy.domain.usecases.tasks.GetTasks

data class MainScreenTasksUseCases(
    val getTasks: GetTasks,
    val getTask: GetTask,
    val deleteTask: DeleteTask,
    val clearTasks: ClearTasks
)