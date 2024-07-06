package com.juul.kable

internal sealed class ObservationEvent<out T> {

    open val characteristic: Characteristic? get() = null

    data class CharacteristicChange<T>(
        override val characteristic: Characteristic,
        val data: T,
    ) : ObservationEvent<T>()

    data class Error(
        override val characteristic: Characteristic,
        val cause: Exception,
    ) : ObservationEvent<Nothing>()

    // Only used on Apple (where characteristic change callback is used for characteristic reads).
    object Disconnected : ObservationEvent<Nothing>()
}

internal fun <T> ObservationEvent<T>.isAssociatedWith(characteristic: Characteristic): Boolean =
    when (val eventCharacteristic = this.characteristic) {
        null -> true // `characteristic` is null for Disconnected, which applies to all characteristics.
        else ->
            eventCharacteristic.characteristicUuid == characteristic.characteristicUuid &&
                eventCharacteristic.serviceUuid == characteristic.serviceUuid
    }
