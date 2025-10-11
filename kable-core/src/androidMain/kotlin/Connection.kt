package com.juul.kable

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION
import android.bluetooth.BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION
import android.bluetooth.BluetoothGatt.GATT_SUCCESS
import android.os.Handler
import com.juul.kable.State.Disconnected
import com.juul.kable.android.GattStatus
import com.juul.kable.coroutines.childSupervisor
import com.juul.kable.external.GATT_AUTH_FAIL
import com.juul.kable.gatt.Callback
import com.juul.kable.gatt.Response
import com.juul.kable.gatt.Response.OnServicesDiscovered
import com.juul.kable.logs.Logger
import com.juul.kable.logs.Logging
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart.ATOMIC
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.coroutineContext
import kotlin.reflect.KClass
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO

internal class BondRequiredException(val status: GattStatus) : IllegalStateException()

private val GattSuccess = GattStatus(GATT_SUCCESS)

private val BondingStatuses = listOf(
    GattStatus(GATT_AUTH_FAIL),
    GattStatus(GATT_INSUFFICIENT_AUTHENTICATION),
    GattStatus(GATT_INSUFFICIENT_ENCRYPTION),
)

/**
 * Represents a Bluetooth Low Energy connection. [Connection] should be initialized with the
 * provided [BluetoothGatt] in a connecting or connected state. When a disconnect occurs (either by
 * invoking [disconnect], or peripheral initiated disconnect), this [Connection] will be
 * [disposed][close] (and cannot be re-used).
 *
 * To disconnect: simply call [disconnect] ([Connection] will be implicitly [closed][close] at the
 * end of the [disconnect] sequence).
 *
 * If [scope], or parent [CoroutineContext] is cancelled prior to [disconnecting][disconnect], then
 * [Connection] will be abruptly [closed][close] (upon completion of [job]) without a prior
 * [disconnect] sequence.
 */
internal class Connection(
    parentContext: CoroutineContext,
    internal val gatt: BluetoothGatt,
    private val threading: Threading,
    private val callback: Callback,
    private val services: MutableStateFlow<List<PlatformDiscoveredService>?>,
    private val disconnectTimeout: Duration,
    logging: Logging,
) {

    private val name = "Kable/Connection/${gatt.device}"

    private val connectionJob = (parentContext.job as CompletableJob).apply {
        invokeOnCompletion(::close)
    }
    private val connectionScope = CoroutineScope(
        parentContext + connectionJob + CoroutineName(name),
    )

    val taskScope = connectionScope.childSupervisor("$name/Tasks")

    private val logger =
        Logger(logging, tag = "Kable/Connection", identifier = gatt.device.toString())

    init {
        // todo: Move this `require` to the PeripheralBuilder.
        require(disconnectTimeout > ZERO) { "Disconnect timeout must be >0, was $disconnectTimeout" }

        onDispose(::disconnect)
        onServiceChanged(::discoverServices)

        on<Disconnected> {
            val state = it.toString()
            logger.debug {
                message = "Disconnect detected"
                detail("state", state)
            }
            dispose(NotConnectedException("Disconnect detected"))
        }
    }

    private val dispatcher = connectionScope.coroutineContext + threading.dispatcher
    private val guard = Mutex()

    suspend fun discoverServices(retries: Int = 1) {
        logger.verbose { message = "Discovering services" }

        repeat(retries) { attempt ->
            val discoveredServices = execute<OnServicesDiscovered> {
                discoverServicesOrThrow()
            }.services.map(::PlatformDiscoveredService)

            if (discoveredServices.isEmpty()) {
                logger.warn {
                    message = "Empty services"
                    detail("attempt", "${attempt + 1} of $retries")
                }
            } else {
                logger.verbose { message = "Discovered ${discoveredServices.count()} services" }
                services.value = discoveredServices
                return
            }
        }
        services.value = emptyList()
    }

    /**
     * Executes specified [BluetoothGatt] [action].
     *
     * Android Bluetooth Low Energy has strict requirements: all I/O must be executed sequentially.
     * In other words, the response for an [action] must be received before another [action] can be
     * performed. Additionally, the Android BLE stack can become unstable if I/O isn't performed on
     * a dedicated thread.
     *
     * These requirements are fulfilled by ensuring that all [action]s are performed behind a
     * [Mutex]. On Android pre-O a single threaded [CoroutineDispatcher] is used, Android O and
     * newer a [CoroutineDispatcher] backed by an Android [Handler] is used (and is also used in the
     * Android BLE [Callback]).
     *
     * @throws GattStatusException If response has a non-`GATT_SUCCESS` status.
     * @throws NotConnectedException If connection has been closed.
     */
    suspend inline fun <reified T : Response> execute(
        noinline action: BluetoothGatt.() -> Unit,
    ): T = execute(T::class, action)

    suspend fun <T : Response> execute(
        type: KClass<T>,
        action: BluetoothGatt.() -> Unit,
    ): T {
        val response = guard.withLock {
            var executed = false
            try {
                withContext(dispatcher) {
                    gatt.action()
                    executed = true
                }
            } catch (e: CancellationException) {
                if (executed) {
                    // Ensure response buffer is received even when calling context is cancelled.
                    // UNDISPATCHED to ensure we're within the `lock` for the `receive`.
                    connectionScope.launch(start = UNDISPATCHED) {
                        val response = callback.onResponse.receive()
                        logger.debug {
                            message = "Discarded response to cancelled request"
                            detail("response", response.toString())
                        }
                    }
                }
                coroutineContext.ensureActive()
                throw e.unwrapCancellationException()
            }

            try {
                connectionScope.async {
                    callback.onResponse.receive()
                }.await()
            } catch (e: CancellationException) {
                coroutineContext.ensureActive()
                throw e.unwrapCancellationException()
            }
        }.also(::checkBondingStatus)
            .also(::checkResponse)

        // `guard` should always enforce a 1:1 matching of request-to-response, but if an Android
        // `BluetoothGattCallback` method is called out-of-order then we'll cast to the wrong type.
        return response as? T
            ?: throw InternalError(
                "Expected response type ${type.simpleName} but received ${response::class.simpleName}",
            )
    }

    /**
     * Mimics [execute] in order to uphold the same sequential execution behavior, while having a
     * dedicated channel for receiving MTU change events.
     *
     * See https://github.com/JuulLabs/kable/issues/86 for more details.
     *
     * @throws GattRequestRejectedException if underlying `BluetoothGatt` method call returns `false`.
     * @throws GattStatusException if response has a non-`GATT_SUCCESS` status.
     */
    suspend fun requestMtu(mtu: Int): Int = guard.withLock {
        try {
            withContext(dispatcher) {
                if (!gatt.requestMtu(mtu)) throw GattRequestRejectedException()
            }
            connectionScope.async { callback.onMtuChanged.receive() }.await()
        } catch (e: CancellationException) {
            coroutineContext.ensureActive()
            throw e.unwrapCancellationException()
        }
    }.also(::checkResponse).mtu

    private suspend fun disconnect() {
        if (callback.state.value is Disconnected) return

        withContext(NonCancellable) {
            try {
                withTimeout(disconnectTimeout) {
                    logger.verbose { message = "Waiting for connection tasks to complete" }
                    taskScope.coroutineContext.job.join()

                    logger.debug { message = "Disconnecting" }
                    gatt.disconnect()

                    callback.state.filterIsInstance<Disconnected>().first()
                }
                logger.info { message = "Disconnected" }
            } catch (e: TimeoutCancellationException) {
                logger.warn { message = "Timed out after $disconnectTimeout waiting for disconnect" }
            }
        }
    }

    private fun close(cause: Throwable?) {
        logger.debug(cause) { message = "Closing" }
        gatt.close()
        setDisconnected()
        threading.release()
        logger.info { message = "Closed" }
    }

    private fun setDisconnected() {
        // Avoid trampling existing `Disconnected` state (and its properties) by only updating if
        // not already `Disconnected`.
        callback.state.update { previous -> previous as? Disconnected ?: Disconnected() }
    }

    private inline fun <reified T : State> on(crossinline action: suspend (T) -> Unit) {
        taskScope.launch {
            action(callback.state.filterIsInstance<T>().first())
        }
    }

    private fun onServiceChanged(action: suspend () -> Unit) {
        callback.onServiceChanged
            .receiveAsFlow()
            .onEach { action() }
            .launchIn(taskScope)
    }

    private fun onDispose(action: suspend () -> Unit) {
        @Suppress("OPT_IN_USAGE")
        connectionScope.launch(start = ATOMIC) {
            try {
                awaitCancellation()
            } finally {
                action()
            }
        }
    }

    private fun dispose(cause: Throwable) = connectionJob.completeExceptionally(cause)
}

private fun checkBondingStatus(response: Response) {
    if (response.status in BondingStatuses) throw BondRequiredException(response.status)
}

private fun checkResponse(response: Response) {
    if (response.status != GattSuccess) {
        throw GattStatusException(response.toString(), status = response.status.value)
    }
}
