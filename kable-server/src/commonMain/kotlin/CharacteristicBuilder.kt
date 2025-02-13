package com.juul.kable.server

import com.juul.kable.descriptor
import kotlin.experimental.and
import kotlin.uuid.Uuid
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private val CCCD = Uuid.descriptor("gatt.client_characteristic_configuration")

public class SubscriptionAction {
    public suspend fun send(value: ByteArray) {
        TODO()
    }
}

public class CharacteristicBuilder(
    service: ServiceBuilder,
    private val uuid: Uuid,
) {

    internal sealed class Property {
        data object Notification : Property()
        data object Indication : Property()
        data class Read(val encrypted: Boolean, val manInTheMiddleProtection: Boolean) : Property()
        data class Write(val encrypted: Boolean, val manInTheMiddleProtection: Boolean) : Property()
    }

    internal val scope = service.scope.createChildScope()
    private var properties = mutableSetOf<Property>()
    private var onRead: (suspend ReadAction.() -> Unit)? = null
    private var onWrite: (suspend (value: ByteArray) -> Unit)? = null
    private var descriptors = mutableMapOf<Uuid, DescriptorBuilder>()

    public fun onSubscription(
        notification: Boolean = true,
        indication: Boolean = true,
        action: suspend SubscriptionAction.() -> Unit,
    ) {
        require(CCCD !in descriptors) { "Descriptor $CCCD (CCCD) already configured" }
        if (notification) properties += Property.Notification
        if (indication) properties += Property.Indication
        descriptor(CCCD) {
            var notificationsJob: Job? = null
            var indicationsJob: Job? = null

            this.onWrite { value ->
                if (value.isNotEmpty()) {
                    val notificationsBit = value[0] and 0b1 != 0.toByte()
                    val indicationsBit = !notificationsBit && value[0] and 0b10 != 0.toByte()

                    check(notificationsBit && !notification) {
                        TODO("GATT error: notifications not supported")
                    }
                    check(indicationsBit && !indication) {
                        TODO("GATT error: notifications not supported")
                    }

                    if (notificationsBit && notificationsJob?.isActive != true) {
                        indicationsJob?.cancel()
                        notificationsJob = scope.launch { action() }
                    } else {
                        notificationsJob?.cancel()
                    }

                    if (indicationsBit && indicationsJob?.isActive != true) {
                        notificationsJob?.cancel()
                        indicationsJob = scope.launch { action() }
                    } else {
                        indicationsJob?.cancel()
                    }
                } else {
                    TODO() // send GATT error
                }
            }
        }
    }

    public fun onRead(
        encrypted: Boolean = false,
        manInTheMiddleProtection: Boolean = true,
        action: suspend ReadAction.() -> Unit,
    ) {
        requireNotConfigured("onRead", onRead)
        properties += Property.Read(encrypted, manInTheMiddleProtection)
        onRead = action
    }

    public fun onWrite(
        encrypted: Boolean = false,
        manInTheMiddleProtection: Boolean = true,
        action: suspend (value: ByteArray) -> Unit,
    ) {
        requireNotConfigured("onWrite", onWrite)
        properties += Property.Write(encrypted, manInTheMiddleProtection)
        onWrite = action
    }

    public fun descriptor(uuid: Uuid, builder: DescriptorBuilder.() -> Unit) {
        require(uuid !in descriptors) { "Descriptor $uuid already configured" }
        descriptors[uuid] = DescriptorBuilder(uuid).apply(builder)
    }

    private fun requireNotConfigured(name: String, property: Any?) {
        require(property == null) { "Characteristic $uuid $name already configured" }
    }
}
