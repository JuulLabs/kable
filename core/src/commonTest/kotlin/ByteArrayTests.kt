package com.juul.kable

import com.juul.kable.Endianness.BigEndian
import com.juul.kable.Endianness.LittleEndian
import kotlin.test.Test
import kotlin.test.assertContentEquals

class ByteArrayTests {

    @Test
    fun uShort_toByteArray_BigEndian() {
        assertContentEquals(
            byteArrayOf(0x12, 0x34),
            4660.toUShort().toByteArray(BigEndian),
        )
    }

    @Test
    fun uShort_toByteArray_LittleEndian() {
        assertContentEquals(
            byteArrayOf(0x34, 0x12),
            4660.toUShort().toByteArray(LittleEndian),
        )
    }

    @Test
    fun uLong_toByteArray_BigEndian() {
        assertContentEquals(
            byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x12, 0x34),
            4660uL.toByteArray(BigEndian),
        )
    }

    @Test
    fun uLong_toByteArray_LittleEndian() {
        assertContentEquals(
            byteArrayOf(0x34, 0x12, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00),
            4660uL.toByteArray(LittleEndian),
        )
    }
}
