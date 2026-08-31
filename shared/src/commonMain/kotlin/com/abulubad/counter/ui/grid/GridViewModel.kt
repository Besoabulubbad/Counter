package com.abulubad.counter.ui.grid

import com.abulubad.counter.data.CounterRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

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
}