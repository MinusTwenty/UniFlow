package com.uniflow.uniflow.data

import app.cash.sqldelight.db.SqlDriver
import com.uniflow.database.UniFlowDatabase
import com.uniflow.uniflow.data.seed.seedAllDemoData

expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}

fun provideDatabase(factory: DatabaseDriverFactory): UniFlowDatabase {
    val db = UniFlowDatabase(factory.createDriver())
    seedAllDemoData(db)
    return db
}
