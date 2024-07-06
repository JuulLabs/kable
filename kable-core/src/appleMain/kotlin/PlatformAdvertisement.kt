package com.juul.kable

import com.benasher44.uuid.Uuid
import platform.Foundation.NSData

public actual interface PlatformAdvertisement : Advertisement {
    public fun serviceDataAsNSData(uuid: Uuid): NSData?
    public val manufacturerDataAsNSData: NSData?
    public fun manufacturerDataAsNSData(companyIdentifierCode: Int): NSData?
}
