package com.juul.kable.bluetooth

import kotlin.js.js

private fun isWatchAdvertisementsSupported(): Boolean =
    js("typeof BluetoothDevice.prototype.watchAdvertisements === 'function'")

private fun isUnwatchAdvertisementsSupported(): Boolean =
    js("typeof BluetoothDevice.prototype.unwatchAdvertisements === 'function'")

internal val isWatchingAdvertisementsSupported
    get() = isWatchAdvertisementsSupported() && isUnwatchAdvertisementsSupported()
