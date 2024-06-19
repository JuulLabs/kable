package com.juul.kable

import com.benasher44.uuid.Uuid
import com.juul.kable.external.BluetoothLEScanFilterInit
import com.juul.kable.external.BluetoothManufacturerDataFilterInit

/**
 * Filtering on Service Data is not supported because it is not implemented:
 * https://github.com/WebBluetoothCG/web-bluetooth/blob/main/implementation-status.md
 *
 * Filtering on Manufacturer Data is supported and a good explanation can be found here:
 * https://github.com/WebBluetoothCG/web-bluetooth/blob/main/data-filters-explainer.md
 */
@Deprecated(
    message = "Replaced with FilterPredicateBuilder",
    replaceWith = ReplaceWith(
        """
        FilterPredicateBuilder().apply {
            name = name
            services = services
            manufacturerData = manufacturerData
        }.build()"
        """,
    ),
    level = DeprecationLevel.WARNING,
)
public data class FilterSet(
    public val services: List<Filter.Service> = emptyList(),
    public val name: Filter.Name.Exact? = null,
    public val namePrefix: Filter.Name.Prefix? = null,
    public val manufacturerData: List<Filter.ManufacturerData> = emptyList(),
)

internal fun FilterSet.toBluetoothLEScanFilterInit(): BluetoothLEScanFilterInit {
    FilterPredicateBuilder().apply {
        services
    }
    val filter = jso<BluetoothLEScanFilterInit>()
    if (services.isNotEmpty()) {
        filter.services = services
            .map(Filter.Service::uuid)
            .map(Uuid::toBluetoothServiceUUID)
            .toTypedArray()
    }
    if (name != null) {
        filter.name = name.exact
    }
    if (namePrefix != null) {
        filter.namePrefix = namePrefix.prefix
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

internal fun List<FilterSet>.toBluetoothLEScanFilterInit(): List<BluetoothLEScanFilterInit> =
    map(FilterSet::toBluetoothLEScanFilterInit)
