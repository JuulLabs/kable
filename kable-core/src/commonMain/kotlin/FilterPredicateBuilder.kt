package com.juul.kable

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
public class FilterPredicateBuilder internal constructor() {
    public var name: Filter.Name? = null
    public var address: String? = null
    public var services: List<Uuid> = emptyList()
    public var manufacturerData: List<Filter.ManufacturerData> = emptyList()

    internal fun build(): FilterPredicate? = buildList {
        name?.let(::add)
        address?.let(Filter::Address)?.let(::add)
        addAll(services.map(Filter::Service))
        addAll(manufacturerData)
    }.ifEmpty { null }?.let(::FilterPredicate)
}
