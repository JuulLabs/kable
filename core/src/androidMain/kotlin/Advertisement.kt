package com.juul.kable

import android.bluetooth.le.ScanRecord
import android.bluetooth.le.ScanResult

public actual class Advertisement(
    public actual val rssi: Int,
    internal val scanResult: ScanResult,
) {

    public actual val name: String?
        get() = scanResult.device.name

    public val scanRecord: ScanRecord?
        get() = scanResult.scanRecord

    override fun toString(): String =
        "Advertisement(name=$name, rssi=$rssi, device=${scanResult.device})"
}
