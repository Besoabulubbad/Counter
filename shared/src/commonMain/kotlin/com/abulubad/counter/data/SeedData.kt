package com.abulubad.counter.data

import com.abulubad.counter.domain.DurationCode
import com.abulubad.counter.domain.Rate
import com.abulubad.counter.domain.Reservation
import com.abulubad.counter.domain.ReservationId
import com.abulubad.counter.domain.ReservationStatus
import com.abulubad.counter.domain.Resource
import com.abulubad.counter.domain.ResourceId
import com.abulubad.counter.domain.Slot
import com.abulubad.counter.domain.SlotId
import com.abulubad.counter.domain.SubResource
import com.abulubad.counter.domain.SubResourceId
import kotlin.random.Random
import kotlin.time.Duration.Companion.minutes
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

data class SeedBundle(
    val resources: List<Resource>,
    val slots: List<Slot>,
    val reservations: List<Reservation>,
)

private val holderNames = listOf(
    "Alvarez", "Okonkwo", "Lindqvist", "Rahman", "Petrova", "Nguyen",
    "Delacroix", "Haddad", "Sorensen", "Yamamoto", "Costa", "Mbeki",
    "Kowalski", "Rossi", "Fernandez", "Andersen", "Kapoor", "Bianchi",
)

private val rates = listOf(
    Rate("STD", "Standard", 6500, "USD"),
    Rate("TWI", "Twilight", 4500, "USD"),
    Rate("MEM", "Member", 3000, "USD"),
)

fun buildSeed(seed: Int = 42): SeedBundle {
    val random = Random(seed)
    val resourceId = ResourceId("res-main")
    val front = SubResource(SubResourceId("sub-front"), "Front nine")
    val back = SubResource(SubResourceId("sub-back"), "Back nine")
    val resource = Resource(resourceId, "Riverbend", listOf(front, back))

    val zone = TimeZone.currentSystemDefault()
    val base = LocalDateTime(2026, 3, 11, 6, 0).toInstant(zone)
    val gaps = intArrayOf(7, 8, 10, 7, 9, 11, 8, 12)
    val timeCount = 200

    val slots = ArrayList<Slot>(timeCount * 2)
    var offset = 0
    for (i in 0 until timeCount) {
        val startsAt = base + offset.minutes
        val rate = rates[i % rates.size]
        for (sub in listOf(front, back)) {
            slots += Slot(
                id = SlotId("slot-${sub.id.value}-$i"),
                resourceId = resourceId,
                subResourceId = sub.id,
                startsAt = startsAt,
                capacity = 6,
                rate = rate,
            )
        }
        offset += gaps[i % gaps.size]
    }

    val statuses = listOf(
        ReservationStatus.HELD,
        ReservationStatus.CONFIRMED,
        ReservationStatus.CONFIRMED,
        ReservationStatus.CHECKED_IN,
        ReservationStatus.NO_SHOW,
        ReservationStatus.CANCELLED,
    )
    val reservations = ArrayList<Reservation>()
    var version = 1L
    for (slot in slots) {
        for (position in 0 until slot.capacity) {
            if (random.nextInt(100) >= 32) continue
            val status = statuses[random.nextInt(statuses.size)]
            val price = slot.rate.priceMinorUnits
            val paid = when {
                status == ReservationStatus.CHECKED_IN -> price
                random.nextInt(100) < 18 -> price / 2
                random.nextInt(100) < 40 -> price
                else -> 0L
            }
            reservations += Reservation(
                id = ReservationId("resv-${slot.id.value}-$position"),
                slotId = slot.id,
                position = position,
                partySize = 1 + random.nextInt(4),
                holderName = holderNames[random.nextInt(holderNames.size)],
                isGuest = random.nextInt(100) < 35,
                duration = if (random.nextBoolean()) DurationCode.EIGHTEEN else DurationCode.NINE,
                rateCode = slot.rate.code,
                priceMinorUnits = price,
                paidMinorUnits = paid,
                bookedOnline = random.nextInt(100) < 45,
                hasCart = random.nextInt(100) < 25,
                status = status,
                version = version++,
            )
        }
    }
    return SeedBundle(listOf(resource), slots, reservations)
}