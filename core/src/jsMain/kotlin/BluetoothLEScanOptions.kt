package com.juul.kable

import com.benasher44.uuid.Uuid
import com.juul.kable.external.BluetoothLEScanFilterInit
import com.juul.kable.external.BluetoothLEScanOptions
import com.juul.kable.external.BluetoothManufacturerDataFilterInit

/** Convert list of public API type to Web Bluetooth (JavaScript) type. */
internal fun List<FilterPredicate>.toBluetoothLEScanOptions(): BluetoothLEScanOptions = jso {
    if (isEmpty()) {
        acceptAllAdvertisements = true
    } else {
        filters = toBluetoothLEScanFilterInit().toTypedArray()
    }
}

internal fun List<FilterPredicate>.toBluetoothLEScanFilterInit(): List<BluetoothLEScanFilterInit> =
    map(FilterPredicate::toBluetoothLEScanFilterInit)

private fun FilterPredicate.toBluetoothLEScanFilterInit(): BluetoothLEScanFilterInit = jso {
    filters
        .filterIsInstance<Filter.Service>()
        .takeIf(Collection<Filter.Service>::isNotEmpty)
        ?.map(Filter.Service::uuid)
        ?.map(Uuid::toBluetoothServiceUUID)
        ?.toTypedArray()
        ?.let { services = it }
    filters
        .filterIsInstance<Filter.Name.Exact>()
        .firstOrNull()
        ?.let { name = it.exact }
    filters
        .filterIsInstance<Filter.Name.Prefix>()
        .firstOrNull()
        ?.let { namePrefix = it.prefix }
    filters
        .filterIsInstance<Filter.ManufacturerData>()
        .takeIf(Collection<Filter.ManufacturerData>::isNotEmpty)
        ?.map(::toBluetoothManufacturerDataFilterInit)
        ?.toTypedArray()
        ?.let { manufacturerData = it }
}

private fun toBluetoothManufacturerDataFilterInit(filter: Filter.ManufacturerData) =
    jso<BluetoothManufacturerDataFilterInit> {
        companyIdentifier = filter.id
        dataPrefix = filter.data
        if (filter.dataMask != null) {
            mask = filter.dataMask
        }
    }
