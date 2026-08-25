package com.example.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import com.example.MainActivity
import com.example.data.DailyTask
import com.example.data.TimelineMeta
import com.example.receiver.TaskReminderReceiver
import com.example.ui.Utils
import java.util.Calendar

object TaskNotificationScheduler {

    private const val TAG = "TaskScheduler"
    const val CHANNEL_ID = "task_timer_channel"

    fun ensureNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID,
                "Task Schedule Timers",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High-priority alarms and notifications when scheduled tasks begin"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 400, 200, 400)
                enableLights(true)
                lightColor = Color.GREEN
                setShowBadge(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                setSound(soundUri, audioAttributes)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleTaskNotification(context: Context, meta: TimelineMeta, task: DailyTask): Boolean {
        val startTime = task.startTime
        if (startTime.isNullOrBlank() || task.isCompleted == 1) {
            cancelTaskNotification(context, task.taskId)
            return false
        }

        ensureNotificationChannel(context)

        val targetTimestamp = calculateTaskStartTime(meta.inceptionTimestamp, task.weekIndex, task.dayOfWeek, startTime)
        val now = System.currentTimeMillis()
        if (targetTimestamp == null || targetTimestamp <= now) {
            Log.d(TAG, "Task start timestamp ($targetTimestamp) is in the past compared to current time ($now) for task: ${task.taskId}")
            return false
        }

        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return false
            val intent = Intent(context, TaskReminderReceiver::class.java).apply {
                action = "com.example.action.TASK_REMINDER"
                putExtra("task_id", task.taskId)
                putExtra("task_title", task.taskTitle)
                putExtra("start_time", task.startTime)
                putExtra("end_time", task.endTime ?: "")
            }

            val requestCode = task.taskId.hashCode()
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val showIntent = PendingIntent.getActivity(
                context,
                requestCode,
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Try setAlarmClock first as it guarantees exact time delivery and bypasses doze/battery restrictions
            try {
                val clockInfo = AlarmManager.AlarmClockInfo(targetTimestamp, showIntent)
                alarmManager.setAlarmClock(clockInfo, pendingIntent)
                Log.d(TAG, "Scheduled alarmClock for task '${task.taskTitle}' at $targetTimestamp (in ${(targetTimestamp - now) / 1000}s)")
                return true
            } catch (se: SecurityException) {
                Log.w(TAG, "setAlarmClock security exception, falling back: ${se.message}")
            }

            // Fallback for systems where setAlarmClock might throw
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetTimestamp, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetTimestamp, pendingIntent)
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetTimestamp, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, targetTimestamp, pendingIntent)
            }
            Log.d(TAG, "Scheduled fallback alarm for task '${task.taskTitle}' at $targetTimestamp")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling alarm for task: ${task.taskId}", e)
            return false
        }
    }

    fun cancelTaskNotification(context: Context, taskId: String) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, TaskReminderReceiver::class.java).apply {
                action = "com.example.action.TASK_REMINDER"
            }
            val requestCode = taskId.hashCode()
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
                Log.d(TAG, "Cancelled alarm for task: $taskId")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling alarm for task: $taskId", e)
        }
    }

    fun rescheduleAllFutureTasks(context: Context, meta: TimelineMeta, tasks: List<DailyTask>) {
        val now = System.currentTimeMillis()
        tasks.filter { it.isCompleted == 0 && !it.startTime.isNullOrBlank() }.forEach { task ->
            val targetTime = calculateTaskStartTime(meta.inceptionTimestamp, task.weekIndex, task.dayOfWeek, task.startTime!!)
            if (targetTime != null && targetTime > now) {
                scheduleTaskNotification(context, meta, task)
            }
        }
    }

    fun calculateTaskStartTime(inceptionTimestamp: Long, weekIndex: Int, dayOfWeek: Int, timeString: String): Long? {
        return try {
            val parts = timeString.trim().split(":")
            if (parts.size != 2) return null
            val hour = parts[0].toIntOrNull() ?: return null
            val minute = parts[1].toIntOrNull() ?: return null

            val isToday = Utils.isCellToday(inceptionTimestamp, weekIndex, dayOfWeek)
            val targetCal = Calendar.getInstance()

            if (isToday) {
                targetCal.apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
            } else {
                val cellTimeInMillis = inceptionTimestamp + (weekIndex * 7L + (dayOfWeek - 1)) * 24L * 60L * 60L * 1000L
                val cellCal = Calendar.getInstance().apply { timeInMillis = cellTimeInMillis }
                targetCal.apply {
                    set(Calendar.YEAR, cellCal.get(Calendar.YEAR))
                    set(Calendar.MONTH, cellCal.get(Calendar.MONTH))
                    set(Calendar.DAY_OF_MONTH, cellCal.get(Calendar.DAY_OF_MONTH))
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
            }
            targetCal.timeInMillis
        } catch (e: Exception) {
            null
        }
    }
}

