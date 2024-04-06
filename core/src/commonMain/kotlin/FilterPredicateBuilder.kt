package com.juul.kable

import com.benasher44.uuid.Uuid

public class FilterPredicateBuilder internal constructor() {
    public sealed class Name {
        public data class Exact(public val exact: String) : Name()
        public data class Prefix(public val prefix: String) : Name()
    }

    public var name: Name? = null
    public var address: String? = null
    public var services: List<Uuid> = emptyList()
    public var manufacturerData: List<Filter.ManufacturerData> = emptyList()

    internal fun build(): FilterPredicate? = buildList {
        name?.let { name ->
            val nameFilter = when (name) {
                is Name.Exact -> Filter.Name(name.exact)
                is Name.Prefix -> Filter.NamePrefix(name.prefix)
            }
            add(nameFilter)
        }
        address?.let { address ->
            add(Filter.Address(address))
        }
        addAll(services.map(Filter::Service))
        addAll(manufacturerData)
    }.ifEmpty { null }
        ?.let(::FilterPredicate)
}
