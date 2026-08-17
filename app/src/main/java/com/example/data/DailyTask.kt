package com.example.data

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Immutable
@Entity(tableName = "daily_tasks")
data class DailyTask(
    @PrimaryKey
    @ColumnInfo(name = "task_id")
    val taskId: String,
    
    @ColumnInfo(name = "week_index")
    val weekIndex: Int,
    
    @ColumnInfo(name = "day_of_week")
    val dayOfWeek: Int,
    
    @ColumnInfo(name = "task_title")
    val taskTitle: String,
    
    @ColumnInfo(name = "is_completed")
    val isCompleted: Int = 0, // 0 = False, 1 = True
    
    @ColumnInfo(name = "created_timestamp")
    val createdTimestamp: Long,

    @ColumnInfo(name = "routine_id")
    val routineId: String? = null,

    @ColumnInfo(name = "habit_id")
    val habitId: String? = null
)
