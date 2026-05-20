package com.example.ui

import com.example.data.DailyTask
import com.example.data.TimelineMeta

sealed interface LifeTrackerUiState {
    object Loading : LifeTrackerUiState
    object Onboarding : LifeTrackerUiState
    data class Dashboard(
        val meta: TimelineMeta,
        val currentWeekIndex: Int,
        val selectedWeekIndex: Int,
        val weekColors: Map<Int, Int>, // Maps week_index -> Level (0..4)
        val selectedWeekTasks: List<DailyTask>
    ) : LifeTrackerUiState
}
