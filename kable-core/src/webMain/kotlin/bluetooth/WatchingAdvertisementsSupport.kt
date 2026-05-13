package com.juul.kable.bluetooth

import kotlin.js.JsAny
import kotlin.js.js
import kotlin.js.undefined

private val watchAdvertisements: JsAny? =
    js("BluetoothDevice.prototype.watchAdvertisements")

private val unwatchAdvertisements: JsAny? =
    js("BluetoothDevice.prototype.unwatchAdvertisements")

internal val canWatchAdvertisements by lazy {
    watchAdvertisements != null && watchAdvertisements != undefined
}

internal val canUnwatchAdvertisements by lazy {
    unwatchAdvertisements != null && unwatchAdvertisements != undefined
}

internal val isWatchingAdvertisementsSupported = canWatchAdvertisements && canUnwatchAdvertisements
