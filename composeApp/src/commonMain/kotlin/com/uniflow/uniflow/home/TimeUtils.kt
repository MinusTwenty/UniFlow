package com.uniflow.uniflow.home

// "12:15" -> minutes since midnight
fun parseTimeToMinutes(t: String): Int? {
    val p = t.trim().split(":")
    if (p.size != 2) return null
    val h = p[0].toIntOrNull() ?: return null
    val m = p[1].toIntOrNull() ?: return null
    return h * 60 + m
}

// "12:15–13:00" -> Pair(start,end) in minutes
fun parseRangeToMinutes(range: String): Pair<Int, Int>? {
    val parts = range.split("–") // en dash
    if (parts.size != 2) return null
    val s = parseTimeToMinutes(parts[0]) ?: return null
    val e = parseTimeToMinutes(parts[1]) ?: return null
    return s to e
}

fun statusFor(lesson: LessonCard, nowMinutes: Int): LessonStatus {
    val (s, e) = parseRangeToMinutes(lesson.time) ?: return LessonStatus.PAST
    return when {
        nowMinutes in s until e -> LessonStatus.ACTIVE
        nowMinutes < s -> LessonStatus.UPCOMING
        else -> LessonStatus.PAST
    }
}

// Break minutes between two lessons (end of a -> start of b); negative -> 0
fun breakBetween(a: LessonCard, b: LessonCard): Int? {
    val aRange = parseRangeToMinutes(a.time) ?: return null
    val bRange = parseRangeToMinutes(b.time) ?: return null
    return (bRange.first - aRange.second).coerceAtLeast(0)
}

/**
 * If now is BETWEEN lessons[i] (ended) and lessons[i+1] (not yet started),
 * return remaining minutes until the next lesson starts.
 */
fun findCurrentBreak(nowMinutes: Int, lessons: List<LessonCard>): BreakInfo? {
    if (lessons.size < 2) return null
    val items = lessons.mapNotNull { l ->
        parseRangeToMinutes(l.time)?.let { range -> Triple(l, range.first, range.second) } // (lesson, start, end)
    }
    for (i in 0 until items.size - 1) {
        val prevEnd = items[i].third
        val nextStart = items[i + 1].second
        if (nowMinutes in (prevEnd + 1) until nextStart) {
            val remaining = (nextStart - nowMinutes).coerceAtLeast(0)
            return BreakInfo(
                nextLessonCode = items[i + 1].first.code,
                nextStartMinutes = nextStart,
                remainingMinutes = remaining
            )
        }
    }
    return null
}

fun minutesToHHmm(mins: Int): String {
    val h = (mins / 60).toString().padStart(2, '0')
    val m = (mins % 60).toString().padStart(2, '0')
    return "$h:$m"
}