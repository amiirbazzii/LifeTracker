package com.example.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.R
import com.example.data.AppDatabase
import com.example.data.DailyTask
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import java.util.Calendar

class TodayTasksWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return TodayTasksRemoteViewsFactory(applicationContext)
    }
}

class TodayTasksRemoteViewsFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {

    private var tasksList = listOf<DailyTask>()

    override fun onCreate() {
        loadData()
    }

    override fun onDataSetChanged() {
        loadData()
    }

    private fun loadData() {
        try {
            runBlocking {
                val db = AppDatabase.getDatabase(context)
                val dao = db.timelineDao()
                val meta = dao.getTimelineMeta().firstOrNull()
                if (meta != null) {
                    val inceptionTimestamp = meta.inceptionTimestamp
                    val elapsedMillis = System.currentTimeMillis() - inceptionTimestamp
                    val weekInMillis = 7 * 24 * 60 * 60 * 1000L
                    val currentWeekIndex = (elapsedMillis / weekInMillis).toInt().coerceIn(0, meta.totalWeeks - 1)

                    val currentDay = com.example.ui.Utils.getTodayDayIndex(inceptionTimestamp, currentWeekIndex)
                    tasksList = dao.getTasksForWeekAndDay(currentWeekIndex, currentDay).first()
                } else {
                    tasksList = emptyList()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            tasksList = emptyList()
        }
    }

    override fun onDestroy() {
        tasksList = emptyList()
    }

    override fun getCount(): Int = tasksList.size

    override fun getViewAt(position: Int): RemoteViews? {
        if (position !in tasksList.indices) return null
        val task = tasksList[position]
        val isCompleted = task.isCompleted == 1
        val isRecurring = task.habitId != null || task.routineId != null

        val views = RemoteViews(context.packageName, R.layout.widget_task_item)

        // 1. Task Title
        views.setTextViewText(R.id.widget_task_title, task.taskTitle.uppercase())

        // 2. Checkbox and Completion State
        if (isCompleted) {
            views.setTextViewText(R.id.widget_task_checkbox, "✓")
            views.setTextColor(R.id.widget_task_checkbox, 0xFFFFFFFF.toInt())
            views.setTextColor(R.id.widget_task_title, 0xFF71717A.toInt())
            views.setTextViewText(R.id.widget_task_points, "DONE")
            views.setTextColor(R.id.widget_task_points, 0xFF52525B.toInt())
        } else {
            views.setTextViewText(R.id.widget_task_checkbox, "○")
            views.setTextColor(R.id.widget_task_checkbox, 0xFFA1A1AA.toInt())
            views.setTextColor(R.id.widget_task_title, 0xFFFFFFFF.toInt())
            val pts = if (task.routineId != null) "+15 PTS" else "+10 PTS"
            views.setTextViewText(R.id.widget_task_points, pts)
            views.setTextColor(R.id.widget_task_points, 0xFFA1A1AA.toInt())
        }

        // 3. Recurring Tag
        if (isRecurring) {
            views.setTextViewText(R.id.widget_task_badge, "↻ RECURRING")
        } else {
            views.setTextViewText(R.id.widget_task_badge, "DAILY TASK")
        }

        // 4. FillInIntent for toggle click
        val fillInIntent = Intent().apply {
            putExtra(TodayTasksWidgetProvider.EXTRA_TASK_ID, task.taskId)
        }
        views.setOnClickFillInIntent(R.id.widget_task_item_root, fillInIntent)
        views.setOnClickFillInIntent(R.id.widget_task_checkbox, fillInIntent)

        return views
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = position.toLong()

    override fun hasStableIds(): Boolean = false
}
