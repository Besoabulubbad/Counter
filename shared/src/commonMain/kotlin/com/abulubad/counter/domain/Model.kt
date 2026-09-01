package com.abulubad.counter.domain

import kotlin.time.Instant

data class Resource(
    val id: ResourceId,
    val name: String,
    val subResources: List<SubResource>,
)

data class SubResource(
    val id: SubResourceId,
    val name: String,
)

data class Rate(
    val code: String,
    val name: String,
    val priceMinorUnits: Long,
    val currency: String,
)

data class Slot(
    val id: SlotId,
    val resourceId: ResourceId,
    val subResourceId: SubResourceId?,
    val startsAt: Instant,
    val capacity: Int,
    val rate: Rate,
)

enum class DurationCode { NINE, EIGHTEEN }

enum class ReservationStatus { HELD, CONFIRMED, CHECKED_IN, NO_SHOW, CANCELLED }

fun ReservationStatus.next(): ReservationStatus? = when (this) {
    ReservationStatus.HELD -> ReservationStatus.CONFIRMED
    ReservationStatus.CONFIRMED -> ReservationStatus.CHECKED_IN
    else -> null
}

data class Reservation(
    val id: ReservationId,
    val slotId: SlotId,
    val position: Int,
    val partySize: Int,
    val holderName: String,
    val isGuest: Boolean,
    val duration: DurationCode,
    val rateCode: String,
    val priceMinorUnits: Long,
    val paidMinorUnits: Long,
    val bookedOnline: Boolean,
    val hasCart: Boolean,
    val status: ReservationStatus,
    val version: Long,
) {
    val balanceMinorUnits: Long get() = priceMinorUnits - paidMinorUnits
    val isPartiallyPaid: Boolean get() = paidMinorUnits in 1 until priceMinorUnits
}