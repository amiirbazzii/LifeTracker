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
import com.example.ui.GoalHubScreen
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
                val categories by viewModel.allCategories.collectAsState()
                val routines by viewModel.allRoutines.collectAsState()

                when (currentScreen) {
                    "goal_input" -> {
                        GoalInputScreen(
                            currentGoal = userGoal,
                            onSave = { newGoal ->
                                viewModel.saveUserGoal(newGoal)
                                currentScreen = "goal_hub"
                            },
                            onBack = { currentScreen = "goal_hub" },
                            onReset = {
                                viewModel.resetTimeline()
                                currentScreen = "dashboard"
                            }
                        )
                    }
                    "goal_hub" -> {
                        val userPoints by viewModel.userPoints.collectAsState()
                        val rewards by viewModel.allRewards.collectAsState()

                        GoalHubScreen(
                            grandGoal = userGoal,
                            userPoints = userPoints,
                            categories = categories,
                            routines = routines,
                            rewards = rewards,
                            onCreateCategory = { name -> viewModel.onCreateCategory(name) },
                            onCreateRoutine = { catId, title, target -> viewModel.onCreateRoutine(catId, title, target) },
                            onAddReward = { name, cost -> viewModel.onAddReward(name, cost) },
                            onClaimReward = { reward -> viewModel.onClaimReward(reward) },
                            onEditGrandGoal = { currentScreen = "goal_input" },
                            onBack = { currentScreen = "dashboard" }
                        )
                    }
                    else -> {
                        DashboardScreen(
                            state = uiState,
                            userGoal = userGoal,
                            categories = categories,
                            routines = routines,
                            onEditGoalClick = { currentScreen = "goal_hub" },
                            onSelectWeek = { weekIndex -> viewModel.selectWeek(weekIndex) },
                            onAddTask = { title, weekIndex, day, routineId -> viewModel.addTask(title, weekIndex, day, routineId) },
                            onToggleTask = { task -> viewModel.toggleTaskCompletion(task) },
                            onDeleteTask = { taskId -> viewModel.deleteTask(taskId) },
                            onReset = { viewModel.resetTimeline() },
                            onIncrementRoutineCompletion = { routineId -> viewModel.incrementRoutineCompletion(routineId) }
                        )
                    }
                }
            }
        }
    }
}
