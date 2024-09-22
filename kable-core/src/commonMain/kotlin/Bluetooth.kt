@file:JvmName("BluetoothCommon")

package com.juul.kable

import kotlinx.coroutines.flow.Flow
import kotlin.jvm.JvmName
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import com.juul.kable.bluetooth.isSupported as isBluetoothSupported

public expect enum class Reason

public object Bluetooth {

    /**
     * Bluetooth Base UUID: `00000000-0000-1000-8000-00805F9B34FB`
     *
     * [Bluetooth Core Specification, Vol 3, Part B: 2.5.1 UUID](https://www.bluetooth.com/specifications/specs/?types=adopted&keyword=Core+Specification)
     */
    @OptIn(ExperimentalUuidApi::class)
    public object BaseUuid {

        private const val mostSignificantBits = 4096L // 00000000-0000-1000
        private const val leastSignificantBits = -9223371485494954757L // 8000-00805F9B34FB

        public operator fun plus(shortUuid: Int): Uuid = plus(shortUuid.toLong())

        /** @param shortUuid 32-bits (or less) short UUID (if larger than 32-bits, will be truncated to 32-bits). */
        public operator fun plus(shortUuid: Long): Uuid =
            Uuid.fromLongs(mostSignificantBits + (shortUuid and 0xFFFF_FFFF shl 32), leastSignificantBits)

        override fun toString(): String = "00000000-0000-1000-8000-00805F9B34FB"
    }

    public sealed class Availability {
        public data object Available : Availability()
        public data class Unavailable(val reason: Reason?) : Availability()
    }

    /**
     * Checks if Bluetooth Low Energy is supported on the system. Being supported (a return of
     * `true`) does not necessarily mean that bluetooth operations will work. The radio could be off
     * or permissions may be denied.
     *
     * This function is idempotent.
     */
    public suspend fun isSupported(): Boolean = isBluetoothSupported()

    public val availability: Flow<Availability> = bluetoothAvailability
}

internal expect val bluetoothAvailability: Flow<Bluetooth.Availability>
