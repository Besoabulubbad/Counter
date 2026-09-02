package com.abulubad.counter.session

import kotlin.concurrent.atomics.AtomicLong
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class TicketLine(
    val id: String,
    val label: String,
    val qty: Int,
    val amountMinorUnits: Long,
    val origin: String,
    val currency: String,
    val oversold: Boolean = false,
    val reservationId: String? = null,
)

@OptIn(ExperimentalAtomicApi::class)
class Ticket {
    private val counter = AtomicLong(0)
    private val _lines = MutableStateFlow<List<TicketLine>>(emptyList())
    val lines: StateFlow<List<TicketLine>> = _lines

    fun add(label: String, qty: Int, amountMinorUnits: Long, origin: String, currency: String, oversold: Boolean = false, reservationId: String? = null) {
        val line = TicketLine("line-${counter.addAndFetch(1L)}", label, qty, amountMinorUnits, origin, currency, oversold, reservationId)
        _lines.update { it + line }
    }

    fun clear() {
        _lines.value = emptyList()
    }
}