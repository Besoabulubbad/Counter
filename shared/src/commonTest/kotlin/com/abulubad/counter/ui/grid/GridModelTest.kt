package com.abulubad.counter.ui.grid

import com.abulubad.counter.data.buildSeed
import com.abulubad.counter.domain.DurationCode
import com.abulubad.counter.domain.Rate
import com.abulubad.counter.domain.Reservation
import com.abulubad.counter.domain.ReservationId
import com.abulubad.counter.domain.ReservationStatus
import com.abulubad.counter.domain.ResourceId
import com.abulubad.counter.domain.Slot
import com.abulubad.counter.domain.SlotId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

class GridModelTest {
    @Test
    fun buildsARowForEverySlot() {
        val seed = buildSeed()
        val grid = buildGrid(seed.slots, seed.reservations)
        assertEquals(seed.slots.size, grid.rows.size)
        assertEquals(6, grid.positions)
    }

    @Test
    fun rowsAreOrderedByTime() {
        val grid = buildGrid(buildSeed().slots, emptyList())
        val starts = grid.rows.map { it.slot.startsAt }
        assertEquals(starts.sorted(), starts)
    }

    @Test
    fun everyCellListMatchesCapacity() {
        val grid = buildGrid(buildSeed().slots, buildSeed().reservations)
        assertTrue(grid.rows.all { it.cells.size == it.slot.capacity })
    }

    @Test
    fun countersReconcile() {
        val grid = buildGrid(buildSeed().slots, buildSeed().reservations)
        assertEquals(grid.counters.slots * 6, grid.counters.open + grid.counters.booked)
        assertTrue(grid.counters.booked > 0)
        assertTrue(grid.counters.held > 0)
    }

    @Test
    fun moveRejectsOccupiedPastAndOutOfRange() {
        val now = Clock.System.now()
        val cells = listOf<Reservation?>(reservationAt(0), null, null, null, null, null)
        val futureRow = GridRow(slotAt(now + 1.hours), "", cells)
        assertTrue(canMoveTo(futureRow, 1, now))
        assertFalse(canMoveTo(futureRow, 0, now))
        assertFalse(canMoveTo(futureRow, 6, now))
        assertFalse(canMoveTo(GridRow(slotAt(now - 1.hours), "", cells), 1, now))
    }

    private fun slotAt(instant: Instant, capacity: Int = 6) = Slot(
        id = SlotId("s1"),
        resourceId = ResourceId("r1"),
        subResourceId = null,
        startsAt = instant,
        capacity = capacity,
        rate = Rate("WD", "Weekday", 6500, "USD"),
    )

    private fun reservationAt(position: Int) = Reservation(
        id = ReservationId("x"),
        slotId = SlotId("s1"),
        position = position,
        partySize = 1,
        holderName = "A",
        isGuest = false,
        duration = DurationCode.EIGHTEEN,
        rateCode = "WD",
        priceMinorUnits = 6500,
        paidMinorUnits = 0,
        bookedOnline = false,
        hasCart = false,
        status = ReservationStatus.HELD,
        version = 1,
    )
}