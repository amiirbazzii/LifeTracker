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

    @Query("SELECT * FROM daily_tasks WHERE week_index = :weekIndex AND day_of_week = :dayOfWeek ORDER BY created_timestamp DESC")
    fun getTasksForWeekAndDay(weekIndex: Int, dayOfWeek: Int): Flow<List<DailyTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: DailyTask)

    @Update
    suspend fun updateTask(task: DailyTask)

    @Query("DELETE FROM daily_tasks WHERE task_id = :taskId")
    suspend fun deleteTask(taskId: String)

    @Query("DELETE FROM daily_tasks")
    suspend fun clearAllTasks()

    // --- Category Operations ---
    @Query("SELECT * FROM categories ORDER BY created_timestamp ASC")
    fun getAllCategories(): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)

    @Update
    suspend fun updateCategory(category: Category)

    @Query("DELETE FROM categories WHERE category_id = :categoryId")
    suspend fun deleteCategory(categoryId: String)

    @Query("DELETE FROM categories")
    suspend fun clearAllCategories()

    // --- Routine Operations ---
    @Query("SELECT * FROM routines ORDER BY created_timestamp ASC")
    fun getAllRoutines(): Flow<List<Routine>>

    @Query("SELECT * FROM routines WHERE category_id = :categoryId ORDER BY created_timestamp ASC")
    fun getRoutinesForCategory(categoryId: String): Flow<List<Routine>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: Routine)

    @Update
    suspend fun updateRoutine(routine: Routine)

    @Query("DELETE FROM routines WHERE routine_id = :routineId")
    suspend fun deleteRoutine(routineId: String)

    @Query("DELETE FROM routines WHERE category_id = :categoryId")
    suspend fun deleteRoutinesForCategory(categoryId: String)

    @Query("DELETE FROM routines")
    suspend fun clearAllRoutines()

    // --- Reward Operations ---
    @Query("SELECT * FROM rewards ORDER BY created_timestamp ASC")
    fun getAllRewards(): Flow<List<Reward>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReward(reward: Reward)

    @Update
    suspend fun updateReward(reward: Reward)

    @Query("DELETE FROM rewards WHERE reward_id = :rewardId")
    suspend fun deleteReward(rewardId: String)

    @Query("DELETE FROM rewards")
    suspend fun clearAllRewards()

    // --- SubGoal Operations ---
    @Query("SELECT * FROM sub_goals ORDER BY start_month ASC, created_timestamp ASC")
    fun getAllSubGoals(): Flow<List<SubGoal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubGoal(subGoal: SubGoal)

    @Update
    suspend fun updateSubGoal(subGoal: SubGoal)

    @Query("DELETE FROM sub_goals WHERE sub_goal_id = :subGoalId")
    suspend fun deleteSubGoal(subGoalId: String)

    @Query("DELETE FROM sub_goals")
    suspend fun clearAllSubGoals()

    // --- DailyHabit Operations ---
    @Query("SELECT * FROM daily_habits ORDER BY createdTimestamp ASC")
    fun getAllDailyHabits(): Flow<List<DailyHabit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyHabit(habit: DailyHabit)

    @Update
    suspend fun updateDailyHabit(habit: DailyHabit)

    @Query("DELETE FROM daily_habits WHERE id = :habitId")
    suspend fun deleteDailyHabit(habitId: String)

    @Query("DELETE FROM daily_habits")
    suspend fun clearAllDailyHabits()
}
