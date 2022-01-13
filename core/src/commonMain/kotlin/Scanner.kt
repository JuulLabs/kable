package com.juul.kable

import com.benasher44.uuid.Uuid
import kotlinx.coroutines.flow.Flow
import kotlin.jvm.JvmName

public interface Scanner {
    public val advertisements: Flow<Advertisement>
}

@Deprecated(
    message = "Replaced with ScannerBuilder DSL",
    replaceWith = ReplaceWith("Scanner { filters = services?.map { Filter.Service(it) } }"),
)
public fun Scanner(services: List<Uuid>?): Scanner =
    Scanner { filters = services?.map { Filter.Service(it) } }

@Deprecated(
    message = "Replaced with ScannerBuilder DSL",
    replaceWith = ReplaceWith("Scanner { this.filters = filters }"),
)
@JvmName("scannerWithFilters")
public fun Scanner(filters: List<Filter>?): Scanner =
    Scanner { this.filters = filters }

public fun Scanner(
    builderAction: ScannerBuilder.() -> Unit = {},
): Scanner = ScannerBuilder().apply(builderAction).build()
