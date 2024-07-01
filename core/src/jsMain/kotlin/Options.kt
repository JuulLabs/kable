package com.juul.kable

import com.benasher44.uuid.Uuid

/** https://developer.mozilla.org/en-US/docs/Web/API/Bluetooth/requestDevice */
public data class Options(
    @Deprecated(
        message = "Replaced with filters builder DSL",
        replaceWith = ReplaceWith("filters = { }"),
        level = DeprecationLevel.WARNING,
    )
    val filterSets: List<FilterSet>? = null,

    /**
     * Access is only granted to services listed as [service filters][Filter.Service] in [filters]. If any additional
     * services need to be accessed, they must be specified in [optionalServices].
     *
     * https://webbluetoothcg.github.io/web-bluetooth/#device-discovery
     */
    val optionalServices: List<Uuid>? = null,

    /**
     * Filters to apply when requesting devices. If predicates are non-empty, then only devices
     * that match at least one of the predicates will appear in the `requestDevice` picker.
     *
     * Filtering on Service Data is not supported because it is not implemented:
     * https://github.com/WebBluetoothCG/web-bluetooth/blob/main/implementation-status.md
     *
     * Filtering on Manufacturer Data is supported and a good explanation can be found here:
     * https://github.com/WebBluetoothCG/web-bluetooth/blob/main/data-filters-explainer.md
     */
    val filters: FiltersBuilder.() -> Unit = { },
)
