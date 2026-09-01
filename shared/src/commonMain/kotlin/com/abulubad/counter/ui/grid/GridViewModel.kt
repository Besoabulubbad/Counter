package com.abulubad.counter.ui.grid

import com.abulubad.counter.data.CounterRepository
import com.abulubad.counter.domain.Reservation
import com.abulubad.counter.domain.next
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GridViewModel(private val repository: CounterRepository) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

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
        val next = reservation.status.next() ?: return
        scope.launch { repository.setStatus(reservation.id, next) }
    }
}