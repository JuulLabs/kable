package com.juul.kable

import kotlinx.coroutines.flow.Flow

public interface Scanner<out T : Advertisement> {
    public val advertisements: Flow<T>
}

@Suppress("FunctionName") // Builder function.
public fun Scanner(
    builderAction: ScannerBuilder.() -> Unit = {},
): PlatformScanner = ScannerBuilder().apply(builderAction).build()
