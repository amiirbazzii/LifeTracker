package com.example.data

import kotlinx.coroutines.flow.Flow

class LifeTrackerRepository(private val timelineDao: TimelineDao) {
    val timelineMeta: Flow<TimelineMeta?> = timelineDao.getTimelineMeta()
    val allTasks: Flow<List<DailyTask>> = timelineDao.getAllTasks()

    fun getTasksForWeek(weekIndex: Int): Flow<List<DailyTask>> {
        return timelineDao.getTasksForWeek(weekIndex)
    }

    suspend fun saveTimeline(meta: TimelineMeta) {
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
    }
}
