package com.abulubad.counter.data

import com.abulubad.counter.data.db.CounterDatabase

fun createCounterDatabase(driverFactory: DatabaseDriverFactory): CounterDatabase =
    CounterDatabase(driverFactory.createDriver())