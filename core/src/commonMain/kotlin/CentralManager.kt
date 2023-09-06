package com.juul.kable

public expect class CentralManager internal constructor(
    options: Map<Any?, *>?,
) {

    public companion object {

        /** Initializes the [CentralManager] on Apple. No-op on Android and JavaScript. */
        public fun initialize(builderAction: CentralBuilder.() -> Unit = {})
    }
}
