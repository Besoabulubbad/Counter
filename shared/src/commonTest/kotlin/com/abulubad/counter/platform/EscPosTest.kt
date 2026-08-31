package com.abulubad.counter.platform

import com.abulubad.counter.domain.Receipt
import com.abulubad.counter.domain.ReceiptLine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EscPosTest {
    @Test
    fun encodesInitHeaderAndCut() {
        val bytes = EscPos.encode(
            Receipt(
                title = "Counter",
                lines = listOf(ReceiptLine("Green fee", 6500)),
                totalMinorUnits = 6500,
                currency = "USD",
            ),
        )
        assertTrue(bytes.size > 8)
        assertEquals(0x1B.toByte(), bytes[0])
        assertEquals(0x40.toByte(), bytes[1])
        assertEquals(0x1D.toByte(), bytes[bytes.size - 3])
        assertEquals(0x56.toByte(), bytes[bytes.size - 2])
        assertEquals(0x00.toByte(), bytes[bytes.size - 1])
    }
}