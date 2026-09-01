package com.abulubad.counter.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class TicketLine(
    val id: String,
    val label: String,
    val qty: Int,
    val amountMinorUnits: Long,
    val origin: String,
    val currency: String,
)

class Ticket {
    private var counter = 0L
    private val _lines = MutableStateFlow<List<TicketLine>>(emptyList())
    val lines: StateFlow<List<TicketLine>> = _lines

    fun add(label: String, qty: Int, amountMinorUnits: Long, origin: String, currency: String) {
        _lines.value = _lines.value + TicketLine("line-${counter++}", label, qty, amountMinorUnits, origin, currency)
    }

    fun clear() {
        _lines.value = emptyList()
    }
}