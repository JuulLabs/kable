package com.juul.kable

import com.juul.kable.Endianness.BigEndian
import com.juul.kable.Endianness.LittleEndian

internal fun ByteArray.toShort(): Int {
    require(size == 2) { "ByteArray must be size of 2 to be converted to a short, was $size" }
    return this[0] and 0xFF shl 8 or (this[1] and 0xFF)
}

private inline infix fun Byte.and(other: Int): Int = toInt() and other

internal enum class Endianness { BigEndian, LittleEndian }

internal fun UShort.toByteArray(endianness: Endianness = BigEndian): ByteArray =
    when (endianness) {
        BigEndian -> byteArrayOf(
            (toInt() and 0xFF00 shr 8).toByte(),
            (this and 0xFFu).toByte(),
        )
        LittleEndian -> byteArrayOf(
            (this and 0xFFu).toByte(),
            (toInt() and 0xFF00 shr 8).toByte(),
        )
    }

internal fun ULong.toByteArray(endianness: Endianness = BigEndian): ByteArray =
    when (endianness) {
        BigEndian -> byteArrayOf(
            (this shr 56 and 0xFFuL).toByte(),
            (this shr 48 and 0xFFuL).toByte(),
            (this shr 40 and 0xFFuL).toByte(),
            (this shr 32 and 0xFFuL).toByte(),
            (this shr 24 and 0xFFuL).toByte(),
            (this shr 16 and 0xFFuL).toByte(),
            (this shr 8 and 0xFFuL).toByte(),
            (this and 0xFFuL).toByte(),
        )
        LittleEndian -> byteArrayOf(
            (this and 0xFFuL).toByte(),
            (this shr 8 and 0xFFuL).toByte(),
            (this shr 16 and 0xFFuL).toByte(),
            (this shr 24 and 0xFFuL).toByte(),
            (this shr 32 and 0xFFuL).toByte(),
            (this shr 40 and 0xFFuL).toByte(),
            (this shr 48 and 0xFFuL).toByte(),
            (this shr 56 and 0xFFuL).toByte(),
        )
    }
