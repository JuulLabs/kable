package com.juul.kable

import com.benasher44.uuid.Uuid

public class OptionsBuilder internal constructor() {

    private var filters: List<FilterPredicate> = emptyList()

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
    public fun filters(builder: FiltersBuilder.() -> Unit) {
        filters = FiltersBuilder().apply(builder).build()
    }

    /**
     * Access is only granted to services listed as [service filters][Filter.Service] in [filters].
     * If any additional services need to be accessed, they must be specified in [optionalServices].
     *
     * https://webbluetoothcg.github.io/web-bluetooth/#device-discovery
     */
    public var optionalServices: List<Uuid> = emptyList()

    internal fun build() = Options(filters, optionalServices)
}
