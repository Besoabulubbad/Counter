package com.abulubad.counter.platform

import com.abulubad.counter.domain.Receipt

expect class ReceiptPrinter() {
    suspend fun print(receipt: Receipt): Result<Unit>
}