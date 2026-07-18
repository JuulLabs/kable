package com.juul.kable.server

import com.juul.kable.Identifier
import com.juul.kable.server.logs.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Session-scoped engine-agnostic request processor: consumes [InboundRequest]s (produced by a
 * [ServerEngine]) and dispatches them to the handlers declared (via the [GattServer] builder DSL)
 * in the [profile].
 *
 * Responsibilities (beyond routing):
 *
 * - Read offset ("long read") handling: read handlers provide the full attribute value; slicing (at
 *   the requested offset) is performed here.
 * - Prepared ("long"/queued) write assembly: write fragments are queued (per central) and assembled
 *   into complete values upon [InboundRequest.ExecuteWrite], so write handlers are always invoked
 *   with complete values.
 * - Client Characteristic Configuration descriptor (CCCD) handling (for engines that don't manage
 *   the CCCD at the platform level): CCCD reads/writes are answered here (never routed to
 *   handlers), with writes translated into subscribe/unsubscribe actions.
 * - Subscription lifecycle: a [SubscriptionAction] coroutine is launched per subscribed central,
 *   and cancelled upon unsubscribe (or disconnect).
 *
 * All state is confined to the coroutine that [processes][launchIn] the request channel (user
 * handlers are launched onto [scope] and never touch dispatcher state).
 */
internal class RequestDispatcher(
    private val scope: CoroutineScope,
    private val profile: ServerProfile,
    private val engine: ServerEngine,
    private val logger: Logger,
    private val subscribers: Map<AttributeKey.Characteristic, MutableStateFlow<Set<Central>>>,
    private val centrals: MutableStateFlow<Set<Central>>,
) {

    private data class SubscriptionKey(
        val identifier: Identifier,
        val characteristic: AttributeKey.Characteristic,
    )

    private class Subscription(
        val central: Central,
        val job: Job,
    )

    private class PreparedWrite(
        val attribute: AttributeKey,
        val offset: Int,
        val value: ByteArray,
    )

    private val subscriptions = mutableMapOf<SubscriptionKey, Subscription>()
    private val connected = mutableMapOf<Identifier, Central>()
    private val preparedWrites = mutableMapOf<Identifier, MutableList<PreparedWrite>>()

    fun launchIn(channel: ReceiveChannel<InboundRequest>): Job = scope.launch {
        for (request in channel) process(request)
    }

    private fun process(request: InboundRequest) {
        when (request) {
            is InboundRequest.Read -> onRead(request)
            is InboundRequest.Write -> onWrite(request)
            is InboundRequest.ExecuteWrite -> onExecuteWrite(request)
            is InboundRequest.Subscribe -> onSubscribe(request)
            is InboundRequest.Unsubscribe -> onUnsubscribe(request)
            is InboundRequest.CentralConnected -> onCentralConnected(request)
            is InboundRequest.CentralDisconnected -> onCentralDisconnected(request)
        }
    }

    /*
     * Reads
     */

    private fun onRead(request: InboundRequest.Read) {
        val staticValue: ByteArray?
        val handler: ReadHandler?
        when (val attribute = request.attribute) {
            is AttributeKey.Characteristic -> {
                val characteristic = profile.characteristicOrNull(attribute)
                    ?: return request.fail(AttError.InvalidHandle)
                staticValue = characteristic.staticValue
                handler = characteristic.read
            }
            is AttributeKey.Descriptor -> {
                if (attribute.descriptorUuid == clientCharacteristicConfigUuid) return onCccdRead(request, attribute)
                val descriptor = profile.descriptorOrNull(attribute)
                    ?: return request.fail(AttError.InvalidHandle)
                staticValue = descriptor.staticValue
                handler = descriptor.read
            }
        }
        when {
            staticValue != null -> respond(request, staticValue)
            handler != null -> scope.launch { serveRead(request, handler) }
            else -> request.fail(AttError.ReadNotPermitted)
        }
    }

    private suspend fun serveRead(request: InboundRequest.Read, handler: ReadHandler) {
        val value = try {
            handler.action(ReadRequest(request.central))
        } catch (e: GattErrorException) {
            request.fail(e.error)
            return
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.warn(e) { "onRead action failed for ${request.attribute}" }
            request.fail(AttError.UnlikelyError)
            return
        }
        respond(request, value)
    }

    /** Responds with [value], sliced at the requested offset (services "long reads"). */
    private fun respond(request: InboundRequest.Read, value: ByteArray) {
        if (request.offset > value.size) {
            request.fail(AttError.InvalidOffset)
        } else {
            request.respond(if (request.offset == 0) value else value.copyOfRange(request.offset, value.size))
        }
    }

    /*
     * Writes
     */

    private fun onWrite(request: InboundRequest.Write) {
        val attribute = request.attribute
        if (attribute is AttributeKey.Descriptor && attribute.descriptorUuid == clientCharacteristicConfigUuid) {
            return onCccdWrite(request, attribute)
        }
        val handler = writeHandlerOrNull(attribute)
        if (handler == null) {
            request.fail?.invoke(AttError.WriteNotPermitted)
            return
        }
        if (request.prepared) {
            preparedWrites
                .getOrPut(request.central.identifier, ::mutableListOf)
                .add(PreparedWrite(attribute, request.offset, request.value))
            request.respond?.invoke()
        } else {
            scope.launch {
                serveWrite(request.central, attribute, handler, request.value, request.respond, request.fail)
            }
        }
    }

    private fun onExecuteWrite(request: InboundRequest.ExecuteWrite) {
        val queue = preparedWrites.remove(request.central.identifier).orEmpty()
        if (!request.commit || queue.isEmpty()) {
            request.respond()
            return
        }
        scope.launch {
            // Preserves the order in which attributes were first written to.
            for ((attribute, fragments) in queue.groupBy(PreparedWrite::attribute)) {
                val value = assemble(fragments)
                if (value == null) {
                    request.fail(AttError.InvalidOffset)
                    return@launch
                }
                val handler = writeHandlerOrNull(attribute)
                if (handler == null) {
                    request.fail(AttError.WriteNotPermitted)
                    return@launch
                }
                val delivered = serveWrite(request.central, attribute, handler, value, respond = null, fail = request.fail)
                if (!delivered) return@launch
            }
            request.respond()
        }
    }

    /** @return assembled fragments, or `null` if fragments are not contiguous. */
    private fun assemble(fragments: List<PreparedWrite>): ByteArray? {
        var value = ByteArray(0)
        for (fragment in fragments.sortedBy(PreparedWrite::offset)) {
            if (fragment.offset != value.size) return null
            value += fragment.value
        }
        return value
    }

    private fun writeHandlerOrNull(attribute: AttributeKey): WriteHandler? = when (attribute) {
        is AttributeKey.Characteristic -> profile.characteristicOrNull(attribute)?.write
        is AttributeKey.Descriptor -> profile.descriptorOrNull(attribute)?.write
    }

    /** @return `true` if [handler] accepted the write (and [respond] — when non-`null` — was invoked). */
    private suspend fun serveWrite(
        central: Central,
        attribute: AttributeKey,
        handler: WriteHandler,
        value: ByteArray,
        respond: (() -> Unit)?,
        fail: ((AttError) -> Unit)?,
    ): Boolean {
        try {
            handler.action(WriteRequest(central), value)
        } catch (e: GattErrorException) {
            fail?.invoke(e.error)
            return false
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.warn(e) { "onWrite action failed for $attribute" }
            fail?.invoke(AttError.UnlikelyError)
            return false
        }
        respond?.invoke()
        return true
    }

    /*
     * Client Characteristic Configuration descriptor (CCCD)
     */

    private fun onCccdRead(request: InboundRequest.Read, attribute: AttributeKey.Descriptor) {
        val key = AttributeKey.Characteristic(attribute.serviceUuid, attribute.characteristicUuid)
        val subscription = profile.characteristicOrNull(key)?.subscription
            ?: return request.fail(AttError.InvalidHandle)
        val subscribed = SubscriptionKey(request.central.identifier, key) in subscriptions
        val value = when {
            !subscribed -> byteArrayOf(0x00, 0x00)
            subscription.indication -> byteArrayOf(0x02, 0x00)
            else -> byteArrayOf(0x01, 0x00)
        }
        respond(request, value)
    }

    private fun onCccdWrite(request: InboundRequest.Write, attribute: AttributeKey.Descriptor) {
        val key = AttributeKey.Characteristic(attribute.serviceUuid, attribute.characteristicUuid)
        val characteristic = profile.characteristicOrNull(key)
        val subscription = characteristic?.subscription
        if (subscription == null) {
            request.fail?.invoke(AttError.InvalidHandle)
            return
        }
        if (request.value.size < 2) {
            request.fail?.invoke(AttError.InvalidAttributeValueLength)
            return
        }
        val bits = request.value[0].toInt()
        val notifications = bits and 0b01 != 0
        val indications = bits and 0b10 != 0
        when {
            !notifications && !indications -> {
                request.respond?.invoke()
                unsubscribe(request.central, key)
            }
            (notifications && !subscription.indication) || (indications && subscription.indication) -> {
                request.respond?.invoke()
                subscribe(request.central, key, characteristic)
            }
            else -> request.fail?.invoke(AttError.RequestNotSupported)
        }
    }

    /*
     * Subscriptions
     */

    private fun onSubscribe(request: InboundRequest.Subscribe) {
        val characteristic = profile.characteristicOrNull(request.attribute) ?: return
        subscribe(request.central, request.attribute, characteristic)
    }

    private fun onUnsubscribe(request: InboundRequest.Unsubscribe) {
        unsubscribe(request.central, request.attribute)
    }

    private fun subscribe(
        central: Central,
        key: AttributeKey.Characteristic,
        characteristic: ServerCharacteristic,
    ) {
        val handler = characteristic.subscription ?: return
        val subscriptionKey = SubscriptionKey(central.identifier, key)
        if (subscriptionKey in subscriptions) return
        logger.debug { "Central ${central.identifier} subscribed to ${characteristic.characteristicUuid}" }
        val subscribedCentral = central
        val job = scope.launch {
            try {
                coroutineScope {
                    val delegate = this
                    val subscriptionScope = object : SubscriptionScope, CoroutineScope by delegate {
                        override val central: Central get() = subscribedCentral
                        override suspend fun send(value: ByteArray) {
                            engine.notify(subscribedCentral, characteristic, value)
                        }
                    }
                    handler.action(subscriptionScope)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.warn(e) { "onSubscription action failed for ${characteristic.characteristicUuid}" }
            }
        }
        subscriptions[subscriptionKey] = Subscription(central, job)
        subscribers[key]?.update { it + central }
        updateCentrals()
    }

    private fun unsubscribe(central: Central, key: AttributeKey.Characteristic) {
        val subscription = subscriptions.remove(SubscriptionKey(central.identifier, key)) ?: return
        logger.debug { "Central ${central.identifier} unsubscribed from ${key.characteristicUuid}" }
        subscription.job.cancel()
        subscribers[key]?.update { set -> set.filterNot { it.identifier == central.identifier }.toSet() }
        updateCentrals()
    }

    /*
     * Connections
     */

    private fun onCentralConnected(request: InboundRequest.CentralConnected) {
        logger.debug { "Central ${request.central.identifier} connected" }
        connected[request.central.identifier] = request.central
        updateCentrals()
    }

    private fun onCentralDisconnected(request: InboundRequest.CentralDisconnected) {
        logger.debug { "Central ${request.central.identifier} disconnected" }
        val identifier = request.central.identifier
        connected.remove(identifier)
        preparedWrites.remove(identifier)
        subscriptions.keys
            .filter { it.identifier == identifier }
            .forEach { unsubscribe(request.central, it.characteristic) }
        updateCentrals()
    }

    private fun updateCentrals() {
        centrals.value = (connected.values + subscriptions.values.map(Subscription::central))
            .associateBy(Central::identifier)
            .values
            .toSet()
    }
}
