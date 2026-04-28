package com.uniflow.uniflow.home

import com.uniflow.database.UniFlowDatabase

fun loadLessonsForDay(
    db: UniFlowDatabase,
    userId: Long,
    termId: Long,
    dayOfWeek: Long
): List<LessonCard> {
    return db.lessonsQueries
        .getUserLessonsForDay(
            user_id = userId,
            term_id = termId,
            day_of_week = dayOfWeek
        )
        .executeAsList()
        .map { lesson ->
            val course = db.coursesQueries
                .getAllCourses()
                .executeAsList()
                .firstOrNull { it.id == lesson.course_id }

            val roomName = lesson.room_id?.let { roomId ->
                db.roomsQueries.getRoomById(roomId).executeAsOneOrNull()?.name
            } ?: "-"

            val teacherName = lesson.teacher_id?.let { teacherId ->
                db.teachersQueries.getTeacherById(teacherId).executeAsOneOrNull()?.name
            } ?: "-"

            LessonCard(
                lessonId = lesson.id,
                dayOfWeek = lesson.day_of_week,
                code = course?.code?.substringAfter("/") ?: "N/A",
                title = course?.name ?: "Ismeretlen tantárgy",
                time = "${lesson.start_time}-${lesson.end_time}",
                room = roomName,
                building = buildingNameFromRoom(roomName),
                teacher = teacherName,
                lessonType = lesson.lesson_type,
                groupCode = lesson.group_code,
                credits = course?.credits ?: 0L,
                weekType = lesson.week_type,
                note = lesson.note,
                validFrom = lesson.valid_from,
                validTo = lesson.valid_to
            )
        }
        .distinctBy {
            "${it.code}|${it.time}|${it.room}|${it.teacher}|${it.lessonType}|${it.groupCode}|${it.weekType}"
        }
}

fun buildingNameFromRoom(room: String?): String {
    if (room.isNullOrBlank()) return "-"

    val normalized = room.trim().uppercase()

    return when {
        normalized == "FITN" -> "Tornaterem"
        normalized.startsWith("DP") -> "Tiszti pav."
        normalized.startsWith("INFO") -> "Tiszti pav."
        normalized.startsWith("INF") -> "Tiszti pav."
        normalized.startsWith("K") -> "Konferencia"
        normalized.startsWith("G") -> "GIK épület"
        else -> "-"
    }
}
