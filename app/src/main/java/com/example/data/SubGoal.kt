package com.example.data

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Immutable
@Entity(tableName = "sub_goals")
data class SubGoal(
    @PrimaryKey
    @ColumnInfo(name = "sub_goal_id")
    val id: String,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "duration_months")
    val durationMonths: Int,

    @ColumnInfo(name = "start_month")
    val startMonth: Int = 0,

    @ColumnInfo(name = "created_timestamp")
    val createdTimestamp: Long = System.currentTimeMillis()
)
