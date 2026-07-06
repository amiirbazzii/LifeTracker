package com.example.data

import kotlinx.coroutines.flow.Flow

class LifeTrackerRepository(private val timelineDao: TimelineDao) {
    val timelineMeta: Flow<TimelineMeta?> = timelineDao.getTimelineMeta()
    val allTasks: Flow<List<DailyTask>> = timelineDao.getAllTasks()
    val allCategories: Flow<List<Category>> = timelineDao.getAllCategories()
    val allRoutines: Flow<List<Routine>> = timelineDao.getAllRoutines()
    val allRewards: Flow<List<Reward>> = timelineDao.getAllRewards()

    fun getTasksForWeek(weekIndex: Int): Flow<List<DailyTask>> {
        return timelineDao.getTasksForWeek(weekIndex)
    }

    fun getRoutinesForCategory(categoryId: String): Flow<List<Routine>> {
        return timelineDao.getRoutinesForCategory(categoryId)
    }

    suspend fun saveTimeline(meta: TimelineMeta) {
        timelineDao.clearTimelineMeta()
        timelineDao.clearAllTasks()
        timelineDao.clearAllCategories()
        timelineDao.clearAllRoutines()
        timelineDao.clearAllRewards()
        timelineDao.insertTimelineMeta(meta)
    }

    suspend fun insertTask(task: DailyTask) {
        timelineDao.insertTask(task)
    }

    suspend fun updateTask(task: DailyTask) {
        timelineDao.updateTask(task)
    }

    suspend fun deleteTask(taskId: String) {
        timelineDao.deleteTask(taskId)
    }

    suspend fun resetApp() {
        timelineDao.clearTimelineMeta()
        timelineDao.clearAllTasks()
        timelineDao.clearAllCategories()
        timelineDao.clearAllRoutines()
        timelineDao.clearAllRewards()
    }

    // --- Category CRUD ---
    suspend fun insertCategory(category: Category) {
        timelineDao.insertCategory(category)
    }

    suspend fun deleteCategory(categoryId: String) {
        timelineDao.deleteCategory(categoryId)
    }

    suspend fun clearAllCategories() {
        timelineDao.clearAllCategories()
    }

    // --- Routine CRUD ---
    suspend fun insertRoutine(routine: Routine) {
        timelineDao.insertRoutine(routine)
    }

    suspend fun updateRoutine(routine: Routine) {
        timelineDao.updateRoutine(routine)
    }

    suspend fun deleteRoutine(routineId: String) {
        timelineDao.deleteRoutine(routineId)
    }

    suspend fun clearAllRoutines() {
        timelineDao.clearAllRoutines()
    }

    // --- Reward CRUD ---
    suspend fun insertReward(reward: Reward) {
        timelineDao.insertReward(reward)
    }

    suspend fun updateReward(reward: Reward) {
        timelineDao.updateReward(reward)
    }

    suspend fun deleteReward(rewardId: String) {
        timelineDao.deleteReward(rewardId)
    }

    suspend fun clearAllRewards() {
        timelineDao.clearAllRewards()
    }
}
