package com.juul.kable

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom
import com.juul.kable.Filter as KFilter

@Deprecated(
    message = "Parameter order and type has changed (use filters from com.juul.kable.Filter package).",
    replaceWith = ReplaceWith("Options(filters, optionalServices)"),
    level = DeprecationLevel.WARNING,
)
public fun Options(
    optionalServices: Array<String> = emptyArray(),
    filters: Array<Options.Filter>? = null,
): Options {
    val newFilters = filters?.toList()
        ?.flatMap { filter ->
            when (filter) {
                is Options.Filter.Name -> listOf(KFilter.Name(filter.name))
                is Options.Filter.NamePrefix -> listOf(KFilter.NamePrefix(filter.namePrefix))
                is Options.Filter.Services -> filter.services.map(KFilter::Service)
            }
        }
    return Options(newFilters, optionalServices.map(::uuidFrom))
}

/** https://developer.mozilla.org/en-US/docs/Web/API/Bluetooth/requestDevice */
public data class Options(
    val filters: List<KFilter>? = null,

    /**
     * Access is only granted to services listed as [service filters][KFilter.Service] in [filters]. If any additional
     * services need to be accessed, they must be specified in [optionalServices].
     *
     * https://webbluetoothcg.github.io/web-bluetooth/#device-discovery
     */
    val optionalServices: List<Uuid>? = null,
) {

    @Deprecated(
        message = "Consolidated to com.juul.kable.Filter.",
        level = DeprecationLevel.WARNING,
    )
    public sealed class Filter {
        public data class Name(val name: String) : Filter()
        public data class NamePrefix(val namePrefix: String) : Filter()
        public data class Services(val services: List<Uuid>) : Filter()
    }
}
