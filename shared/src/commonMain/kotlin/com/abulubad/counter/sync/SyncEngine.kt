package com.abulubad.counter.sync

import com.abulubad.counter.data.CounterRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString

class SyncEngine(private val repository: CounterRepository, private val backend: FakeBackend) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _offline = MutableStateFlow(false)
    val offline: StateFlow<Boolean> = _offline
    private val _forceConflict = MutableStateFlow(false)
    val forceConflict: StateFlow<Boolean> = _forceConflict
    private val _conflict = MutableStateFlow<ConflictInfo?>(null)
    val conflict: StateFlow<ConflictInfo?> = _conflict
    private var started = false

    fun setOffline(value: Boolean) {
        _offline.value = value
    }

    fun setForceConflict(value: Boolean) {
        _forceConflict.value = value
        backend.forceConflict = value
    }

    fun resolveRetry() {
        _conflict.value = null
    }

    fun resolveDiscard() {
        val current = _conflict.value ?: return
        scope.launch {
            repository.markApplied(current.seq)
            _conflict.value = null
        }
    }

    fun start() {
        if (started) return
        started = true
        scope.launch {
            while (true) {
                if (!_offline.value && _conflict.value == null) {
                    for (entry in repository.pending()) {
                        if (_offline.value || _conflict.value != null) break
                        when (val result = backend.apply(entry)) {
                            SyncResult.Applied -> repository.markApplied(entry.seq)
                            is SyncResult.Conflict -> {
                                val input = SyncJson.decodeFromString<AdvanceStatusInput>(entry.payload)
                                _conflict.value = ConflictInfo(entry.seq, input.reservationId, result.serverVersion)
                            }
                        }
                    }
                }
                delay(600)
            }
        }
    }
}