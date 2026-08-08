package com.example.talkmy.ui.navigation

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object EditTask : Screen("edit_task?taskId={taskId}") {
        fun createRoute(taskId: Int? = null) = "edit_task?taskId=$taskId"
    }
}
