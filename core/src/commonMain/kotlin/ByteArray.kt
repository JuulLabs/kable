package com.juul.kable

/**
 * Copied from Tuulbox.
 * https://github.com/JuulLabs/tuulbox/blob/fde4eb74d2aeb37b6aaac2002bccc553adc02c5a/encoding/src/commonMain/kotlin/HexString.kt
 */
internal fun ByteArray.toHexString(
    separator: String? = null,
    prefix: String? = null,
    lowerCase: Boolean = false,
): String {
    if (isEmpty()) return ""
    val hexCode = if (lowerCase) "0123456789abcdef" else "0123456789ABCDEF"
    val capacity = size * (2 + (prefix?.length ?: 0)) + (size - 1) * (separator?.length ?: 0)
    val r = StringBuilder(capacity)
    for (b in this) {
        if (separator != null && r.isNotEmpty()) r.append(separator)
        if (prefix != null) r.append(prefix)
        r.append(hexCode[b.toInt() shr 4 and 0xF])
        r.append(hexCode[b.toInt() and 0xF])
    }
    return r.toString()
}

internal fun ByteArray.toShort(): Int {
    require(size == 2) { "ByteArray must be size of 2 to be converted to a short, was $size" }
    return this[0] and 0xFF shl 8 or (this[1] and 0xFF)
}

private inline infix fun Byte.and(other: Int): Int = toInt() and other
