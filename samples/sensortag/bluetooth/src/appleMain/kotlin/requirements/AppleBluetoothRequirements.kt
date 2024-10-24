package com.juul.sensortag.bluetooth.requirements

import com.juul.sensortag.bluetooth.requirements.Deficiency.BluetoothOff
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import platform.CoreBluetooth.CBCentralManager
import platform.CoreBluetooth.CBCentralManagerDelegateProtocol
import platform.CoreBluetooth.CBCentralManagerOptionShowPowerAlertKey
import platform.CoreBluetooth.CBManagerStatePoweredOff
import platform.darwin.NSObject

// Prevent triggering "turn on bluetooth" dialog.
// https://chrismaddern.com/determine-whether-bluetooth-is-enabled-on-ios-passively/
private val options = mapOf<Any?, Any>(CBCentralManagerOptionShowPowerAlertKey to false)

internal object AppleBluetoothRequirements : BluetoothRequirements {

    // Need to hold strong-reference to CBCentralManager and its delegate while in use.
    private var managerRef: NSObject? = null
    private var delegateRef: NSObject? = null

    override val deficiencies: Flow<Set<Deficiency>> = callbackFlow {
        val delegate = object : NSObject(), CBCentralManagerDelegateProtocol {
            override fun centralManagerDidUpdateState(central: CBCentralManager) {
                trySend(central.state).onFailure {
                    // Silently ignore.
                }
            }
        }

        managerRef = CBCentralManager(delegate, null, options)
        delegateRef = delegate

        awaitClose {
            managerRef = null
            delegateRef = null
        }
    }.map { state ->
        buildSet {
            if (state == CBManagerStatePoweredOff) add(BluetoothOff)
        }
    }
}
