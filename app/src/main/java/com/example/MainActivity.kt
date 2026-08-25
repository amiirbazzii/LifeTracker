package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.content.ContextCompat
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
                val context = LocalContext.current
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { _ -> }

                LaunchedEffect(Unit) {
                    com.example.util.TaskNotificationScheduler.ensureNotificationChannel(context)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    val app = LocalContext.current.applicationContext as android.app.Application
                    val viewModel: LifeTrackerViewModel = viewModel(
                        factory = LifeTrackerViewModel.Factory(app)
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
                    },
                    onSkip = {
                        viewModel.initializeTimeline(5, "")
                    }
                )
            }
            is LifeTrackerUiState.Dashboard -> {
                val categories by viewModel.allCategories.collectAsStateWithLifecycle()
                val routines by viewModel.allRoutines.collectAsStateWithLifecycle()
                val subGoals by viewModel.allSubGoals.collectAsStateWithLifecycle()
                val habits by viewModel.allDailyHabits.collectAsStateWithLifecycle()
                val allTasks by viewModel.allTasks.collectAsStateWithLifecycle()

                when (currentScreen) {
                    "onboarding" -> {
                        OnboardingScreen(
                            initialGoal = userGoal,
                            initialYears = uiState.meta.targetYears,
                            initialStep = 1,
                            canDismiss = true,
                            onInitialize = { years, goal ->
                                viewModel.initializeTimeline(years, goal)
                                currentScreen = "dashboard"
                            },
                            onSkip = {
                                currentScreen = "dashboard"
                            }
                        )
                    }
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
                        val userPoints by viewModel.userPoints.collectAsStateWithLifecycle()
                        val rewards by viewModel.allRewards.collectAsStateWithLifecycle()

                        GoalHubScreen(
                            grandGoal = userGoal,
                            targetYears = uiState.meta.targetYears,
                            userPoints = userPoints,
                            categories = categories,
                            routines = routines,
                            rewards = rewards,
                            subGoals = subGoals,
                            habits = habits,
                            tasks = allTasks,
                            onCreateCategory = { name -> viewModel.onCreateCategory(name) },
                            onCreateRoutine = { catId, title, target -> viewModel.onCreateRoutine(catId, title, target) },
                            onAddReward = { name, cost -> viewModel.onAddReward(name, cost) },
                            onClaimReward = { reward -> viewModel.onClaimReward(reward) },
                            onCreateSubGoal = { title, duration -> viewModel.onCreateSubGoal(title, duration) },
                            onUpdateSubGoal = { id, title, duration -> viewModel.onUpdateSubGoal(id, title, duration) },
                            onDeleteSubGoal = { id -> viewModel.onDeleteSubGoal(id) },
                            onUpdateCategory = { id, name -> viewModel.onUpdateCategory(id, name) },
                            onDeleteCategory = { id -> viewModel.onDeleteCategory(id) },
                            onUpdateRoutine = { id, title, target -> viewModel.onUpdateRoutine(id, title, target) },
                            onDeleteRoutine = { id -> viewModel.onDeleteRoutine(id) },
                            onCreateHabit = { title -> viewModel.onCreateHabit(title) },
                            onUpdateHabit = { id, title -> viewModel.onUpdateHabit(id, title) },
                            onDeleteHabit = { id -> viewModel.onDeleteHabit(id) },
                            onEditGrandGoal = { currentScreen = "onboarding" },
                            onBack = { currentScreen = "dashboard" },
                            onSaveGrandGoal = { newGoal -> viewModel.saveUserGoal(newGoal) },
                            onReset = {
                                viewModel.resetTimeline()
                                currentScreen = "dashboard"
                            }
                        )
                    }
                    else -> {
                        DashboardScreen(
                            state = uiState,
                            userGoal = userGoal,
                            categories = categories,
                            routines = routines,
                            subGoals = subGoals,
                            onEditGoalClick = {
                                if (userGoal.isNotBlank()) {
                                    currentScreen = "goal_hub"
                                } else {
                                    currentScreen = "onboarding"
                                }
                            },
                            onSelectWeek = { weekIndex -> viewModel.selectWeek(weekIndex) },
                            onAddTask = { title, weekIndex, day, routineId -> viewModel.addTask(title, weekIndex, day, routineId) },
                            onToggleTask = { task -> viewModel.toggleTaskCompletion(task) },
                            onDeleteTask = { taskId -> viewModel.deleteTask(taskId) },
                            onReset = { viewModel.resetTimeline() },
                            onIncrementRoutineCompletion = { routineId -> viewModel.incrementRoutineCompletion(routineId) },
                            onUpdateTaskTimer = { task, start, end -> viewModel.updateTaskTimer(task, start, end) }
                        )
                    }
                }
            }
        }
    }
}
