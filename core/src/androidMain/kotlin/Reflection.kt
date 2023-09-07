@file:SuppressLint("PrivateApi")

package com.juul.kable

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.content.Context

/**
 * Workaround for
 * [Race condition in BluetoothGatt when using BluetoothDevice#connectGatt()](https://issuetracker.google.com/issues/36995652).
 *
 * Kotlin adaptation of RxAndroidBle's
 * [`BleConnectionCompat.java`](https://github.com/dariuszseweryn/RxAndroidBle/blob/60b99f28766c208179055e96db894c66ac090ad9/rxandroidble/src/main/java/com/polidea/rxandroidble2/internal/util/BleConnectionCompat.java).
 */
internal fun BluetoothDevice.connectGattWithReflection(
    context: Context,
    autoConnect: Boolean,
    callback: BluetoothGattCallback,
    transport: Int,
): BluetoothGatt? {
    return try {
        val bluetoothAdapter = getBluetoothAdapterOrNull() ?: return null
        val iBluetoothManager = bluetoothAdapter.invoke("getBluetoothManager") ?: return null
        val iBluetoothGatt = iBluetoothManager.invoke("getBluetoothGatt") ?: return null

        val bluetoothGatt = BluetoothGatt(context, iBluetoothGatt, this, transport)
            .apply { setAutoConnect(autoConnect) }
        val successful = bluetoothGatt.connect(autoConnect, callback)
        bluetoothGatt.takeIf { successful }
    } catch (e: ReflectiveOperationException) {
        null
    } catch (e: IllegalArgumentException) {
        null
    }
}

private fun Any.invoke(method: String): Any? =
    javaClass.getDeclaredMethod(method).apply { isAccessible = true }.invoke(this)

private fun BluetoothGatt(
    context: Context,
    iBluetoothGatt: Any,
    bluetoothDevice: BluetoothDevice,
    transport: Int,
): BluetoothGatt = BluetoothGatt::class.java.declaredConstructors[0]
    .apply { isAccessible = true }
    .run {
        when (val parameters = parameterTypes.size) {
            3 -> newInstance(context, iBluetoothGatt, bluetoothDevice)
            4 -> newInstance(context, iBluetoothGatt, bluetoothDevice, transport)
            else -> error("Unsupported BluetoothGatt constructor with $parameters parameters")
        } as BluetoothGatt
    }

private fun BluetoothGatt.setAutoConnect(value: Boolean) {
    javaClass.getDeclaredField("mAutoConnect").apply {
        isAccessible = true
        setBoolean(this@setAutoConnect, value)
    }
}

private fun BluetoothGatt.connect(
    autoConnect: Boolean,
    callback: BluetoothGattCallback,
): Boolean = javaClass
    .getDeclaredMethod("connect", java.lang.Boolean::class.java, BluetoothGattCallback::class.java)
    .run {
        isAccessible = true
        invoke(this@connect, autoConnect, callback) as Boolean
    }
    .also { successful -> if (!successful) close() }
