package com.abulubad.counter.platform

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

actual fun formatCurrency(minorUnits: Long, currency: String): String {
    val unit = Currency.getInstance(currency)
    val digits = unit.defaultFractionDigits.coerceAtLeast(0)
    val format = NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
        this.currency = unit
        minimumFractionDigits = digits
        maximumFractionDigits = digits
    }
    return format.format(BigDecimal(minorUnits).movePointLeft(digits))
}