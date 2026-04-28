@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.uniflow.uniflow.home

import com.uniflow.uniflow.settings.LessonNotificationLeadTime
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlin.time.Clock

data class LessonNotificationSchedule(
    val notificationId: Long,
    val title: String,
    val body: String,
    val triggerAt: Long
)

expect object LessonNotificationScheduler {
    fun requestPermissionIfNeeded()
    fun scheduleAll(schedules: List<LessonNotificationSchedule>)
    fun cancelAll()
}

fun buildUpcomingLessonSchedules(
    lessons: List<LessonCard>,
    leadTime: LessonNotificationLeadTime,
    fromDate: LocalDate,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
    maxSchedules: Int = 32
): List<LessonNotificationSchedule> {
    val minutesBefore = leadTime.minutesBefore ?: return emptyList()
    val nowMillis = Clock.System.now().toEpochMilliseconds()
    val schedules = mutableListOf<LessonNotificationSchedule>()

    for (offset in 0..42) {
        if (schedules.size >= maxSchedules) break

        val date = fromDate.plus(DatePeriod(days = offset))
        val weekday = date.dayOfWeek.isoDayNumber.toLong()
        val parity = WeekUtil.isoWeekParity(date)

        lessons.forEach { lesson ->
            if (lesson.dayOfWeek != weekday) return@forEach
            if (!matchesWeekType(lesson.weekType, parity)) return@forEach
            if (!isWithinValidity(date, lesson.validFrom, lesson.validTo)) return@forEach

            val startParts = lesson.time.substringBefore('-').trim().split(":")
            val hour = startParts.getOrNull(0)?.toIntOrNull() ?: return@forEach
            val minute = startParts.getOrNull(1)?.toIntOrNull() ?: return@forEach

            val lessonStart = date.atTime(hour, minute).toInstant(timeZone)
            val triggerAt = lessonStart.toEpochMilliseconds() - minutesBefore * 60_000L
            if (triggerAt <= nowMillis) return@forEach

            schedules += LessonNotificationSchedule(
                notificationId = lessonNotificationId(lesson.lessonId, triggerAt),
                title = "${lesson.code} hamarosan kezdodik",
                body = buildString {
                    append("${lesson.title} • ${lesson.time}")
                    if (lesson.room.isNotBlank() && lesson.room != "-") {
                        append(" • ")
                        append(lesson.room)
                    }
                },
                triggerAt = triggerAt
            )
        }
    }

    return schedules
        .sortedBy { it.triggerAt }
        .distinctBy { it.notificationId }
        .take(maxSchedules)
}

private fun lessonNotificationId(lessonId: Long, triggerAt: Long): Long =
    (lessonId shl 32) xor (triggerAt / 60_000L)

private fun matchesWeekType(weekType: String, parity: WeekUtil.WeekParity): Boolean {
    return when (weekType.uppercase()) {
        "EVERY" -> true
        "ODD" -> parity == WeekUtil.WeekParity.Odd
        "EVEN" -> parity == WeekUtil.WeekParity.Even
        else -> true
    }
}

private fun isWithinValidity(date: LocalDate, validFrom: String, validTo: String): Boolean {
    val from = validFrom.takeIf { it.isNotBlank() }?.let(LocalDate::parse) ?: return true
    val to = validTo.takeIf { it.isNotBlank() }?.let(LocalDate::parse) ?: return true
    return date >= from && date <= to
}
