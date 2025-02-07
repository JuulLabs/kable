package com.juul.kable.scan

import android.bluetooth.le.ScanCallback.SCAN_FAILED_ALREADY_STARTED
import android.bluetooth.le.ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED
import android.bluetooth.le.ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED
import android.bluetooth.le.ScanCallback.SCAN_FAILED_INTERNAL_ERROR
import android.bluetooth.le.ScanCallback.SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES
import android.bluetooth.le.ScanCallback.SCAN_FAILED_SCANNING_TOO_FREQUENTLY

@JvmInline
internal value class ScanError(internal val errorCode: Int) {

    override fun toString(): String = when (errorCode) {
        SCAN_FAILED_ALREADY_STARTED -> "SCAN_FAILED_ALREADY_STARTED"
        SCAN_FAILED_APPLICATION_REGISTRATION_FAILED -> "SCAN_FAILED_APPLICATION_REGISTRATION_FAILED"
        SCAN_FAILED_INTERNAL_ERROR -> "SCAN_FAILED_INTERNAL_ERROR"
        SCAN_FAILED_FEATURE_UNSUPPORTED -> "SCAN_FAILED_FEATURE_UNSUPPORTED"
        SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES -> "SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES"
        SCAN_FAILED_SCANNING_TOO_FREQUENTLY -> "SCAN_FAILED_SCANNING_TOO_FREQUENTLY"
        else -> "UNKNOWN"
    }.let { name -> "$name($errorCode)" }
}

internal val ScanError.message: String
    get() = when (errorCode) {
        SCAN_FAILED_ALREADY_STARTED ->
            "Failed to start scan as BLE scan with the same settings is already started by the app"

        // Can occur if app has not been granted permission to scan (e.g. missing location permission).
        // https://github.com/NordicSemiconductor/Android-Scanner-Compat-Library/issues/73
        SCAN_FAILED_APPLICATION_REGISTRATION_FAILED ->
            "Failed to start scan as app cannot be registered"

        SCAN_FAILED_INTERNAL_ERROR -> "Failed to start scan due to an internal error"

        SCAN_FAILED_FEATURE_UNSUPPORTED ->
            "Failed to start power optimized scan as this feature is not supported"

        SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES ->
            "Failed to start scan as it is out of hardware resources"

        SCAN_FAILED_SCANNING_TOO_FREQUENTLY ->
            "Failed to start scan as application tries to scan too frequently"

        else -> "Unknown scan error code: $errorCode"
    }
