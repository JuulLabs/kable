package com.juul.sensortag.bluetooth

import android.Manifest.permission.BLUETOOTH_CONNECT
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS

private const val EnableBluetoothRequestCode = 55001

public class AndroidSystemControl(private val activity: Activity) : SystemControl {

    override fun showLocationSettings() {
        activity.startActivity(Intent(ACTION_LOCATION_SOURCE_SETTINGS))
    }

    /** @throws SecurityException If [BLUETOOTH_CONNECT] permission has not been granted on Android 12 (API 31) or newer. */
    @SuppressLint("MissingPermission")
    override fun requestToTurnBluetoothOn() {
        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        activity.startActivityForResult(intent, EnableBluetoothRequestCode)
    }
}
