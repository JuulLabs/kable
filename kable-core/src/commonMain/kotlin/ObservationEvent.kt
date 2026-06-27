package com.juul.kable

internal sealed class ObservationEvent<out T> {

    open val characteristic: Characteristic? get() = null

    data class CharacteristicChange<T>(
        override val characteristic: PlatformDiscoveredCharacteristic,
        val data: T,
    ) : ObservationEvent<T>()

    data class Error(
        override val characteristic: PlatformDiscoveredCharacteristic,
        val cause: Exception,
    ) : ObservationEvent<Nothing>()

    // Only used on Apple (where characteristic change callback is used for characteristic reads).
    object Disconnected : ObservationEvent<Nothing>()
}

internal fun <T> ObservationEvent<T>.isAssociatedWith(
    characteristic: Characteristic,
    forceCharacteristicEqualityByUuid: Boolean,
): Boolean {
    val eventCharacteristic = this.characteristic
    return when {
        // `characteristic` is null for Disconnected, which applies to all characteristics.
        eventCharacteristic == null -> true

        !forceCharacteristicEqualityByUuid &&
            eventCharacteristic is DiscoveredCharacteristic &&
            characteristic is DiscoveredCharacteristic -> eventCharacteristic == characteristic

        else ->
            eventCharacteristic.characteristicUuid == characteristic.characteristicUuid &&
                eventCharacteristic.serviceUuid == characteristic.serviceUuid
    }
}
