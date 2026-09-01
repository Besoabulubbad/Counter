package com.abulubad.counter.sync

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

const val MutationAdvanceStatus = "advanceReservationStatus"

@Serializable
data class AdvanceStatusInput(
    val reservationId: String,
    val status: String,
    val expectedVersion: Long,
)

val SyncJson = Json { encodeDefaults = true }