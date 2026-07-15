package com.juul.kable

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.BOND_BONDED
import android.bluetooth.BluetoothDevice.BOND_BONDING
import android.bluetooth.BluetoothDevice.BOND_NONE
import com.juul.kable.PlatformAdvertisement.BondState

internal fun BluetoothDevice.toBondState(): BondState = when (bondState) {
    BOND_NONE -> BondState.None
    BOND_BONDING -> BondState.Bonding
    BOND_BONDED -> BondState.Bonded
    else -> error("Unknown bond state: $bondState")
}
