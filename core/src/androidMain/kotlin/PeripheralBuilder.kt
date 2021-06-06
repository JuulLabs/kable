package com.juul.kable

import kotlin.coroutines.cancellation.CancellationException

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

public actual class OnConnectPeripheral internal constructor(
    private val peripheral: AndroidPeripheral
) {

    /** @throws NotReadyException if invoked without an established [connection][Peripheral.connect]. */
    @Throws(CancellationException::class, IOException::class, NotReadyException::class)
    public actual suspend fun read(
        characteristic: Characteristic,
    ): ByteArray = peripheral.read(characteristic)

    /** @throws NotReadyException if invoked without an established [connection][Peripheral.connect]. */
    @Throws(CancellationException::class, IOException::class, NotReadyException::class)
    public actual suspend fun read(
        descriptor: Descriptor,
    ): ByteArray = peripheral.read(descriptor)

    /** @throws NotReadyException if invoked without an established [connection][Peripheral.connect]. */
    @Throws(CancellationException::class, IOException::class, NotReadyException::class)
    public actual suspend fun write(
        characteristic: Characteristic,
        data: ByteArray,
        writeType: WriteType,
    ) {
        peripheral.write(characteristic, data, writeType)
    }

    /** @throws NotReadyException if invoked without an established [connection][Peripheral.connect]. */
    @Throws(CancellationException::class, IOException::class, NotReadyException::class)
    public actual suspend fun write(
        descriptor: Descriptor,
        data: ByteArray,
    ) {
        peripheral.write(descriptor, data)
    }

    /** @throws NotReadyException if invoked without an established [connection][Peripheral.connect]. */
    public suspend fun requestMtu(mtu: Int): Unit = peripheral.requestMtu(mtu)
}

public actual class PeripheralBuilder internal actual constructor() {

    internal var onConnect: OnConnectAction = {}
    public actual fun onConnect(action: OnConnectAction) {
        onConnect = action
    }

    /** Preferred transport for GATT connections to remote dual-mode devices. */
    public var transport: Transport = Transport.Le

    /** Preferred PHY for connections to remote LE device. */
    public var phy: Phy = Phy.Le1M
}
