/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.permissions.bluetooth

import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionDelegate
import dev.icerock.moko.permissions.PermissionState
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreBluetooth.CBCentralManager
import platform.CoreBluetooth.CBCentralManagerDelegateProtocol
import platform.CoreBluetooth.CBManager
import platform.CoreBluetooth.CBManagerAuthorization
import platform.CoreBluetooth.CBManagerAuthorizationAllowedAlways
import platform.CoreBluetooth.CBManagerAuthorizationDenied
import platform.CoreBluetooth.CBManagerAuthorizationNotDetermined
import platform.CoreBluetooth.CBManagerAuthorizationRestricted
import platform.CoreBluetooth.CBManagerState
import platform.CoreBluetooth.CBManagerStatePoweredOff
import platform.CoreBluetooth.CBManagerStatePoweredOn
import platform.CoreBluetooth.CBManagerStateResetting
import platform.CoreBluetooth.CBManagerStateUnauthorized
import platform.CoreBluetooth.CBManagerStateUnknown
import platform.CoreBluetooth.CBManagerStateUnsupported
import platform.Foundation.NSSelectorFromString
import platform.darwin.NSObject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal class BluetoothPermissionDelegate(
    private val permission: Permission
) : PermissionDelegate {
    @OptIn(ExperimentalForeignApi::class)
    override suspend fun providePermission() {
        // To maintain compatibility with iOS 12 (@see https://developer.apple.com/documentation/corebluetooth/cbmanagerauthorization)
        val isNotDetermined: Boolean =
            if (CBManager.resolveClassMethod(NSSelectorFromString("authorization"))) {
                CBManager.authorization == CBManagerAuthorizationNotDetermined
            } else {
                CBCentralManager().state == CBManagerStateUnknown
            }

        val state: CBManagerState = if (isNotDetermined) {
            suspendCoroutine { continuation ->
                CBCentralManager(object : NSObject(), CBCentralManagerDelegateProtocol {
                    override fun centralManagerDidUpdateState(central: CBCentralManager) {
                        continuation.resume(central.state)
                    }
                }, null)
            }
        } else {
            CBCentralManager().state
        }

        when (state) {
            CBManagerStatePoweredOn -> return
            CBManagerStateUnauthorized -> throw DeniedAlwaysException(permission)
            CBManagerStatePoweredOff ->
                throw DeniedException(permission, "Bluetooth is powered off")

            CBManagerStateResetting ->
                throw DeniedException(permission, "Bluetooth is restarting")

            CBManagerStateUnsupported ->
                throw DeniedAlwaysException(permission, "Bluetooth is not supported on this device")

            CBManagerStateUnknown ->
                error("Bluetooth state should be known at this point")

            else ->
                error("Unknown state (Permissions library should be updated) : $state")
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun getPermissionState(): PermissionState {
        // To maintain compatibility with iOS 12 (@see https://developer.apple.com/documentation/corebluetooth/cbmanagerauthorization)
        if (CBManager.resolveClassMethod(NSSelectorFromString("authorization"))) {
            val state: CBManagerAuthorization = CBManager.authorization
            return when (state) {
                CBManagerAuthorizationNotDetermined -> PermissionState.NotDetermined
                CBManagerAuthorizationAllowedAlways, CBManagerAuthorizationRestricted -> PermissionState.Granted
                CBManagerAuthorizationDenied -> PermissionState.DeniedAlways
                else -> error("unknown state $state")
            }
        }
        val state: CBManagerState = CBCentralManager().state
        return when (state) {
            CBManagerStatePoweredOn -> PermissionState.Granted
            CBManagerStateUnauthorized, CBManagerStatePoweredOff,
            CBManagerStateResetting, CBManagerStateUnsupported -> PermissionState.DeniedAlways

            CBManagerStateUnknown -> PermissionState.NotDetermined
            else -> error("unknown state $state")
        }
    }
}

actual val bluetoothLEDelegate: PermissionDelegate =
    BluetoothPermissionDelegate(BluetoothLEPermission)
actual val bluetoothScanDelegate: PermissionDelegate =
    BluetoothPermissionDelegate(BluetoothScanPermission)
actual val bluetoothAdvertiseDelegate: PermissionDelegate =
    BluetoothPermissionDelegate(BluetoothAdvertisePermission)
actual val bluetoothConnectDelegate: PermissionDelegate =
    BluetoothPermissionDelegate(BluetoothConnectPermission)
