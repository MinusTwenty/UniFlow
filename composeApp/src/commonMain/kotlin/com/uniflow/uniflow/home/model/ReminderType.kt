package com.uniflow.uniflow.home

enum class ReminderType(val dbValue: String, val label: String) {
    GENERAL("GENERAL", "Tanulás"),
    EXAM("EXAM", "ZH / Vizsga"),
    ASSIGNMENT("ASSIGNMENT", "Beadandó"),
    LESSON("LESSON", "Óra");

    companion object {
        fun fromDb(value: String): ReminderType {
            return entries.firstOrNull { it.dbValue == value } ?: GENERAL
        }
    }
}
