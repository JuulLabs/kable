package com.juul.kable

public sealed class State {

    public object Connecting : State()

    public object Connected : State()

    public object Disconnecting : State()

    /**
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
             * - Android: `GATT_CONN_FAIL_ESTABLISH`
             * - Apple: `CBErrorConnectionFailed`
             */
            public object Failed : Status()

            /**
             * - Android: `GATT_CONN_TIMEOUT`
             * - Apple: `CBErrorConnectionTimeout`
             */
            public object Timeout : Status()

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
