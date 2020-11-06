package com.juul.kable

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGatt.GATT_SUCCESS
import android.os.RemoteException
import com.juul.kable.gatt.Callback
import com.juul.kable.gatt.GattStatus
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

public class OutOfOrderGattCallbackException internal constructor(
    message: String,
) : IllegalStateException(message)

internal data class CharacteristicChange(
    val characteristic: Characteristic,
    val data: ByteArray,
)

private val GattSuccess = GattStatus(GATT_SUCCESS)

internal class Connection(
    internal val bluetoothGatt: BluetoothGatt,
    private val dispatcher: CoroutineDispatcher,
    private val callback: Callback,
    private val invokeOnClose: () -> Unit,
) {

    init {
        callback.invokeOnDisconnected(::close)
    }

    val characteristicChanges = callback.onCharacteristicChanged
        .map { (bluetoothGattCharacteristic, value) ->
            CharacteristicChange(bluetoothGattCharacteristic.toLazyCharacteristic(), value)
        }

    private val lock = Mutex()

    /**
     * Executes specified [BluetoothGatt] [action].
     *
     * Android Bluetooth Low Energy has strict requirements: all I/O must be executed sequentially. In other words, the
     * response for an [action] must be received before another [action] can be performed. Additionally, the Android BLE
     * stack can be unstable if I/O isn't performed on a dedicated thread.
     *
     * These requirements are fulfilled by ensuring that all [action]s are performed behind a [Mutex]. On Android pre-O
     * a single threaded [CoroutineDispatcher] is used, Android O and newer a [CoroutineDispatcher] backed by an Android
     * `Handler` is used (and is also used in the Android BLE [Callback]).
     *
     * @throws GattStatusException if response has a non-`GATT_SUCCESS` status.
     */
    suspend inline fun <reified T> execute(
        crossinline action: BluetoothGatt.() -> Boolean,
    ): T = lock.withLock {
        withContext(dispatcher) {
            // todo: Exception type (other than RemoteException; that is uniform across platforms) — extend IOException?
            action.invoke(bluetoothGatt) || throw RemoteException("BluetoothGatt method call returned false")
        }

        val response = try {
            callback.onResponse.receive()
        } catch (e: ConnectionLostException) {
            throw ConnectionLostException(cause = e)
        }

        if (response.status != GattSuccess) throw GattStatusException(response.toString())

        // `lock` should always enforce a 1:1 matching of request to response, but if an Android `BluetoothGattCallback`
        // method gets called out of order then we'll cast to the wrong response type.
        response as? T
            ?: throw OutOfOrderGattCallbackException(
                "Unexpected response type ${response.javaClass.simpleName} received"
            )
    }

    fun close() {
        bluetoothGatt.close()
        invokeOnClose.invoke()
    }
}
