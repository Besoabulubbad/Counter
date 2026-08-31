package com.abulubad.counter.ui.grid

import com.abulubad.counter.domain.Reservation
import com.abulubad.counter.domain.ReservationStatus
import com.abulubad.counter.domain.Slot

data class Counters(
    val total: Int,
    val booked: Int,
    val paid: Int,
    val noShows: Int,
    val available: Int,
)

data class GridRow(
    val slot: Slot,
    val cells: List<Reservation?>,
)

data class GridUiState(
    val rows: List<GridRow>,
    val positions: Int,
    val counters: Counters,
) {
    companion object {
        val Empty = GridUiState(emptyList(), 0, Counters(0, 0, 0, 0, 0))
    }
}

fun buildGrid(slots: List<Slot>, reservations: List<Reservation>): GridUiState {
    if (slots.isEmpty()) return GridUiState.Empty
    val bySlot = reservations.groupBy { it.slotId }
    val rows = slots
        .sortedWith(compareBy({ it.startsAt }, { it.subResourceId?.value ?: "" }, { it.id.value }))
        .map { slot ->
            val byPosition = bySlot[slot.id].orEmpty().associateBy { it.position }
            GridRow(slot, (0 until slot.capacity).map { byPosition[it] })
        }
    val totalPositions = slots.sumOf { it.capacity }
    val active = reservations.filter { it.status != ReservationStatus.CANCELLED }
    val counters = Counters(
        total = totalPositions,
        booked = active.size,
        paid = active.count { it.priceMinorUnits > 0 && it.paidMinorUnits >= it.priceMinorUnits },
        noShows = reservations.count { it.status == ReservationStatus.NO_SHOW },
        available = totalPositions - active.size,
    )
    return GridUiState(rows, slots.maxOf { it.capacity }, counters)
}