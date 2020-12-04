package com.juul.sensortag

const val TAG = "SensorTag"

actual object Log {

    actual fun info(message: String) {
        android.util.Log.i(TAG, message)
    }

    fun debug(message: String) {
        android.util.Log.d(TAG, message)
    }

    fun verbose(message: String) {
        android.util.Log.v(TAG, message)
    }
}
