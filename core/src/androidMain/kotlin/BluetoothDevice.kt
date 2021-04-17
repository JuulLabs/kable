package com.juul.kable

import android.annotation.TargetApi
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.PHY_LE_1M_MASK
import android.bluetooth.BluetoothDevice.PHY_LE_2M_MASK
import android.bluetooth.BluetoothDevice.PHY_LE_CODED_MASK
import android.bluetooth.BluetoothDevice.TRANSPORT_AUTO
import android.bluetooth.BluetoothDevice.TRANSPORT_BREDR
import android.bluetooth.BluetoothDevice.TRANSPORT_LE
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import com.juul.kable.gatt.Callback
import kotlinx.coroutines.android.asCoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.newSingleThreadContext

/**
 * @param transport is only used on API level >= 23.
 * @param phy is only used on API level >= 26.
 */
internal fun BluetoothDevice.connect(
    context: Context,
    transport: Transport,
    phy: Phy,
    state: MutableStateFlow<State>,
    invokeOnClose: () -> Unit,
): Connection? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        connectApi26(context, transport, phy, state, invokeOnClose)
    } else {
        connectApi21(context, transport, state, invokeOnClose)
    }

/**
 * @param transport is only used on API level >= 23.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
private fun BluetoothDevice.connectApi21(
    context: Context,
    transport: Transport,
    state: MutableStateFlow<State>,
    invokeOnClose: () -> Unit,
): Connection? {
    val callback = Callback(state)
    val bluetoothGatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        connectGatt(context, false, callback, transport.intValue)
    } else {
        connectGatt(context, false, callback)
    } ?: return null

    // Explicitly set Connecting state so when Peripheral is suspending until Connected, it doesn't incorrectly see
    // Disconnected before the connection request has kicked off the Connecting state (via Callback).
    state.value = State.Connecting

    val dispatcher = newSingleThreadContext(threadName)
    return Connection(
        bluetoothGatt,
        dispatcher,
        callback,
        invokeOnClose = {
            dispatcher.close()
            invokeOnClose.invoke()
        }
    )
}

@TargetApi(Build.VERSION_CODES.O)
private fun BluetoothDevice.connectApi26(
    context: Context,
    transport: Transport,
    phy: Phy,
    state: MutableStateFlow<State>,
    invokeOnClose: () -> Unit,
): Connection? {
    val thread = HandlerThread(threadName).apply { start() }
    try {
        val handler = Handler(thread.looper)
        val dispatcher = handler.asCoroutineDispatcher()
        val callback = Callback(state)

        val bluetoothGatt =
            connectGatt(context, false, callback, transport.intValue, phy.intValue, handler)
                ?: return null

        // Explicitly set Connecting state so when Peripheral is suspending until Connected, it doesn't incorrectly see
        // Disconnected before the connection request has kicked off the Connecting state (via Callback).
        state.value = State.Connecting

        return Connection(
            bluetoothGatt,
            dispatcher,
            callback,
            invokeOnClose = {
                thread.quit()
                invokeOnClose.invoke()
            }
        )
    } catch (t: Throwable) {
        thread.quit()
        throw t
    }
}

private val Transport.intValue: Int
    @TargetApi(Build.VERSION_CODES.M)
    get() = when (this) {
        Transport.Auto -> TRANSPORT_AUTO
        Transport.BreDr -> TRANSPORT_BREDR
        Transport.Le -> TRANSPORT_LE
    }

private val Phy.intValue: Int
    @TargetApi(Build.VERSION_CODES.O)
    get() = when (this) {
        Phy.Le1M -> PHY_LE_1M_MASK
        Phy.Le2M -> PHY_LE_2M_MASK
        Phy.LeCoded -> PHY_LE_CODED_MASK
    }

private val BluetoothDevice.threadName: String
    get() = "Gatt@$this"
