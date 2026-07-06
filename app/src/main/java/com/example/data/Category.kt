package com.example.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey
    @ColumnInfo(name = "category_id")
    val id: String,
    
    @ColumnInfo(name = "category_name")
    val name: String,
    
    @ColumnInfo(name = "created_timestamp")
    val createdTimestamp: Long = System.currentTimeMillis()
)
