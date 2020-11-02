package com.juul.kable

import android.bluetooth.BluetoothDevice
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

public fun CoroutineScope.central(
    context: Context
): AndroidCentral = AndroidCentral(coroutineContext, context)

public class AndroidCentral internal constructor(
    parentCoroutineContext: CoroutineContext,
    androidContext: Context
) : Central {

    private val scope = CoroutineScope(parentCoroutineContext + Job(parentCoroutineContext[Job]))
    private val applicationContext = androidContext.applicationContext

    public override fun scanner(): Scanner = AndroidScanner()

    public override fun peripheral(
        advertisement: Advertisement
    ): Peripheral = peripheral(advertisement.bluetoothDevice)

    public fun peripheral(
        bluetoothDevice: BluetoothDevice
    ): Peripheral = scope.peripheral(applicationContext, bluetoothDevice)
}
