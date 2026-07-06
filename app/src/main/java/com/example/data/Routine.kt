package com.example.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routines")
data class Routine(
    @PrimaryKey
    @ColumnInfo(name = "routine_id")
    val id: String,
    
    @ColumnInfo(name = "category_id")
    val categoryId: String,
    
    @ColumnInfo(name = "routine_title")
    val title: String,
    
    @ColumnInfo(name = "target_count")
    val targetCount: Int,
    
    @ColumnInfo(name = "completed_count")
    val completedCount: Int = 0,
    
    @ColumnInfo(name = "created_timestamp")
    val createdTimestamp: Long = System.currentTimeMillis()
)
