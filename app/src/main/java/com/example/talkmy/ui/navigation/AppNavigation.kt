package com.example.talkmy.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.talkmy.ui.features.edittask.EditTaskScreen
import com.example.talkmy.ui.features.mainscreen.MainScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background) {
        NavHost(
            navController = navController,
            startDestination = Screen.Main.route,
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
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
