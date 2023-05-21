package com.juul.kable

import com.benasher44.uuid.Uuid
import platform.Foundation.NSData
import platform.Foundation.NSUUID

public interface CoreBluetoothAdvertisement : Advertisement {
    public val identifier: NSUUID
    public fun serviceDataAsNSData(uuid: Uuid): NSData?
    public val manufacturerDataAsNSData: NSData?
    public fun manufacturerDataAsNSData(companyIdentifierCode: Int): NSData?
}
