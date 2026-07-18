@file:OptIn(ExperimentalForeignApi::class)

package com.juul.kable.server

import com.juul.kable.Bluetooth
import com.juul.kable.WriteType.WithResponse
import com.juul.kable.WriteType.WithoutResponse
import com.juul.kable.server.logs.Logger
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.convert
import platform.CoreBluetooth.CBAttributePermissions
import platform.CoreBluetooth.CBAttributePermissionsReadEncryptionRequired
import platform.CoreBluetooth.CBAttributePermissionsReadable
import platform.CoreBluetooth.CBAttributePermissionsWriteEncryptionRequired
import platform.CoreBluetooth.CBAttributePermissionsWriteable
import platform.CoreBluetooth.CBCharacteristicProperties
import platform.CoreBluetooth.CBCharacteristicPropertyIndicate
import platform.CoreBluetooth.CBCharacteristicPropertyIndicateEncryptionRequired
import platform.CoreBluetooth.CBCharacteristicPropertyNotify
import platform.CoreBluetooth.CBCharacteristicPropertyNotifyEncryptionRequired
import platform.CoreBluetooth.CBCharacteristicPropertyRead
import platform.CoreBluetooth.CBCharacteristicPropertyWrite
import platform.CoreBluetooth.CBCharacteristicPropertyWriteWithoutResponse
import platform.CoreBluetooth.CBMutableCharacteristic
import platform.CoreBluetooth.CBMutableDescriptor
import platform.CoreBluetooth.CBMutableService

private val characteristicUserDescriptionUuid = Bluetooth.BaseUuid + 0x2901
private val characteristicPresentationFormatUuid = Bluetooth.BaseUuid + 0x2904

internal fun ServerService.toCBMutableService(
    logger: Logger,
): Pair<CBMutableService, Map<AttributeKey.Characteristic, CBMutableCharacteristic>> {
    val registry = mutableMapOf<AttributeKey.Characteristic, CBMutableCharacteristic>()
    val service = CBMutableService(uuid.toCBUUID(), primary)
    service.setCharacteristics(
        characteristics.map { characteristic ->
            characteristic.toCBMutableCharacteristic(logger).also { cbCharacteristic ->
                registry[AttributeKey.Characteristic(uuid, characteristic.characteristicUuid)] = cbCharacteristic
            }
        },
    )
    return service to registry
}

private fun ServerCharacteristic.toCBMutableCharacteristic(logger: Logger): CBMutableCharacteristic {
    var properties: CBCharacteristicProperties = 0.convert()
    var permissions: CBAttributePermissions = 0.convert()

    if (read != null || staticValue != null) {
        properties = properties or CBCharacteristicPropertyRead
        permissions = permissions or when (read?.security ?: Security.None) {
            Security.None -> CBAttributePermissionsReadable
            // Core Bluetooth does not distinguish man-in-the-middle protection.
            Security.Encrypted, Security.EncryptedMitm -> CBAttributePermissionsReadEncryptionRequired
        }
    }
    write?.let { write ->
        if (WithResponse in write.writeTypes) properties = properties or CBCharacteristicPropertyWrite
        if (WithoutResponse in write.writeTypes) properties = properties or CBCharacteristicPropertyWriteWithoutResponse
        permissions = permissions or when (write.security) {
            Security.None -> CBAttributePermissionsWriteable
            Security.Encrypted, Security.EncryptedMitm -> CBAttributePermissionsWriteEncryptionRequired
        }
    }
    subscription?.let { subscription ->
        properties = properties or when {
            subscription.indication && subscription.security != Security.None -> CBCharacteristicPropertyIndicateEncryptionRequired
            subscription.indication -> CBCharacteristicPropertyIndicate
            subscription.security != Security.None -> CBCharacteristicPropertyNotifyEncryptionRequired
            else -> CBCharacteristicPropertyNotify
        }
    }

    return CBMutableCharacteristic(
        type = characteristicUuid.toCBUUID(),
        properties = properties,
        value = staticValue?.toNSData(),
        permissions = permissions,
    ).apply {
        val cbDescriptors = this@toCBMutableCharacteristic.descriptors
            .mapNotNull { it.toCBMutableDescriptorOrNull(logger) }
        if (cbDescriptors.isNotEmpty()) setDescriptors(cbDescriptors)
    }
}

/**
 * Core Bluetooth only supports Characteristic User Description (`0x2901`) and Characteristic
 * Presentation Format (`0x2904`) descriptors, both with static values. Unsupported descriptors are
 * omitted (with a logged warning).
 *
 * https://developer.apple.com/documentation/corebluetooth/cbmutabledescriptor
 */
private fun ServerDescriptor.toCBMutableDescriptorOrNull(logger: Logger): CBMutableDescriptor? {
    val value = staticValue
    if (value == null) {
        logger.warn {
            "Descriptor $descriptorUuid (of characteristic $characteristicUuid) omitted: Core Bluetooth " +
                "does not support dynamic (`onRead`/`onWrite`) descriptors"
        }
        return null
    }
    return when (descriptorUuid) {
        characteristicUserDescriptionUuid ->
            CBMutableDescriptor(descriptorUuid.toCBUUID(), value.decodeToString())
        characteristicPresentationFormatUuid ->
            CBMutableDescriptor(descriptorUuid.toCBUUID(), value.toNSData())
        else -> {
            logger.warn {
                "Descriptor $descriptorUuid (of characteristic $characteristicUuid) omitted: Core Bluetooth " +
                    "only supports Characteristic User Description (0x2901) and Characteristic Presentation " +
                    "Format (0x2904) descriptors"
            }
            null
        }
    }
}
