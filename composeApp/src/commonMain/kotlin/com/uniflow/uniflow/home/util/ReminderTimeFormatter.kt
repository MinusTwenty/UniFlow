package com.uniflow.uniflow.home

import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime

@OptIn(kotlin.time.ExperimentalTime::class)
fun formatReminderTriggerAt(epochMillis: Long): String {
    val dt = Instant.fromEpochMilliseconds(epochMillis)
        .toLocalDateTime(TimeZone.currentSystemDefault())

    val y = dt.date.year
    val m = dt.date.month.number.toString().padStart(2, '0')
    val d = dt.date.day.toString().padStart(2, '0')
    val hh = dt.hour.toString().padStart(2, '0')
    val mm = dt.minute.toString().padStart(2, '0')

    return "$y.$m.$d $hh:$mm"
}
