package com.juul.kable

import kotlinx.coroutines.flow.Flow

public interface Scanner {
    public val advertisements: Flow<Advertisement>
}

@Deprecated(
    message = "Replaced with ScannerBuilder DSL",
    replaceWith = ReplaceWith("Scanner { this.services = services }"),
)
public fun Scanner(filters: List<Filter>?): Scanner =
    ScannerBuilder().apply { this.filters = filters }.build()

public fun Scanner(
    builderAction: ScannerBuilder.() -> Unit = {},
): Scanner = ScannerBuilder().apply(builderAction).build()
