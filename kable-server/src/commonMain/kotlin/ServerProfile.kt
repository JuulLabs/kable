package com.juul.kable.server

import com.juul.kable.Characteristic
import com.juul.kable.Characteristic.Properties
import com.juul.kable.Descriptor
import com.juul.kable.WriteType
import com.juul.kable.WriteType.WithResponse
import com.juul.kable.WriteType.WithoutResponse
import com.juul.kable.descriptor
import kotlin.uuid.Uuid

/** Client Characteristic Configuration descriptor (CCCD) UUID (`0x2902`). */
internal val clientCharacteristicConfigUuid = Uuid.descriptor("gatt.client_characteristic_configuration")

// Characteristic property bits, per Bluetooth Core Specification, Vol 3, Part G: 3.3.1.1 Characteristic Properties.
private const val PROPERTY_READ = 1 shl 1 // 0x02
private const val PROPERTY_WRITE_WITHOUT_RESPONSE = 1 shl 2 // 0x04
private const val PROPERTY_WRITE = 1 shl 3 // 0x08
private const val PROPERTY_NOTIFY = 1 shl 4 // 0x10
private const val PROPERTY_INDICATE = 1 shl 5 // 0x20

internal class ReadHandler(
    val security: Security,
    val action: ReadAction,
)

internal class WriteHandler(
    val writeTypes: Set<WriteType>,
    val security: Security,
    val action: WriteAction,
)

internal class SubscriptionHandler(
    val indication: Boolean,
    val security: Security,
    val action: SubscriptionAction,
)

/** Platform-agnostic GATT profile (as declared via the [GattServer] builder DSL). */
internal class ServerProfile(
    val services: List<ServerService>,
) {

    private val characteristics = services
        .flatMap(ServerService::characteristics)
        .associateBy { AttributeKey.Characteristic(it.serviceUuid, it.characteristicUuid) }

    private val descriptors = services
        .flatMap(ServerService::characteristics)
        .flatMap(ServerCharacteristic::descriptors)
        .associateBy { AttributeKey.Descriptor(it.serviceUuid, it.characteristicUuid, it.descriptorUuid) }

    fun characteristicOrNull(key: AttributeKey.Characteristic): ServerCharacteristic? = characteristics[key]

    fun characteristicOrNull(characteristic: Characteristic): ServerCharacteristic? =
        characteristicOrNull(AttributeKey.Characteristic(characteristic.serviceUuid, characteristic.characteristicUuid))

    fun descriptorOrNull(key: AttributeKey.Descriptor): ServerDescriptor? = descriptors[key]
}

internal class ServerService(
    val uuid: Uuid,
    val primary: Boolean,
    val characteristics: List<ServerCharacteristic>,
)

internal class ServerCharacteristic(
    override val serviceUuid: Uuid,
    override val characteristicUuid: Uuid,
    val staticValue: ByteArray?,
    val read: ReadHandler?,
    val write: WriteHandler?,
    val subscription: SubscriptionHandler?,
    val descriptors: List<ServerDescriptor>,
) : Characteristic {

    val properties: Properties = Properties(
        (if (read != null || staticValue != null) PROPERTY_READ else 0) or
            (if (write?.writeTypes?.contains(WithResponse) == true) PROPERTY_WRITE else 0) or
            (if (write?.writeTypes?.contains(WithoutResponse) == true) PROPERTY_WRITE_WITHOUT_RESPONSE else 0) or
            (if (subscription?.indication == false) PROPERTY_NOTIFY else 0) or
            (if (subscription?.indication == true) PROPERTY_INDICATE else 0),
    )

    override fun toString(): String =
        "ServerCharacteristic(serviceUuid=$serviceUuid, characteristicUuid=$characteristicUuid, properties=$properties)"
}

internal class ServerDescriptor(
    override val serviceUuid: Uuid,
    override val characteristicUuid: Uuid,
    override val descriptorUuid: Uuid,
    val staticValue: ByteArray?,
    val read: ReadHandler?,
    val write: WriteHandler?,
) : Descriptor {

    override fun toString(): String =
        "ServerDescriptor(serviceUuid=$serviceUuid, characteristicUuid=$characteristicUuid, descriptorUuid=$descriptorUuid)"
}
