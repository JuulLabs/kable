package com.juul.kable

internal actual fun PlatformAdvertisement.capture(): AdvertisementCapture = when (this) {
    is ScanResultAndroidAdvertisement -> capture()
    is CapturedAndroidAdvertisement -> capture
    else -> AdvertisementCapture(
        name = name,
        peripheralName = peripheralName,
        identifier = address,
        isConnectable = isConnectable,
        rssi = rssi,
        txPower = txPower,
        uuids = uuids,
        // `PlatformAdvertisement` does not provide enumeration of service data.
        serviceData = emptyMap(),
        manufacturerData = manufacturerData?.let { mapOf(it.code to it.data) }.orEmpty(),
        bytes = bytes,
    )
}

internal actual fun AdvertisementCapture.restore(): PlatformAdvertisement =
    CapturedAndroidAdvertisement(this)
