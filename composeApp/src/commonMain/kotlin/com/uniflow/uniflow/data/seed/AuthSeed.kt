package com.uniflow.uniflow.data.seed

import com.uniflow.database.UniFlowDatabase
import com.uniflow.uniflow.auth.hashPasswordWithSalt
import com.uniflow.uniflow.auth.PasswordHasher
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun seedDemoUsers(db: UniFlowDatabase) {
    val existingUsers = db.authQueries.countUsers().executeAsOne()

    if (existingUsers > 0L) return

    seedUser(db, username = "134288", password = "UniFlow123")
    seedUser(db, username = "user2", password = "UniFlow123")
    seedUser(db, username = "user3", password = "UniFlow123")
}

@OptIn(ExperimentalTime::class)
private fun seedUser(
    db: UniFlowDatabase,
    username: String,
    password: String
) {
    val salt = PasswordHasher.generateSalt()
    val hash = hashPasswordWithSalt(password, salt)
    val createdAt = Clock.System.now().toEpochMilliseconds()

    db.authQueries.insertUser(
        username = username,
        password_hash = hash,
        password_salt = salt,
        created_at = createdAt
    )
}