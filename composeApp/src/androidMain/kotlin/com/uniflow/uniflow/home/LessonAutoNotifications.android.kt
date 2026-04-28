package com.uniflow.uniflow.home

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.uniflow.uniflow.R

actual object LessonNotificationScheduler {
    actual fun requestPermissionIfNeeded() {
        ReminderNotificationScheduler.requestPermissionIfNeeded()
    }

    actual fun scheduleAll(schedules: List<LessonNotificationSchedule>) {
        cancelAll()
        val context = AndroidAppContextHolder.context
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        schedules.forEach { schedule ->
            val pendingIntent = pendingIntent(context, schedule, PendingIntent.FLAG_UPDATE_CURRENT)
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        schedule.triggerAt,
                        pendingIntent
                    )
                }

                else -> {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        schedule.triggerAt,
                        pendingIntent
                    )
                }
            }
        }
    }

    actual fun cancelAll() {
        val context = AndroidAppContextHolder.context
        val notificationManager = NotificationManagerCompat.from(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        for (requestCode in 200000..200128) {
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                Intent(context, LessonNotificationReceiver::class.java),
                pendingFlags(PendingIntent.FLAG_NO_CREATE)
            )
            pendingIntent?.let {
                alarmManager.cancel(it)
                it.cancel()
            }
            notificationManager.cancel(requestCode)
        }
    }

    private fun pendingIntent(
        context: Context,
        schedule: LessonNotificationSchedule,
        flag: Int
    ): PendingIntent {
        val requestCode = requestCode(schedule.notificationId)
        val intent = Intent(context, LessonNotificationReceiver::class.java).apply {
            putExtra("notification_id", requestCode)
            putExtra("title", schedule.title)
            putExtra("body", schedule.body)
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            pendingFlags(flag)
        )
    }

    private fun pendingFlags(flag: Int): Int {
        val immutableFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            0
        }
        return flag or immutableFlag
    }

    private fun requestCode(notificationId: Long): Int =
        200000 + (notificationId.absoluteValue % 129L).toInt()
}

class LessonNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra("notification_id", 200000)
        val title = intent.getStringExtra("title").orEmpty().ifBlank { "Ora hamarosan kezdodik" }
        val body = intent.getStringExtra("body").orEmpty().ifBlank { "Nezd meg a kovetkezo oradat." }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val builder = NotificationCompat.Builder(context, "uniflow_reminders")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }
}

private val Long.absoluteValue: Long
    get() = if (this < 0) -this else this
