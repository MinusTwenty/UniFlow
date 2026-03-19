package com.uniflow.uniflow.data.seed

import com.uniflow.database.UniFlowDatabase
import com.uniflow.uniflow.data.seed.model.DemoSemesterSeed

fun seedSemester(db: UniFlowDatabase, data: DemoSemesterSeed) {
    val user = db.authQueries.getUserByUsername(data.username).executeAsOneOrNull() ?: return
    val term = ensureTerm(db, data.termName, data.termStart, data.termEnd)

    data.lessons.forEach { lesson ->
        val teacher = lesson.teacherName?.let { ensureTeacher(db, it) }
        val room = lesson.roomName?.let { ensureRoom(db, it) }
        val course = ensureCourse(db, lesson.courseCode, lesson.courseName, lesson.credits)

        enrollIfMissing(db, user.id, term.id, course.id)

        try {
            db.lessonsQueries.insertLesson(
                course.id,
                term.id,
                lesson.dayOfWeek,
                lesson.startTime,
                lesson.endTime,
                lesson.weekType,
                lesson.groupCode,
                lesson.lessonType,
                room?.id,
                teacher?.id,
                lesson.validFrom,
                lesson.validTo,
                lesson.note
            )
        } catch (_: Throwable) {
        }

        val insertedLesson = db.lessonsQueries
            .getLessonsForCourseTerm(course.id, term.id)
            .executeAsList()
            .firstOrNull {
                it.day_of_week == lesson.dayOfWeek &&
                        it.start_time == lesson.startTime &&
                        it.end_time == lesson.endTime &&
                        it.group_code == lesson.groupCode
            }

        if (insertedLesson != null) {
            lesson.occurrenceDates.forEach { date ->
                try {
                    db.lessonOccurrencesQueries.insertOccurrence(insertedLesson.id, date)
                } catch (_: Throwable) {
                }
            }
        }
    }
}