package com.abulubad.counter.sync

import com.abulubad.counter.data.CounterRepository
import com.abulubad.counter.data.DatabaseDriverFactory
import com.abulubad.counter.data.createCounterDatabase
import com.abulubad.counter.data.db.OutboxEntry
import com.abulubad.counter.domain.ReservationStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.encodeToString

class SyncEngineTest {
    private suspend fun seededRepository(): CounterRepository =
        CounterRepository(createCounterDatabase(DatabaseDriverFactory())).also { it.seedIfEmpty() }

    @Test
    fun outboxSurvivesOfflineRoundTrip() = runBlocking {
        val repo = seededRepository()
        val engine = SyncEngine(repo, FakeBackend().apply { latencyMillis = 0 })
        engine.setOffline(true)
        engine.start()

        val reservation = repo.reservations().first().first()
        repo.setStatus(reservation.id, ReservationStatus.CONFIRMED, reservation.version)
        assertTrue(repo.pending().isNotEmpty())

        engine.setOffline(false)
        withTimeout(5_000) { repo.outboxDepth().first { it == 0L } }
        assertEquals(0L, repo.outboxDepth().first())
        engine.stop()
    }

    @Test
    fun versionConflictSurfacesThenRetryResolves() = runBlocking {
        val repo = seededRepository()
        val engine = SyncEngine(repo, FakeBackend().apply { latencyMillis = 0 })
        engine.setForceConflict(true)
        engine.start()

        val reservation = repo.reservations().first().first()
        repo.setStatus(reservation.id, ReservationStatus.CONFIRMED, reservation.version)

        val info = withTimeout(5_000) { engine.conflict.first { it != null } }
        assertNotNull(info)
        assertEquals(reservation.id.value, info.reservationId)
        assertEquals(reservation.version + 1, info.serverVersion)

        engine.resolveRetry()
        withTimeout(5_000) { repo.outboxDepth().first { it == 0L } }
        assertEquals(null, engine.conflict.first())
        engine.stop()
    }

    @Test
    fun backendRejectsSecondBookingOfSameCell() = runBlocking {
        val backend = FakeBackend().apply { latencyMillis = 0 }
        fun bookEntry(seq: Long, reservationId: String) = OutboxEntry(
            seq,
            MutationBook,
            SyncJson.encodeToString(BookInput(reservationId, "slot-1", 2, 1, "Walk-in", "EIGHTEEN", "WD", 6500L)),
            0L,
        )
        assertEquals(SyncResult.Applied, backend.apply(bookEntry(1L, "r-1")))
        assertTrue(backend.apply(bookEntry(2L, "r-2")) is SyncResult.Conflict)
    }
}