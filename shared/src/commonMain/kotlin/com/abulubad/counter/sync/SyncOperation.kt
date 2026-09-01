package com.abulubad.counter.sync

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

const val MutationAdvanceStatus = "advanceReservationStatus"
const val MutationBook = "bookReservation"
const val MutationMove = "moveReservation"

@Serializable
data class AdvanceStatusInput(
    val reservationId: String,
    val status: String,
    val expectedVersion: Long,
)

@Serializable
data class BookInput(
    val reservationId: String,
    val slotId: String,
    val position: Int,
    val partySize: Int,
    val holderName: String,
    val duration: String,
    val rateCode: String,
    val priceMinorUnits: Long,
)

@Serializable
data class MoveInput(
    val reservationId: String,
    val toSlotId: String,
    val toPosition: Int,
    val expectedVersion: Long,
)

val SyncJson = Json { encodeDefaults = true }

data class ConflictInfo(
    val seq: Long,
    val reservationId: String,
    val serverVersion: Long,
)