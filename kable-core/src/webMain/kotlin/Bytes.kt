package com.juul.kable

import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.toByteArray

internal fun ArrayBuffer.toByteArray(): ByteArray = Int8Array(this).toByteArray()
