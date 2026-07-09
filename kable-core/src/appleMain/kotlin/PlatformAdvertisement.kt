package com.juul.kable

import platform.CoreBluetooth.CBPeripheral
import platform.Foundation.NSData
import kotlin.uuid.Uuid

public actual interface PlatformAdvertisement : Advertisement {
    public fun serviceDataAsNSData(uuid: Uuid): NSData?
    public val manufacturerDataAsNSData: NSData?
    public fun manufacturerDataAsNSData(companyIdentifierCode: Int): NSData?

    /**
     * This is an internal API and may be removed from a future release. If you are using it, please
     * open an issue and report your use case.
     */
    @InternalKableApi
    public val cbPeripheral: CBPeripheral
}
