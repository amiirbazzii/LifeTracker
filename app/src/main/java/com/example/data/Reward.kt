package com.example.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rewards")
data class Reward(
    @PrimaryKey
    @ColumnInfo(name = "reward_id")
    val id: String,
    
    @ColumnInfo(name = "reward_name")
    val name: String,
    
    @ColumnInfo(name = "point_cost")
    val pointCost: Int,
    
    @ColumnInfo(name = "claimed_count")
    val claimedCount: Int = 0,
    
    @ColumnInfo(name = "created_timestamp")
    val createdTimestamp: Long = System.currentTimeMillis()
)
