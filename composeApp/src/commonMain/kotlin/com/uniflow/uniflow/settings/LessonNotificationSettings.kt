package com.uniflow.uniflow.settings

import com.russhwolf.settings.Settings

private const val LESSON_NOTIFICATION_KEY = "lesson_notification_lead_time"

enum class LessonNotificationLeadTime(val storageValue: String, val minutesBefore: Int?) {
    OFF("OFF", null),
    FIVE_MINUTES("FIVE_MINUTES", 5),
    TWENTY_MINUTES("TWENTY_MINUTES", 20),
    ONE_HOUR("ONE_HOUR", 60);

    val label: String
        get() = when (this) {
            OFF -> "Soha"
            FIVE_MINUTES -> "5 perc"
            TWENTY_MINUTES -> "20 perc"
            ONE_HOUR -> "1 óra"
        }
}

class LessonNotificationSettings(
    private val settings: Settings
) {
    fun getSavedLeadTime(): LessonNotificationLeadTime {
        val saved = settings.getStringOrNull(LESSON_NOTIFICATION_KEY)
            ?: return LessonNotificationLeadTime.TWENTY_MINUTES

        return LessonNotificationLeadTime.entries.firstOrNull { it.storageValue == saved }
            ?: LessonNotificationLeadTime.TWENTY_MINUTES
    }

    fun saveLeadTime(leadTime: LessonNotificationLeadTime) {
        settings.putString(LESSON_NOTIFICATION_KEY, leadTime.storageValue)
    }
}
