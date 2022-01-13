package com.juul.sensortag

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.net.Uri
import android.provider.Settings

object RequestCode {
    const val EnableBluetooth = 55001
}

fun Activity.enableBluetooth() {
    startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), RequestCode.EnableBluetooth)
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
