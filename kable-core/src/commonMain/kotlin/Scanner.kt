package com.juul.kable

import com.juul.kable.Bluetooth.Availability.Available
import kotlinx.coroutines.flow.Flow

public interface Scanner<out T : Advertisement> {

    /**
     * [Bluetooth.availability] flow should emit [Available] before collecting from [advertisements] flow.
     *
     * @throws IllegalStateException If scanning could not be initiated (e.g. bluetooth or scan feature unavailable).
     * @throws UnmetRequirementException If a transient state was not satisfied (e.g. bluetooth disabled).
     */
    public val advertisements: Flow<T>
}

/**
 * @throws IllegalArgumentException If an invalid configuration is specified (e.g. using MAC address filter on Apple platforms).
 */
@Suppress("FunctionName") // Builder function.
public fun Scanner(
    builderAction: ScannerBuilder.() -> Unit = {},
): PlatformScanner = ScannerBuilder().apply(builderAction).build()
