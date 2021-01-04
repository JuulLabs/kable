package com.juul.kable

import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array

internal fun ArrayBuffer.toByteArray(): ByteArray = Int8Array(this).unsafeCast<ByteArray>()
