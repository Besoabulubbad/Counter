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
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

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
    val today = Clock.System.now().toLocalDateTime(zone)
    val base = LocalDateTime(today.year, today.month, today.day, 6, 0).toInstant(zone)
    val gaps = intArrayOf(7, 8, 10, 7, 9, 11, 8, 12)
    val timeCount = 90

    val slots = ArrayList<Slot>(timeCount * 2)
    val reservations = ArrayList<Reservation>()
    var version = 1L
    var offset = 0
    for (i in 0 until timeCount) {
        val startsAt = base + offset.minutes
        val rate = rates[i % rates.size]
        val density = when {
            i < 24 -> 0.78
            i < 80 -> 0.34
            else -> 0.10
        }
        for (sub in listOf(front, back)) {
            val slot = Slot(
                id = SlotId("slot-${sub.id.value}-$i"),
                resourceId = resourceId,
                subResourceId = sub.id,
                startsAt = startsAt,
                capacity = 6,
                rate = rate,
            )
            slots += slot
            for (position in 0 until slot.capacity) {
                if (random.nextDouble() >= density) continue
                val roll = random.nextDouble()
                val status = when {
                    roll < 0.14 -> ReservationStatus.HELD
                    roll < 0.72 -> ReservationStatus.CONFIRMED
                    roll < 0.90 -> ReservationStatus.CHECKED_IN
                    else -> ReservationStatus.NO_SHOW
                }
                val price = rate.priceMinorUnits
                val paid = when {
                    status == ReservationStatus.HELD -> 0L
                    random.nextDouble() < 0.22 -> ((price * (0.3 + random.nextDouble() * 0.4)).toLong() / 100) * 100
                    else -> price
                }
                reservations += Reservation(
                    id = ReservationId("resv-${slot.id.value}-$position"),
                    slotId = slot.id,
                    position = position,
                    partySize = 1 + random.nextInt(4),
                    holderName = holderNames[random.nextInt(holderNames.size)],
                    isGuest = random.nextInt(100) < 35,
                    duration = if (random.nextDouble() < 0.72) DurationCode.EIGHTEEN else DurationCode.NINE,
                    rateCode = rate.code,
                    priceMinorUnits = price,
                    paidMinorUnits = paid,
                    bookedOnline = random.nextInt(100) < 45,
                    hasCart = random.nextInt(100) < 30,
                    status = status,
                    version = version++,
                )
            }
        }
        offset += gaps[i % gaps.size]
    }
    return SeedBundle(listOf(resource), slots, reservations)
}