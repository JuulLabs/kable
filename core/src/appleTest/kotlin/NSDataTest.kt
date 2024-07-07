@file:OptIn(ExperimentalForeignApi::class)

package com.juul.kable.test

import com.juul.kable.toByteArray
import com.juul.kable.toNSData
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSData
import platform.Foundation.create
import kotlin.test.Test
import kotlin.test.assertEquals

class NSDataTest {
    @Test
    fun nSDataToByteArray_emptyNSData_isEmpty() {
        val data = NSData.create(length = 0u, bytes = null).toByteArray()
        assertEquals(0, data.size)
    }

    @Test
    fun uShortToNSData_littleEndian() {
        val rawValue: UShort = 4660u
        val expected = byteArrayOf(0x34, 0x12).toNSData()
        val actual = rawValue.toNSData(littleEndian = true)
        assertEquals(expected, actual)
    }

    @Test
    fun uShortToNSData_bigEndian() {
        val rawValue: UShort = 4660u
        val expected = byteArrayOf(0x12, 0x34).toNSData()
        val actual = rawValue.toNSData(littleEndian = false)
        assertEquals(expected, actual)
    }
}
