package com.abulubad.counter.platform

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

actual fun formatCurrency(minorUnits: Long, currency: String): String {
    val unit = Currency.getInstance(currency)
    val format = NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
        this.currency = unit
    }
    val amount = BigDecimal(minorUnits).movePointLeft(unit.defaultFractionDigits.coerceAtLeast(0))
    return format.format(amount)
}