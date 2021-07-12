package com.juul.kable

/**
 * Copied from Tuulbox.
 * https://github.com/JuulLabs/tuulbox/blob/fde4eb74d2aeb37b6aaac2002bccc553adc02c5a/encoding/src/commonMain/kotlin/HexString.kt
 */
internal fun ByteArray.toHexString(
    separator: String? = null,
    prefix: String? = null,
    lowerCase: Boolean = false
): String {
    if (size == 0) return ""
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
