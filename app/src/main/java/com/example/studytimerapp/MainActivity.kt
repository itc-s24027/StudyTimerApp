package com.example.studytimerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.studytimerapp.ui.StudyTaskViewModel
import com.example.studytimerapp.ui.TaskListScreen
import com.example.studytimerapp.ui.TimerScreen
import com.example.studytimerapp.ui.theme.StudyTimerAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StudyTimerAppTheme {
                StudyTimerApp()
            }
        }
    }
}

@Composable
fun StudyTimerApp(viewModel: StudyTaskViewModel = viewModel()) {
    val navController = rememberNavController()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "task_list",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("task_list") {
                TaskListScreen(
                    viewModel = viewModel,
                    onStartTimer = { taskId ->
                        navController.navigate("timer/$taskId")
                    }
                )
            }
            composable(
                route = "timer/{taskId}",
                arguments = listOf(navArgument("taskId") { type = NavType.IntType })
            ) { backStackEntry ->
                val taskId = backStackEntry.arguments?.getInt("taskId") ?: 0
                TimerScreen(
                    taskId = taskId,
                    viewModel = viewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
