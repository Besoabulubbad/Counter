package com.abulubad.counter.sync

import com.abulubad.counter.data.CounterRepository
import com.abulubad.counter.data.db.OutboxEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString

class SyncEngine(
    private val repository: CounterRepository,
    private val backend: FakeBackend,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    private val _offline = MutableStateFlow(false)
    val offline: StateFlow<Boolean> = _offline
    private val _forceConflict = MutableStateFlow(false)
    val forceConflict: StateFlow<Boolean> = _forceConflict
    private val _conflict = MutableStateFlow<ConflictInfo?>(null)
    val conflict: StateFlow<ConflictInfo?> = _conflict
    private var job: Job? = null

    fun setOffline(value: Boolean) {
        _offline.value = value
    }

    fun setForceConflict(value: Boolean) {
        _forceConflict.value = value
        backend.forceConflict = value
    }

    fun resolveRetry() {
        val current = _conflict.value ?: return
        scope.launch {
            repository.rebaseConflict(current.seq, current.reservationId, current.serverVersion)
            _conflict.value = null
        }
    }

    fun resolveDiscard() {
        val current = _conflict.value ?: return
        scope.launch {
            repository.markApplied(current.seq)
            _conflict.value = null
        }
    }

    fun start() {
        if (job != null) return
        job = scope.launch {
            while (true) {
                if (!_offline.value && _conflict.value == null) {
                    for (entry in repository.pending()) {
                        if (_offline.value || _conflict.value != null) break
                        when (val result = backend.apply(entry)) {
                            SyncResult.Applied -> repository.markApplied(entry.seq)
                            is SyncResult.Conflict ->
                                _conflict.value = ConflictInfo(entry.seq, reservationIdOf(entry), result.serverVersion)
                        }
                    }
                }
                delay(600)
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    private fun reservationIdOf(entry: OutboxEntry): String = when (entry.operation) {
        MutationAdvanceStatus -> SyncJson.decodeFromString<AdvanceStatusInput>(entry.payload).reservationId
        MutationMove -> SyncJson.decodeFromString<MoveInput>(entry.payload).reservationId
        MutationBook -> SyncJson.decodeFromString<BookInput>(entry.payload).reservationId
        else -> ""
    }
}