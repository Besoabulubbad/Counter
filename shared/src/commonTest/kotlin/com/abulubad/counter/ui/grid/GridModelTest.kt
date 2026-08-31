package com.abulubad.counter.ui.grid

import com.abulubad.counter.data.buildSeed
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
}