package com.juul.kable

import kotlinx.coroutines.flow.Flow

public interface Scanner {
    public val advertisements: Flow<Advertisement>
}

@Suppress("FunctionName") // Builder function.
public fun Scanner(
    builderAction: ScannerBuilder.() -> Unit = {},
): PlatformScanner = ScannerBuilder().apply(builderAction).build()
