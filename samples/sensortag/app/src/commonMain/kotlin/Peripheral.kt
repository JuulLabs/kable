package com.juul.sensortag

import com.juul.kable.Peripheral
import kotlinx.coroutines.cancel
import kotlin.concurrent.Volatile

@Volatile
var peripheral: Peripheral? = null
    set(value) {
        val current = field
        if (current !== value) current?.cancel()
        field = value
    }
