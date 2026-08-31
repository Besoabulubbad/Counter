package com.abulubad.counter.domain

data class ReceiptLine(
    val label: String,
    val amountMinorUnits: Long,
)

data class Receipt(
    val title: String,
    val lines: List<ReceiptLine>,
    val totalMinorUnits: Long,
    val currency: String,
)