package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.DashboardScreen
import com.example.ui.GoalInputScreen
import com.example.ui.LifeTrackerUiState
import com.example.ui.LifeTrackerViewModel
import com.example.ui.OnboardingScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    val context = LocalContext.current.applicationContext as android.app.Application
                    val viewModel: LifeTrackerViewModel = viewModel(
                        factory = LifeTrackerViewModel.Factory(context)
                    )
                    LifeTrackerApp(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun LifeTrackerApp(
    viewModel: LifeTrackerViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val userGoal by viewModel.userGoal.collectAsStateWithLifecycle()
    var currentScreen by remember { mutableStateOf("dashboard") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (val uiState = state) {
            is LifeTrackerUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            is LifeTrackerUiState.Onboarding -> {
                OnboardingScreen(
                    onInitialize = { years, goal ->
                        viewModel.initializeTimeline(years, goal)
                    }
                )
            }
            is LifeTrackerUiState.Dashboard -> {
                if (currentScreen == "goal_input") {
                    GoalInputScreen(
                        currentGoal = userGoal,
                        onSave = { newGoal ->
                            viewModel.saveUserGoal(newGoal)
                            currentScreen = "dashboard"
                        },
                        onBack = { currentScreen = "dashboard" },
                        onReset = {
                            viewModel.resetTimeline()
                            currentScreen = "dashboard"
                        }
                    )
                } else {
                    DashboardScreen(
                        state = uiState,
                        userGoal = userGoal,
                        onEditGoalClick = { currentScreen = "goal_input" },
                        onSelectWeek = { weekIndex -> viewModel.selectWeek(weekIndex) },
                        onAddTask = { title, weekIndex, day -> viewModel.addTask(title, weekIndex, day) },
                        onToggleTask = { task -> viewModel.toggleTaskCompletion(task) },
                        onDeleteTask = { taskId -> viewModel.deleteTask(taskId) },
                        onReset = { viewModel.resetTimeline() }
                    )
                }
            }
        }
    }
}
