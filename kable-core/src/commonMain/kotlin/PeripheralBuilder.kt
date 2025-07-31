package com.juul.kable

import com.juul.kable.logs.LoggingBuilder
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private val MTU_RANGE = 23..517

public expect class ServicesDiscoveredPeripheral {

    public suspend fun read(
        characteristic: Characteristic,
    ): ByteArray

    public suspend fun write(
        characteristic: Characteristic,
        data: ByteArray,
        writeType: WriteType = WriteType.WithoutResponse,
    ): Unit

    public suspend fun read(
        descriptor: Descriptor,
    ): ByteArray

    public suspend fun write(
        descriptor: Descriptor,
        data: ByteArray,
    ): Unit
}

public class ObservationExceptionPeripheral internal constructor(peripheral: Peripheral) {
    public val state: StateFlow<State> = peripheral.state
}

public class AndroidConfiguration {
    /** The MTU to request upon connecting to remote peripheral. */
    public var mtu: Int? = null
        set(value) {
            if (value != null) {
                require(value in MTU_RANGE) { "MTU must be within $MTU_RANGE, $value was requested" }
            }
            field = value
        }
}

internal typealias OnAndroidBuilder = AndroidConfiguration.() -> Unit
internal typealias ServicesDiscoveredAction = suspend ServicesDiscoveredPeripheral.() -> Unit
internal typealias ObservationExceptionHandler = suspend ObservationExceptionPeripheral.(cause: Exception) -> Unit

internal val defaultDisconnectTimeout = 5.seconds

public expect class PeripheralBuilder internal constructor() {

    public fun onAndroid(action: OnAndroidBuilder)

    public fun logging(init: LoggingBuilder)

    /**
     * Registers a [ServicesDiscoveredAction] for the [Peripheral] that is invoked after initial
     * service discovery (upon establishing a connection).
     *
     * Is **not** invoked upon subsequent service re-discoveries (due to peripheral service database
     * changing while connected).
     */
    public fun onServicesDiscovered(action: ServicesDiscoveredAction)

    /**
     * Registers an [ObservationExceptionHandler] for the [Peripheral]. When registered, observation failures are
     * passed to the [ObservationExceptionHandler] instead of through [observation][Peripheral.observe] flows. Any
     * exceptions in the [ObservationExceptionHandler] will propagate through (and terminate) the associated
     * [observation][Peripheral.observe] flow.
     *
     * Some failures are due to connection drops before the connection state has propagated from the system, the
     * [ObservationExceptionHandler] can be useful for ignoring failures that precursor a connection drop:
     *
     * ```
     * Peripheral(advertisement) {
     *     observationExceptionHandler { cause ->
     *         // Only propagate failure if we don't see a disconnect within a second.
     *         withTimeoutOrNull(1_000L) {
     *             state.first { it is Disconnected }
     *         } ?: throw IllegalStateException("Observation failure occurred.", cause)
     *         println("Ignored failure associated with disconnect: $cause")
     *     }
     * }
     * ```
     */
    public fun observationExceptionHandler(handler: ObservationExceptionHandler)

    /**
     * Amount of time to allow system to gracefully disconnect before forcefully closing the
     * peripheral.
     *
     * Only applicable on Android.
     */
    public var disconnectTimeout: Duration
}
