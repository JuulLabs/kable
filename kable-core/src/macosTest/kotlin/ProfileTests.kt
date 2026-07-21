package com.juul.kable.test

import com.juul.kable.PlatformDiscoveredService
import com.juul.kable.findCharacteristic
import platform.CoreBluetooth.CBAttributePermissionsReadable
import platform.CoreBluetooth.CBCharacteristic
import platform.CoreBluetooth.CBCharacteristicPropertyRead
import platform.CoreBluetooth.CBMutableCharacteristic
import platform.CoreBluetooth.CBMutableService
import platform.CoreBluetooth.CBUUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.uuid.Uuid

class ProfileTests {

    @Test
    fun characteristicsWithSameUuid_haveDistinctInstanceIds_andAreNotEqual() {
        val characteristicUuid = Uuid.random()
        val service = cbService(
            Uuid.random(),
            listOf(cbCharacteristic(characteristicUuid), cbCharacteristic(characteristicUuid)),
        )
        val discoveredService = PlatformDiscoveredService(service, instanceId = 0)

        val (first, second) = discoveredService.characteristics
        assertEquals(expected = 0, actual = first.instanceId)
        assertEquals(expected = 1, actual = second.instanceId)
        assertEquals(expected = first.characteristicUuid, actual = second.characteristicUuid)
        assertNotEquals(illegal = first, actual = second)
    }

    @Test
    fun sameService_wrappedTwice_producesEqualDiscoveredCharacteristics() {
        val service = cbService(
            Uuid.random(),
            listOf(cbCharacteristic(Uuid.random()), cbCharacteristic(Uuid.random())),
        )

        val wrapped1 = PlatformDiscoveredService(service, instanceId = 0)
        val wrapped2 = PlatformDiscoveredService(service, instanceId = 0)

        assertEquals(expected = wrapped1, actual = wrapped2)
        assertEquals(expected = wrapped1.characteristics, actual = wrapped2.characteristics)
        assertEquals(expected = wrapped1.hashCode(), actual = wrapped2.hashCode())
    }

    @Test
    fun servicesWithSameUuid_haveDistinctInstanceIds_andCharacteristicsAreNotEqual() {
        val serviceUuid = Uuid.random()
        val characteristicUuid = Uuid.random()
        val services = listOf(
            cbService(serviceUuid, listOf(cbCharacteristic(characteristicUuid))),
            cbService(serviceUuid, listOf(cbCharacteristic(characteristicUuid))),
        ).mapIndexed { index, service -> PlatformDiscoveredService(service, instanceId = index) }

        assertNotEquals(illegal = services[0], actual = services[1])
        assertNotEquals(
            illegal = services[0].characteristics.single(),
            actual = services[1].characteristics.single(),
        )
    }

    @Test
    fun findCharacteristic_multipleCharacteristicsWithSameUuid_returnsCorrespondingDiscoveredCharacteristic() {
        val characteristicUuid = Uuid.random()
        val service = cbService(
            Uuid.random(),
            listOf(cbCharacteristic(characteristicUuid), cbCharacteristic(characteristicUuid)),
        )
        val services = listOf(PlatformDiscoveredService(service, instanceId = 0))

        val cbCharacteristic = service.characteristics!![1] as CBCharacteristic
        assertSame(
            expected = services.single().characteristics[1],
            actual = services.findCharacteristic(cbCharacteristic),
        )
    }

    @Test
    fun findCharacteristic_characteristicNotWithinServices_returnsNull() {
        val service = cbService(Uuid.random(), listOf(cbCharacteristic(Uuid.random())))
        val services = listOf(PlatformDiscoveredService(service, instanceId = 0))

        assertNull(services.findCharacteristic(cbCharacteristic(Uuid.random())))
    }
}

private fun cbCharacteristic(uuid: Uuid) = CBMutableCharacteristic(
    CBUUID.UUIDWithString(uuid.toString()),
    CBCharacteristicPropertyRead,
    null,
    CBAttributePermissionsReadable,
)

private fun cbService(
    uuid: Uuid,
    characteristics: List<CBMutableCharacteristic>,
) = CBMutableService(
    CBUUID.UUIDWithString(uuid.toString()),
    true,
).apply {
    setCharacteristics(characteristics)
}
