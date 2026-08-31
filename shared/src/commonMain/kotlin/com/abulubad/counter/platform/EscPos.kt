package com.abulubad.counter.platform

import com.abulubad.counter.domain.Receipt

object EscPos {
    private const val LF = 0x0A
    private const val ESC = 0x1B
    private const val GS = 0x1D

    fun encode(receipt: Receipt): ByteArray {
        val out = ArrayList<Byte>()
        fun control(vararg values: Int) = values.forEach { out.add(it.toByte()) }
        fun line(value: String) {
            out.addAll(value.encodeToByteArray().asList())
            out.add(LF.toByte())
        }

        control(ESC, 0x40)
        control(ESC, 0x61, 0x01)
        line(receipt.title)
        control(ESC, 0x61, 0x00)
        receipt.lines.forEach { item ->
            line("${item.label}  ${formatCurrency(item.amountMinorUnits, receipt.currency)}")
        }
        line("Total  ${formatCurrency(receipt.totalMinorUnits, receipt.currency)}")
        control(LF, LF, LF)
        control(GS, 0x56, 0x00)
        return out.toByteArray()
    }
}