package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_habits")
data class DailyHabit(
    @PrimaryKey val id: String,
    val title: String,
    val createdTimestamp: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)
