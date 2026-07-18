package com.juul.kable.server

import com.juul.kable.Bluetooth
import com.juul.kable.WriteType.WithResponse
import com.juul.kable.WriteType.WithoutResponse
import com.juul.kable.indicate
import com.juul.kable.notify
import com.juul.kable.read
import com.juul.kable.write
import com.juul.kable.writeWithoutResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private val testServiceUuid = Bluetooth.BaseUuid + 0x180D
private val testCharacteristicUuid = Bluetooth.BaseUuid + 0x2A37
private val testDescriptorUuid = Bluetooth.BaseUuid + 0x2901

class GattServerBuilderTests {

    @Test
    fun duplicateService_throws() {
        assertFailsWith<IllegalArgumentException> {
            GattServerBuilder().apply {
                service(testServiceUuid) { characteristic(testCharacteristicUuid) { value = byteArrayOf(1) } }
                service(testServiceUuid) { characteristic(testCharacteristicUuid) { value = byteArrayOf(1) } }
            }
        }
    }

    @Test
    fun duplicateCharacteristic_throws() {
        assertFailsWith<IllegalArgumentException> {
            GattServerBuilder().apply {
                service(testServiceUuid) {
                    characteristic(testCharacteristicUuid) { value = byteArrayOf(1) }
                    characteristic(testCharacteristicUuid) { value = byteArrayOf(1) }
                }
            }
        }
    }

    @Test
    fun duplicateDescriptor_throws() {
        assertFailsWith<IllegalArgumentException> {
            GattServerBuilder().apply {
                service(testServiceUuid) {
                    characteristic(testCharacteristicUuid) {
                        onRead { byteArrayOf(1) }
                        descriptor(testDescriptorUuid) { value = byteArrayOf(1) }
                        descriptor(testDescriptorUuid) { value = byteArrayOf(1) }
                    }
                }
            }
        }
    }

    @Test
    fun cccdDeclaration_throws() {
        assertFailsWith<IllegalArgumentException> {
            GattServerBuilder().apply {
                service(testServiceUuid) {
                    characteristic(testCharacteristicUuid) {
                        onSubscription { }
                        descriptor(clientCharacteristicConfigUuid) { value = byteArrayOf(0, 0) }
                    }
                }
            }
        }
    }

    @Test
    fun staticValueWithHandler_throws() {
        assertFailsWith<IllegalArgumentException> {
            GattServerBuilder().apply {
                service(testServiceUuid) {
                    characteristic(testCharacteristicUuid) {
                        value = byteArrayOf(1)
                        onRead { byteArrayOf(2) }
                    }
                }
            }.build()
        }
    }

    @Test
    fun characteristicWithoutBehavior_throws() {
        assertFailsWith<IllegalArgumentException> {
            GattServerBuilder().apply {
                service(testServiceUuid) {
                    characteristic(testCharacteristicUuid) { }
                }
            }.build()
        }
    }

    @Test
    fun descriptorWithoutBehavior_throws() {
        assertFailsWith<IllegalArgumentException> {
            GattServerBuilder().apply {
                service(testServiceUuid) {
                    characteristic(testCharacteristicUuid) {
                        onRead { byteArrayOf(1) }
                        descriptor(testDescriptorUuid) { }
                    }
                }
            }.build()
        }
    }

    @Test
    fun duplicateOnRead_throws() {
        assertFailsWith<IllegalArgumentException> {
            GattServerBuilder().apply {
                service(testServiceUuid) {
                    characteristic(testCharacteristicUuid) {
                        onRead { byteArrayOf(1) }
                        onRead { byteArrayOf(2) }
                    }
                }
            }
        }
    }

    @Test
    fun emptyWriteTypes_throws() {
        assertFailsWith<IllegalArgumentException> {
            GattServerBuilder().apply {
                service(testServiceUuid) {
                    characteristic(testCharacteristicUuid) {
                        onWrite(writeTypes = emptySet()) { }
                    }
                }
            }
        }
    }

    @Test
    fun properties_areInferredFromDeclaredBehavior() {
        val profile = GattServerBuilder().apply {
            service(testServiceUuid) {
                characteristic(testCharacteristicUuid) {
                    onRead { byteArrayOf(1) }
                    onWrite(writeTypes = setOf(WithResponse)) { }
                    onSubscription { }
                }
            }
        }.build()

        val properties = profile.services.single().characteristics.single().properties
        assertTrue(properties.read)
        assertTrue(properties.write)
        assertFalse(properties.writeWithoutResponse)
        assertTrue(properties.notify)
        assertFalse(properties.indicate)
    }

    @Test
    fun indicationAndWriteWithoutResponseProperties_areInferred() {
        val profile = GattServerBuilder().apply {
            service(testServiceUuid) {
                characteristic(testCharacteristicUuid) {
                    onWrite(writeTypes = setOf(WithoutResponse)) { }
                    onSubscription(indication = true) { }
                }
            }
        }.build()

        val properties = profile.services.single().characteristics.single().properties
        assertFalse(properties.read)
        assertFalse(properties.write)
        assertTrue(properties.writeWithoutResponse)
        assertFalse(properties.notify)
        assertTrue(properties.indicate)
    }

    @Test
    fun staticValue_isCopied() {
        val value = byteArrayOf(1, 2, 3)
        val profile = GattServerBuilder().apply {
            service(testServiceUuid) {
                characteristic(testCharacteristicUuid) { this.value = value }
            }
        }.build()
        value[0] = 99

        val characteristic = profile.services.single().characteristics.single()
        assertEquals(1, characteristic.staticValue!![0])
        assertTrue(characteristic.properties.read)
    }
}
