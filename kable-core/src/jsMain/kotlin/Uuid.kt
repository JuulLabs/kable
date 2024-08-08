package com.juul.kable

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom
import com.juul.kable.external.BluetoothServiceUUID
import com.juul.kable.external.BluetoothUUID

// Number of characters in a 16-bit UUID alias in string hex representation
private const val UUID_ALIAS_STRING_LENGTH = 4

/**
 * Per [Web Bluetooth](https://webbluetoothcg.github.io/web-bluetooth/#typedefdef-uuid) Draft Community Group Report,
 * UUIDs are represented as `DOMString`:
 *
 * ```
 * typedef DOMString UUID;
 * ```
 */
internal typealias UUID = String

internal fun UUID.toUuid(): Uuid =
    uuidFrom(
        when (length) {
            UUID_ALIAS_STRING_LENGTH -> BluetoothUUID.canonicalUUID(toInt(16))
            else -> this
        },
    )

internal fun List<Uuid>.toBluetoothServiceUUID(): Array<BluetoothServiceUUID> =
    map(Uuid::toBluetoothServiceUUID)
        .toTypedArray()

// Note: Web Bluetooth requires that UUIDs be provided as lowercase strings.
internal fun Uuid.toBluetoothServiceUUID(): BluetoothServiceUUID =
    toString().lowercase()
