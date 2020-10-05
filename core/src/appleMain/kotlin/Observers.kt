package com.juul.kable

import co.touchlab.stately.isolate.IsolateState
import platform.CoreBluetooth.CBUUID

internal class Observers : IsolateState<MutableMap<CBUUID, Int>>({ mutableMapOf() }) {

    fun increment(key: CBUUID): Int = access {
        val newValue = (it[key] ?: 0) + 1
        it[key] = newValue
        newValue
    }

    fun decrement(key: CBUUID): Int = access {
        val newValue = (it[key] ?: 0) - 1
        if (newValue < 1) it -= key else it[key] = newValue
        newValue
    }
}
