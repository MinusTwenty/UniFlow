package com.uniflow.uniflow.home

data class LessonReminderUi(
    val id: Long,
    val title: String,
    val description: String?,
    val reminderType: String,
    val triggerAt: Long,
    val isEnabled: Boolean,
    val createdAt: Long
)