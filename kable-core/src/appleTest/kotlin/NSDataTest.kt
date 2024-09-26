@file:OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)

package com.juul.kable.test

import com.juul.kable.toByteArray
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSData
import platform.Foundation.NSDataBase64DecodingIgnoreUnknownCharacters
import platform.Foundation.create
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val FOO = "Zm9v" // Base64("foo")
private const val BAR = "YmFy" // Base64("bar")

class NSDataTest {

    @Test
    fun nSDataToByteArray_emptyNSData_isEmpty() {
        val data = NSData.create(length = 0u, bytes = null).toByteArray()
        assertEquals(0, data.size)
    }

    @Test
    fun nsData_differentData_isNotEqual() {
        val data1 = NSData.create(FOO, NSDataBase64DecodingIgnoreUnknownCharacters)!!
        val data2 = NSData.create(BAR, NSDataBase64DecodingIgnoreUnknownCharacters)!!
        assertTrue { data1 != data2 }
    }

    @Test
    fun nsData_sameData_isEqualToData() {
        val data1 = NSData.create(FOO, NSDataBase64DecodingIgnoreUnknownCharacters)!!
        val data2 = NSData.create(FOO, NSDataBase64DecodingIgnoreUnknownCharacters)!!
        assertTrue { data1 == data2 }
    }
}
