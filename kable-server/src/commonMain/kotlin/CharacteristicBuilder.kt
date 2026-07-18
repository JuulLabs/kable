package com.juul.kable.server

import com.juul.kable.ExperimentalKableApi
import com.juul.kable.WriteType
import com.juul.kable.WriteType.WithResponse
import com.juul.kable.WriteType.WithoutResponse
import kotlin.uuid.Uuid

@ExperimentalKableApi
public class CharacteristicBuilder internal constructor(
    private val serviceUuid: Uuid,
    private val uuid: Uuid,
) {

    /**
     * Static value of this characteristic, making the characteristic read-only: reads are served
     * (from this value) without invoking any handler, and the value never changes.
     *
     * Mutually exclusive with [onRead], [onWrite] and [onSubscription] (for a characteristic with a
     * changing — or lazily produced — value, use [onRead] instead).
     */
    public var value: ByteArray? = null

    private var read: ReadHandler? = null
    private var write: WriteHandler? = null
    private var subscription: SubscriptionHandler? = null
    private val descriptors = mutableMapOf<Uuid, DescriptorBuilder>()

    /**
     * Registers a [ReadAction] to be invoked when a remote [Central] reads this characteristic.
     * Adds the `read` property to this characteristic.
     *
     * [action] returns the full value of the characteristic; when a value is larger than can be
     * transmitted in a single response, "long read" (read blob) offset handling is performed by
     * Kable. A read may be rejected by throwing [GattErrorException] from [action].
     *
     * @param security required for a remote [Central] to read this characteristic.
     */
    public fun onRead(
        security: Security = Security.None,
        action: ReadAction,
    ) {
        requireNotDeclared("onRead", read)
        read = ReadHandler(security, action)
    }

    /**
     * Registers a [WriteAction] to be invoked when a remote [Central] writes this characteristic.
     * Adds the `write` and/or `writeWithoutResponse` properties (per [writeTypes]) to this
     * characteristic.
     *
     * Long ("prepared") writes are assembled by Kable: [action] is always invoked with the complete
     * written value. A write may be rejected by throwing [GattErrorException] from [action] (for
     * [WithoutResponse] writes, the rejection is not communicated to the remote [Central], per
     * Bluetooth specification).
     *
     * @param writeTypes [WriteType]s supported for this characteristic, must not be empty.
     * @param security required for a remote [Central] to write this characteristic.
     */
    public fun onWrite(
        writeTypes: Set<WriteType> = setOf(WithResponse, WithoutResponse),
        security: Security = Security.None,
        action: WriteAction,
    ) {
        requireNotDeclared("onWrite", write)
        require(writeTypes.isNotEmpty()) { "onWrite writeTypes must not be empty" }
        write = WriteHandler(writeTypes, security, action)
    }

    /**
     * Registers a [SubscriptionAction] to be invoked when a remote [Central] subscribes to this
     * characteristic. Adds the `notify` (or `indicate`, when [indication] is `true`) property to
     * this characteristic.
     *
     * [action] is launched (in a dedicated coroutine, per subscribed [Central]) when a [Central]
     * subscribes, and is cancelled when the [Central] unsubscribes (or disconnects), or when the
     * server is [stopped][GattServer.stop]. Values are sent to the subscribed [Central] via
     * [send][SubscriptionScope.send]:
     *
     * ```
     * onSubscription {
     *     while (true) {
     *         send(measure())
     *         delay(1.seconds)
     *     }
     * }
     * ```
     *
     * Notifications may also be pushed (to all subscribed centrals) via [GattServer.notify].
     *
     * The Client Characteristic Configuration descriptor (CCCD) is automatically added to this
     * characteristic (on platforms where it is not managed by the system) and must not be declared
     * via [descriptor].
     *
     * @param indication `true` to send indications (acknowledged), `false` (default) to send notifications (unacknowledged).
     * @param security required for a remote [Central] to subscribe to this characteristic.
     */
    public fun onSubscription(
        indication: Boolean = false,
        security: Security = Security.None,
        action: SubscriptionAction,
    ) {
        requireNotDeclared("onSubscription", subscription)
        subscription = SubscriptionHandler(indication, security, action)
    }

    /**
     * Declares a descriptor identified by [uuid].
     *
     * The Client Characteristic Configuration descriptor (CCCD) is managed by Kable (see
     * [onSubscription]) and must not be declared.
     *
     * On Apple, Core Bluetooth only supports descriptors with [static values][DescriptorBuilder.value]
     * (of specific types, e.g. Characteristic User Description); unsupported descriptors are
     * omitted (with a logged warning) on Apple.
     *
     * @param uuid of the descriptor, must be unique within this characteristic.
     */
    public fun descriptor(
        uuid: Uuid,
        builderAction: DescriptorBuilder.() -> Unit,
    ) {
        require(uuid != clientCharacteristicConfigUuid) {
            "Client Characteristic Configuration descriptor is managed by Kable (via `onSubscription`) and must not be declared"
        }
        require(uuid !in descriptors) { "Descriptor $uuid already declared in characteristic ${this.uuid}" }
        descriptors[uuid] = DescriptorBuilder(serviceUuid, this.uuid, uuid).apply(builderAction)
    }

    internal fun build(): ServerCharacteristic {
        val value = value
        if (value != null) {
            require(read == null && write == null && subscription == null) {
                "Characteristic $uuid static `value` is mutually exclusive with `onRead`, `onWrite` and `onSubscription`"
            }
        } else {
            require(read != null || write != null || subscription != null) {
                "Characteristic $uuid must declare a static `value`, or at least one of: `onRead`, `onWrite`, `onSubscription`"
            }
        }
        return ServerCharacteristic(
            serviceUuid = serviceUuid,
            characteristicUuid = uuid,
            staticValue = value?.copyOf(),
            read = read,
            write = write,
            subscription = subscription,
            descriptors = descriptors.values.map(DescriptorBuilder::build),
        )
    }

    private fun requireNotDeclared(name: String, handler: Any?) {
        require(handler == null) { "Characteristic $uuid $name already declared" }
    }
}
