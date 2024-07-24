package com.juul.kable

import com.benasher44.uuid.Uuid
import platform.CoreBluetooth.CBAdvertisementDataLocalNameKey
import platform.CoreBluetooth.CBAdvertisementDataManufacturerDataKey
import platform.CoreBluetooth.CBAdvertisementDataServiceUUIDsKey
import platform.CoreBluetooth.CBUUID
import platform.Foundation.NSData

internal value class AdvertisementData(private val source: Map<String, Any>) {

    val serviceUuids: List<Uuid>?
        get() = (source[CBAdvertisementDataServiceUUIDsKey] as? List<CBUUID>)?.map(CBUUID::toUuid)

    val localName: String?
        get() = source[CBAdvertisementDataLocalNameKey] as? String

    val manufacturerData: ManufacturerData?
        get() = (source[CBAdvertisementDataManufacturerDataKey] as? NSData)?.toManufacturerData()
}

internal fun Map<String, Any>.asAdvertisementData() = AdvertisementData(this)
