package com.uniflow.uniflow.home

expect object ReminderNotificationScheduler {
    fun requestPermissionIfNeeded()
    fun schedule(reminder: LessonReminderUi)
    fun cancel(reminderId: Long)
}
