package com.juul.kable.server

import android.Manifest
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.annotation.RequiresPermission
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
                addService(service.build())
                callback.onServiceAdded.receive()
            }
        }
    }

    override suspend fun stop() {
        scope.coroutineContext.cancelAndJoinChildren()
    }
}
