package com.juul.kable

import com.juul.kable.State.Connecting
import com.juul.kable.State.Disconnected
import com.juul.kable.coroutines.childSupervisor
import com.juul.kable.external.BluetoothDevice
import com.juul.kable.external.BluetoothRemoteGATTCharacteristic
import com.juul.kable.external.BluetoothRemoteGATTServer
import com.juul.kable.logs.Logger
import com.juul.kable.logs.Logging
import com.juul.kable.logs.Logging.DataProcessor.Operation
import com.juul.kable.logs.detail
import js.errors.JsError
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart.ATOMIC
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.TimeoutCancellationException
import com.juul.kable.interop.await
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.io.IOException
import org.khronos.webgl.DataView
import org.w3c.dom.events.Event
import web.errors.DOMException
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.js.JsAny
import kotlin.js.JsException
import kotlin.js.Promise
import kotlin.js.thrownValue
import kotlin.js.toList
import kotlin.js.unsafeCast
import kotlin.time.Duration

private typealias ObservationListener = (Event) -> Unit

private const val GATT_SERVER_DISCONNECTED = "gattserverdisconnected"
private const val CHARACTERISTIC_VALUE_CHANGED = "characteristicvaluechanged"

internal class Connection(
    parentContext: CoroutineContext,
    private val bluetoothDevice: BluetoothDevice,
    private val state: MutableStateFlow<State>,
    private val discoveredServices: MutableStateFlow<List<PlatformDiscoveredService>?>,
    private val characteristicChanges: MutableSharedFlow<ObservationEvent<DataView>>,
    private val disconnectTimeout: Duration,
    logging: Logging,
) {

    private val name = "Kable/Connection/${bluetoothDevice.id}"

    private val connectionJob = (parentContext.job as CompletableJob).apply {
        invokeOnCompletion(::close)
    }
    private val connectionScope = CoroutineScope(
        parentContext + connectionJob + CoroutineName(name),
    )

    val taskScope = connectionScope.childSupervisor("$name/Tasks")

    private val logger = Logger(logging, tag = "Kable/Connection", identifier = bluetoothDevice.id)

    private val disconnectedListener: (Event) -> Unit = {
        logger.debug { message = GATT_SERVER_DISCONNECTED }
        state.value = Disconnected()
    }

    init {
        onDispose(::disconnect)
        registerDisconnectedListener()

        on<Disconnected> {
            val state = it.toString()
            logger.debug {
                message = "Disconnect detected"
                detail("state", state)
            }
            dispose(NotConnectedException("Disconnect detected"))
        }
    }

    private val gatt = bluetoothDevice.gatt
        // `BluetootDevice.gatt` is `null` on Web Bluetooth Permission denial; as such, when `null`
        // we throw `InternalException`, as the Web Bluetooth Permission API spec is not stable, nor
        // is it utilized by Kable.
        // https://webbluetoothcg.github.io/web-bluetooth/#permission-api-integration
        ?: throw InternalError("GATT server unavailable")

    private fun servicesOrThrow(): List<PlatformDiscoveredService> =
        discoveredServices.value ?: error("Services have not been discovered")

    suspend fun discoverServices() {
        logger.verbose { message = "Discovering services" }
        state.value = Connecting.Services
        discoveredServices.value = execute(BluetoothRemoteGATTServer::getPrimaryServices)
            .toList()
            .map { it.toDiscoveredService(logger) }
    }

    private val observationListeners = mutableMapOf<PlatformCharacteristic, ObservationListener>()

    suspend fun startObservation(characteristic: Characteristic) {
        logger.debug {
            message = "Starting observation"
            detail(characteristic)
        }

        val platformCharacteristic = servicesOrThrow().obtain(characteristic, Notify or Indicate)
        if (platformCharacteristic in observationListeners) return

        val listener = platformCharacteristic.createObservationListener()
        observationListeners[platformCharacteristic] = listener

        platformCharacteristic.apply {
            logger.verbose {
                message = "addEventListener"
                detail(characteristic)
                detail("event", CHARACTERISTIC_VALUE_CHANGED)
            }
            addEventListener(CHARACTERISTIC_VALUE_CHANGED, listener)

            try {
                execute { startNotifications() }
            } catch (e: JsException) {
                removeCharacteristicValueChangedListener(listener)
                observationListeners.remove(platformCharacteristic)

                coroutineContext.ensureActive()
                throw when (e) {
                    is DOMException -> IOException("Failed to start notification", e)
                    else -> InternalError("Unexpected start notification failure", e)
                }
            }
        }
    }

    suspend fun stopObservation(characteristic: Characteristic) {
        logger.debug {
            message = "Stopping observation"
            detail(characteristic)
        }

        val platformCharacteristic = servicesOrThrow().obtain(characteristic, Notify or Indicate)

        platformCharacteristic.apply {
            try {
                execute { stopNotifications() }
            } catch (e: JsException) {
                coroutineContext.ensureActive()
                when (e.thrownValue) {
                    // DOMException: Failed to execute 'stopNotifications' on 'BluetoothRemoteGATTCharacteristic':
                    // Characteristic with UUID [...] is no longer valid. Remember to retrieve the characteristic
                    // again after reconnecting.
                    is DOMException -> throw IOException("Failed to stop notification", e)

                    is NotConnectedException -> {
                        // No-op: System implicitly clears notifications on disconnect.
                    }

                    else -> throw InternalError("Unexpected stop notification failure", e)
                }
            } finally {
                val listener = observationListeners.remove(platformCharacteristic) ?: return
                removeCharacteristicValueChangedListener(listener)
            }
        }
    }

    private val guard = Mutex()

    suspend fun <T : JsAny?> execute(
        action: BluetoothRemoteGATTServer.() -> Promise<T>,
    ): T = guard.withLock {
        unwrapCancellationExceptions {
            withContext(connectionScope.coroutineContext) {
                gatt.action().await()
            }
        }
    }

    private suspend fun disconnect() {
        if (state.value is Disconnected) return

        withContext(NonCancellable) {
            try {
                withTimeout(disconnectTimeout) {
                    logger.verbose { message = "Waiting for connection tasks to complete" }
                    taskScope.coroutineContext.job.join()

                    logger.debug { message = "Disconnecting" }
                    disconnectGatt()

                    state.filterIsInstance<Disconnected>().first()
                }
                logger.info { message = "Disconnected" }
            } catch (e: TimeoutCancellationException) {
                logger.warn { message = "Timed out after $disconnectTimeout waiting for disconnect" }
            } finally {
                disconnectGatt()
                // Force the state as there are cases where the disconnected callback is not invoked
                state.value = Disconnected()
            }
        }
    }

    private var didDisconnectGatt = false
    private fun disconnectGatt() {
        if (didDisconnectGatt) return
        logger.verbose { message = "gatt.disconnect" }
        gatt.disconnect()
        didDisconnectGatt = true
    }

    private fun close(cause: Throwable?) {
        logger.debug(cause) { message = "Closing" }
        unregisterDisconnectedListener()
        clearObservationListeners()
        logger.info { message = "Closed" }
    }

    private fun registerDisconnectedListener() {
        bluetoothDevice.addEventListener(GATT_SERVER_DISCONNECTED, disconnectedListener)
    }

    private fun unregisterDisconnectedListener() {
        bluetoothDevice.removeEventListener(GATT_SERVER_DISCONNECTED, disconnectedListener)
    }

    private fun PlatformCharacteristic.createObservationListener(): ObservationListener = { event ->
        val target = event.target?.unsafeCast<BluetoothRemoteGATTCharacteristic>()
        val data = target?.value!!
        logger.debug {
            message = CHARACTERISTIC_VALUE_CHANGED
            detail(this@createObservationListener)
            detail(data, Operation.Change)
        }

        // We're abusing `PlatformDiscoveredCharacteristic` by passing an `emptyList()` for
        // descriptors but this allows us to keep the `interface` hierarchy simpler. We can get away
        // with this because propagating observation events don't use the descriptors (we needed
        // `PlatformDiscoveredCharacteristic` for the equality functions used by `isAssociatedWith`).
        val discoveredCharacteristic = PlatformDiscoveredCharacteristic(this, emptyList())

        val characteristicChange = ObservationEvent.CharacteristicChange(discoveredCharacteristic, data)

        if (!characteristicChanges.tryEmit(characteristicChange)) {
            logger.error {
                message = "Failed to emit characteristic change"
                detail("change", characteristicChange.toString())
            }
        }
    }

    private fun clearObservationListeners() {
        observationListeners.forEach { (characteristic, listener) ->
            characteristic.removeCharacteristicValueChangedListener(listener)
        }
        observationListeners.clear()
    }

    private fun PlatformCharacteristic.removeCharacteristicValueChangedListener(
        listener: ObservationListener,
    ) {
        logger.verbose {
            message = "removeEventListener"
            detail(this@removeCharacteristicValueChangedListener)
            detail("event", CHARACTERISTIC_VALUE_CHANGED)
        }
        removeEventListener(CHARACTERISTIC_VALUE_CHANGED, listener)
    }

    private inline fun <reified T : State> on(crossinline action: suspend (T) -> Unit) {
        taskScope.launch {
            action(state.filterIsInstance<T>().first())
        }
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
