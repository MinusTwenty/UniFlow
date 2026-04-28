package com.uniflow.uniflow.home

import kotlin.time.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.daysUntil
import kotlin.time.ExperimentalTime

/**
 * Static week utilities (ISO week, week-year, academic week, parity).
 * Pure functions, no globals.
 */
object WeekUtil {

    // --- Public API ---

    /** Today's ISO week number and week-year based on the local timezone. */
    fun currentIsoWeekInfo(tz: TimeZone = TimeZone.currentSystemDefault()): IsoWeekInfo {
        val today = today(tz)
        return isoWeekInfo(today)
    }

    /** ISO week and week-year for any date. */
    fun isoWeekInfo(date: LocalDate): IsoWeekInfo {
        val dow = isoDow(date.dayOfWeek)
        val thursday = date.plus(DatePeriod(days = 4 - dow))          // Thursday determines the week-year
        val weekYear = thursday.year

        val jan4 = LocalDate(weekYear, 1, 4)                          // Week 1 always contains Jan 4 (ISO)
        val week1Monday = jan4.minus(DatePeriod(days = isoDow(jan4.dayOfWeek) - 1))

        val thisMonday = date.minus(DatePeriod(days = dow - 1))
        val daysBetween = week1Monday.daysUntil(thisMonday)
        val week = (daysBetween / 7) + 1

        return IsoWeekInfo(week = week, weekYear = weekYear)
    }

    /**
     * Academic week = 1 + full weeks elapsed since semesterStart.
     * Returns null if date < semesterStart, or if semesterEnd != null and date > semesterEnd.
     */
    fun academicWeek(
        date: LocalDate,
        semesterStart: LocalDate,
        semesterEnd: LocalDate? = null
    ): Int? {
        if (date < semesterStart) return null
        if (semesterEnd != null && date > semesterEnd) return null

        // Align to Mondays for an “academic week” that starts on Monday.
        val startMonday = semesterStart.minus(DatePeriod(days = isoDow(semesterStart.dayOfWeek) - 1))
        val dateMonday = date.minus(DatePeriod(days = isoDow(date.dayOfWeek) - 1))
        val weeks = startMonday.daysUntil(dateMonday) / 7
        return weeks + 1 // 1-based
    }

    /** Even/odd ISO week parity for a date. */
    fun isoWeekParity(date: LocalDate): WeekParity =
        if (isoWeekInfo(date).week % 2 == 0) WeekParity.Even else WeekParity.Odd

    /** Convenience: get today's LocalDate in a timezone. */
    @OptIn(ExperimentalTime::class)
    fun today(tz: TimeZone = TimeZone.currentSystemDefault()): LocalDate =
        Clock.System.now().toLocalDateTime(tz).date

    // --- Helpers ---

    data class IsoWeekInfo(val week: Int, val weekYear: Int)
    enum class WeekParity { Even, Odd }

    private fun isoDow(d: DayOfWeek): Int = when (d) {
        DayOfWeek.MONDAY -> 1
        DayOfWeek.TUESDAY -> 2
        DayOfWeek.WEDNESDAY -> 3
        DayOfWeek.THURSDAY -> 4
        DayOfWeek.FRIDAY -> 5
        DayOfWeek.SATURDAY -> 6
        DayOfWeek.SUNDAY -> 7
    }
}

fun findActiveLessonRemainingSeconds(nowSeconds: Int, lessons: List<LessonCard>): Int? {
    for (lesson in lessons) {
        val range = parseRangeToSeconds(lesson.time) ?: continue
        val start = range.first
        val end = range.second
        if (nowSeconds in start until end) {
            return (end - nowSeconds).coerceAtLeast(0)
        }
    }
    return null
}
