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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class LifeTrackerViewModel(private val repository: LifeTrackerRepository) : ViewModel() {

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
                        sr <= 25f -> 1
                        sr <= 50f -> 2
                        sr <= 75f -> 3
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

    fun initializeTimeline(years: Int) {
        viewModelScope.launch {
            val totalWeeks = years * 52
            val meta = TimelineMeta(
                targetYears = years,
                totalWeeks = totalWeeks,
                inceptionTimestamp = System.currentTimeMillis()
            )
            repository.saveTimeline(meta)
        }
    }

    fun selectWeek(index: Int) {
        _selectedWeekIndex.value = index
    }

    fun addTask(title: String, weekIndex: Int, dayOfWeek: Int) {
        viewModelScope.launch {
            val task = DailyTask(
                taskId = UUID.randomUUID().toString(),
                weekIndex = weekIndex,
                dayOfWeek = dayOfWeek,
                taskTitle = title,
                isCompleted = 0,
                createdTimestamp = System.currentTimeMillis()
            )
            repository.insertTask(task)
        }
    }

    fun toggleTaskCompletion(task: DailyTask) {
        viewModelScope.launch {
            val updated = task.copy(isCompleted = if (task.isCompleted == 1) 0 else 1)
            repository.updateTask(updated)
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
        }
    }

    companion object {
        fun Factory(application: Application): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val database = AppDatabase.getDatabase(application)
                val repository = LifeTrackerRepository(database.timelineDao())
                LifeTrackerViewModel(repository)
            }
        }
    }
}
