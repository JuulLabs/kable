package com.juul.kable

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
