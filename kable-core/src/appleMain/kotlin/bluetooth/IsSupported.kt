package com.juul.kable.bluetooth

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import platform.CoreBluetooth.CBCentralManager
import platform.CoreBluetooth.CBCentralManagerDelegateProtocol
import platform.CoreBluetooth.CBCentralManagerOptionShowPowerAlertKey
import platform.CoreBluetooth.CBManagerState
import platform.CoreBluetooth.CBManagerStateResetting
import platform.CoreBluetooth.CBManagerStateUnknown
import platform.CoreBluetooth.CBManagerStateUnsupported
import platform.darwin.NSObject

// Prevent triggering bluetooth dialog (note that permission dialog will still appear).
// https://chrismaddern.com/determine-whether-bluetooth-is-enabled-on-ios-passively/
// https://stackoverflow.com/a/58600900
private val options = mapOf<Any?, Any>(CBCentralManagerOptionShowPowerAlertKey to false)

private var cachedState: CBManagerState? = null
private val mutex = Mutex()

internal actual suspend fun isSupported() = mutex.withLock {
    cachedState ?: awaitState().also { cachedState = it }
} != CBManagerStateUnsupported

// Need to hold strong-reference to CBCentralManager and its delegate while in use.
private var managerRef: NSObject? = null
private var delegateRef: NSObject? = null

private suspend fun awaitState() = callbackFlow {
    val delegate = object : NSObject(), CBCentralManagerDelegateProtocol {
        override fun centralManagerDidUpdateState(central: CBCentralManager) {
            trySend(central.state).onFailure {
                // Silently ignore.
            }
        }
    }

    delegateRef = delegate
    managerRef = CBCentralManager(delegate, null, options)

    awaitClose {
        managerRef = null
        delegateRef = null
    }
}.first { it.isDetermined }

private val CBManagerState.isDetermined: Boolean
    get() = this != CBManagerStateUnknown && this != CBManagerStateResetting
