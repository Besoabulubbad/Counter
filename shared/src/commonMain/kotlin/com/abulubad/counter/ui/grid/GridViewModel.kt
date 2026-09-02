package com.abulubad.counter.ui.grid

import com.abulubad.counter.data.CounterRepository
import com.abulubad.counter.domain.DurationCode
import com.abulubad.counter.domain.Receipt
import com.abulubad.counter.domain.ReceiptLine
import com.abulubad.counter.domain.Reservation
import com.abulubad.counter.domain.ReservationId
import com.abulubad.counter.domain.ReservationStatus
import com.abulubad.counter.domain.Slot
import com.abulubad.counter.domain.SlotId
import com.abulubad.counter.domain.next
import com.abulubad.counter.platform.ReceiptPrinter
import com.abulubad.counter.session.Ticket
import com.abulubad.counter.session.TicketLine
import com.abulubad.counter.sync.ConflictInfo
import com.abulubad.counter.sync.SyncEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GridViewModel(
    private val repository: CounterRepository,
    private val syncEngine: SyncEngine,
    private val ticket: Ticket,
    private val receiptPrinter: ReceiptPrinter,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun start() = syncEngine.start()

    val offline: StateFlow<Boolean> = syncEngine.offline

    val outboxDepth: StateFlow<Long> =
        repository.outboxDepth().stateIn(scope, SharingStarted.WhileSubscribed(5_000), 0L)

    fun toggleOffline() = syncEngine.setOffline(!syncEngine.offline.value)

    val forceConflict: StateFlow<Boolean> = syncEngine.forceConflict

    val conflict: StateFlow<ConflictInfo?> = syncEngine.conflict

    fun toggleForceConflict() = syncEngine.setForceConflict(!syncEngine.forceConflict.value)

    fun resolveRetry() = syncEngine.resolveRetry()

    fun resolveDiscard() = syncEngine.resolveDiscard()

    val ticketLines: StateFlow<List<TicketLine>> = ticket.lines

    val ticketTotal: StateFlow<Long> =
        ticket.lines.map { lines -> lines.sumOf { it.amountMinorUnits * it.qty } }
            .stateIn(scope, SharingStarted.WhileSubscribed(5_000), 0L)

    fun addToTicket(reservation: Reservation) =
        ticket.add("Green fee · ${reservation.holderName}", 1, reservation.priceMinorUnits, "from grid", "USD", reservationId = reservation.id.value)

    fun addItemToTicket(name: String, priceMinorUnits: Long, sku: String, oversold: Boolean) =
        ticket.add(name, 1, priceMinorUnits, "from order · $sku", "USD", oversold)

    fun pay() {
        val lines = ticket.lines.value
        if (lines.isEmpty()) return
        val receipt = Receipt(
            title = "Counter",
            lines = lines.map { ReceiptLine(it.label, it.amountMinorUnits * it.qty) },
            totalMinorUnits = lines.sumOf { it.amountMinorUnits * it.qty },
            currency = lines.first().currency,
        )
        val onTicket = state.value.rows.asSequence().flatMap { it.cells.asSequence() }.filterNotNull().associateBy { it.id.value }
        val settled = lines.mapNotNull { it.reservationId }.distinct().mapNotNull { onTicket[it] }
        scope.launch {
            if (receiptPrinter.print(receipt).isSuccess) {
                settled.forEach { repository.takePayment(it.id, it.priceMinorUnits, it.version) }
                ticket.clear()
            }
        }
    }

    val state: StateFlow<GridUiState> =
        combine(
            repository.slots(),
            repository.reservations(),
            repository.resources(),
        ) { slots, reservations, resources ->
            val subLabels = resources
                .flatMap { it.subResources }
                .associate { it.id.value to it.name.substringBefore(' ') }
            buildGrid(slots, reservations, subLabels)
        }.stateIn(scope, SharingStarted.WhileSubscribed(5_000), GridUiState.Empty)

    suspend fun seed() = repository.seedIfEmpty()

    fun advance(reservation: Reservation) {
        val next = reservation.status.next()
        when {
            next != null -> scope.launch { repository.setStatus(reservation.id, next, reservation.version) }
            reservation.status == ReservationStatus.CHECKED_IN -> addToTicket(reservation)
            reservation.status == ReservationStatus.NO_SHOW || reservation.status == ReservationStatus.CANCELLED ->
                scope.launch { repository.setStatus(reservation.id, ReservationStatus.HELD, reservation.version) }
        }
    }

    fun book(slot: Slot, position: Int) {
        scope.launch { repository.book(slot, position, "Walk-in", 1, DurationCode.EIGHTEEN) }
    }

    fun move(id: ReservationId, toSlotId: SlotId, toPosition: Int, expectedVersion: Long) {
        scope.launch { repository.move(id, toSlotId, toPosition, expectedVersion) }
    }
}