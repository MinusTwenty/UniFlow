package com.uniflow.uniflow.data.seed

import com.uniflow.database.UniFlowDatabase

internal fun ensureTeacher(db: UniFlowDatabase, name: String) =
    db.teachersQueries.getTeacherByName(name).executeAsOneOrNull()
        ?: run {
            db.teachersQueries.insertTeacher(name)
            db.teachersQueries.getTeacherByName(name).executeAsOne()
        }

internal fun ensureRoom(db: UniFlowDatabase, name: String) =
    db.roomsQueries.getRoomByName(name).executeAsOneOrNull()
        ?: run {
            db.roomsQueries.insertRoom(name)
            db.roomsQueries.getRoomByName(name).executeAsOne()
        }

internal fun ensureCourse(
    db: UniFlowDatabase,
    code: String,
    name: String,
    credits: Long = 0L
) =
    db.coursesQueries.getCourseByCode(code).executeAsOneOrNull()
        ?: run {
            db.coursesQueries.insertCourse(code, name, credits)
            db.coursesQueries.getCourseByCode(code).executeAsOne()
        }

internal fun ensureTerm(
    db: UniFlowDatabase,
    name: String,
    start: String,
    end: String
) =
    db.termsQueries.getTermByName(name).executeAsOneOrNull()
        ?: run {
            db.termsQueries.insertTerm(name, start, end)
            db.termsQueries.getTermByName(name).executeAsOne()
        }

internal fun enrollIfMissing(
    db: UniFlowDatabase,
    userId: Long,
    termId: Long,
    courseId: Long
) {
    try {
        db.enrollmentQueries.enroll(userId, termId, courseId)
    } catch (_: Throwable) {
    }
}