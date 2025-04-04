package com.juul.kable

import com.juul.kable.external.BluetoothLEScanFilterInit
import com.juul.kable.external.BluetoothLEScanOptions
import com.juul.kable.external.BluetoothManufacturerDataFilterInit
import com.juul.kable.external.BluetoothServiceDataFilterInit
import js.objects.jso
import kotlin.uuid.Uuid

/** Convert list of public API type to Web Bluetooth (JavaScript) type. */
internal fun List<FilterPredicate>.toBluetoothLEScanOptions(): BluetoothLEScanOptions = jso {
    if (isEmpty()) {
        acceptAllAdvertisements = true
    } else {
        filters = toBluetoothLEScanFilterInit()
    }
}

internal fun List<FilterPredicate>.toBluetoothLEScanFilterInit(): Array<BluetoothLEScanFilterInit> =
    map(FilterPredicate::toBluetoothLEScanFilterInit)
        .toTypedArray()

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
    filters
        .filterIsInstance<Filter.ServiceData>()
        .takeIf(Collection<Filter.ServiceData>::isNotEmpty)
        ?.map(::toBluetoothServiceDataFilterInit)
        ?.toTypedArray()
        ?.let { serviceData = it }
}

private fun toBluetoothManufacturerDataFilterInit(filter: Filter.ManufacturerData) =
    jso<BluetoothManufacturerDataFilterInit> {
        companyIdentifier = filter.id
        if (filter.data != null) {
            dataPrefix = filter.data
        }
        if (filter.dataMask != null) {
            mask = filter.dataMask
        }
    }

private fun toBluetoothServiceDataFilterInit(filter: Filter.ServiceData) =
    jso<BluetoothServiceDataFilterInit> {
        service = filter.uuid.toBluetoothServiceUUID()
        if (filter.data != null) {
            dataPrefix = filter.data
        }
        if (filter.dataMask != null) {
            mask = filter.dataMask
        }
    }
