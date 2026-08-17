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
import com.example.data.SubGoal
import com.example.data.DailyHabit
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.content.Context
import android.widget.Toast
import java.util.UUID

class LifeTrackerViewModel(
    private val repository: LifeTrackerRepository,
    private val application: Application,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

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

    val allSubGoals: StateFlow<List<SubGoal>> = repository.allSubGoals.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allDailyHabits: StateFlow<List<DailyHabit>> = repository.allDailyHabits.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allTasks: StateFlow<List<DailyTask>> = repository.allTasks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch(ioDispatcher) {
            combine(
                repository.timelineMeta,
                repository.allDailyHabits
            ) { meta, habits ->
                meta to habits
            }.collectLatest { (meta, habits) ->
                if (meta != null && habits.isNotEmpty()) {
                    autoInjectDailyHabits(meta, habits)
                }
            }
        }
    }

    private suspend fun autoInjectDailyHabits(meta: TimelineMeta, habits: List<DailyHabit>) {
        val elapsedMillis = System.currentTimeMillis() - meta.inceptionTimestamp
        val weekInMillis = 7 * 24 * 60 * 60 * 1000L
        val computedWeek = (elapsedMillis / weekInMillis).toInt()
        val currentWeek = computedWeek.coerceIn(0, meta.totalWeeks - 1)
        val currentDay = Utils.getTodayDayIndex(meta.inceptionTimestamp, currentWeek)

        val currentTasks = repository.allTasks.first()
        val todayTasks = currentTasks.filter { it.weekIndex == currentWeek && it.dayOfWeek == currentDay }

        for (habit in habits.filter { it.isActive }) {
            val alreadyInjected = todayTasks.any { it.habitId == habit.id }
            if (!alreadyInjected) {
                val task = DailyTask(
                    taskId = UUID.randomUUID().toString(),
                    weekIndex = currentWeek,
                    dayOfWeek = currentDay,
                    taskTitle = habit.title,
                    isCompleted = 0,
                    createdTimestamp = System.currentTimeMillis(),
                    habitId = habit.id
                )
                repository.insertTask(task)
            }
        }
        com.example.widget.TodayTasksWidgetProvider.refreshAllWidgets(application)
    }

    fun ensureDailyHabitsInjected() {
        viewModelScope.launch(ioDispatcher) {
            val meta = repository.timelineMeta.first() ?: return@launch
            val habits = repository.allDailyHabits.first()
            if (habits.isNotEmpty()) {
                autoInjectDailyHabits(meta, habits)
            }
        }
    }

    private suspend fun showToast(message: String) {
        withContext(Dispatchers.Main) {
            Toast.makeText(application, message, Toast.LENGTH_SHORT).show()
        }
    }

    fun onCreateSubGoal(title: String, durationMonths: Int) {
        viewModelScope.launch(ioDispatcher) {
            val currentSubGoals = repository.allSubGoals.first()
            val nextStartMonth = currentSubGoals.sumOf { it.durationMonths }
            val subGoal = SubGoal(
                id = UUID.randomUUID().toString(),
                title = title,
                durationMonths = durationMonths,
                startMonth = nextStartMonth,
                createdTimestamp = System.currentTimeMillis()
            )
            repository.insertSubGoal(subGoal)
            showToast("Sub-goal '$title' created")
        }
    }

    fun onUpdateSubGoal(subGoalId: String, newTitle: String, newDurationMonths: Int) {
        viewModelScope.launch(ioDispatcher) {
            val currentSubGoals = repository.allSubGoals.first()
            val existing = currentSubGoals.find { it.id == subGoalId }
            if (existing != null) {
                val updated = existing.copy(title = newTitle, durationMonths = newDurationMonths)
                repository.updateSubGoal(updated)
                recalculateSubGoalStartMonths()
            }
        }
    }

    fun onDeleteSubGoal(subGoalId: String) {
        viewModelScope.launch(ioDispatcher) {
            repository.deleteSubGoal(subGoalId)
            recalculateSubGoalStartMonths()
        }
    }

    private suspend fun recalculateSubGoalStartMonths() {
        val currentSubGoals = repository.allSubGoals.first()
        var cumulativeMonth = 0
        currentSubGoals.forEach { sg ->
            if (sg.startMonth != cumulativeMonth) {
                repository.updateSubGoal(sg.copy(startMonth = cumulativeMonth))
            }
            cumulativeMonth += sg.durationMonths
        }
    }

    fun saveUserGoal(goal: String) {
        viewModelScope.launch(ioDispatcher) {
            _userGoal.value = goal
            prefs.edit().putString("user_goal", goal).apply()
        }
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
        viewModelScope.launch(ioDispatcher) {
            val category = Category(
                id = UUID.randomUUID().toString(),
                name = name,
                createdTimestamp = System.currentTimeMillis()
            )
            repository.insertCategory(category)
            showToast("Category '$name' created")
        }
    }

    fun onUpdateCategory(categoryId: String, newName: String) {
        viewModelScope.launch(ioDispatcher) {
            val categories = repository.allCategories.first()
            val category = categories.find { it.id == categoryId }
            if (category != null) {
                val updated = category.copy(name = newName)
                repository.updateCategory(updated)
            }
        }
    }

    fun onDeleteCategory(categoryId: String) {
        viewModelScope.launch(ioDispatcher) {
            repository.deleteCategory(categoryId)
            repository.deleteRoutinesForCategory(categoryId)
        }
    }

    fun onCreateRoutine(catId: String, title: String, target: Int) {
        viewModelScope.launch(ioDispatcher) {
            val routine = Routine(
                id = UUID.randomUUID().toString(),
                categoryId = catId,
                title = title,
                targetCount = target,
                completedCount = 0,
                createdTimestamp = System.currentTimeMillis()
            )
            repository.insertRoutine(routine)
            showToast("Routine '$title' created")
        }
    }

    fun onUpdateRoutine(routineId: String, newTitle: String, newTarget: Int) {
        viewModelScope.launch(ioDispatcher) {
            val routines = repository.allRoutines.first()
            val routine = routines.find { it.id == routineId }
            if (routine != null) {
                val updated = routine.copy(title = newTitle, targetCount = newTarget)
                repository.updateRoutine(updated)
                showToast("Routine '$newTitle' updated")
            }
        }
    }

    fun onDeleteRoutine(routineId: String) {
        viewModelScope.launch(ioDispatcher) {
            val routines = repository.allRoutines.first()
            val routine = routines.find { it.id == routineId }
            val routineTitle = routine?.title ?: ""
            repository.deleteRoutine(routineId)
            if (routineTitle.isNotBlank()) {
                showToast("Routine '$routineTitle' deleted")
            } else {
                showToast("Routine deleted")
            }
        }
    }

    // --- Daily Habits / Rituals (Separate Dedicated Feature) ---
    fun onCreateHabit(title: String) {
        viewModelScope.launch(ioDispatcher) {
            val habit = DailyHabit(
                id = UUID.randomUUID().toString(),
                title = title,
                createdTimestamp = System.currentTimeMillis(),
                isActive = true
            )
            repository.insertDailyHabit(habit)
            showToast("Habit '$title' created")
            ensureDailyHabitsInjected()
        }
    }

    fun onUpdateHabit(habitId: String, newTitle: String) {
        viewModelScope.launch(ioDispatcher) {
            val habits = repository.allDailyHabits.first()
            val habit = habits.find { it.id == habitId }
            if (habit != null) {
                val updated = habit.copy(title = newTitle)
                repository.updateDailyHabit(updated)

                // Update today's uncompleted daily tasks linked to this habit
                val tasks = repository.allTasks.first()
                tasks.filter { it.habitId == habitId && it.isCompleted == 0 }.forEach { task ->
                    repository.updateTask(task.copy(taskTitle = newTitle))
                }
                showToast("Habit '$newTitle' updated")
            }
        }
    }

    fun onDeleteHabit(habitId: String) {
        viewModelScope.launch(ioDispatcher) {
            val habits = repository.allDailyHabits.first()
            val habit = habits.find { it.id == habitId }
            val habitTitle = habit?.title ?: ""
            repository.deleteDailyHabit(habitId)

            // Remove uncompleted daily tasks linked to this habit
            val tasks = repository.allTasks.first()
            tasks.filter { it.habitId == habitId && it.isCompleted == 0 }.forEach { task ->
                repository.deleteTask(task.taskId)
            }
            if (habitTitle.isNotBlank()) {
                showToast("Habit '$habitTitle' deleted")
            } else {
                showToast("Habit deleted")
            }
        }
    }

    fun onAddReward(name: String, cost: Int) {
        viewModelScope.launch(ioDispatcher) {
            val reward = Reward(
                id = UUID.randomUUID().toString(),
                name = name,
                pointCost = cost,
                claimedCount = 0,
                createdTimestamp = System.currentTimeMillis()
            )
            repository.insertReward(reward)
            showToast("Reward '$name' created")
        }
    }

    fun onClaimReward(reward: Reward) {
        viewModelScope.launch(ioDispatcher) {
            if (deductPoints(reward.pointCost)) {
                val updated = reward.copy(claimedCount = reward.claimedCount + 1)
                repository.updateReward(updated)
                showToast("Reward '${reward.name}' claimed!")
            }
        }
    }

    fun incrementRoutineCompletion(routineId: String) {
        viewModelScope.launch(ioDispatcher) {
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
        withContext(Dispatchers.Default) {
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
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LifeTrackerUiState.Loading
    )

    fun initializeTimeline(years: Int, goal: String) {
        viewModelScope.launch(ioDispatcher) {
            val totalWeeks = years * 52
            val meta = TimelineMeta(
                targetYears = years,
                totalWeeks = totalWeeks,
                inceptionTimestamp = System.currentTimeMillis()
            )
            repository.saveTimeline(meta)
            saveUserGoal(goal)
            com.example.widget.TodayTasksWidgetProvider.refreshAllWidgets(application)
            com.example.widget.GoalMatrixWidgetProvider.refreshAllWidgets(application)
        }
    }

    fun selectWeek(index: Int) {
        _selectedWeekIndex.value = index
    }

    fun addTask(title: String, weekIndex: Int, dayOfWeek: Int, routineId: String? = null) {
        viewModelScope.launch(ioDispatcher) {
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
            com.example.widget.TodayTasksWidgetProvider.refreshAllWidgets(application)
            com.example.widget.GoalMatrixWidgetProvider.refreshAllWidgets(application)
        }
    }

    fun toggleTaskCompletion(task: DailyTask) {
        viewModelScope.launch(ioDispatcher) {
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
            com.example.widget.TodayTasksWidgetProvider.refreshAllWidgets(application)
            com.example.widget.GoalMatrixWidgetProvider.refreshAllWidgets(application)
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch(ioDispatcher) {
            repository.deleteTask(taskId)
            com.example.widget.TodayTasksWidgetProvider.refreshAllWidgets(application)
            com.example.widget.GoalMatrixWidgetProvider.refreshAllWidgets(application)
        }
    }

    fun resetTimeline() {
        viewModelScope.launch(ioDispatcher) {
            repository.resetApp()
            _selectedWeekIndex.value = null
            saveUserGoal("")
            _userPoints.value = 0
            prefs.edit().putInt("user_points", 0).apply()
            com.example.widget.TodayTasksWidgetProvider.refreshAllWidgets(application)
            com.example.widget.GoalMatrixWidgetProvider.refreshAllWidgets(application)
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
