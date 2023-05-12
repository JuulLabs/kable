package com.juul.kable

import kotlinx.coroutines.flow.StateFlow

@Deprecated(
    message = "Moved as nested class of `AndroidPeripheral`.",
    replaceWith = ReplaceWith("AndroidPeripheral.Priority"),
)
public typealias Priority = AndroidPeripheral.Priority

public interface AndroidPeripheral : Peripheral {

    public enum class Priority { Low, Balanced, High }

    /**
     * Returns the hardware address of this [AndroidPeripheral].
     *
     * For example, "00:11:22:AA:BB:CC".
     */
    public val address: String

    public fun requestConnectionPriority(priority: Priority): Boolean

    /**
     * Requests that the current connection's MTU be changed. Suspends until the MTU changes, or failure occurs. The
     * negotiated MTU value is returned, which may not be [mtu] value requested if the remote peripheral negotiated an
     * alternate MTU.
     *
     * @throws NotReadyException if invoked without an established [connection][Peripheral.connect].
     * @throws GattRequestRejectedException if Android was unable to fulfill the MTU change request.
     * @throws GattStatusException if MTU change request failed.
     */
    public suspend fun requestMtu(mtu: Int): Int

    /**
     * [StateFlow] of the most recently negotiated MTU. The MTU will change upon a successful request to change the MTU
     * (via [requestMtu]), or if the peripheral initiates an MTU change. [StateFlow]'s `value` will be `null` until MTU
     * is negotiated.
     */
    public val mtu: StateFlow<Int?>
}
