package com.juul.kable

public sealed class State {

    public sealed class Connecting : State() {
        /**
         * [Peripheral] has initiated the process of connecting, via Bluetooth.
         *
         * I/O operations (e.g. [write][Peripheral.write] and [read][Peripheral.read]) will throw [NotReadyException]
         * while in this state.
         */
        public object Bluetooth : Connecting()

        /**
         * [Peripheral] has connected, but has not yet discovered services.
         *
         * I/O operations (e.g. [write][Peripheral.write] and [read][Peripheral.read]) will throw [IllegalStateOperation]
         * while in this state.
         */
        public object Services : Connecting()

        /**
         * [Peripheral] is wiring up [Observers][Peripheral.observe].
         *
         * I/O operations (e.g. [write][Peripheral.write] and [read][Peripheral.read]) are permitted while in this state.
         */
        public object Observes : Connecting()
    }

    /**
     * [Peripheral] is ready (i.e. has connected, discovered services and wired up [observers][Peripheral.observe]).
     */
    public object Connected : State()

    public object Disconnecting : State()

    /**
     * Triggered either after an established connection has dropped or after a connection attempt has failed.
     *
     * @param status represents status (cause) of [Disconnected] [State]. Always `null` for Javascript target.
     */
    public data class Disconnected(val status: Status? = null) : State() {

        /**
         * State statuses translated from their respective platforms:
         *
         * - Android: https://android.googlesource.com/platform/external/bluetooth/bluedroid/+/lollipop-release/stack/include/gatt_api.h#106
         * - Apple: `CBError.h` from the Core Bluetooth framework headers
         */
        public sealed class Status {

            /**
             * - Android: `GATT_CONN_TERMINATE_PEER_USER`
             * - Apple: `CBErrorPeripheralDisconnected`
             */
            public object PeripheralDisconnected : Status()

            /**
             * - Android: `GATT_CONN_TERMINATE_LOCAL_HOST`
             */
            public object CentralDisconnected : Status()

            /**
             * - Android: `GATT_CONN_FAIL_ESTABLISH`
             * - Apple: `CBErrorConnectionFailed`
             */
            public object Failed : Status()

            /**
             * - Android: `GATT_CONN_L2C_FAILURE`
             */
            public object L2CapFailure : Status()

            /**
             * - Android: `GATT_CONN_TIMEOUT`
             * - Apple: `CBErrorConnectionTimeout`
             */
            public object Timeout : Status()

            /**
             * - Android: `GATT_CONN_LMP_TIMEOUT`
             */
            public object LinkManagerProtocolTimeout : Status()

            /**
             * - Apple: `CBErrorUnknownDevice`
             */
            public object UnknownDevice : Status()

            /**
             * - Android: `GATT_CONN_CANCEL`
             * - Apple: `CBErrorOperationCancelled`
             */
            public object Cancelled : Status()

            /**
             * - Apple: `CBErrorConnectionLimitReached`
             */
            public object ConnectionLimitReached : Status()

            /**
             * - Apple: `CBErrorEncryptionTimedOut`
             */
            public object EncryptionTimedOut : Status()

            /** Catch-all for any statuses that are unknown for a platform. */
            public data class Unknown(val status: Int) : Status()

            override fun toString(): String = when (this) {
                PeripheralDisconnected -> "Peripheral Disconnected"
                CentralDisconnected -> "Central Disconnected"
                Failed -> "Failed"
                L2CapFailure -> "L2Cap Failure"
                Timeout -> "Timeout"
                LinkManagerProtocolTimeout -> "LinkManager Protocol Timeout"
                UnknownDevice -> "Unknown Device"
                Cancelled -> "Cancelled"
                ConnectionLimitReached -> "Connection Limit Reached"
                EncryptionTimedOut -> "Encryption Timed Out"
                // Handled via `Unknown` sub-classes (`toString` of `data class`).
                is Unknown -> error("Unreachable")
            }
        }

        override fun toString(): String =
            "Disconnected(${if (status is Status.Unknown) status.status else status.toString()})"
    }

    override fun toString(): String = when (this) {
        Connected -> "Connected"
        Connecting.Bluetooth -> "Connecting.Bluetooth"
        Connecting.Observes -> "Connecting.Observes"
        Connecting.Services -> "Connecting.Services"
        Disconnecting -> "Disconnecting"
        // Handled via `Disconnected` sub-classes (`toString` of `data class`).
        is Disconnected ->  error("Unreachable")
    }
}

/**
 * Returns `true` if `this` is at least in state [T], where [State]s are ordered:
 * - [State.Disconnected] (smallest)
 * - [State.Disconnecting]
 * - [State.Connecting.Bluetooth]
 * - [State.Connecting.Services]
 * - [State.Connecting.Observes]
 * - [State.Connected] (largest)
 */
internal inline fun <reified T : State> State.isAtLeast(): Boolean {
    val currentState = when (this) {
        is State.Disconnected -> 0
        State.Disconnecting -> 1
        State.Connecting.Bluetooth -> 2
        State.Connecting.Services -> 3
        State.Connecting.Observes -> 4
        State.Connected -> 5
    }
    val targetState = when (T::class) {
        State.Disconnected::class -> 0
        State.Disconnecting::class -> 1
        State.Connecting.Bluetooth::class -> 2
        State.Connecting.Services::class -> 3
        State.Connecting.Observes::class -> 4
        State.Connected::class -> 5
        else -> error("Unreachable.")
    }
    return currentState >= targetState
}
