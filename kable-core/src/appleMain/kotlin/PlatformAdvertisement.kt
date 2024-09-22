package com.juul.kable

import platform.Foundation.NSData
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

public actual interface PlatformAdvertisement : Advertisement {
    @OptIn(ExperimentalUuidApi::class)
    public fun serviceDataAsNSData(uuid: Uuid): NSData?
    public val manufacturerDataAsNSData: NSData?
    public fun manufacturerDataAsNSData(companyIdentifierCode: Int): NSData?
}
