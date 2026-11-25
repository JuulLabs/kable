package com.juul.kable

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class ObservationEventTests {

    private val serviceUuid = Uuid.random()
    private val characteristicUuid = Uuid.random()

    @Test
    fun isAssociatedWith_matchingUuids_isTrue() {
        val event = ObservationEvent.CharacteristicChange(
            characteristic = characteristicOf(serviceUuid, characteristicUuid),
            data = byteArrayOf(),
        )
        assertTrue(
            event.isAssociatedWith(
                characteristicOf(serviceUuid, characteristicUuid),
                forceCharacteristicEqualityByUuid = false,
            ),
        )
    }

    @Test
    fun isAssociatedWith_differentCharacteristicUuid_isFalse() {
        val event = ObservationEvent.CharacteristicChange(
            characteristic = characteristicOf(serviceUuid, characteristicUuid),
            data = byteArrayOf(),
        )
        assertFalse(
            event.isAssociatedWith(
                characteristicOf(serviceUuid, Uuid.random()),
                forceCharacteristicEqualityByUuid = false,
            ),
        )
    }

    @Test
    fun isAssociatedWith_differentServiceUuid_isFalse() {
        val event = ObservationEvent.CharacteristicChange(
            characteristic = characteristicOf(serviceUuid, characteristicUuid),
            data = byteArrayOf(),
        )
        assertFalse(
            event.isAssociatedWith(
                characteristicOf(Uuid.random(), characteristicUuid),
                forceCharacteristicEqualityByUuid = false,
            ),
        )
    }

    @Test
    fun isAssociatedWith_disconnectedEvent_isTrueForAnyCharacteristic() {
        val event = ObservationEvent.Disconnected
        assertTrue(
            event.isAssociatedWith(
                characteristicOf(serviceUuid, characteristicUuid),
                forceCharacteristicEqualityByUuid = false,
            ),
        )
    }
}
