package com.juul.kable

public sealed class State {

    public sealed class Connecting : State() {
        /**
         * Peripheral is in the process of connecting,  via Bluetooth.
         */
        public object Bluetooth : Connecting()

        /**
         * Peripheral has connected, but is discovering services.
         */
        public object Services : Connecting()

        /**
         * Peripheral is wiring up Observers.
         */
        public object Observes : Connecting()
    }

    /**
     * Peripheral is ready (i.e. has connected, discovered services and wired up the Observers).
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
        }
    }
}
