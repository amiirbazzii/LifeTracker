package com.example.ui

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.example.data.DailyTask
import com.example.data.TimelineMeta

@Stable
sealed interface LifeTrackerUiState {
    @Immutable
    object Loading : LifeTrackerUiState
    
    @Immutable
    object Onboarding : LifeTrackerUiState
    
    @Immutable
    data class Dashboard(
        val meta: TimelineMeta,
        val currentWeekIndex: Int,
        val selectedWeekIndex: Int,
        val weekColors: Map<Int, Int>, // Maps week_index -> Level (0..4)
        val selectedWeekTasks: List<DailyTask>,
        val currentWeekTasks: List<DailyTask>
    ) : LifeTrackerUiState
}
