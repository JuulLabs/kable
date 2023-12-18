package com.juul.kable

import com.juul.kable.logs.Logging
import com.juul.kable.logs.LoggingBuilder

/** Preferred transport for GATT connections to remote dual-mode devices. */
public enum class Transport {

    /** No preference of physical transport for GATT connections to remote dual-mode devices. */
    Auto,

    /** Prefer BR/EDR transport for GATT connections to remote dual-mode devices. */
    BrEdr,

    /** Prefer LE transport for GATT connections to remote dual-mode devices. */
    Le,
}

/** Preferred Physical Layer (PHY) for connections to remote LE devices. */
public enum class Phy {

    /** Bluetooth LE 1M PHY. */
    Le1M,

    /**
     * Bluetooth LE 2M PHY.
     *
     * Per [Exploring Bluetooth 5 – Going the Distance](https://www.bluetooth.com/blog/exploring-bluetooth-5-going-the-distance/#mcetoc_1d7vdh6b25):
     * "The new LE 2M PHY allows the physical layer to operate at 2 Ms/s and thus enables higher data rates than LE 1M
     * and Bluetooth 4."
     */
    Le2M,

    /**
     * Bluetooth LE Coded PHY.
     *
     * Per [Exploring Bluetooth 5 – Going the Distance](https://www.bluetooth.com/blog/exploring-bluetooth-5-going-the-distance/#mcetoc_1d7vdh6b26):
     * "The LE Coded PHY allows range to be quadrupled (approximately), compared to Bluetooth® 4 and this has been
     * accomplished without increasing the transmission power required."
     */
    LeCoded,
}

public actual class ServicesDiscoveredPeripheral internal constructor(
    private val peripheral: AndroidPeripheral,
) {

    public actual suspend fun read(
        characteristic: Characteristic,
    ): ByteArray = peripheral.read(characteristic)

    public actual suspend fun read(
        descriptor: Descriptor,
    ): ByteArray = peripheral.read(descriptor)

    public actual suspend fun write(
        characteristic: Characteristic,
        data: ByteArray,
        writeType: WriteType,
    ) {
        peripheral.write(characteristic, data, writeType)
    }

    public actual suspend fun write(
        descriptor: Descriptor,
        data: ByteArray,
    ) {
        peripheral.write(descriptor, data)
    }

    public suspend fun requestMtu(
        mtu: Int,
    ): Int = peripheral.requestMtu(mtu)
}

public actual class PeripheralBuilder internal actual constructor() {

    internal var logging: Logging = Logging()
    public actual fun logging(init: LoggingBuilder) {
        val logging = Logging()
        logging.init()
        this.logging = logging
    }

    internal var onServicesDiscovered: ServicesDiscoveredAction = {}
    public actual fun onServicesDiscovered(action: ServicesDiscoveredAction) {
        onServicesDiscovered = action
    }

    internal var observationExceptionHandler: ObservationExceptionHandler = { cause -> throw cause }
    public actual fun observationExceptionHandler(handler: ObservationExceptionHandler) {
        observationExceptionHandler = handler
    }

    internal var autoConnectPredicate: () -> Boolean = { false }

    /**
     * Whether to automatically connect as soon as the remote device becomes available ([predicate]
     * returns `true` — [connection][Peripheral.connect] attempts will wait indefinitely unless
     * wrapped in a `withTimeout`), or to directly connect to the remote device ([predicate] returns
     * `false` — connection attempts timeout after ~30 seconds).
     *
     * [predicate] is called once per connection attempt, not per call to
     * [connect][Peripheral.connect].
     */
    public fun autoConnectIf(predicate: () -> Boolean) {
        autoConnectPredicate = predicate
    }

    /** Preferred transport for GATT connections to remote dual-mode devices. */
    public var transport: Transport = Transport.Le

    /** Preferred PHY for connections to remote LE device. */
    public var phy: Phy = Phy.Le1M

    public var threadingStrategy: ThreadingStrategy = OnDemandThreadingStrategy
}
