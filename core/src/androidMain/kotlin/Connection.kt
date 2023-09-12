package com.juul.kable

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGatt.GATT_SUCCESS
import com.juul.kable.gatt.Callback
import com.juul.kable.gatt.GattStatus
import com.juul.kable.gatt.Response
import com.juul.kable.logs.Logger
import com.juul.kable.logs.Logging
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

public class OutOfOrderGattCallbackException internal constructor(
    message: String,
) : IllegalStateException(message)

private val GattSuccess = GattStatus(GATT_SUCCESS)

internal class Connection(
    scope: CoroutineScope,
    internal val bluetoothGatt: BluetoothGatt,
    internal val dispatcher: CoroutineDispatcher,
    private val callback: Callback,
    logging: Logging,
) {

    private val job = Job(scope.coroutineContext[Job]).apply {
        invokeOnCompletion {
            bluetoothGatt.close()
        }
    }
    private val scope = CoroutineScope(scope.coroutineContext + job + CoroutineName("Kable/Connection/${bluetoothGatt.device.address}"))

    private val logger = Logger(logging, tag = "Kable/Connection", identifier = bluetoothGatt.device.address)

    private val lock = Mutex()
    private var deferredResponse: Deferred<Response>? = null

    /**
     * Executes specified [BluetoothGatt] [action].
     *
     * Android Bluetooth Low Energy has strict requirements: all I/O must be executed sequentially. In other words, the
     * response for an [action] must be received before another [action] can be performed. Additionally, the Android BLE
     * stack can become unstable if I/O isn't performed on a dedicated thread.
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
        deferredResponse?.let {
            if (it.isActive) {
                // Discard response as we've performed another `execute` without the previous finishing. This happens if
                // a previous `execute` was cancelled after invoking GATT action, but before receiving response from
                // callback channel. See the following issues for more details:
                // https://github.com/JuulLabs/kable/issues/326
                // https://github.com/JuulLabs/kable/issues/450
                val response = it.await()
                logger.warn {
                    message = "Discarded response"
                    detail("response", response.toString())
                }
            }
        }

        withContext(dispatcher) {
            if (!bluetoothGatt.action()) throw GattRequestRejectedException()
        }
        val deferred = scope.async { callback.onResponse.receive() }
        deferredResponse = deferred

        val response = try {
            deferred.await()
        } catch (e: ConnectionLostException) {
            throw ConnectionLostException(cause = e)
        }
        deferredResponse = null

        if (response.status != GattSuccess) throw GattStatusException(response.toString())

        // `lock` should always enforce a 1:1 matching of request to response, but if an Android `BluetoothGattCallback`
        // method gets called out of order then we'll cast to the wrong response type.
        response as? T
            ?: throw OutOfOrderGattCallbackException(
                "Unexpected response type ${response.javaClass.simpleName} received",
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
        withContext(dispatcher) {
            if (!bluetoothGatt.requestMtu(mtu)) throw GattRequestRejectedException()
        }

        val response = try {
            callback.onMtuChanged.receive()
        } catch (e: ConnectionLostException) {
            throw ConnectionLostException(cause = e)
        }

        if (response.status != GattSuccess) throw GattStatusException(response.toString())
        response.mtu
    }
}
