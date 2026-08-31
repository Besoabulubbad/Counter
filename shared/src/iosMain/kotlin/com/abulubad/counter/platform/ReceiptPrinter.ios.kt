package com.abulubad.counter.platform

import com.abulubad.counter.domain.Receipt
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.NSDate
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.create
import platform.Foundation.timeIntervalSince1970
import platform.Foundation.writeToFile

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual class ReceiptPrinter {
    actual suspend fun print(receipt: Receipt): Result<Unit> = withContext(Dispatchers.Default) {
        runCatching {
            val bytes = EscPos.encode(receipt)
            val stamp = NSDate().timeIntervalSince1970.toLong()
            val path = NSTemporaryDirectory() + "receipt-$stamp.escpos"
            val data = bytes.usePinned { pinned ->
                NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
            }
            if (!data.writeToFile(path, atomically = true)) error("receipt write failed")
        }
    }
}