package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TimelineDao {
    @Query("SELECT * FROM timeline_meta LIMIT 1")
    fun getTimelineMeta(): Flow<TimelineMeta?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimelineMeta(meta: TimelineMeta)

    @Query("DELETE FROM timeline_meta")
    suspend fun clearTimelineMeta()

    @Query("SELECT * FROM daily_tasks ORDER BY created_timestamp DESC")
    fun getAllTasks(): Flow<List<DailyTask>>

    @Query("SELECT * FROM daily_tasks WHERE week_index = :weekIndex ORDER BY created_timestamp DESC")
    fun getTasksForWeek(weekIndex: Int): Flow<List<DailyTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: DailyTask)

    @Update
    suspend fun updateTask(task: DailyTask)

    @Query("DELETE FROM daily_tasks WHERE task_id = :taskId")
    suspend fun deleteTask(taskId: String)

    @Query("DELETE FROM daily_tasks")
    suspend fun clearAllTasks()
}
