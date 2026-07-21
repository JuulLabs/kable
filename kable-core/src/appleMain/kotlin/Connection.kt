package com.juul.kable

import com.juul.kable.PeripheralDelegate.Response
import com.juul.kable.PeripheralDelegate.Response.DidDiscoverCharacteristicsForService
import com.juul.kable.PeripheralDelegate.Response.DidDiscoverDescriptorsForCharacteristic
import com.juul.kable.PeripheralDelegate.Response.DidDiscoverServices
import com.juul.kable.State.Disconnected
import com.juul.kable.coroutines.childSupervisor
import com.juul.kable.logs.Logger
import com.juul.kable.logs.Logging
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CompletableJob
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
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.io.IOException
import platform.CoreBluetooth.CBCharacteristic
import platform.CoreBluetooth.CBPeripheral
import platform.CoreBluetooth.CBService
import platform.CoreBluetooth.CBUUID
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.coroutineContext
import kotlin.reflect.KClass
import kotlin.time.Duration

internal class Connection(
    parentContext: CoroutineContext,
    private val central: CentralManager,
    private val peripheral: CBPeripheral,
    private val delegate: PeripheralDelegate,
    private val state: MutableStateFlow<State>,
    private val services: MutableStateFlow<List<PlatformDiscoveredService>?>,
    private val disconnectTimeout: Duration,
    identifier: String,
    logging: Logging,
) {

    private val name = "Kable/Connection/$identifier"

    private val connectionJob = (parentContext.job as CompletableJob).apply {
        invokeOnCompletion(::close)
    }
    private val connectionScope = CoroutineScope(
        parentContext + connectionJob + CoroutineName(name),
    )

    val taskScope = connectionScope.childSupervisor("$name/Tasks")

    private val logger = Logger(logging, tag = "Kable/Connection", identifier = identifier)

    init {
        onDispose(::disconnect)
        onServiceChanged { invalidatedServices ->
            discoverServices(invalidatedServices.map { cbService -> cbService.UUID })
        }

        on<Disconnected> {
            val state = it.toString()
            logger.debug {
                message = "Disconnect detected"
                detail("state", state)
            }
            dispose(NotConnectedException("Disconnect detected"))
        }
    }

    private val dispatcher = connectionScope.coroutineContext + central.dispatcher
    internal val guard = Mutex()

    /** @param serviceUuids to discover (list of service UUIDs), or `null` for all. */
    suspend fun discoverServices(serviceUuids: List<CBUUID>? = null) {
        logger.verbose { message = "discoverServices" }
        execute<DidDiscoverServices> {
            peripheral.discoverServices(serviceUuids)
        }

        // Cast should be safe since `CBPeripheral.services` type is `[CBService]?`, according to:
        // https://developer.apple.com/documentation/corebluetooth/cbperipheral/services
        @Suppress("UNCHECKED_CAST")
        val discoveredServices = peripheral.services as List<CBService>?

        discoveredServices?.forEach { service ->
            execute<DidDiscoverCharacteristicsForService> {
                peripheral.discoverCharacteristics(null, service)
            }

            // Cast should be safe since `CBService.characteristics` type is `[CBCharacteristic]?`,
            // according to: https://developer.apple.com/documentation/corebluetooth/cbservice/characteristics
            @Suppress("UNCHECKED_CAST")
            val discoveredCharacteristics = service.characteristics as List<CBCharacteristic>?

            discoveredCharacteristics?.forEach { characteristic ->
                execute<DidDiscoverDescriptorsForCharacteristic> {
                    peripheral.discoverDescriptorsForCharacteristic(characteristic)
                }
            }
        }

        services.value = peripheral.services
            .orEmpty()
            .map { it as PlatformService }
            .map(::PlatformDiscoveredService)
    }

    suspend inline fun <reified T : Response> execute(
        noinline action: CBPeripheral.() -> Unit,
    ): T = execute(T::class, action)

    suspend fun <T : Response> execute(
        type: KClass<T>,
        action: CBPeripheral.() -> Unit,
    ): T {
        val response = guard.withLock {
            var executed = false
            try {
                withContext(dispatcher) {
                    peripheral.action()
                    executed = true
                }
            } catch (e: CancellationException) {
                if (executed) {
                    // Ensure response buffer is received even when calling context is cancelled.
                    // UNDISPATCHED to ensure we're within the `lock` for the `receive`.
                    connectionScope.launch(start = UNDISPATCHED) {
                        val response = delegate.response.receive()
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
                    delegate.response.receive()
                }.await()
            } catch (e: CancellationException) {
                coroutineContext.ensureActive()
                throw e.unwrapCancellationException()
            }
        }.also(::checkResponse)

        // `guard` should always enforce a 1:1 matching of request-to-response, but if an Android
        // `BluetoothGattCallback` method is called out-of-order then we'll cast to the wrong type.
        return response as? T
            ?: throw InternalError(
                "Expected response type ${type.simpleName} but received ${response::class.simpleName}",
            )
    }

    // Not routed through `execute`: CoreBluetooth's `didOpenL2CAPChannel` carries no PSM, so it cannot be
    // correlated to a request — the only safe model is one open at a time. `guard` provides that (shared
    // with every other operation), and unlike `execute` this holds the guard until the callback is
    // consumed even when the caller is cancelled, so a late channel is always torn down here rather than
    // leaking or being misdelivered to the next open.
    internal suspend fun openL2CapChannel(psm: UShort): L2CapSocket = guard.withLock {
        val pending = delegate.beginL2CapOpen()
        var executed = false
        try {
            withContext(dispatcher) {
                peripheral.openL2CAPChannel(psm)
                executed = true
            }
        } catch (e: CancellationException) {
            if (executed) {
                abandonL2CapChannelWhenDelivered(pending)
            } else {
                delegate.cancelL2CapOpen(pending, e.unwrapCancellationException())
            }
            throw e.unwrapCancellationException()
        } catch (e: Throwable) {
            delegate.cancelL2CapOpen(pending, e)
            throw e
        }

        val result = try {
            withContext(NonCancellable) { pending.await() }
        } catch (e: Throwable) {
            coroutineContext.ensureActive()
            throw e
        }

        // Cancelled while awaiting the (uncancellable) callback: the channel arrived but has no owner.
        // abandon(), not close(): close()'s deterministic collection is futile here, because this very
        // frame still pins the channel (via `pending`/`result`) until it dies — the channel can only be
        // collected after this function has thrown, which abandon()'s teardown-then-GC.schedule() allows.
        if (!coroutineContext.isActive) {
            result.channel?.let { AppleL2CapSocket(it).abandon() }
            coroutineContext.ensureActive()
        }

        buildL2CapSocket(result)
    }

    private suspend fun abandonL2CapChannelWhenDelivered(
        pending: CompletableDeferred<PeripheralDelegate.L2CapOpenResult>,
    ) {
        // abandon(), not close(): the caller's frame keeps `pending` (and through it the channel)
        // reachable until the open throws, so a collection forced here cannot release the channel —
        // abandon() tears down and GC.schedule()s, letting the release happen once the frame is gone.
        val result = withContext(NonCancellable) { runCatching { pending.await() }.getOrNull() }
        result?.channel?.let { AppleL2CapSocket(it).abandon() }
    }

    private fun buildL2CapSocket(result: PeripheralDelegate.L2CapOpenResult): L2CapSocket {
        result.error?.let { error ->
            // A failure should carry no channel, but abandon one if CoreBluetooth ever delivers both.
            result.channel?.let { AppleL2CapSocket(it).abandon() }
            // NSError.code is a 32-bit NSInteger on watchosArm64, so widen explicitly.
            throw L2CapException(error.description, code = error.code.toLong())
        }
        val channel = result.channel
            ?: throw L2CapException("couldn't open L2CAP channel", code = 0)
        return AppleL2CapSocket(channel)
    }

    private suspend fun disconnect() {
        if (state.value is Disconnected) {
            logger.debug { message = "Skipping disconnect sequence" }
            return
        }

        withContext(NonCancellable) {
            try {
                withTimeout(disconnectTimeout) {
                    logger.verbose { message = "Waiting for connection tasks to complete" }
                    taskScope.coroutineContext.job.join()

                    logger.debug { message = "Disconnecting" }
                    cancelPeripheralConnection()

                    state.filterIsInstance<Disconnected>().first()
                }
                logger.info { message = "Disconnected" }
            } catch (_: TimeoutCancellationException) {
                logger.warn { message = "Timed out after $disconnectTimeout waiting for disconnect" }
            } finally {
                cancelPeripheralConnection()
            }
        }
    }

    private var didCancelPeripheralConnection = false
    private suspend fun cancelPeripheralConnection() {
        if (didCancelPeripheralConnection) return
        logger.verbose { message = "cancelPeripheralConnection" }
        central.cancelPeripheralConnection(peripheral)
        didCancelPeripheralConnection = true
    }

    private fun close(cause: Throwable?) {
        logger.debug(cause) { message = "Closing" }
        delegate.close(cause)
        logger.info { message = "Closed" }
    }

    private inline fun <reified T : State> on(crossinline action: suspend (T) -> Unit) {
        taskScope.launch {
            action(state.filterIsInstance<T>().first())
        }
    }

    private fun onServiceChanged(action: suspend (List<CBService>) -> Unit) {
        delegate.onServiceChanged
            .receiveAsFlow()
            .onEach { (invalidatedServices) -> action(invalidatedServices) }
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

private fun checkResponse(response: Response) {
    val error = response.error
    if (error != null) throw IOException(error.description, cause = null)
}
