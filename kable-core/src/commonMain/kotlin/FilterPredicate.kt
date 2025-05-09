package com.juul.kable

import com.juul.kable.Filter.Address
import com.juul.kable.Filter.Name
import com.juul.kable.Filter.Service
import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid

@JvmInline
internal value class FilterPredicate(
    /** A non-empty list of filters, all of which must match to satisfy this predicate. */
    val filters: List<Filter>,
) {
    init {
        require(filters.isNotEmpty())
    }
}

internal fun List<FilterPredicate>.flatten(): List<Filter> =
    flatMap(FilterPredicate::filters)

/**
 * Returns `true` if at least one of the predicates match the given parameters. Also returns
 * `true` if empty because there are no predicates that _do not_ match the inputs.
 */
internal fun List<FilterPredicate>.matches(
    services: List<Uuid>? = null,
    name: String? = null,
    address: String? = null,
    manufacturerData: ManufacturerData? = null,
    serviceData: Map<Uuid, ByteArray>? = null,
) = if (isEmpty()) {
    true
} else {
    any { it.matches(services, name, address, manufacturerData, serviceData) }
}

/** Returns `true` if all of the filters on this predicate match the given parameters. */
internal fun FilterPredicate.matches(
    services: List<Uuid>? = null,
    name: String? = null,
    address: String? = null,
    manufacturerData: ManufacturerData? = null,
    serviceData: Map<Uuid, ByteArray>? = null,
): Boolean = filters.all { it.matches(services, name, address, manufacturerData, serviceData) }

private fun Filter.matches(
    services: List<Uuid>?,
    name: String?,
    address: String?,
    manufacturerData: ManufacturerData?,
    serviceData: Map<Uuid, ByteArray>?,
): Boolean = when (this) {
    is Address -> matches(address)
    is Filter.ManufacturerData -> matches(manufacturerData?.code, manufacturerData?.data)
    is Filter.ServiceData -> serviceData != null && uuid in serviceData && matches(serviceData[uuid])
    is Name -> matches(name)
    is Service -> matches(services)
}
