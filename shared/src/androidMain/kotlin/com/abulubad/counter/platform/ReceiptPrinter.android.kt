package com.abulubad.counter.platform

import com.abulubad.counter.domain.Receipt
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual class ReceiptPrinter {
    actual suspend fun print(receipt: Receipt): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val dir = File(System.getProperty("java.io.tmpdir"), "counter-receipts").apply { mkdirs() }
            File(dir, "receipt-${System.currentTimeMillis()}.escpos").writeBytes(EscPos.encode(receipt))
        }
    }
}