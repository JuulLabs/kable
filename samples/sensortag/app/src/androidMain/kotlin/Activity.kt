package com.juul.sensortag

import android.Manifest.permission.BLUETOOTH_CONNECT
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.net.Uri
import android.provider.Settings

object RequestCode {
    const val EnableBluetooth = 55001
}

/** @throws SecurityException if [BLUETOOTH_CONNECT] permission has not been granted on Android 12 (API 31) or newer. */
@SuppressLint("MissingPermission")
fun Activity.enableBluetooth() {
    startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), RequestCode.EnableBluetooth)
}

fun Activity.showLocationSettings() {
    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
}

fun Activity.openAppDetails() {
    startActivity(Intent().apply {
        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        addCategory(Intent.CATEGORY_DEFAULT)
        data = Uri.parse("package:$packageName")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
    })
}
