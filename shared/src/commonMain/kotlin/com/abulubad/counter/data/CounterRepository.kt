package com.abulubad.counter.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.abulubad.counter.data.db.CounterDatabase
import com.abulubad.counter.domain.Reservation
import com.abulubad.counter.domain.Resource
import com.abulubad.counter.domain.Slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

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