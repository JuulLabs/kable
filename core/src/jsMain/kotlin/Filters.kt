package com.juul.kable

import com.benasher44.uuid.Uuid
import com.juul.kable.external.BluetoothLEScanFilterInit
import com.juul.kable.external.BluetoothManufacturerDataFilterInit

/** Convert list of public API type to array of Web Bluetooth (JavaScript) type. */
internal fun List<Filter>.toBluetoothLEScanFilterInit(): Array<BluetoothLEScanFilterInit> {
    val filters = mutableListOf<BluetoothLEScanFilterInit>()

    // Consolidate all `Service` filters into a single `BluetoothLEScanFilterInit`.
    val serviceFilters = filterIsInstance<Filter.Service>()
    if (serviceFilters.isNotEmpty()) {
        filters += jso<BluetoothLEScanFilterInit> {
            services = serviceFilters
                .map(Filter.Service::uuid)
                .map(Uuid::toBluetoothServiceUUID)
                .toTypedArray()
        }
    }

    // Consolidate all `ManufacturerData` filters into a single `BluetoothLEScanFilterInit`.
    val manufacturerDataFilters = filterIsInstance<Filter.ManufacturerData>()
        .map { filter ->
            jso<BluetoothManufacturerDataFilterInit> {
                companyIdentifier = filter.id
                dataPrefix = filter.data
                if (filter.dataMask != null) {
                    mask = filter.dataMask
                }
            }
        }
        .toTypedArray()
    if (manufacturerDataFilters.isNotEmpty()) {
        filters += jso<BluetoothLEScanFilterInit> {
            manufacturerData = manufacturerDataFilters
        }
    }

    forEach { filter ->
        when (filter) {
            is Filter.Service -> {} // No-op: Already added above.
            is Filter.Name -> filters += jso<BluetoothLEScanFilterInit> { name = filter.name }
            is Filter.NamePrefix -> filters += jso<BluetoothLEScanFilterInit> { namePrefix = filter.prefix }
            is Filter.Address -> {} // No-op: Not supported natively (filtered later via flow).
            is Filter.ManufacturerData -> {} // No-op: Already added above.
        }
    }

    return filters.toTypedArray()
}
