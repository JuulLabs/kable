package com.juul.kable

import kotlin.uuid.Uuid

public class FilterPredicateBuilder internal constructor() {
    public var name: Filter.Name? = null
    public var address: String? = null
    public var services: List<Uuid> = emptyList()
    public var manufacturerData: List<Filter.ManufacturerData> = emptyList()
    public var serviceData: List<Filter.ServiceData> = emptyList()

    internal fun build(): FilterPredicate? = buildList {
        name?.let(::add)
        address?.let(Filter::Address)?.let(::add)
        addAll(services.map(Filter::Service))
        addAll(manufacturerData)
        addAll(serviceData)
    }.ifEmpty { null }?.let(::FilterPredicate)
}
