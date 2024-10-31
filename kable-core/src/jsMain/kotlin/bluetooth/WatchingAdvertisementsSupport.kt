package com.juul.kable.bluetooth

internal val canWatchAdvertisements by lazy {
    js("BluetoothDevice.prototype.watchAdvertisements") != null
}

internal val canUnwatchAdvertisements by lazy {
    js("BluetoothDevice.prototype.unwatchAdvertisements") != null
}

internal val isWatchingAdvertisementsSupported = canWatchAdvertisements && canUnwatchAdvertisements
