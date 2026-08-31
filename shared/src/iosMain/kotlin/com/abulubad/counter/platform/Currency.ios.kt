package com.abulubad.counter.platform

import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterCurrencyStyle

actual fun formatCurrency(minorUnits: Long, currency: String): String {
    val formatter = NSNumberFormatter().apply {
        numberStyle = NSNumberFormatterCurrencyStyle
        currencyCode = currency
    }
    val amount = minorUnits.toDouble() / 100.0
    return formatter.stringFromNumber(NSNumber(double = amount)) ?: amount.toString()
}