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
import com.juul.kable.logs.Logging
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.android.asCoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.newSingleThreadContext

internal sealed class Threading {

    abstract val dispatcher: CoroutineDispatcher

    /** Available on Android O (API 26) and above. */
    data class Handler(
        val thread: HandlerThread,
        val handler: android.os.Handler,
        override val dispatcher: CoroutineDispatcher,
    ) : Threading()

    /** Used on Android versions **lower** than Android O (API 26). */
    data class SingleThreadContext(
        override val dispatcher: ExecutorCoroutineDispatcher,
    ) : Threading()
}

internal fun Threading.close() {
    when (this) {
        is Threading.Handler -> thread.quit()
        is Threading.SingleThreadContext -> dispatcher.close()
    }
}

/**
 * Creates the [Threading] that will be used for Bluetooth communication. The returned [Threading] is returned in a
 * started state and must be shutdown when no longer needed.
 */
internal fun BluetoothDevice.threading(): Threading =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val thread = HandlerThread(threadName).apply { start() }
        val handler = Handler(thread.looper)
        val dispatcher = handler.asCoroutineDispatcher()
        Threading.Handler(thread, handler, dispatcher)
    } else {
        Threading.SingleThreadContext(newSingleThreadContext(threadName))
    }

/**
 * @param transport is only used on API level >= 23.
 * @param phy is only used on API level >= 26.
 */
internal fun BluetoothDevice.connect(
    scope: CoroutineScope,
    context: Context,
    transport: Transport,
    phy: Phy,
    state: MutableStateFlow<State>,
    mtu: MutableStateFlow<Int?>,
    onCharacteristicChanged: MutableSharedFlow<ObservationEvent<ByteArray>>,
    logging: Logging,
    threading: Threading,
): Connection? {
    val callback = Callback(state, mtu, onCharacteristicChanged, logging, address)

    val bluetoothGatt = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
            val handler = (threading as Threading.Handler).handler
            connectGatt(context, false, callback, transport.intValue, phy.intValue, handler)
        }
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> connectGatt(context, false, callback, transport.intValue)
        else -> connectGatt(context, false, callback)
    } ?: return null

    return Connection(scope, bluetoothGatt, threading.dispatcher, callback, logging)
}

private val Transport.intValue: Int
    @TargetApi(Build.VERSION_CODES.M)
    get() = when (this) {
        Transport.Auto -> TRANSPORT_AUTO
        Transport.BrEdr -> TRANSPORT_BREDR
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
