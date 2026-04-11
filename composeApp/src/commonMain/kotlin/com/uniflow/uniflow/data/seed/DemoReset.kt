package com.uniflow.uniflow.data.seed

import com.uniflow.database.UniFlowDatabase

fun resetAndReseedDemoData(db: UniFlowDatabase) {
    db.transaction {
        // Fontos: törlési sorrend a kapcsolatok miatt
        db.lessonOccurrencesQueries.deleteAllLessonOccurrences()
        db.enrollmentQueries.deleteAllEnrollments()
        db.lessonsQueries.deleteAllLessons()
        db.termsQueries.deleteAllTerms()
        db.coursesQueries.deleteAllCourses()
        db.roomsQueries.deleteAllRooms()
        db.teachersQueries.deleteAllTeachers()
        db.authQueries.deleteAllUsers()
    }

    seedAllDemoData(db)
}