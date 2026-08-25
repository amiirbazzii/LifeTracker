package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.AppDatabase
import com.example.util.TaskNotificationScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val dao = db.timelineDao()
                    val meta = dao.getTimelineMeta().firstOrNull() ?: return@launch
                    val tasks = dao.getAllTasks().firstOrNull() ?: return@launch
                    TaskNotificationScheduler.rescheduleAllFutureTasks(context, meta, tasks)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
