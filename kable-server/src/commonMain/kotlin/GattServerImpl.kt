package com.juul.kable.server

import com.juul.kable.Characteristic
import com.juul.kable.logs.Logging
import com.juul.kable.server.GattServer.State.Started
import com.juul.kable.server.GattServer.State.Starting
import com.juul.kable.server.GattServer.State.Stopped
import com.juul.kable.server.GattServer.State.Stopping
import com.juul.kable.server.logs.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.concurrent.atomics.AtomicBoolean

internal class GattServerImpl(
    private val profile: ServerProfile,
    logging: Logging,
    private val engine: ServerEngine,
) : GattServer {

    private val logger = Logger(logging)

    private val rootJob = SupervisorJob()
    private val rootScope = CoroutineScope(
        rootJob +
            CoroutineExceptionHandler { _, cause -> logger.warn(cause) { "Uncaught failure in GattServer coroutine" } } +
            CoroutineName("Kable/GattServer"),
    )

    private val guard = Mutex()
    private var session: CoroutineScope? = null // Guarded by `guard`.
    private val closed = AtomicBoolean(false)

    private val _state = MutableStateFlow<GattServer.State>(Stopped())
    override val state: StateFlow<GattServer.State> = _state.asStateFlow()

    private val _centrals = MutableStateFlow<Set<Central>>(emptySet())
    override val centrals: StateFlow<Set<Central>> = _centrals.asStateFlow()

    private val subscribers: Map<AttributeKey.Characteristic, MutableStateFlow<Set<Central>>> =
        profile.services
            .flatMap(ServerService::characteristics)
            .filter { it.subscription != null }
            .associate {
                AttributeKey.Characteristic(it.serviceUuid, it.characteristicUuid) to
                    MutableStateFlow<Set<Central>>(emptySet())
            }

    override suspend fun start(): CoroutineScope = guard.withLock {
        check(!closed.load()) { "GattServer is closed" }
        session?.let { return@withLock it }

        _state.value = Starting
        val scope = CoroutineScope(rootScope.coroutineContext + SupervisorJob(rootJob))
        try {
            val requests = engine.open(profile)
            RequestDispatcher(scope, profile, engine, logger, subscribers, _centrals).launchIn(requests)
            session = scope
            _state.value = Started
            logger.debug { "GattServer started" }
            scope
        } catch (e: CancellationException) {
            cleanupFailedStart(scope)
            _state.value = Stopped()
            throw e
        } catch (e: Exception) {
            cleanupFailedStart(scope)
            _state.value = Stopped(e)
            throw e
        }
    }

    private suspend fun cleanupFailedStart(scope: CoroutineScope) {
        scope.cancel()
        try {
            engine.close()
        } catch (e: Exception) {
            logger.warn(e) { "Failure while closing engine after failed start" }
        }
    }

    override suspend fun stop() {
        guard.withLock { stopLocked() }
    }

    private suspend fun stopLocked() {
        val scope = session ?: return
        _state.value = Stopping

        // Cancels the request dispatcher, subscriptions, advertising, and any consumer coroutines
        // launched from the session scope (returned by `start`).
        val job = scope.coroutineContext.job
        job.cancel()
        job.join()

        try {
            engine.close()
        } catch (e: Exception) {
            logger.warn(e) { "Failure while closing engine" }
        }

        subscribers.values.forEach { it.value = emptySet() }
        _centrals.value = emptySet()
        session = null
        _state.value = Stopped()
        logger.debug { "GattServer stopped" }
    }

    override suspend fun advertise(builderAction: AdvertisementParametersBuilder.() -> Unit) {
        val parameters = AdvertisementParametersBuilder().apply(builderAction).build()
        val scope = guard.withLock {
            checkNotNull(session) { "GattServer is not started" }
        }

        // Advertising is started as a child of the session scope (so that it spins down when the
        // server is stopped), while cancellation of the caller stops advertising.
        val advertising = scope.async { engine.advertise(parameters) }
        try {
            advertising.await()
        } catch (e: CancellationException) {
            // Rethrows if the caller was cancelled (cancelling `advertising` via `finally`),
            // otherwise the server was stopped while advertising: return normally.
            currentCoroutineContext().ensureActive()
        } finally {
            advertising.cancel()
        }
    }

    override suspend fun notify(
        characteristic: Characteristic,
        value: ByteArray,
        centrals: Collection<Central>?,
    ) {
        val serverCharacteristic = resolve(characteristic)
        guard.withLock {
            checkNotNull(session) { "GattServer is not started" }
        }
        val key = AttributeKey.Characteristic(serverCharacteristic.serviceUuid, serverCharacteristic.characteristicUuid)
        val subscribed = subscribers.getValue(key).value
        val targets = if (centrals == null) {
            subscribed
        } else {
            subscribed.filter { subscriber -> centrals.any { it.identifier == subscriber.identifier } }
        }
        targets.forEach { central ->
            engine.notify(central, serverCharacteristic, value)
        }
    }

    override fun subscribers(characteristic: Characteristic): StateFlow<Set<Central>> {
        val serverCharacteristic = resolve(characteristic)
        val key = AttributeKey.Characteristic(serverCharacteristic.serviceUuid, serverCharacteristic.characteristicUuid)
        return subscribers.getValue(key).asStateFlow()
    }

    /**
     * @throws NoSuchElementException if [characteristic] is not in the [profile].
     * @throws IllegalArgumentException if [characteristic] was not configured with `onSubscription`.
     */
    private fun resolve(characteristic: Characteristic): ServerCharacteristic {
        val serverCharacteristic = profile.characteristicOrNull(characteristic)
            ?: throw NoSuchElementException(
                "Characteristic ${characteristic.characteristicUuid} of service ${characteristic.serviceUuid} not found",
            )
        require(serverCharacteristic.subscription != null) {
            "Characteristic ${characteristic.characteristicUuid} was not configured with `onSubscription`"
        }
        return serverCharacteristic
    }

    override fun close() {
        if (!closed.compareAndSet(expectedValue = false, newValue = true)) return
        rootScope.launch(NonCancellable) {
            guard.withLock { stopLocked() }
            rootJob.cancel()
        }
    }
}
