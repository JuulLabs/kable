package com.juul.kable

import com.juul.kable.btleplug.BtleplugAdvertisement
import kotlinx.io.bytestring.ByteString

internal actual fun PlatformAdvertisement.capture(): AdvertisementCapture = when (this) {
    is BtleplugAdvertisement -> capture()
    else -> captureCommon()
}

internal actual fun AdvertisementCapture.restore(): PlatformAdvertisement = BtleplugAdvertisement(
    manufacturerDataMap = manufacturerData.entries
        .associate { (code, data) -> code.toUShort() to ByteString(data) },
    serviceData = serviceData.mapValues { (_, data) -> ByteString(data) },
    name = name,
    peripheralName = peripheralName,
    identifier = identifier.toIdentifier(),
    rssi = rssi,
    txPower = txPower,
    uuids = uuids,
)
