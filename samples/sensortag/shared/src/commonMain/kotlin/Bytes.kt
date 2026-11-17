package com.juul.sensortag

fun ByteArray.readShort(offset: Int): Short {
    val value = get(offset) and 0xff or (get(offset + 1) and 0xff shl 8)
    return value.toShort()
}

infix fun Byte.and(other: Int): Int = toInt() and other
