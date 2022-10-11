package com.juul.kable.test

import com.juul.kable.toByteArray
import platform.Foundation.NSData
import platform.Foundation.create
import kotlin.test.Test
import kotlin.test.assertEquals

class NSDataTest {
    @Test
    fun nSDataToByteArray_emptyNSData_isEmpty() {
        val data = NSData.create(length = 0, bytes = null).toByteArray()
        assertEquals(0, data.size)
    }
}
