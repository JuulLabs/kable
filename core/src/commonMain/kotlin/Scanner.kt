package com.juul.kable

import com.benasher44.uuid.Uuid
import kotlinx.coroutines.flow.Flow

public interface Scanner {
    public val advertisements: Flow<Advertisement>
}

@Deprecated(
    message = "Replaced with ScannerBuilder DSL",
    replaceWith = ReplaceWith("Scanner { this.services = services }"),
)
public fun Scanner(services: List<Uuid>?): Scanner =
    ScannerBuilder().apply { this.services = services }.build()

public fun Scanner(
    builderAction: ScannerBuilder.() -> Unit = {},
): Scanner = ScannerBuilder().apply(builderAction).build()
