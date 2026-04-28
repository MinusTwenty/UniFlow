@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.uniflow.uniflow.home

import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter

actual object LessonNotificationScheduler {
    private const val lessonPrefix = "lesson_auto_"

    actual fun requestPermissionIfNeeded() {
        UNUserNotificationCenter.currentNotificationCenter().requestAuthorizationWithOptions(
            options = UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
        ) { _, _ -> }
    }

    actual fun scheduleAll(schedules: List<LessonNotificationSchedule>) {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        center.getPendingNotificationRequestsWithCompletionHandler { requests ->
            val lessonIds = requests.orEmpty()
                .mapNotNull { it as? UNNotificationRequest }
                .map { it.identifier }
                .filter { it.startsWith(lessonPrefix) }

            if (lessonIds.isNotEmpty()) {
                center.removePendingNotificationRequestsWithIdentifiers(lessonIds)
                center.removeDeliveredNotificationsWithIdentifiers(lessonIds)
            }

            schedules.forEach { schedule ->
                val secondsUntilTrigger =
                    ((schedule.triggerAt - currentTimeMillis()) / 1000.0).coerceAtLeast(1.0)

                val content = UNMutableNotificationContent().apply {
                    setTitle(schedule.title)
                    setBody(schedule.body)
                    setSound(platform.UserNotifications.UNNotificationSound.defaultSound)
                }

                val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(
                    timeInterval = secondsUntilTrigger,
                    repeats = false
                )

                val request = UNNotificationRequest.requestWithIdentifier(
                    identifier = lessonPrefix + schedule.notificationId,
                    content = content,
                    trigger = trigger
                )

                center.addNotificationRequest(request) { _ -> }
            }
        }
    }

    actual fun cancelAll() {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        center.getPendingNotificationRequestsWithCompletionHandler { requests ->
            val lessonIds = requests.orEmpty()
                .mapNotNull { it as? UNNotificationRequest }
                .map { it.identifier }
                .filter { it.startsWith(lessonPrefix) }
            if (lessonIds.isNotEmpty()) {
                center.removePendingNotificationRequestsWithIdentifiers(lessonIds)
                center.removeDeliveredNotificationsWithIdentifiers(lessonIds)
            }
        }
    }

    private fun currentTimeMillis(): Long = platform.posix.time(null).toLong() * 1000L
}
