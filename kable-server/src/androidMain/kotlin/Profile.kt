package com.juul.kable.server

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattCharacteristic.PERMISSION_READ
import android.bluetooth.BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED
import android.bluetooth.BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED_MITM
import android.bluetooth.BluetoothGattCharacteristic.PERMISSION_WRITE
import android.bluetooth.BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED
import android.bluetooth.BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED_MITM
import android.bluetooth.BluetoothGattCharacteristic.PROPERTY_INDICATE
import android.bluetooth.BluetoothGattCharacteristic.PROPERTY_NOTIFY
import android.bluetooth.BluetoothGattCharacteristic.PROPERTY_READ
import android.bluetooth.BluetoothGattCharacteristic.PROPERTY_WRITE
import android.bluetooth.BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothGattService.SERVICE_TYPE_PRIMARY
import android.bluetooth.BluetoothGattService.SERVICE_TYPE_SECONDARY
import com.juul.kable.WriteType.WithResponse
import com.juul.kable.WriteType.WithoutResponse
import kotlin.uuid.toJavaUuid

internal fun ServerService.toBluetoothGattService(): BluetoothGattService = BluetoothGattService(
    uuid.toJavaUuid(),
    if (primary) SERVICE_TYPE_PRIMARY else SERVICE_TYPE_SECONDARY,
).apply {
    this@toBluetoothGattService.characteristics.forEach { characteristic ->
        addCharacteristic(characteristic.toBluetoothGattCharacteristic())
    }
}

internal fun ServerCharacteristic.toBluetoothGattCharacteristic(): BluetoothGattCharacteristic {
    var properties = 0
    var permissions = 0

    if (read != null || staticValue != null) {
        properties = properties or PROPERTY_READ
        permissions = permissions or (read?.security ?: Security.None).readPermission
    }
    write?.let { write ->
        if (WithResponse in write.writeTypes) properties = properties or PROPERTY_WRITE
        if (WithoutResponse in write.writeTypes) properties = properties or PROPERTY_WRITE_NO_RESPONSE
        permissions = permissions or write.security.writePermission
    }
    subscription?.let { subscription ->
        properties = properties or if (subscription.indication) PROPERTY_INDICATE else PROPERTY_NOTIFY
    }

    return BluetoothGattCharacteristic(characteristicUuid.toJavaUuid(), properties, permissions).also { characteristic ->
        descriptors.forEach { descriptor ->
            characteristic.addDescriptor(descriptor.toBluetoothGattDescriptor())
        }
        subscription?.let { subscription ->
            // Explicitly attach the Client Characteristic Configuration descriptor (CCCD), without
            // which most centrals fail to subscribe (CCCD reads/writes are handled by the
            // `RequestDispatcher`).
            characteristic.addDescriptor(
                BluetoothGattDescriptor(
                    clientCharacteristicConfigUuid.toJavaUuid(),
                    PERMISSION_READ or subscription.security.writePermission,
                ),
            )
        }
    }
}

internal fun ServerDescriptor.toBluetoothGattDescriptor(): BluetoothGattDescriptor {
    var permissions = 0
    if (read != null || staticValue != null) permissions = permissions or (read?.security ?: Security.None).readPermission
    write?.let { permissions = permissions or it.security.writePermission }
    return BluetoothGattDescriptor(descriptorUuid.toJavaUuid(), permissions)
}

private val Security.readPermission: Int
    get() = when (this) {
        Security.None -> PERMISSION_READ
        Security.Encrypted -> PERMISSION_READ_ENCRYPTED
        Security.EncryptedMitm -> PERMISSION_READ_ENCRYPTED_MITM
    }

private val Security.writePermission: Int
    get() = when (this) {
        Security.None -> PERMISSION_WRITE
        Security.Encrypted -> PERMISSION_WRITE_ENCRYPTED
        Security.EncryptedMitm -> PERMISSION_WRITE_ENCRYPTED_MITM
    }
