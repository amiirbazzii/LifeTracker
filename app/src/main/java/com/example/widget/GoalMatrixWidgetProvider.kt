package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class GoalMatrixWidgetProvider : AppWidgetProvider() {

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

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        updateAppWidget(context, appWidgetManager, appWidgetId)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH_MATRIX) {
            refreshAllWidgets(context)
        }
    }

    companion object {
        const val ACTION_REFRESH_MATRIX = "com.example.widget.ACTION_REFRESH_MATRIX"

        fun refreshAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, GoalMatrixWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

            // Notify ListView to re-query data and re-render rows
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.matrix_widget_row_list)

            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_goal_matrix)

            // 1. Set up title with actual total weeks if available
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val dao = db.timelineDao()
                    val meta = dao.getTimelineMeta().firstOrNull()
                    val totalWeeks = meta?.totalWeeks ?: 540
                    views.setTextViewText(R.id.matrix_widget_title, "Micro timline matrix ($totalWeeks week)")
                    appWidgetManager.partiallyUpdateAppWidget(appWidgetId, views)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // 2. Connect empty view
            views.setEmptyView(R.id.matrix_widget_row_list, R.id.matrix_widget_empty_view)

            // 3. Set up RemoteViewsService Adapter for the row ListView
            val serviceIntent = Intent(context, GoalMatrixWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = Uri.parse("content://com.example.widget/matrix/$appWidgetId")
            }
            views.setRemoteAdapter(R.id.matrix_widget_row_list, serviceIntent)

            // 4. Set up PendingIntent template to launch MainActivity when clicking any square / row
            val appIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingAppTemplate = PendingIntent.getActivity(
                context,
                20,
                appIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            views.setPendingIntentTemplate(R.id.matrix_widget_row_list, pendingAppTemplate)
            views.setOnClickPendingIntent(R.id.matrix_widget_title, pendingAppTemplate)

            // 5. Update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
