@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.uniflow.uniflow.home

import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter
import platform.posix.time

actual object ReminderNotificationScheduler {
    actual fun requestPermissionIfNeeded() {
        UNUserNotificationCenter.currentNotificationCenter().requestAuthorizationWithOptions(
            options = UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
        ) { _, _ -> }
    }

    actual fun schedule(reminder: LessonReminderUi) {
        if (!reminder.isEnabled) {
            cancel(reminder.id)
            return
        }

        val secondsUntilTrigger = ((reminder.triggerAt - currentTimeMillis()) / 1000.0).coerceAtLeast(1.0)
        if (secondsUntilTrigger <= 1.0 && reminder.triggerAt < currentTimeMillis()) {
            cancel(reminder.id)
            return
        }

        val content = UNMutableNotificationContent().apply {
            setTitle(reminder.title)
            setBody(buildBody(reminder))
            setSound(platform.UserNotifications.UNNotificationSound.defaultSound)
        }

        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(
            timeInterval = secondsUntilTrigger,
            repeats = false
        )

        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = reminder.id.toString(),
            content = content,
            trigger = trigger
        )

        val center = UNUserNotificationCenter.currentNotificationCenter()
        center.removePendingNotificationRequestsWithIdentifiers(listOf(reminder.id.toString()))
        center.addNotificationRequest(request) { _ -> }
    }

    actual fun cancel(reminderId: Long) {
        val identifier = reminderId.toString()
        val center = UNUserNotificationCenter.currentNotificationCenter()
        center.removePendingNotificationRequestsWithIdentifiers(listOf(identifier))
        center.removeDeliveredNotificationsWithIdentifiers(listOf(identifier))
    }

    private fun buildBody(reminder: LessonReminderUi): String {
        val lessonPart = reminder.lessonCode?.let { "Óra: $it" }
        return listOfNotNull(
            lessonPart,
            reminder.description?.takeIf { it.isNotBlank() }
        ).joinToString(" • ").ifBlank { "Ideje megnézni az emlékeztetőt." }
    }

    private fun currentTimeMillis(): Long = time(null).toLong() * 1000L
}
