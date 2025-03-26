package com.juul.kable

import platform.CoreBluetooth.CBAdvertisementDataLocalNameKey
import platform.CoreBluetooth.CBAdvertisementDataManufacturerDataKey
import platform.CoreBluetooth.CBAdvertisementDataServiceDataKey
import platform.CoreBluetooth.CBAdvertisementDataServiceUUIDsKey
import platform.CoreBluetooth.CBUUID
import platform.Foundation.NSData
import kotlin.uuid.Uuid

internal value class AdvertisementData(private val source: Map<String, Any>) {

    val serviceUuids: List<Uuid>?
        get() = (source[CBAdvertisementDataServiceUUIDsKey] as? List<CBUUID>)?.map(CBUUID::toUuid)

    val localName: String?
        get() = source[CBAdvertisementDataLocalNameKey] as? String

    val manufacturerData: ManufacturerData?
        get() = (source[CBAdvertisementDataManufacturerDataKey] as? NSData)?.toManufacturerData()

    /**
     * Per [CBAdvertisementDataServiceDataKey](https://developer.apple.com/documentation/corebluetooth/cbadvertisementdataservicedatakey):
     *
     * > A dictionary that contains service-specific advertisement data.
     * > The keys ([CBUUID] objects) represent `CBService` UUIDs, and the values ([NSData] objects)
     * > represent service-specific data.
     */
    @Suppress("UNCHECKED_CAST")
    val serviceData: Map<CBUUID, NSData>?
        get() = source[CBAdvertisementDataServiceDataKey] as Map<CBUUID, NSData>?
}

internal fun Map<String, Any>.asAdvertisementData() = AdvertisementData(this)
