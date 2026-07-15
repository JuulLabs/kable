package com.juul.kable

internal actual fun PlatformAdvertisement.capture(): AdvertisementCapture = when (this) {
    is CBPeripheralCoreBluetoothAdvertisement -> capture()
    is CapturedCoreBluetoothAdvertisement -> capture
    else -> captureCommon()
}

internal actual fun AdvertisementCapture.restore(): PlatformAdvertisement =
    CapturedCoreBluetoothAdvertisement(this)
