package com.juul.kable

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import platform.CoreBluetooth.CBCentralManagerOptionRestoreIdentifierKey
import platform.Foundation.NSUUID
import platform.Foundation.NSUserDefaults

private const val CBCENTRALMANAGER_RESTORATION_ID = "kable-central-manager"
private const val CBCENTRALMANAGER_CONSUMER_ID_KEY = "kable-central-manager-consumer-id"

private val guard = SynchronizedObject()

// This value is needed to ensure multiple instances of Kable running on the same iOS device do not
// cross pollinate restored instances of CBCentralManager. The value will live for the lifetime of
// the consuming app.
private val consumerId: String
    get() = synchronized(guard) {
        NSUserDefaults.standardUserDefaults.stringForKey(CBCENTRALMANAGER_CONSUMER_ID_KEY)
            ?: NSUUID().UUIDString().also {
                NSUserDefaults.standardUserDefaults.setObject(it, CBCENTRALMANAGER_CONSUMER_ID_KEY)
            }
    }

internal fun CentralManager.Configuration.toOptions(): Map<Any?, *>? {
    return if (stateRestoration) {
        mapOf(CBCentralManagerOptionRestoreIdentifierKey to "$CBCENTRALMANAGER_RESTORATION_ID-$consumerId")
    } else {
        null
    }
}

internal fun CentralManager.Configuration.Builder.build(): CentralManager.Configuration =
    CentralManager.Configuration(stateRestoration)
