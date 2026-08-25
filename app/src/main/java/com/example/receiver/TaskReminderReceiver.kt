package com.example.receiver

import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.util.TaskNotificationScheduler

class TaskReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getStringExtra("task_id") ?: return
        val taskTitle = intent.getStringExtra("task_title") ?: "Scheduled Task"
        val startTime = intent.getStringExtra("start_time") ?: ""
        val endTime = intent.getStringExtra("end_time") ?: ""

        Log.d("TaskReminderReceiver", "Alarm received for task: $taskTitle (id=$taskId)")

        TaskNotificationScheduler.ensureNotificationChannel(context)

        val appIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            taskId.hashCode(),
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val endText = if (endTime.isNotBlank()) " • Scheduled until $endTime" else ""
        val contentText = "Task has started: $startTime$endText"
        val bigText = "Task '$taskTitle' has started ($startTime$endText). Time to get it done!"

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(context, TaskNotificationScheduler.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("⏰ TASK STARTED: ${taskTitle.uppercase()}")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setSound(defaultSoundUri)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, false)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(taskId.hashCode(), notification)
            Log.d("TaskReminderReceiver", "Notification posted successfully for task: $taskTitle")
        } catch (e: SecurityException) {
            Log.e("TaskReminderReceiver", "SecurityException posting notification", e)
        } catch (e: Exception) {
            Log.e("TaskReminderReceiver", "Exception posting notification", e)
        }
    }
}

