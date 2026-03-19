package com.uniflow.uniflow.data.seed

import com.uniflow.database.UniFlowDatabase
import com.uniflow.uniflow.data.seed.semesters.user1Semester1Seed

fun seedAllDemoData(db: UniFlowDatabase) {
    seedDemoUsers(db)

    listOf(
        user1Semester1Seed
    ).forEach { seedSemester(db, it) }
}