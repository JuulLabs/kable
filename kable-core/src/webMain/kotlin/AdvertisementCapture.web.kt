package com.juul.kable

internal actual fun PlatformAdvertisement.capture(): AdvertisementCapture = when (this) {
    is BluetoothAdvertisingEventWebBluetoothAdvertisement -> capture()
    is CapturedWebBluetoothAdvertisement -> capture
    else -> captureCommon()
}

internal actual fun AdvertisementCapture.restore(): PlatformAdvertisement =
    CapturedWebBluetoothAdvertisement(this)
