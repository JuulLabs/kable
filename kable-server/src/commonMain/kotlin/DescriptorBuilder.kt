package com.juul.kable.server

import com.juul.kable.ExperimentalKableApi
import kotlin.uuid.Uuid

@ExperimentalKableApi
public class DescriptorBuilder internal constructor(
    private val serviceUuid: Uuid,
    private val characteristicUuid: Uuid,
    private val uuid: Uuid,
) {

    /**
     * Static value of this descriptor, making the descriptor read-only: reads are served (from this
     * value) without invoking any handler, and the value never changes.
     *
     * Mutually exclusive with [onRead] and [onWrite].
     */
    public var value: ByteArray? = null

    private var read: ReadHandler? = null
    private var write: WriteHandler? = null

    /**
     * Registers a [ReadAction] to be invoked when a remote [Central] reads this descriptor.
     *
     * Not supported on Apple (Core Bluetooth does not deliver descriptor requests to the app);
     * descriptors with an [onRead] handler are omitted (with a logged warning) on Apple.
     *
     * @param security required for a remote [Central] to read this descriptor.
     */
    public fun onRead(
        security: Security = Security.None,
        action: ReadAction,
    ) {
        require(read == null) { "Descriptor $uuid onRead already declared" }
        read = ReadHandler(security, action)
    }

    /**
     * Registers a [WriteAction] to be invoked when a remote [Central] writes this descriptor.
     *
     * Not supported on Apple (Core Bluetooth does not deliver descriptor requests to the app);
     * descriptors with an [onWrite] handler are omitted (with a logged warning) on Apple.
     *
     * @param security required for a remote [Central] to write this descriptor.
     */
    public fun onWrite(
        security: Security = Security.None,
        action: WriteAction,
    ) {
        require(write == null) { "Descriptor $uuid onWrite already declared" }
        write = WriteHandler(setOf(com.juul.kable.WriteType.WithResponse), security, action)
    }

    internal fun build(): ServerDescriptor {
        val value = value
        if (value != null) {
            require(read == null && write == null) {
                "Descriptor $uuid static `value` is mutually exclusive with `onRead` and `onWrite`"
            }
        } else {
            require(read != null || write != null) {
                "Descriptor $uuid must declare a static `value`, or at least one of: `onRead`, `onWrite`"
            }
        }
        return ServerDescriptor(
            serviceUuid = serviceUuid,
            characteristicUuid = characteristicUuid,
            descriptorUuid = uuid,
            staticValue = value?.copyOf(),
            read = read,
            write = write,
        )
    }
}
