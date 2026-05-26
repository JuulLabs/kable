package com.juul.kable.bluetooth

import com.juul.kable.jsUndefinedAny
import kotlin.js.JsAny
import kotlin.js.js

private val watchAdvertisements: JsAny? =
    js("BluetoothDevice.prototype.watchAdvertisements")

private val unwatchAdvertisements: JsAny? =
    js("BluetoothDevice.prototype.unwatchAdvertisements")

internal val canWatchAdvertisements by lazy {
    watchAdvertisements != null && watchAdvertisements != jsUndefinedAny
}

internal val canUnwatchAdvertisements by lazy {
    unwatchAdvertisements != null && unwatchAdvertisements != jsUndefinedAny
}

internal val isWatchingAdvertisementsSupported = canWatchAdvertisements && canUnwatchAdvertisements
