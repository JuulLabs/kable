package com.juul.kable

import android.annotation.TargetApi
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.PHY_LE_1M_MASK
import android.bluetooth.BluetoothDevice.TRANSPORT_AUTO
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import com.juul.kable.gatt.Callback
import kotlinx.coroutines.android.asCoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.newSingleThreadContext

internal fun BluetoothDevice.connect(
    context: Context,
    state: MutableStateFlow<State>,
): Connection? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        connectApi26(context, state)
    } else {
        connectApi21(context, state)
    }

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
private fun BluetoothDevice.connectApi21(
    context: Context,
    state: MutableStateFlow<State>,
): Connection? {
    val callback = Callback(state)
    val bluetoothGatt = connectGatt(context, false, callback) ?: return null
    val dispatcher = newSingleThreadContext(threadName)
    return Connection(bluetoothGatt, dispatcher, callback, dispatcher::close)
}

@TargetApi(Build.VERSION_CODES.O)
private fun BluetoothDevice.connectApi26(
    context: Context,
    state: MutableStateFlow<State>,
): Connection? {
    val thread = HandlerThread(threadName)
    val handler = Handler(thread.looper)
    val dispatcher = handler.asCoroutineDispatcher()
    val callback = Callback(state)

    // todo: Have `transport` and `phy` be configurable.
    val transport = TRANSPORT_AUTO
    val phy = PHY_LE_1M_MASK

    val bluetoothGatt =
        connectGatt(context, false, callback, transport, phy, handler) ?: return null
    thread.start()
    return Connection(bluetoothGatt, dispatcher, callback, thread::quit)
}

private val BluetoothDevice.threadName: String
    get() = "Gatt@$this"
