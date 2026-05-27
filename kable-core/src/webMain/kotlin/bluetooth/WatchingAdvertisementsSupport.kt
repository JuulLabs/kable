package com.juul.kable.bluetooth

import kotlin.js.js

internal val canWatchAdvertisements: Boolean by lazy {
    js("typeof BluetoothDevice.prototype.watchAdvertisements === 'function'")
}

internal val canUnwatchAdvertisements: Boolean by lazy {
    js("typeof BluetoothDevice.prototype.unwatchAdvertisements === 'function'")
}

internal val isWatchingAdvertisementsSupported = canWatchAdvertisements && canUnwatchAdvertisements
