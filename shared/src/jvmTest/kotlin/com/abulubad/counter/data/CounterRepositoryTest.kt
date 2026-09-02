package com.abulubad.counter.data

import com.abulubad.counter.sync.MutationMove
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class CounterRepositoryTest {
    private suspend fun seededRepository(): CounterRepository =
        CounterRepository(createCounterDatabase(DatabaseDriverFactory())).also { it.seedIfEmpty() }

    @Test
    fun moveRejectsOccupiedTargetCell() = runBlocking {
        val repo = seededRepository()
        val shared = repo.reservations().first().groupBy { it.slotId }.values.first { it.size >= 2 }
        val mover = shared[0]
        val occupant = shared[1]

        val moved = repo.move(mover.id, occupant.slotId, occupant.position, mover.version)

        assertFalse(moved)
        val afterMover = repo.reservations().first().first { it.id == mover.id }
        assertEquals(mover.slotId, afterMover.slotId)
        assertEquals(mover.position, afterMover.position)
        assertTrue(repo.pending().none { it.operation == MutationMove })
    }

    @Test
    fun moveSucceedsToEmptyCell() = runBlocking {
        val repo = seededRepository()
        val reservations = repo.reservations().first()
        val takenBySlot = reservations.groupBy { it.slotId }.mapValues { entry -> entry.value.map { it.position }.toSet() }
        val target = repo.slots().first().firstNotNullOf { slot ->
            (0 until slot.capacity).firstOrNull { it !in takenBySlot[slot.id].orEmpty() }?.let { slot.id to it }
        }
        val mover = reservations.first { it.slotId != target.first }

        val moved = repo.move(mover.id, target.first, target.second, mover.version)

        assertTrue(moved)
        val afterMover = repo.reservations().first().first { it.id == mover.id }
        assertEquals(target.first, afterMover.slotId)
        assertEquals(target.second, afterMover.position)
        assertTrue(repo.pending().any { it.operation == MutationMove })
    }
}