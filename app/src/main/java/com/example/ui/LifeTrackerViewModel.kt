package com.example.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.AppDatabase
import com.example.data.DailyTask
import com.example.data.LifeTrackerRepository
import com.example.data.TimelineMeta
import com.example.data.Category
import com.example.data.Routine
import com.example.data.Reward
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.content.Context
import java.util.UUID

class LifeTrackerViewModel(private val repository: LifeTrackerRepository, private val application: Application) : ViewModel() {

    private val prefs = application.getSharedPreferences("life_tracker_prefs", Context.MODE_PRIVATE)

    private val _userGoal = MutableStateFlow(prefs.getString("user_goal", "") ?: "")
    val userGoal = _userGoal.asStateFlow()

    private val _userPoints = MutableStateFlow(prefs.getInt("user_points", 0))
    val userPoints = _userPoints.asStateFlow()

    val allCategories: StateFlow<List<Category>> = repository.allCategories.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allRoutines: StateFlow<List<Routine>> = repository.allRoutines.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allRewards: StateFlow<List<Reward>> = repository.allRewards.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun saveUserGoal(goal: String) {
        _userGoal.value = goal
        prefs.edit().putString("user_goal", goal).apply()
    }

    fun addPoints(points: Int) {
        val newPoints = _userPoints.value + points
        _userPoints.value = newPoints
        prefs.edit().putInt("user_points", newPoints).apply()
    }

    fun deductPoints(points: Int): Boolean {
        val current = _userPoints.value
        if (current >= points) {
            val newPoints = current - points
            _userPoints.value = newPoints
            prefs.edit().putInt("user_points", newPoints).apply()
            return true
        }
        return false
    }

    fun onCreateCategory(name: String) {
        viewModelScope.launch {
            val category = Category(
                id = UUID.randomUUID().toString(),
                name = name,
                createdTimestamp = System.currentTimeMillis()
            )
            repository.insertCategory(category)
        }
    }

    fun onCreateRoutine(catId: String, title: String, target: Int) {
        viewModelScope.launch {
            val routine = Routine(
                id = UUID.randomUUID().toString(),
                categoryId = catId,
                title = title,
                targetCount = target,
                completedCount = 0,
                createdTimestamp = System.currentTimeMillis()
            )
            repository.insertRoutine(routine)
        }
    }

    fun onAddReward(name: String, cost: Int) {
        viewModelScope.launch {
            val reward = Reward(
                id = UUID.randomUUID().toString(),
                name = name,
                pointCost = cost,
                claimedCount = 0,
                createdTimestamp = System.currentTimeMillis()
            )
            repository.insertReward(reward)
        }
    }

    fun onClaimReward(reward: Reward) {
        viewModelScope.launch {
            if (deductPoints(reward.pointCost)) {
                val updated = reward.copy(claimedCount = reward.claimedCount + 1)
                repository.updateReward(updated)
            }
        }
    }

    fun incrementRoutineCompletion(routineId: String) {
        viewModelScope.launch {
            val routines = repository.allRoutines.first()
            val routine = routines.find { it.id == routineId }
            if (routine != null) {
                val updated = routine.copy(completedCount = routine.completedCount + 1)
                repository.updateRoutine(updated)
                addPoints(100) // Achieving a milestone = +100 Points
            }
        }
    }

    private val _selectedWeekIndex = MutableStateFlow<Int?>(null)
    val selectedWeekIndex = _selectedWeekIndex.asStateFlow()

    val uiState: StateFlow<LifeTrackerUiState> = combine(
        repository.timelineMeta,
        repository.allTasks,
        _selectedWeekIndex
    ) { meta, tasks, selectedWeek ->
        if (meta == null) {
            LifeTrackerUiState.Onboarding
        } else {
            val totalWeeks = meta.totalWeeks
            val inceptionTimestamp = meta.inceptionTimestamp

            // Calculate current week index since inception
            val elapsedMillis = System.currentTimeMillis() - inceptionTimestamp
            val weekInMillis = 7 * 24 * 60 * 60 * 1000L
            val computedWeek = (elapsedMillis / weekInMillis).toInt()
            val currentWeek = computedWeek.coerceIn(0, totalWeeks - 1)

            // Selected week defaults to current active week if not set
            val activeSelectedWeek = selectedWeek ?: currentWeek

            // Group tasks by week to calculate success rates globally
            val tasksGroupedByWeek = tasks.groupBy { it.weekIndex }
            val weekColors = (0 until totalWeeks).associateWith { wIndex ->
                val weekTasks = tasksGroupedByWeek[wIndex] ?: emptyList()
                val totalCreated = weekTasks.size
                if (totalCreated == 0) {
                    0
                } else {
                    val completed = weekTasks.count { it.isCompleted == 1 }
                    val sr = (completed.toFloat() / totalCreated.toFloat()) * 100f
                    when {
                        sr == 0f -> 0
                        sr < 33f -> 1
                        sr < 66f -> 2
                        sr < 100f -> 3
                        else -> 4
                    }
                }
            }

            // Tasks corresponding to the currently selected week index
            val selectedWeekTasks = tasksGroupedByWeek[activeSelectedWeek] ?: emptyList()
            val currentWeekTasks = tasksGroupedByWeek[currentWeek] ?: emptyList()

            LifeTrackerUiState.Dashboard(
                meta = meta,
                currentWeekIndex = currentWeek,
                selectedWeekIndex = activeSelectedWeek,
                weekColors = weekColors,
                selectedWeekTasks = selectedWeekTasks,
                currentWeekTasks = currentWeekTasks
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LifeTrackerUiState.Loading
    )

    fun initializeTimeline(years: Int, goal: String) {
        viewModelScope.launch {
            val totalWeeks = years * 52
            val meta = TimelineMeta(
                targetYears = years,
                totalWeeks = totalWeeks,
                inceptionTimestamp = System.currentTimeMillis()
            )
            repository.saveTimeline(meta)
            saveUserGoal(goal)
        }
    }

    fun selectWeek(index: Int) {
        _selectedWeekIndex.value = index
    }

    fun addTask(title: String, weekIndex: Int, dayOfWeek: Int, routineId: String? = null) {
        viewModelScope.launch {
            val task = DailyTask(
                taskId = UUID.randomUUID().toString(),
                weekIndex = weekIndex,
                dayOfWeek = dayOfWeek,
                taskTitle = title,
                isCompleted = 0,
                createdTimestamp = System.currentTimeMillis(),
                routineId = routineId
            )
            repository.insertTask(task)
        }
    }

    fun toggleTaskCompletion(task: DailyTask) {
        viewModelScope.launch {
            val willBeCompleted = task.isCompleted == 0
            val updated = task.copy(isCompleted = if (willBeCompleted) 1 else 0)
            repository.updateTask(updated)
            
            val pointsDiff = if (task.routineId != null) 15 else 10
            if (willBeCompleted) {
                addPoints(pointsDiff)
            } else {
                // Deduct but don't go below 0
                val current = _userPoints.value
                val newPoints = (current - pointsDiff).coerceAtLeast(0)
                _userPoints.value = newPoints
                prefs.edit().putInt("user_points", newPoints).apply()
            }
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
        }
    }

    fun resetTimeline() {
        viewModelScope.launch {
            repository.resetApp()
            _selectedWeekIndex.value = null
            saveUserGoal("")
            _userPoints.value = 0
            prefs.edit().putInt("user_points", 0).apply()
        }
    }

    companion object {
        fun Factory(application: Application): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val database = AppDatabase.getDatabase(application)
                val repository = LifeTrackerRepository(database.timelineDao())
                LifeTrackerViewModel(repository, application)
            }
        }
    }
}
