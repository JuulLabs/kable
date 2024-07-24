package com.juul.kable

private typealias FilterBuilderAction = FilterPredicateBuilder.() -> Unit

public class FiltersBuilder internal constructor() {
    private val filterBuilderActions: MutableList<FilterBuilderAction> = mutableListOf()

    public fun match(builderAction: FilterBuilderAction) {
        filterBuilderActions.add(builderAction)
    }

    internal fun build() = filterBuilderActions.mapNotNull { builderAction ->
        FilterPredicateBuilder().apply(builderAction).build()
    }
}
