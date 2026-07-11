package com.example.data

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Immutable
@Entity(tableName = "timeline_meta")
data class TimelineMeta(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    @ColumnInfo(name = "target_years")
    val targetYears: Int,
    
    @ColumnInfo(name = "total_weeks")
    val totalWeeks: Int,
    
    @ColumnInfo(name = "inception_timestamp")
    val inceptionTimestamp: Long
)
