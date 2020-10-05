package com.juul.kable

import com.benasher44.uuid.Uuid
import com.juul.kable.CentralManagerDelegate.Response.DidDiscoverPeripheral
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import platform.CoreBluetooth.CBManagerStatePoweredOn

public actual class Scanner internal constructor(
    central: CentralManager,
    services: List<Uuid>?
) {

    public actual val peripherals: Flow<Advertisement> =
        central.delegate
            .response
            .onStart {
                central.awaitPoweredOn()
                central.scanForPeripheralsWithServices(services, options = null)
            }
            .onCompletion {
                central.stopScan()
            }
            .filterIsInstance<DidDiscoverPeripheral>()
            .map { (cbPeripheral, rssi, advertisementData) ->
                Advertisement(rssi.intValue, advertisementData, cbPeripheral)
            }
}

private suspend fun CentralManager.awaitPoweredOn(): Unit {
    delegate.state.first { it == CBManagerStatePoweredOn }
}
