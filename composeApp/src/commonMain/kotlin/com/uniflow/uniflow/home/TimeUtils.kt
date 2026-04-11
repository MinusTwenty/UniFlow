package com.uniflow.uniflow.home

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Clock


/**
 * Pure, static helpers for lesson time handling.
 * Uses kotlinx-datetime (KMP-safe).
 */

// Accept EN DASH or simple hyphen between times
private val RANGE_SPLIT = Regex("[–-]")

// "HH:mm" or "HH:mm:ss" -> seconds since midnight
fun parseTimeToSeconds(t: String): Int? {
    val parts = t.trim().split(":")
    if (parts.size !in 2..3) return null
    val h = parts[0].toIntOrNull() ?: return null
    val m = parts[1].toIntOrNull() ?: return null
    val s = if (parts.size == 3) parts[2].toIntOrNull() ?: return null else 0
    if (h !in 0..23 || m !in 0..59 || s !in 0..59) return null
    return h * 3600 + m * 60 + s
}

// "HH:mm–HH:mm" or "HH:mm:ss–HH:mm:ss" -> (start,end) seconds
fun parseRangeToSeconds(range: String): Pair<Int, Int>? {
    val parts = range.split(RANGE_SPLIT)
    if (parts.size != 2) return null
    val s = parseTimeToSeconds(parts[0]) ?: return null
    val e = parseTimeToSeconds(parts[1]) ?: return null
    return s to e
}

fun statusFor(lesson: LessonCard, nowSeconds: Int): LessonStatus {
    val (s, e) = parseRangeToSeconds(lesson.time) ?: return LessonStatus.PAST
    return when {
        nowSeconds in s until e -> LessonStatus.ACTIVE
        nowSeconds < s -> LessonStatus.UPCOMING
        else -> LessonStatus.PAST
    }
}

// Kept for callers that use minutes
fun breakBetween(a: LessonCard, b: LessonCard): Int? {
    val aRange = parseRangeToSeconds(a.time) ?: return null
    val bRange = parseRangeToSeconds(b.time) ?: return null
    return ((bRange.first - aRange.second) / 60).coerceAtLeast(0)
}

/**
 * Returns remaining seconds until the next lesson starts when:
 *  - now is BEFORE the first lesson of the day, or
 *  - now is BETWEEN any two lessons (after one ended, before the next starts).
 * Otherwise returns null (during lesson or after the last one).
 */
fun findBreakRemainingSeconds(nowSeconds: Int, lessons: List<LessonCard>): Int? {
    if (lessons.isEmpty()) return null
    val ranges = lessons.mapNotNull { l ->
        parseRangeToSeconds(l.time)?.let { (start, end) -> start to end }
    }
    if (ranges.isEmpty()) return null

    // Before first lesson
    val firstStart = ranges.first().first
    if (nowSeconds < firstStart) {
        return (firstStart - nowSeconds).coerceAtLeast(0)
    }

    // Between lessons
    for (i in 0 until ranges.lastIndex) {
        val endPrev = ranges[i].second
        val startNext = ranges[i + 1].first
        if (nowSeconds in endPrev until startNext) {
            return (startNext - nowSeconds).coerceAtLeast(0)
        }
    }

    // During a lesson or after last lesson
    return null
}

/** Current local time since midnight in seconds (KMP-safe with kotlinx-datetime). */
@OptIn(ExperimentalTime::class)
    fun currentSecondsSinceMidnight(): Int {
        val t = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time
        println(t)
        return t.hour * 3600 + t.minute * 60 + t.second }

/** Format: if >= 1 hour -> "x ó y p", else -> "x p y mp" */
fun formatHuDurationDynamic(seconds: Int): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h >= 1) "${h} ó ${m} p" else "${m} p ${s} mp"
}

@OptIn(ExperimentalTime::class)
fun currentDayOfWeekIso(): Int {
    val date = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date

    return date.dayOfWeek.isoDayNumber
}