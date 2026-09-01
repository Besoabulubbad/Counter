package com.abulubad.counter.sync

import com.abulubad.counter.data.CounterRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SyncEngine(private val repository: CounterRepository, private val backend: FakeBackend) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _offline = MutableStateFlow(false)
    val offline: StateFlow<Boolean> = _offline
    private var started = false

    fun setOffline(value: Boolean) {
        _offline.value = value
    }

    fun start() {
        if (started) return
        started = true
        scope.launch {
            while (true) {
                if (!_offline.value) {
                    for (entry in repository.pending()) {
                        if (_offline.value) break
                        backend.apply(entry)
                        repository.markApplied(entry.seq)
                    }
                }
                delay(600)
            }
        }
    }
}