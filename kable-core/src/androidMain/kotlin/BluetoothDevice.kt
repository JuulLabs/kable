package com.juul.kable

import android.annotation.TargetApi
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.PHY_LE_1M_MASK
import android.bluetooth.BluetoothDevice.PHY_LE_2M_MASK
import android.bluetooth.BluetoothDevice.PHY_LE_CODED_MASK
import android.bluetooth.BluetoothDevice.TRANSPORT_AUTO
import android.bluetooth.BluetoothDevice.TRANSPORT_BREDR
import android.bluetooth.BluetoothDevice.TRANSPORT_LE
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.content.Context
import android.os.Build
import com.juul.kable.gatt.Callback
import com.juul.kable.logs.Logging
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration

/**
 * @param transport is only used on API level >= 23.
 * @param phy is only used on API level >= 26.
 */
internal fun BluetoothDevice.connect(
    coroutineContext: CoroutineContext,
    context: Context,
    autoConnect: Boolean,
    transport: Transport,
    phy: Phy,
    state: MutableStateFlow<State>,
    services: MutableStateFlow<List<DiscoveredService>?>,
    mtu: MutableStateFlow<Int?>,
    onCharacteristicChanged: MutableSharedFlow<ObservationEvent<ByteArray>>,
    logging: Logging,
    threadingStrategy: ThreadingStrategy,
    disconnectTimeout: Duration,
): Connection {
    val callback = Callback(state, mtu, onCharacteristicChanged, logging, address)
    val threading = threadingStrategy.acquire()

    val bluetoothGatt = try {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                val handler = (threading as Threading.Handler).handler
                connectGatt(context, autoConnect, callback, transport.intValue, phy.intValue, handler)
            }

            Build.VERSION.SDK_INT <= Build.VERSION_CODES.M && autoConnect ->
                connectGattWithReflection(context, true, callback, transport.intValue)
                    ?: connectGattCompat(context, true, callback, transport.intValue)

            else -> connectGattCompat(context, autoConnect, callback, transport.intValue)
        }
            ?: error(
                "Connection rejected by Android system due to either: bluetooth hardware unavailable, invalid MAC address, or Binder remote-invocation error",
            )
    } catch (t: Throwable) {
        threading.release()
        throw t
    }

    return Connection(coroutineContext, bluetoothGatt, threading, callback, services, disconnectTimeout, logging)
}

private fun BluetoothDevice.connectGattCompat(
    context: Context,
    autoConnect: Boolean,
    callback: BluetoothGattCallback,
    transport: Int,
): BluetoothGatt? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> connectGatt(context, autoConnect, callback, transport)
    else -> connectGatt(context, autoConnect, callback)
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
