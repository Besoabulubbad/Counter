package com.abulubad.counter.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.abulubad.counter.data.db.CounterDatabase

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver =
        NativeSqliteDriver(CounterDatabase.Schema, "counter.db")
}