package com.abulubad.counter.sync

import com.abulubad.counter.data.db.OutboxEntry
import kotlinx.coroutines.delay
import kotlinx.serialization.decodeFromString

sealed interface SyncResult {
    data object Applied : SyncResult
    data class Conflict(val serverVersion: Long) : SyncResult
}

class FakeBackend {
    var latencyMillis: Long = 420
    var forceConflict: Boolean = false

    suspend fun apply(entry: OutboxEntry): SyncResult {
        delay(latencyMillis)
        if (!forceConflict) return SyncResult.Applied
        val input = SyncJson.decodeFromString<AdvanceStatusInput>(entry.payload)
        return SyncResult.Conflict(serverVersion = input.expectedVersion + 1)
    }
}