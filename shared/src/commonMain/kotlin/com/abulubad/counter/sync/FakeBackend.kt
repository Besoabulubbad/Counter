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
    private val versions = mutableMapOf<String, Long>()
    private val occupied = mutableSetOf<String>()

    suspend fun apply(entry: OutboxEntry): SyncResult {
        delay(latencyMillis)
        return when (entry.operation) {
            MutationAdvanceStatus -> {
                val input = SyncJson.decodeFromString<AdvanceStatusInput>(entry.payload)
                versioned(input.reservationId, input.expectedVersion)
            }
            MutationMove -> {
                val input = SyncJson.decodeFromString<MoveInput>(entry.payload)
                versioned(input.reservationId, input.expectedVersion)
            }
            MutationTakePayment -> {
                val input = SyncJson.decodeFromString<PaymentInput>(entry.payload)
                versioned(input.reservationId, input.expectedVersion)
            }
            MutationBook -> book(SyncJson.decodeFromString(entry.payload))
            else -> SyncResult.Applied
        }
    }

    private fun versioned(reservationId: String, expectedVersion: Long): SyncResult {
        val known = versions[reservationId]
        if (forceConflict && known == null) {
            val ahead = expectedVersion + 1
            versions[reservationId] = ahead
            return SyncResult.Conflict(ahead)
        }
        val server = known ?: expectedVersion
        if (expectedVersion < server) return SyncResult.Conflict(server)
        versions[reservationId] = expectedVersion + 1
        return SyncResult.Applied
    }

    private fun book(input: BookInput): SyncResult {
        val cell = "${input.slotId}:${input.position}"
        if (!occupied.add(cell)) return SyncResult.Conflict(versions[input.reservationId] ?: 1)
        versions[input.reservationId] = 1
        return SyncResult.Applied
    }
}