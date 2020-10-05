package com.juul.kable

import android.annotation.TargetApi
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import com.juul.kable.gatt.Callback
import kotlinx.coroutines.android.asCoroutineDispatcher
import kotlinx.coroutines.newSingleThreadContext

internal fun BluetoothDevice.connect(context: Context): Connection? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        establishConnection26(context)
    } else {
        establishConnection21(context)
    }

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
private fun BluetoothDevice.establishConnection21(context: Context): Connection? {
    val dispatcher = newSingleThreadContext()
    val callback = Callback()
    val bluetoothGatt = connectGatt(context, false, callback) ?: return null
    return Connection(bluetoothGatt, dispatcher, callback)
}

@TargetApi(Build.VERSION_CODES.O)
private fun BluetoothDevice.establishConnection26(context: Context): Connection? {
    val thread = HandlerThread("")
    val handler = Handler(thread.looper)
    val dispatcher = handler.asCoroutineDispatcher()
    val callback = Callback()
    val bluetoothGatt =
        connectGatt(context, false, callback, transport, phy, handler) ?: return null
    thread.start()
    return Connection(bluetoothGatt, dispatcher, callback)
}
