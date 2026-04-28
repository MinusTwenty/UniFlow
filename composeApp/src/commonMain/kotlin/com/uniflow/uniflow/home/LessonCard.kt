package com.uniflow.uniflow.home

data class LessonCard(
    val lessonId: Long,
    val dayOfWeek: Long,
    val code: String,
    val title: String,
    val time: String,
    val room: String,
    val building: String,
    val teacher: String,
    val lessonType: String,
    val groupCode: String,
    val credits: Long,
    val weekType: String,
    val note: String?,
    val validFrom: String = "",
    val validTo: String = ""
)
