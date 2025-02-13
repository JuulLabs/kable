package com.juul.kable.server

import android.Manifest
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothGattService.SERVICE_TYPE_PRIMARY
import android.bluetooth.BluetoothGattService.SERVICE_TYPE_SECONDARY
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.annotation.RequiresPermission
import kotlin.uuid.toJavaUuid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

public class BluetoothGattServer internal constructor(
    private val builder: ServerBuilder,
) : Server {

    private val scope = CoroutineScope()

    override val clients: StateFlow<List<Client>>
        get() = TODO("Not yet implemented")

    private val executor = Executor()

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun start() {
        // fixme: don't launch again if already started
        scope.launch {
            val bluetoothManager: BluetoothManager
            val context: Context

            val callback = Callback()
            val server = bluetoothManager.openGattServer(context, callback)
            server.addServices(builder.services.values, callback)
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private suspend fun BluetoothGattServer.addServices(
        services: Iterable<ServiceBuilder>,
        callback: Callback,
    ) {
        services.forEach { service ->
            executor.execute {
                addService(service.toBluetoothGattService())
                callback.onServiceAdded.receive()
            }
        }
    }

    override suspend fun stop() {
        scope.coroutineContext.cancelAndJoinChildren()
    }
}

private fun ServiceBuilder.toBluetoothGattService() = BluetoothGattService(
    uuid.toJavaUuid(),
    if (primary) SERVICE_TYPE_PRIMARY else SERVICE_TYPE_SECONDARY,
).apply {
    this@toBluetoothGattService.characteristics.values.forEach { characteristic ->
        addCharacteristic(characteristic.toBluetoothGattCharacteristic())
    }
}

private fun CharacteristicBuilder.toBluetoothGattCharacteristic(): BluetoothGattCharacteristic {
    BluetoothGattCharacteristic()
}
