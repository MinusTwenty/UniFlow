package com.uniflow.uniflow.settings

import com.russhwolf.settings.Settings

private const val LESSON_NOTIFICATION_LEAD_TIME_KEY = "lesson_notification_lead_time"

enum class LessonNotificationLeadTime(
    val label: String,
    val minutesBefore: Int
) {
    OFF("Kikapcsolva", 0),
    MINUTES_5("5 perccel elotte", 5),
    MINUTES_10("10 perccel elotte", 10),
    MINUTES_15("15 perccel elotte", 15),
    MINUTES_30("30 perccel elotte", 30)
}

class LessonNotificationSettings(
    private val settings: Settings
) {
    fun getSavedLeadTime(): LessonNotificationLeadTime {
        val saved = settings.getStringOrNull(LESSON_NOTIFICATION_LEAD_TIME_KEY)
            ?: return LessonNotificationLeadTime.OFF

        return runCatching {
            LessonNotificationLeadTime.valueOf(saved)
        }.getOrElse {
            LessonNotificationLeadTime.OFF
        }
    }

    fun saveLeadTime(leadTime: LessonNotificationLeadTime) {
        settings.putString(LESSON_NOTIFICATION_LEAD_TIME_KEY, leadTime.name)
    }
}
