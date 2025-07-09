package com.juul.kable.bluetooth

internal actual suspend fun isSupported(): Boolean =
    com.juul.kable.btleplug.ffi.isSupported()
