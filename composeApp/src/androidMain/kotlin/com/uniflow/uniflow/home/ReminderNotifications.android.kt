package com.uniflow.uniflow.home

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.uniflow.uniflow.R

actual object ReminderNotificationScheduler {
    private const val channelId = "uniflow_reminders"
    private const val channelName = "UniFlow emlékeztetők"
    private const val permissionRequestCode = 4107

    actual fun requestPermissionIfNeeded() {
        val activity = AndroidAppContextHolder.activity ?: return
        ensureChannel(activity)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    permissionRequestCode
                )
            }
        }
    }

    actual fun schedule(reminder: LessonReminderUi) {
        val context = AndroidAppContextHolder.context
        if (!reminder.isEnabled) {
            cancel(reminder.id)
            return
        }
        if (reminder.triggerAt <= System.currentTimeMillis()) {
            cancel(reminder.id)
            return
        }

        ensureChannel(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = pendingIntent(context, reminder, PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.cancel(pendingIntent)

        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminder.triggerAt,
                    pendingIntent
                )
            }

            else -> {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    reminder.triggerAt,
                    pendingIntent
                )
            }
        }
    }

    actual fun cancel(reminderId: Long) {
        val context = AndroidAppContextHolder.context
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = pendingIntent(
            context = context,
            reminderId = reminderId,
            title = "",
            body = "",
            flag = PendingIntent.FLAG_NO_CREATE
        )
        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
        NotificationManagerCompat.from(context).cancel(reminderId.toInt())
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (notificationManager.getNotificationChannel(channelId) != null) return

        notificationManager.createNotificationChannel(
            NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Órákhoz tartozó emlékeztetők"
            }
        )
    }

    private fun pendingIntent(
        context: Context,
        reminder: LessonReminderUi,
        flag: Int
    ): PendingIntent = pendingIntent(
        context = context,
        reminderId = reminder.id,
        title = reminder.title,
        body = buildBody(reminder),
        flag = flag
    ) ?: error("PendingIntent létrehozása sikertelen")

    private fun pendingIntent(
        context: Context,
        reminderId: Long,
        title: String,
        body: String,
        flag: Int
    ): PendingIntent? {
        val intent = Intent(context, ReminderNotificationReceiver::class.java).apply {
            putExtra("reminder_id", reminderId)
            putExtra("title", title)
            putExtra("body", body)
        }

        val immutableFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            0
        }

        return PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            flag or immutableFlag
        )
    }

    private fun buildBody(reminder: LessonReminderUi): String {
        val lessonPart = reminder.lessonCode?.let { "Óra: $it" }
        return listOfNotNull(
            lessonPart,
            reminder.description?.takeIf { it.isNotBlank() }
        ).joinToString(" • ").ifBlank { "Ideje megnézni az emlékeztetőt." }
    }
}

class ReminderNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra("reminder_id", 0L)
        val title = intent.getStringExtra("title").orEmpty().ifBlank { "UniFlow emlékeztető" }
        val body = intent.getStringExtra("body").orEmpty().ifBlank { "Új emlékeztetőd van." }

        val builder = NotificationCompat.Builder(context, "uniflow_reminders")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        NotificationManagerCompat.from(context).notify(reminderId.toInt(), builder.build())
    }
}
