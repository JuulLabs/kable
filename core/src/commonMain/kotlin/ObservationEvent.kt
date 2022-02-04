package com.juul.kable

internal sealed class ObservationEvent<T> {

    abstract val characteristic: Characteristic

    data class CharacteristicChange<T>(
        override val characteristic: Characteristic,
        val data: T,
    ) : ObservationEvent<T>()

    data class Error<T>(
        override val characteristic: Characteristic,
        val cause: Exception,
    ) : ObservationEvent<T>()
}

internal fun <T> ObservationEvent<T>.isAssociatedWith(characteristic: Characteristic): Boolean =
    this.characteristic.characteristicUuid == characteristic.characteristicUuid &&
        this.characteristic.serviceUuid == characteristic.serviceUuid
