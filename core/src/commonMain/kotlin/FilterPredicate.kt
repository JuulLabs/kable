package com.juul.kable

import com.benasher44.uuid.Uuid
import com.juul.kable.Filter.Address
import com.juul.kable.Filter.Name
import com.juul.kable.Filter.Service

internal data class FilterPredicate(
    /** A non-empty list of filters, all of which must match to satisfy this predicate. */
    val filters: List<Filter>,
) {
    init {
        require(filters.isNotEmpty())
    }
}

internal data class FilterPredicateSet(
    /** A list of predicates, any of which may match to satisfy this predicate set. */
    val predicates: List<FilterPredicate> = emptyList(),
)

internal fun FilterPredicateSet.isEmpty(): Boolean =
    predicates.isEmpty()

internal fun FilterPredicateSet.flatten(): List<Filter> =
    predicates.flatMap(FilterPredicate::filters)

/**
 * Returns `true` if at least one of the predicates match the given parameters. Also returns
 * `true` if empty because there are no predicates that _do not_ match the inputs.
 */
internal fun FilterPredicateSet.matches(
    services: List<Uuid>? = null,
    name: String? = null,
    address: String? = null,
    manufacturerData: ManufacturerData? = null,
) = if (isEmpty()) {
    true
} else {
    predicates.any { it.matches(services, name, address, manufacturerData) }
}

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
    is Service -> matches(services)
}
