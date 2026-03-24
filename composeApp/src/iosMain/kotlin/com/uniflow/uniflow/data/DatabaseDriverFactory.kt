package com.uniflow.uniflow.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.uniflow.database.UniFlowDatabase

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(UniFlowDatabase.Schema, "uniflow.db")
    }
}