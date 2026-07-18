package com.juul.kable.server

import android.bluetooth.BluetoothDevice
import com.juul.kable.Identifier

/** Default ATT MTU, per Bluetooth Core Specification, Vol 3, Part G: 5.2.1 ATT_MTU. */
internal const val DEFAULT_ATT_MTU = 23

/** Size of ATT headers (for notifications/writes), in bytes. */
internal const val ATT_HEADER_SIZE = 3

internal class AndroidCentral(
    internal val device: BluetoothDevice,
    private val mtu: () -> Int,
) : Central {

    override val identifier: Identifier
        get() = device.address

    override val maximumNotificationLength: Int
        get() = mtu() - ATT_HEADER_SIZE

    override fun equals(other: Any?): Boolean =
        other is AndroidCentral && other.device.address == device.address

    override fun hashCode(): Int = device.address.hashCode()

    override fun toString(): String = "Central(identifier=${device.address})"
}
