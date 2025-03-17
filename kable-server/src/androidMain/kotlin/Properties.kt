package com.juul.kable.server

import android.bluetooth.BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED
import android.bluetooth.BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED_MITM
import android.bluetooth.BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED
import android.bluetooth.BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED_MITM
import android.bluetooth.BluetoothGattCharacteristic.PROPERTY_INDICATE
import android.bluetooth.BluetoothGattCharacteristic.PROPERTY_NOTIFY
import android.bluetooth.BluetoothGattCharacteristic.PROPERTY_READ
import android.bluetooth.BluetoothGattCharacteristic.PROPERTY_WRITE
import android.bluetooth.BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE
import com.juul.kable.server.CharacteristicBuilder.Property
import com.juul.kable.server.CharacteristicBuilder.Property.Indication
import com.juul.kable.server.CharacteristicBuilder.Property.Notification

/** @return [Pair] of properties [Int] and permissions [Int]. */
internal fun Set<Property>.build(): Pair<Int, Int> {
    var properties = 0
    var permissions = 0
    forEach { property ->
        when (property) {
            Notification -> properties = properties or PROPERTY_NOTIFY
            Indication -> properties = properties or PROPERTY_INDICATE
            is Property.Read -> {
                properties = properties or PROPERTY_READ
                if (property.encrypted) permissions = permissions or PERMISSION_READ_ENCRYPTED
                if (property.manInTheMiddleProtection) permissions = permissions or PERMISSION_READ_ENCRYPTED_MITM
            }
            is Property.Write -> {
                properties = properties or PROPERTY_WRITE
                if (!property.withResponse) properties = properties or PROPERTY_WRITE_NO_RESPONSE
                if (property.encrypted) permissions = permissions or PERMISSION_WRITE_ENCRYPTED
                if (property.manInTheMiddleProtection) permissions = permissions or PERMISSION_WRITE_ENCRYPTED_MITM
            }
        }
    }
    return properties to permissions
}
