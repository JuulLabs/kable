@file:OptIn(ExperimentalForeignApi::class)

package com.juul.kable

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.create
import platform.posix.memcpy

// https://stackoverflow.com/a/58521109
internal fun NSData.toByteArray(): ByteArray = ByteArray(length.toInt()).apply {
    if (length > 0u) {
        usePinned {
            memcpy(it.addressOf(0), bytes, length)
        }
    }
}

// https://stackoverflow.com/a/58521109
internal fun ByteArray.toNSData(): NSData = memScoped {
    NSData.create(
        bytes = allocArrayOf(this@toNSData),
        length = size.convert(),
    )
}

internal fun UShort.toNSData(littleEndian: Boolean = true): NSData {
    val result = ByteArray(UShort.SIZE_BYTES)
    val rawValue = toInt()
    for (offset in 0 until UShort.SIZE_BYTES) {
        val bitCountOffset = offset * Byte.SIZE_BITS
        val index = if (littleEndian) offset else UShort.SIZE_BYTES - offset - 1
        result[index] = (rawValue.ushr(bitCountOffset) and 0xFF).toByte()
    }
    return result.toNSData()
}
