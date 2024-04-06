package com.juul.kable

import com.benasher44.uuid.Uuid
import com.juul.kable.Filter.Address
import com.juul.kable.Filter.Name
import com.juul.kable.Filter.NamePrefix
import com.juul.kable.Filter.Service

internal data class FilterPredicate(
    /** A non-empty list of filters, all of which must match to satisfy this predicate. */
    val filters: List<Filter>,
)

internal data class FilterPredicateSet(
    /** A list of predicates, any of which may match to satisfy this predicate set. */
    val predicates: List<FilterPredicate>,
)

internal fun FilterPredicateSet.isEmpty(): Boolean =
    predicates.isEmpty()

/** Returns `true` if any of the predicates match the given parameters. */
internal fun FilterPredicateSet.matches(
    services: List<Uuid>? = null,
    name: String? = null,
    address: String? = null,
    manufacturerData: ManufacturerData? = null,
) = predicates.any { it.matches(services, name, address, manufacturerData) }

/** Returns `true` if all of the filters on this predicate match the given parameters. */
internal fun FilterPredicate.matches(
    services: List<Uuid>? = null,
    name: String? = null,
    address: String? = null,
    manufacturerData: ManufacturerData? = null,
): Boolean = filters.all { it.matches(services, name, address, manufacturerData) }

private fun Filter.matches(
    services: List<Uuid>? = null,
    name: String? = null,
    address: String? = null,
    manufacturerData: ManufacturerData? = null,
): Boolean = when (this) {
    is Address -> matches(address)
    is Filter.ManufacturerData -> matches(manufacturerData?.code, manufacturerData?.data)
    is Name -> matches(name)
    is NamePrefix -> matches(name)
    is Service -> matches(services)
}
