package com.uniflow.uniflow.data.seed

import com.uniflow.database.UniFlowDatabase
import com.uniflow.uniflow.data.seed.semesters.user1Semester1Seed
import com.uniflow.uniflow.data.seed.semesters.user1Semester2Seed
import com.uniflow.uniflow.data.seed.semesters.user2Semester1Seed
import com.uniflow.uniflow.data.seed.semesters.user2Semester2Seed
import com.uniflow.uniflow.data.seed.semesters.user3Semester1Seed
import com.uniflow.uniflow.data.seed.semesters.user3Semester2Seed
fun seedAllDemoData(db: UniFlowDatabase) {
    seedDemoUsers(db)

    listOf(
        user1Semester1Seed,
        user1Semester2Seed,
        user2Semester1Seed,
        user2Semester2Seed,
        user3Semester1Seed,
        user3Semester2Seed
    ).forEach { seedSemester(db, it) }
}