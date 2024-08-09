@file:JvmName("BluetoothCommon")

package com.juul.kable

import com.benasher44.uuid.Uuid
import kotlinx.coroutines.flow.Flow
import kotlin.jvm.JvmName
import com.juul.kable.bluetooth.availability as bluetoothAvailability
import com.juul.kable.bluetooth.isSupported as isBluetoothSupported
import com.juul.kable.bluetooth.state as bluetoothState

public object Bluetooth {

    public sealed class State {

        public data object Off : State()

        /**
         * Indicates that bluetooth radio is "on". The bluetooth state of [On] does not necessarily
         * mean that bluetooth operations will succeed, as permissions may not be granted.
         *
         * On JavaScript, the state of the radio is never known, so this state is defaulted to if
         * bluetooth is supported.
         */
        public data object On : State()

        public data object TurningOff : State() // Android-only
        public data object TurningOn : State() // Android-only

        public data class Unavailable(val reason: Reason?) : State() {
            public enum class Reason {

                /** Only applicable on Android 11 (API 30) and lower. */
                LocationServicesDisabled,

                Unauthorized, // Apple-only
            }
        }

        public data class Unknown(val reason: Reason?) : State() {
            public enum class Reason {
                Resetting, // Apple-only
                // TODO: Android
            }
        }
    }

    public sealed class Availability {

        /**
         * Bluetooth [is supported][isSupported] and [state] is [On][Bluetooth.State.On].
         *
         * On JavaScript, the state of the radio is never known. [Available] is defaulted to if
         * bluetooth is supported. An [Availability] of [Available] does mean that
         * `requestPeripheral` can be called, but may not necessarily resolve a [Peripheral]
         * (e.g. due to permission or security restrictions).
         */
        public data object Available : Availability()

        public data class Unavailable(val reason: AvailabilityReason?) : Availability()
    }

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

    /**
     * Checks if Bluetooth Low Energy is supported on the system. Being supported (a return of
     * `true`) does not necessarily mean that bluetooth operations will work. The [state] may be
     * [Off][State.Off] or permission may be denied.
     *
     * This function is idempotent.
     */
    public suspend fun isSupported(): Boolean = isBluetoothSupported()

    /**
     * The [State] of the bluetooth radio.
     *
     * On JavaScript, the state of the radio is never known. [On][Bluetooth.State.On] is defaulted
     * to if bluetooth is supported.
     *
     * @throws IllegalStateException If bluetooth is not supported.
     */
    public val state: Flow<State> = bluetoothState

    public val availability: Flow<Availability> = bluetoothAvailability
}

