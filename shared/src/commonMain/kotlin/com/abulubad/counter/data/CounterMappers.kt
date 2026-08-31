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
import kotlin.time.Instant
import com.abulubad.counter.data.db.Reservation as ReservationRow
import com.abulubad.counter.data.db.Resource as ResourceRow
import com.abulubad.counter.data.db.Slot as SlotRow
import com.abulubad.counter.data.db.SubResource as SubResourceRow

internal fun SlotRow.toDomain(): Slot = Slot(
    id = SlotId(id),
    resourceId = ResourceId(resourceId),
    subResourceId = subResourceId?.let(::SubResourceId),
    startsAt = Instant.fromEpochMilliseconds(startsAtEpochMs),
    capacity = capacity.toInt(),
    rate = Rate(rateCode, rateName, ratePriceMinor, rateCurrency),
)

internal fun ReservationRow.toDomain(): Reservation = Reservation(
    id = ReservationId(id),
    slotId = SlotId(slotId),
    position = position.toInt(),
    partySize = partySize.toInt(),
    holderName = holderName,
    isGuest = isGuest,
    duration = DurationCode.valueOf(duration),
    rateCode = rateCode,
    priceMinorUnits = priceMinorUnits,
    paidMinorUnits = paidMinorUnits,
    bookedOnline = bookedOnline,
    hasCart = hasCart,
    status = ReservationStatus.valueOf(status),
    version = version,
)

internal fun resourcesToDomain(
    resources: List<ResourceRow>,
    subResources: List<SubResourceRow>,
): List<Resource> {
    val byResource = subResources.groupBy { it.resourceId }
    return resources.map { row ->
        Resource(
            id = ResourceId(row.id),
            name = row.name,
            subResources = byResource[row.id].orEmpty().map {
                SubResource(SubResourceId(it.id), it.name)
            },
        )
    }
}