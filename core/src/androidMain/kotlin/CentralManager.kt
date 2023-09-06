package com.juul.kable

public actual class CentralManager actual constructor(
    options: Map<Any?, *>?,
) {

    public actual companion object {

        public actual fun initialize(builderAction: CentralBuilder.() -> Unit) {
            // No-op
        }
    }
}
