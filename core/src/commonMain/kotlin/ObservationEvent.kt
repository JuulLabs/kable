package com.juul.kable

internal sealed class ObservationEvent<T> {

    abstract val characteristic: Characteristic

    data class CharacteristicChange<T>(
        override val characteristic: Characteristic,
        val data: T,
    ) : ObservationEvent<T>()

    data class Error<T>(
        override val characteristic: Characteristic,
        val cause: Throwable,
    ) : ObservationEvent<T>()
}

internal fun <T> dematerialize(event: ObservationEvent<T>): T = when (event) {
    is ObservationEvent.Error -> throw event.cause
    is ObservationEvent.CharacteristicChange -> event.data
}

internal fun <T> ObservationEvent<T>.isAssociatedWith(characteristic: Characteristic): Boolean =
    this.characteristic.characteristicUuid == characteristic.characteristicUuid &&
        this.characteristic.serviceUuid == characteristic.serviceUuid
