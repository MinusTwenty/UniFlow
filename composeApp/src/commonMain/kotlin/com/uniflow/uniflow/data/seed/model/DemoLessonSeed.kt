package com.uniflow.uniflow.data.seed.model

data class DemoLessonSeed(
    val courseCode: String,
    val courseName: String,
    val credits: Long = 0,
    val dayOfWeek: Long,
    val startTime: String,
    val endTime: String,
    val weekType: String,
    val groupCode: String,
    val lessonType: String,
    val roomName: String?,
    val teacherName: String?,
    val validFrom: String,
    val validTo: String,
    val note: String? = null,
    val occurrenceDates: List<String> = emptyList()
)