package com.abulubad.counter.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeedDataTest {
    @Test
    fun seedShapeIsStable() {
        val seed = buildSeed()
        assertEquals(400, seed.slots.size)
        assertEquals(1, seed.resources.size)
        assertEquals(2, seed.resources.single().subResources.size)
        assertTrue(seed.reservations.isNotEmpty())
    }

    @Test
    fun frontAndBackShareClockTimes() {
        val byTime = buildSeed().slots.groupBy { it.startsAt }
        assertTrue(byTime.values.any { it.size == 2 })
    }

    @Test
    fun slotIntervalsAreIrregular() {
        val starts = buildSeed().slots.map { it.startsAt }.distinct().sorted()
        val gaps = starts.zipWithNext { a, b -> b - a }.distinct()
        assertTrue(gaps.size > 1)
    }

    @Test
    fun someReservationsArePartiallyPaid() {
        assertTrue(buildSeed().reservations.any { it.isPartiallyPaid })
    }

    @Test
    fun seedIsDeterministic() {
        assertEquals(buildSeed().reservations.size, buildSeed().reservations.size)
    }
}