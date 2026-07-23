package com.juul.kable

import com.juul.kable.logs.Hex
import com.juul.kable.logs.Logging.DataProcessor
import com.juul.kable.logs.Logging.DataProcessor.Operation.Read
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ObsoleteKableApi::class)
class HexTests {

    private fun process(
        processor: DataProcessor,
        data: ByteArray,
    ): String = processor.process(
        data = data,
        operation = Read,
        serviceUuid = null,
        characteristicUuid = null,
        descriptorUuid = null,
    )

    @Test
    fun defaultHex_emptyData_returnsEmptyString() {
        assertEquals(
            expected = "",
            actual = process(Hex, byteArrayOf()),
        )
    }

    @Test
    fun defaultHex_singleByte_returnsUpperCaseHex() {
        assertEquals(
            expected = "0A",
            actual = process(Hex, byteArrayOf(0x0A)),
        )
    }

    @Test
    fun defaultHex_multipleBytes_defaultsToSpaceSeparatorAndUpperCase() {
        assertEquals(
            expected = "00 01 0F 10 FF",
            actual = process(Hex, byteArrayOf(0x00, 0x01, 0x0F, 0x10, 0xFF.toByte())),
        )
    }

    @Test
    fun lowerCase_multipleBytes_returnsLowerCaseHex() {
        val hex = Hex { lowerCase = true }
        assertEquals(
            expected = "00 01 0f 10 ff",
            actual = process(hex, byteArrayOf(0x00, 0x01, 0x0F, 0x10, 0xFF.toByte())),
        )
    }

    @Test
    fun customSeparator_multipleBytes_usesConfiguredSeparator() {
        val hex = Hex { separator = ":" }
        assertEquals(
            expected = "DE:AD:BE:EF",
            actual = process(hex, byteArrayOf(0xDE.toByte(), 0xAD.toByte(), 0xBE.toByte(), 0xEF.toByte())),
        )
    }

    @Test
    fun emptySeparator_multipleBytes_producesContiguousHex() {
        val hex = Hex { separator = "" }
        assertEquals(
            expected = "DEADBEEF",
            actual = process(hex, byteArrayOf(0xDE.toByte(), 0xAD.toByte(), 0xBE.toByte(), 0xEF.toByte())),
        )
    }

    @Test
    fun customSeparatorAndLowerCase_combined() {
        val hex = Hex {
            separator = "-"
            lowerCase = true
        }
        assertEquals(
            expected = "de-ad-be-ef",
            actual = process(hex, byteArrayOf(0xDE.toByte(), 0xAD.toByte(), 0xBE.toByte(), 0xEF.toByte())),
        )
    }

    @Test
    fun customSeparator_singleByte_hasNoSeparator() {
        val hex = Hex { separator = ":" }
        assertEquals(
            expected = "FF",
            actual = process(hex, byteArrayOf(0xFF.toByte())),
        )
    }
}
