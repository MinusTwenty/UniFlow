package com.uniflow.uniflow.home

data class LessonCard(
    val code: String,      // e.g. "TOR"
    val time: String,      // e.g. "12:15 - 13:00"
    val room: String,      // e.g. "B-02" or "G312"
    val teacher: String    // e.g. "Tanár: XY"
)