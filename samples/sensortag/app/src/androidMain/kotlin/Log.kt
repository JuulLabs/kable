package com.juul.sensortag

const val TAG = "SensorTag"

actual object Log {

    actual fun info(message: String) {
        android.util.Log.i(TAG, message)
    }
}
