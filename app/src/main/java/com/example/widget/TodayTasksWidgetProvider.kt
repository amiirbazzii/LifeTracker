package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Calendar

class TodayTasksWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val action = intent.action
        if (action == ACTION_TOGGLE_TASK) {
            val taskId = intent.getStringExtra(EXTRA_TASK_ID) ?: return
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val dao = db.timelineDao()
                    val allTasks = dao.getAllTasks().first()
                    val task = allTasks.find { it.taskId == taskId }
                    if (task != null) {
                        val willBeCompleted = task.isCompleted == 0
                        val updated = task.copy(isCompleted = if (willBeCompleted) 1 else 0)
                        dao.updateTask(updated)

                        // Update user points
                        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                        val currentPoints = prefs.getInt("user_points", 0)
                        val pointsDiff = if (task.routineId != null) 15 else 10
                        val newPoints = if (willBeCompleted) {
                            currentPoints + pointsDiff
                        } else {
                            (currentPoints - pointsDiff).coerceAtLeast(0)
                        }
                        prefs.edit().putInt("user_points", newPoints).apply()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    refreshAllWidgets(context)
                    GoalMatrixWidgetProvider.refreshAllWidgets(context)
                    pendingResult.finish()
                }
            }
        } else if (action == ACTION_REFRESH_WIDGET) {
            refreshAllWidgets(context)
            GoalMatrixWidgetProvider.refreshAllWidgets(context)
        }
    }

    companion object {
        const val ACTION_TOGGLE_TASK = "com.example.widget.ACTION_TOGGLE_TASK"
        const val ACTION_REFRESH_WIDGET = "com.example.widget.ACTION_REFRESH_WIDGET"
        const val EXTRA_TASK_ID = "extra_task_id"

        fun refreshAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, TodayTasksWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

            // Notify ListView to refresh items
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_task_list)

            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_today_tasks)

            // 1. Setup Intent to launch MainActivity when clicking Header or + APP button
            val appIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingAppIntent = PendingIntent.getActivity(
                context,
                0,
                appIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_header, pendingAppIntent)
            views.setOnClickPendingIntent(R.id.widget_btn_open_app, pendingAppIntent)

            // 2. Setup Refresh Intent
            val refreshIntent = Intent(context, TodayTasksWidgetProvider::class.java).apply {
                action = ACTION_REFRESH_WIDGET
            }
            val pendingRefreshIntent = PendingIntent.getBroadcast(
                context,
                1,
                refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_refresh, pendingRefreshIntent)

            // 3. Connect empty view
            views.setEmptyView(R.id.widget_task_list, R.id.widget_empty_view)

            // 4. Setup RemoteViewsService Adapter for the Task ListView
            val serviceIntent = Intent(context, TodayTasksWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = Uri.parse("content://com.example.widget/tasks/$appWidgetId")
            }
            views.setRemoteAdapter(R.id.widget_task_list, serviceIntent)

            // 5. Setup PendingIntent Template for item clicks (Toggle completion)
            val toggleIntent = Intent(context, TodayTasksWidgetProvider::class.java).apply {
                action = ACTION_TOGGLE_TASK
            }
            val pendingToggleTemplate = PendingIntent.getBroadcast(
                context,
                2,
                toggleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            views.setPendingIntentTemplate(R.id.widget_task_list, pendingToggleTemplate)

            // 6. Update task counter asynchronously
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val dao = db.timelineDao()
                    val meta = dao.getTimelineMeta().firstOrNull()
                    if (meta != null) {
                        val inceptionTimestamp = meta.inceptionTimestamp
                        val elapsedMillis = System.currentTimeMillis() - inceptionTimestamp
                        val weekInMillis = 7 * 24 * 60 * 60 * 1000L
                        val currentWeekIndex = (elapsedMillis / weekInMillis).toInt().coerceIn(0, meta.totalWeeks - 1)

                        val currentDay = com.example.ui.Utils.getTodayDayIndex(inceptionTimestamp, currentWeekIndex)
                        val todayTasks = dao.getTasksForWeekAndDay(currentWeekIndex, currentDay).first()
                        val completedCount = todayTasks.count { it.isCompleted == 1 }
                        val totalCount = todayTasks.size
                        views.setTextViewText(R.id.widget_counter, "$completedCount/$totalCount")
                    } else {
                        views.setTextViewText(R.id.widget_counter, "0/0")
                    }
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                } catch (e: Exception) {
                    e.printStackTrace()
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }

            // Perform initial synchronous update so widget renders immediately
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
