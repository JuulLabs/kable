package com.juul.kable

import platform.CoreBluetooth.CBCentralManagerOptionRestoreIdentifierKey
import platform.Foundation.NSUUID
import platform.Foundation.NSUserDefaults

private const val CBCENTRALMANAGER_RESTORATION_ID = "kable-central-manager"
private const val CBCENTRALMANAGER_CONSUMER_ID_KEY = "kable-central-manager-consumer-id"

// This value is needed to ensure multiple instances of Kable running on the same iOS device do not
// cross pollinate restored instances of CBCentralManager. The value will live for the lifetime of
// the consuming app.
private val consumerId: String
    get() = NSUserDefaults.standardUserDefaults.stringForKey(CBCENTRALMANAGER_CONSUMER_ID_KEY)
        ?: NSUUID().UUIDString().also {
            NSUserDefaults.standardUserDefaults.setObject(it, CBCENTRALMANAGER_CONSUMER_ID_KEY)
        }

public actual class CentralBuilder internal actual constructor() {

    public actual var stateRestoration: Boolean = false

    internal actual fun build(): CentralManager {
        val options: Map<Any?, *>? = if (stateRestoration) {
            mapOf(CBCentralManagerOptionRestoreIdentifierKey to "$CBCENTRALMANAGER_RESTORATION_ID-$consumerId")
        } else {
            null
        }
        return CentralManager(options)
    }
}
