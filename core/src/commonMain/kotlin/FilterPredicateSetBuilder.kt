package com.juul.kable

private typealias FilterBuilderAction = FilterPredicateBuilder.() -> Unit

public class FilterPredicateSetBuilder internal constructor() {
    private val filterBuilderActions: MutableList<FilterBuilderAction> = mutableListOf()

    public fun match(builderAction: FilterBuilderAction) {
        filterBuilderActions.add(builderAction)
    }

    internal fun build() = FilterPredicateSet(
        filterBuilderActions.mapNotNull { builderAction ->
            val builder = FilterPredicateBuilder()
            builder.builderAction()
            builder.build()
        },
    )
}
