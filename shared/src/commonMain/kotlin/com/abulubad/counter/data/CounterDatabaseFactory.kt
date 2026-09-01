package com.abulubad.counter.data

import com.abulubad.counter.data.db.CounterDatabase

fun createCounterDatabase(driverFactory: DatabaseDriverFactory): CounterDatabase {
    val driver = driverFactory.createDriver()
    driver.execute(null, "PRAGMA foreign_keys = ON", 0)
    return CounterDatabase(driver)
}