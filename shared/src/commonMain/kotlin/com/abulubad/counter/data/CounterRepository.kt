package com.abulubad.counter.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import com.abulubad.counter.data.db.CounterDatabase
import com.abulubad.counter.data.db.OutboxEntry
import com.abulubad.counter.domain.DurationCode
import com.abulubad.counter.domain.Reservation
import com.abulubad.counter.domain.ReservationId
import com.abulubad.counter.domain.ReservationStatus
import com.abulubad.counter.domain.Resource
import com.abulubad.counter.domain.Slot
import com.abulubad.counter.domain.SlotId
import com.abulubad.counter.domain.newReservationId
import com.abulubad.counter.sync.AdvanceStatusInput
import com.abulubad.counter.sync.BookInput
import com.abulubad.counter.sync.MoveInput
import com.abulubad.counter.sync.MutationAdvanceStatus
import com.abulubad.counter.sync.MutationBook
import com.abulubad.counter.sync.MutationMove
import com.abulubad.counter.sync.SyncJson
import kotlin.time.Clock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

class CounterRepository(private val database: CounterDatabase) {
    private val queries = database.counterQueries

    fun slots(): Flow<List<Slot>> =
        queries.selectAllSlots().asFlow().mapToList(Dispatchers.Default)
            .map { rows -> rows.map { it.toDomain() } }

    fun reservations(): Flow<List<Reservation>> =
        queries.selectAllReservations().asFlow().mapToList(Dispatchers.Default)
            .map { rows -> rows.map { it.toDomain() } }

    fun resources(): Flow<List<Resource>> =
        combine(
            queries.selectAllResources().asFlow().mapToList(Dispatchers.Default),
            queries.selectAllSubResources().asFlow().mapToList(Dispatchers.Default),
        ) { resources, subs -> resourcesToDomain(resources, subs) }

    suspend fun setStatus(id: ReservationId, status: ReservationStatus, expectedVersion: Long) = withContext(Dispatchers.Default) {
        val payload = SyncJson.encodeToString(AdvanceStatusInput(id.value, status.name, expectedVersion))
        queries.transaction {
            queries.updateReservationStatus(status.name, id.value)
            queries.insertOutbox(MutationAdvanceStatus, payload, Clock.System.now().toEpochMilliseconds())
        }
    }

    suspend fun book(slot: Slot, position: Int, holderName: String, partySize: Int, duration: DurationCode) = withContext(Dispatchers.Default) {
        val id = newReservationId().value
        val payload = SyncJson.encodeToString(
            BookInput(id, slot.id.value, position, partySize, holderName, duration.name, slot.rate.code, slot.rate.priceMinorUnits),
        )
        queries.transaction {
            queries.insertReservation(
                id,
                slot.id.value,
                position.toLong(),
                partySize.toLong(),
                holderName,
                false,
                duration.name,
                slot.rate.code,
                slot.rate.priceMinorUnits,
                0L,
                false,
                false,
                ReservationStatus.HELD.name,
                1L,
            )
            queries.insertOutbox(MutationBook, payload, Clock.System.now().toEpochMilliseconds())
        }
    }

    suspend fun move(id: ReservationId, toSlotId: SlotId, toPosition: Int, expectedVersion: Long) = withContext(Dispatchers.Default) {
        val payload = SyncJson.encodeToString(MoveInput(id.value, toSlotId.value, toPosition, expectedVersion))
        queries.transaction {
            queries.updateReservationSlotPosition(toSlotId.value, toPosition.toLong(), id.value)
            queries.insertOutbox(MutationMove, payload, Clock.System.now().toEpochMilliseconds())
        }
    }

    fun outboxDepth(): Flow<Long> = queries.countOutbox().asFlow().mapToOne(Dispatchers.Default)

    suspend fun pending(): List<OutboxEntry> = withContext(Dispatchers.Default) {
        queries.selectOutbox().executeAsList()
    }

    suspend fun markApplied(seq: Long) = withContext(Dispatchers.Default) {
        queries.deleteOutbox(seq)
    }

    suspend fun rebaseConflict(seq: Long, reservationId: String, serverVersion: Long) = withContext(Dispatchers.Default) {
        val entry = queries.selectOutboxBySeq(seq).executeAsOneOrNull() ?: return@withContext
        val rebased = rebasePayload(entry.operation, entry.payload, serverVersion) ?: return@withContext
        queries.transaction {
            queries.updateOutboxPayload(rebased, seq)
            queries.updateReservationVersion(serverVersion + 1, reservationId)
        }
    }

    private fun rebasePayload(operation: String, payload: String, serverVersion: Long): String? = when (operation) {
        MutationAdvanceStatus ->
            SyncJson.encodeToString(SyncJson.decodeFromString<AdvanceStatusInput>(payload).copy(expectedVersion = serverVersion))
        MutationMove ->
            SyncJson.encodeToString(SyncJson.decodeFromString<MoveInput>(payload).copy(expectedVersion = serverVersion))
        else -> null
    }

    suspend fun seedIfEmpty(bundle: SeedBundle = buildSeed()) = withContext(Dispatchers.Default) {
        if (queries.countSlots().executeAsOne() > 0L) return@withContext
        queries.transaction {
            bundle.resources.forEach { resource ->
                queries.insertResource(resource.id.value, resource.name)
                resource.subResources.forEach { sub ->
                    queries.insertSubResource(sub.id.value, resource.id.value, sub.name)
                }
            }
            bundle.slots.forEach { slot ->
                queries.insertSlot(
                    slot.id.value,
                    slot.resourceId.value,
                    slot.subResourceId?.value,
                    slot.startsAt.toEpochMilliseconds(),
                    slot.capacity.toLong(),
                    slot.rate.code,
                    slot.rate.name,
                    slot.rate.priceMinorUnits,
                    slot.rate.currency,
                )
            }
            bundle.reservations.forEach { reservation ->
                queries.insertReservation(
                    reservation.id.value,
                    reservation.slotId.value,
                    reservation.position.toLong(),
                    reservation.partySize.toLong(),
                    reservation.holderName,
                    reservation.isGuest,
                    reservation.duration.name,
                    reservation.rateCode,
                    reservation.priceMinorUnits,
                    reservation.paidMinorUnits,
                    reservation.bookedOnline,
                    reservation.hasCart,
                    reservation.status.name,
                    reservation.version,
                )
            }
        }
    }
}