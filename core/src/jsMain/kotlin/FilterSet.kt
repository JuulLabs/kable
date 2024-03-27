package com.juul.kable

import com.benasher44.uuid.Uuid
import com.juul.kable.external.BluetoothLEScanFilterInit
import com.juul.kable.external.BluetoothManufacturerDataFilterInit

public data class FilterSet(
    public val services: List<Uuid> = emptyList(),
    public val name: String? = null,
    public val namePrefix: String? = null,
    public val manufacturerData: List<Filter.ManufacturerData> = emptyList(),
) {

    internal fun toBluetoothLEScanFilterInit(): BluetoothLEScanFilterInit {
        val filter = jso<BluetoothLEScanFilterInit>()
        if (services.isNotEmpty()) {
            filter.services = services
                .map(Uuid::toBluetoothServiceUUID)
                .toTypedArray()
        }
        if (name != null) {
            filter.name = name
        }
        if (namePrefix != null) {
            filter.namePrefix = namePrefix
        }
        if (manufacturerData.isNotEmpty()) {
            filter.manufacturerData = manufacturerData
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
        }
        return filter
    }
}

internal fun List<FilterSet>.toBluetoothLEScanFilterInit(): Array<BluetoothLEScanFilterInit> =
    map(FilterSet::toBluetoothLEScanFilterInit).toTypedArray()
