package com.juul.kable

import com.juul.kable.external.BluetoothLEScanFilterInit
import com.juul.kable.external.BluetoothLEScanOptions
import com.juul.kable.external.BluetoothManufacturerDataFilterInit
import com.juul.kable.external.BluetoothServiceDataFilterInit
import js.objects.unsafeJso
import kotlin.js.JsArray
import kotlin.js.toJsArray
import kotlin.uuid.Uuid
import org.khronos.webgl.toInt8Array

/** Convert list of public API type to Web Bluetooth (JavaScript) type. */
internal fun List<FilterPredicate>.toBluetoothLEScanOptions(): BluetoothLEScanOptions = unsafeJso {
    if (isEmpty()) {
        acceptAllAdvertisements = true
    } else {
        filters = toBluetoothLEScanFilterInit()
    }
}

internal fun List<FilterPredicate>.toBluetoothLEScanFilterInit(): JsArray<BluetoothLEScanFilterInit> =
    map(FilterPredicate::toBluetoothLEScanFilterInit)
        .toJsArray()

private fun FilterPredicate.toBluetoothLEScanFilterInit(): BluetoothLEScanFilterInit = unsafeJso {
    filters
        .filterIsInstance<Filter.Service>()
        .takeIf(Collection<Filter.Service>::isNotEmpty)
        ?.map(Filter.Service::uuid)
        ?.map(Uuid::toBluetoothServiceUUID)
        ?.toTypedArray()
        ?.let { services = it.toJsArray() }
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
        ?.let { manufacturerData = it.toJsArray() }
    filters
        .filterIsInstance<Filter.ServiceData>()
        .takeIf(Collection<Filter.ServiceData>::isNotEmpty)
        ?.map(::toBluetoothServiceDataFilterInit)
        ?.toTypedArray()
        ?.let { serviceData = it.toJsArray() }
}

private fun toBluetoothManufacturerDataFilterInit(filter: Filter.ManufacturerData) =
    unsafeJso<BluetoothManufacturerDataFilterInit> {
        companyIdentifier = filter.id
        if (filter.data != null) {
            dataPrefix = filter.data.toInt8Array()
        }
        if (filter.dataMask != null) {
            mask = filter.dataMask.toInt8Array()
        }
    }

private fun toBluetoothServiceDataFilterInit(filter: Filter.ServiceData) =
    unsafeJso<BluetoothServiceDataFilterInit> {
        service = filter.uuid.toBluetoothServiceUUID()
        if (filter.data != null) {
            dataPrefix = filter.data.toInt8Array()
        }
        if (filter.dataMask != null) {
            mask = filter.dataMask.toInt8Array()
        }
    }
