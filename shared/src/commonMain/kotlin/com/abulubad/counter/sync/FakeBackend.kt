package com.abulubad.counter.sync

import com.abulubad.counter.data.db.OutboxEntry
import kotlinx.coroutines.delay

class FakeBackend {
    var latencyMillis: Long = 420

    suspend fun apply(entry: OutboxEntry) {
        delay(latencyMillis)
    }
}