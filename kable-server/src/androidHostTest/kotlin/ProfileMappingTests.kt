package com.juul.kable.server

import android.bluetooth.BluetoothGattCharacteristic.PERMISSION_READ
import android.bluetooth.BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED
import android.bluetooth.BluetoothGattCharacteristic.PERMISSION_WRITE
import android.bluetooth.BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED
import android.bluetooth.BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED_MITM
import android.bluetooth.BluetoothGattCharacteristic.PROPERTY_INDICATE
import android.bluetooth.BluetoothGattCharacteristic.PROPERTY_NOTIFY
import android.bluetooth.BluetoothGattCharacteristic.PROPERTY_READ
import android.bluetooth.BluetoothGattCharacteristic.PROPERTY_WRITE
import android.bluetooth.BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE
import android.bluetooth.BluetoothGattService.SERVICE_TYPE_PRIMARY
import android.bluetooth.BluetoothGattService.SERVICE_TYPE_SECONDARY
import android.os.Build
import com.juul.kable.Bluetooth
import com.juul.kable.WriteType.WithResponse
import com.juul.kable.WriteType.WithoutResponse
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.uuid.toJavaUuid

private val serviceUuid = Bluetooth.BaseUuid + 0x180D
private val characteristicUuid = Bluetooth.BaseUuid + 0x2A37
private val descriptorUuid = Bluetooth.BaseUuid + 0x2901

private fun service(builderAction: ServiceBuilder.() -> Unit): ServerService =
    GattServerBuilder()
        .apply { service(serviceUuid, builderAction = builderAction) }
        .build()
        .services
        .single()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.S])
class ProfileMappingTests {

    @Test
    fun service_isMappedWithTypeAndUuid() {
        val primary = service {
            characteristic(characteristicUuid) { value = byteArrayOf(1) }
        }.toBluetoothGattService()
        assertEquals(SERVICE_TYPE_PRIMARY, primary.type)
        assertEquals(serviceUuid.toJavaUuid(), primary.uuid)
        assertEquals(characteristicUuid.toJavaUuid(), primary.characteristics.single().uuid)

        val secondary = GattServerBuilder()
            .apply {
                service(serviceUuid, primary = false) {
                    characteristic(characteristicUuid) { value = byteArrayOf(1) }
                }
            }
            .build()
            .services
            .single()
            .toBluetoothGattService()
        assertEquals(SERVICE_TYPE_SECONDARY, secondary.type)
    }

    @Test
    fun readCharacteristic_hasReadPropertyAndPermission() {
        val characteristic = service {
            characteristic(characteristicUuid) {
                onRead { byteArrayOf(1) }
            }
        }.toBluetoothGattService().characteristics.single()

        assertEquals(PROPERTY_READ, characteristic.properties)
        assertEquals(PERMISSION_READ, characteristic.permissions)
    }

    @Test
    fun encryptedRead_isMappedToEncryptedPermission() {
        val characteristic = service {
            characteristic(characteristicUuid) {
                onRead(security = Security.Encrypted) { byteArrayOf(1) }
            }
        }.toBluetoothGattService().characteristics.single()

        assertEquals(PROPERTY_READ, characteristic.properties)
        assertEquals(PERMISSION_READ_ENCRYPTED, characteristic.permissions)
    }

    @Test
    fun writeCharacteristic_isMappedPerWriteTypes() {
        val bothWriteTypes = service {
            characteristic(characteristicUuid) {
                onWrite { }
            }
        }.toBluetoothGattService().characteristics.single()
        assertEquals(PROPERTY_WRITE or PROPERTY_WRITE_NO_RESPONSE, bothWriteTypes.properties)
        assertEquals(PERMISSION_WRITE, bothWriteTypes.permissions)

        val withResponseOnly = service {
            characteristic(characteristicUuid) {
                onWrite(writeTypes = setOf(WithResponse), security = Security.Encrypted) { }
            }
        }.toBluetoothGattService().characteristics.single()
        assertEquals(PROPERTY_WRITE, withResponseOnly.properties)
        assertEquals(PERMISSION_WRITE_ENCRYPTED, withResponseOnly.permissions)

        val withoutResponseOnly = service {
            characteristic(characteristicUuid) {
                onWrite(writeTypes = setOf(WithoutResponse)) { }
            }
        }.toBluetoothGattService().characteristics.single()
        assertEquals(PROPERTY_WRITE_NO_RESPONSE, withoutResponseOnly.properties)
    }

    @Test
    fun subscribableCharacteristic_hasCccdAttached() {
        val characteristic = service {
            characteristic(characteristicUuid) {
                onSubscription { }
            }
        }.toBluetoothGattService().characteristics.single()

        assertEquals(PROPERTY_NOTIFY, characteristic.properties)
        val cccd = assertNotNull(characteristic.getDescriptor(clientCharacteristicConfigUuid.toJavaUuid()))
        assertEquals(PERMISSION_READ or PERMISSION_WRITE, cccd.permissions)
    }

    @Test
    fun indicationCharacteristic_withSecurity_isMappedToSecureCccd() {
        val characteristic = service {
            characteristic(characteristicUuid) {
                onSubscription(indication = true, security = Security.EncryptedMitm) { }
            }
        }.toBluetoothGattService().characteristics.single()

        assertEquals(PROPERTY_INDICATE, characteristic.properties)
        val cccd = assertNotNull(characteristic.getDescriptor(clientCharacteristicConfigUuid.toJavaUuid()))
        assertEquals(PERMISSION_READ or PERMISSION_WRITE_ENCRYPTED_MITM, cccd.permissions)
    }

    @Test
    fun nonSubscribableCharacteristic_hasNoCccd() {
        val characteristic = service {
            characteristic(characteristicUuid) {
                onRead { byteArrayOf(1) }
            }
        }.toBluetoothGattService().characteristics.single()

        assertNull(characteristic.getDescriptor(clientCharacteristicConfigUuid.toJavaUuid()))
    }

    @Test
    fun descriptors_areMappedWithPermissions() {
        val written = mutableListOf<ByteArray>()
        val characteristic = service {
            characteristic(characteristicUuid) {
                onRead { byteArrayOf(1) }
                descriptor(descriptorUuid) {
                    onRead { byteArrayOf(2) }
                    onWrite(security = Security.Encrypted) { value -> written += value }
                }
            }
        }.toBluetoothGattService().characteristics.single()

        val descriptor = assertNotNull(characteristic.getDescriptor(descriptorUuid.toJavaUuid()))
        assertEquals(PERMISSION_READ or PERMISSION_WRITE_ENCRYPTED, descriptor.permissions)
    }

    @Test
    fun staticValues_areMappedAsReadable() {
        val service = service {
            characteristic(characteristicUuid) {
                value = byteArrayOf(1, 2, 3)
                descriptor(descriptorUuid) { value = byteArrayOf(4) }
            }
        }
        val characteristic = service.toBluetoothGattService().characteristics.single()

        assertEquals(PROPERTY_READ, characteristic.properties)
        assertEquals(PERMISSION_READ, characteristic.permissions)
        assertContentEquals(byteArrayOf(1, 2, 3), service.characteristics.single().staticValue)

        val descriptor = assertNotNull(characteristic.getDescriptor(descriptorUuid.toJavaUuid()))
        assertEquals(PERMISSION_READ, descriptor.permissions)
    }
}
