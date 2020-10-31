package com.juul.kable

import android.bluetooth.BluetoothAdapter
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

public fun CoroutineScope.central(
    context: Context
): Central = AndroidCentral(coroutineContext, context)

public class AndroidCentral internal constructor(
    parentCoroutineContext: CoroutineContext,
    androidContext: Context
) : Central {

    private val scope = CoroutineScope(parentCoroutineContext + Job(parentCoroutineContext[Job]))
    private val applicationContext = androidContext.applicationContext

    public override fun scanner(): Scanner = AndroidScanner()

    public override fun peripheral(
        advertisement: Advertisement
    ): Peripheral = scope.peripheral(applicationContext, advertisement.scanResult.device)

    public fun peripheral(
        macAddress: String
    ): Peripheral = scope.peripheral(applicationContext, bluetoothDeviceOf(macAddress))
}

private fun bluetoothDeviceOf(macAddress: String) =
    BluetoothAdapter.getDefaultAdapter().getRemoteDevice(macAddress)
