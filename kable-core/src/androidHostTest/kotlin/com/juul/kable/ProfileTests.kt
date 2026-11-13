package com.juul.kable

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import io.mockk.every
import io.mockk.mockk
import nl.jqno.equalsverifier.EqualsVerifier
import nl.jqno.equalsverifier.api.SingleTypeEqualsVerifierApi
import kotlin.test.Test
import kotlin.uuid.toJavaUuid

class ProfileTests {

    @Test
    fun PlatformDiscoveredService_equals_verified() {
        EqualsVerifier
            .forClass(PlatformDiscoveredService::class.java)
            .withIgnoredFields("characteristics")
            .withMocks()
            .verify()
    }

    @Test
    fun PlatformDiscoveredCharacteristic_equals_verified() {
        EqualsVerifier
            .forClass(PlatformDiscoveredCharacteristic::class.java)
            .withIgnoredFields("descriptors")
            .withMocks()
            .verify()
    }

    @Test
    fun PlatformDiscoveredDescriptor_equals_verified() {
        EqualsVerifier
            .forClass(PlatformDiscoveredDescriptor::class.java)
            .withMocks()
            .verify()
    }
}

private val redService = mockk<BluetoothGattService> {
    every { instanceId } returns 1
    every { uuid } returns (Bluetooth.BaseUuid + 0x1).toJavaUuid()
}
private val blueService = mockk<BluetoothGattService> {
    every { instanceId } returns 2
    every { uuid } returns (Bluetooth.BaseUuid + 0x2).toJavaUuid()
}

private val redCharacteristic = mockk<BluetoothGattCharacteristic> {
    every { instanceId } returns 3
    every { service } returns redService
    every { uuid } returns (Bluetooth.BaseUuid + 0x3).toJavaUuid()
}
private val blueCharacteristic = mockk<BluetoothGattCharacteristic> {
    every { instanceId } returns 4
    every { service } returns redService
    every { uuid } returns (Bluetooth.BaseUuid + 0x4).toJavaUuid()
}

private val redDescriptor = mockk<BluetoothGattDescriptor> {
    every { characteristic } returns redCharacteristic
    every { uuid } returns (Bluetooth.BaseUuid + 0x5).toJavaUuid()
}
private val blueDescriptor = mockk<BluetoothGattDescriptor> {
    every { characteristic } returns blueCharacteristic
    every { uuid } returns (Bluetooth.BaseUuid + 0x6).toJavaUuid()
}

private fun <T> SingleTypeEqualsVerifierApi<T>.withMocks(): SingleTypeEqualsVerifierApi<T> =
    withPrefabValues(BluetoothGattService::class.java, redService, blueService)
        .withPrefabValues(BluetoothGattCharacteristic::class.java, redCharacteristic, blueCharacteristic)
        .withPrefabValues(BluetoothGattDescriptor::class.java, redDescriptor, blueDescriptor)
