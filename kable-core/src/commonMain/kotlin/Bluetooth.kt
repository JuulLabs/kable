@file:JvmName("BluetoothCommon")

package com.juul.kable

import com.benasher44.uuid.Uuid
import kotlinx.coroutines.flow.Flow
import kotlin.jvm.JvmName
import com.juul.kable.bluetooth.isSupported as isBluetoothSupported

@Deprecated(
    message = "`Bluetooth.availability` has inconsistent behavior across platforms. " +
        "Will be removed in a future release. " +
        "See https://github.com/JuulLabs/kable/issues/737 for more details.",
)
public expect enum class Reason

public object Bluetooth {

    /**
     * Bluetooth Base UUID: `00000000-0000-1000-8000-00805F9B34FB`
     *
     * [Bluetooth Core Specification, Vol 3, Part B: 2.5.1 UUID](https://www.bluetooth.com/specifications/specs/?types=adopted&keyword=Core+Specification)
     */
    public object BaseUuid {

        private const val mostSignificantBits = 4096L // 00000000-0000-1000
        private const val leastSignificantBits = -9223371485494954757L // 8000-00805F9B34FB

        public operator fun plus(shortUuid: Int): Uuid = plus(shortUuid.toLong())

        /** @param shortUuid 32-bits (or less) short UUID (if larger than 32-bits, will be truncated to 32-bits). */
        public operator fun plus(shortUuid: Long): Uuid =
            Uuid(mostSignificantBits + (shortUuid and 0xFFFF_FFFF shl 32), leastSignificantBits)

        override fun toString(): String = "00000000-0000-1000-8000-00805F9B34FB"
    }

    @Deprecated(
        message = "`Bluetooth.availability` has inconsistent behavior across platforms. " +
            "Will be removed in a future release. " +
            "See https://github.com/JuulLabs/kable/issues/737 for more details.",
    )
    public sealed class Availability {
        @Deprecated(
            message = "`Bluetooth.availability` has inconsistent behavior across platforms. " +
                "Will be removed in a future release. " +
                "See https://github.com/JuulLabs/kable/issues/737 for more details.",
        )
        public data object Available : Availability()

        @Deprecated(
            message = "`Bluetooth.availability` has inconsistent behavior across platforms. " +
                "Will be removed in a future release. " +
                "See https://github.com/JuulLabs/kable/issues/737 for more details.",
        )
        public data class Unavailable(val reason: Reason?) : Availability()
    }

    @Deprecated(
        message = "`Bluetooth.availability` has inconsistent behavior across platforms. " +
            "Will be removed in a future release. " +
            "See https://github.com/JuulLabs/kable/issues/737 for more details.",
    )
    public val availability: Flow<Availability> = bluetoothAvailability

    /**
     * Checks if Bluetooth Low Energy is supported on the system. Being supported (a return of
     * `true`) does not necessarily mean that bluetooth operations will work. The radio could be off
     * or permissions may be denied.
     *
     * Due to Core Bluetooth limitations (unavoidable dialog upon checking if supported), this
     * function **always** returns `true` on Apple (even if Bluetooth is not supported).
     *
     * This function is idempotent.
     */
    @ExperimentalApi // Due to the inability to query Bluetooth support w/o showing a dialog on Apple, this function may be removed.
    public suspend fun isSupported(): Boolean = isBluetoothSupported()
}

internal expect val bluetoothAvailability: Flow<Bluetooth.Availability>
