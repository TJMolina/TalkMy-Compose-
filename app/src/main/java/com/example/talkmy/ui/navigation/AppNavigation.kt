package com.example.talkmy.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.talkmy.ui.components.WebViewManager
import com.example.talkmy.ui.features.edittask.EditTaskScreen
import com.example.talkmy.ui.features.mainscreen.MainScreen

@Composable
fun AppNavigation(webViewManager: WebViewManager) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Main.route
    ) {
        composable(Screen.Main.route) {
            MainScreen(
                onAddTask = {
                    navController.navigate(Screen.EditTask.createRoute())
                },
                onEditTask = { taskId ->
                    navController.navigate(Screen.EditTask.createRoute(taskId))
                }
            )
        }
        composable(
            route = Screen.EditTask.route,
            arguments = listOf(
                navArgument("taskId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val taskIdString = backStackEntry.arguments?.getString("taskId")
            val taskId = taskIdString?.toIntOrNull()
            
            EditTaskScreen(
                taskId = taskId,
                webViewManager = webViewManager,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
