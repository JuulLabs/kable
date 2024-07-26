package com.juul.kable

import com.benasher44.uuid.Uuid

/** https://developer.mozilla.org/en-US/docs/Web/API/Bluetooth/requestDevice */
public fun Options(builder: OptionsBuilder.() -> Unit): Options =
    OptionsBuilder().apply(builder).build()

@Deprecated(
    message = "Use Options builder instead. See https://github.com/JuulLabs/kable/issues/723 for details.",
    replaceWith = ReplaceWith("Options { }"),
)
public fun Options(
    filters: List<Filter>? = null,
    filterSets: List<FilterSet>? = null,
    optionalServices: List<Uuid>? = null,
): Options = Options {
    filters {
        if (filters != null) {
            filters.forEach { filter ->
                match {
                    when (filter) {
                        is Filter.Name -> name = filter
                        is Filter.Service -> services = listOf(filter.uuid)
                        is Filter.ManufacturerData -> manufacturerData = listOf(filter)
                        is Filter.Address -> { /* no-op */ }
                    }
                }
            }
        } else if (filterSets != null) {
            filterSets.forEach { filterSet ->
                match {
                    name = filterSet.name
                    services = filterSet.services.map { it.uuid }
                    manufacturerData = filterSet.manufacturerData
                }
            }
        }
    }
    this.optionalServices = optionalServices
}

public data class Options internal constructor(

    @Deprecated(
        message = "Replaced with filters builder DSL. See https://github.com/JuulLabs/kable/issues/723 for details.",
        replaceWith = ReplaceWith("Options { filters { } }"),
        level = DeprecationLevel.WARNING,
    )
    val filterSets: List<FilterSet>? = null,

    /**
     * Access is only granted to services listed as [service filters][Filter.Service] in [filters]. If any additional
     * services need to be accessed, they must be specified in [optionalServices].
     *
     * https://webbluetoothcg.github.io/web-bluetooth/#device-discovery
     */
    @Deprecated(
        message = "Use Options builder instead. See https://github.com/JuulLabs/kable/issues/723 for details.",
        replaceWith = ReplaceWith("Options { optionalServices = listOf() }"),
    )
    val optionalServices: List<Uuid>? = null,

    internal val filterPredicates: List<FilterPredicate>,
)
