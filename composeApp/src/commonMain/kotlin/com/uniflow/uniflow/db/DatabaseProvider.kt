package com.uniflow.uniflow.db

import com.uniflow.database.UniFlowDatabase
import app.cash.sqldelight.db.SqlDriver

expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}

fun provideDatabase(factory: DatabaseDriverFactory): UniFlowDatabase {
    return UniFlowDatabase(factory.createDriver())
}