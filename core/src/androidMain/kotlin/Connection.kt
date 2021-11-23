package com.juul.kable

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGatt.GATT_SUCCESS
import com.juul.kable.AndroidObservationEvent.CharacteristicChange
import com.juul.kable.gatt.Callback
import com.juul.kable.gatt.GattStatus
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

public class OutOfOrderGattCallbackException internal constructor(
    message: String,
) : IllegalStateException(message)

private val GattSuccess = GattStatus(GATT_SUCCESS)

internal class Connection(
    internal val bluetoothGatt: BluetoothGatt,
    internal val dispatcher: CoroutineDispatcher,
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
     * @throws GattRequestRejectedException if underlying `BluetoothGatt` method call returns `false`.
     * @throws GattStatusException if response has a non-`GATT_SUCCESS` status.
     */
    suspend inline fun <reified T> execute(
        crossinline action: BluetoothGatt.() -> Boolean,
    ): T = lock.withLock {
        withBluetoothLeContext {
            action.invoke(bluetoothGatt)
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

    /**
     * Mimics [execute] in order to uphold the same sequential execution behavior, while having a dedicated channel for
     * receiving MTU change events (so that peripheral initiated MTU changes don't result in
     * [OutOfOrderGattCallbackException]).
     *
     * See https://github.com/JuulLabs/kable/issues/86 for more details.
     *
     * @throws GattRequestRejectedException if underlying `BluetoothGatt` method call returns `false`.
     * @throws GattStatusException if response has a non-`GATT_SUCCESS` status.
     */
    suspend fun requestMtu(mtu: Int): Int = lock.withLock {
        withBluetoothLeContext {
            bluetoothGatt.requestMtu(mtu)
        }

        val response = try {
            callback.onMtuChanged.receive()
        } catch (e: ConnectionLostException) {
            throw ConnectionLostException(cause = e)
        }

        if (response.status != GattSuccess) throw GattStatusException(response.toString())
        response.mtu
    }

    fun close() {
        bluetoothGatt.close()
        invokeOnClose.invoke()
    }

    private suspend inline fun withBluetoothLeContext(crossinline action: suspend () -> Boolean) {
        try {
            withContext(dispatcher) {
                action.invoke() || throw GattRequestRejectedException()
            }
        } catch (e: CancellationException) {
            // When a disconnect event is received on the BluetoothGattCallback we shutdown the thread backing the
            // dispatcher that is used for BLE operations. Per Handler.asCoroutineDispatcher documentation:
            //
            // > If the underlying handler is closed and its message-scheduling methods start to return `false` on
            // > an attempt to submit a continuation task to the resulting dispatcher, then the [Job] of the
            // > affected task is [cancelled][Job.cancel]...
            //
            // We re-throw as an appropriate exception type to indicate the connection loss (rather than cancelling the
            // calling scope).
            throw ConnectionLostException(cause = e)
        }
    }
}
