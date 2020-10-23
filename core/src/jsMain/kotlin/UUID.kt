package com.juul.kable

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom

/**
 * Per [Web Bluetooth](https://webbluetoothcg.github.io/web-bluetooth/#typedefdef-uuid) Draft Community Group Report,
 * UUIDs are represented as `DOMString`:
 *
 * ```
 * typedef DOMString UUID;
 * ```
 */
internal typealias UUID = String

internal fun UUID.toUuid(): Uuid = uuidFrom(this)
