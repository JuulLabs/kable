package com.juul.kable.btleplug

import com.juul.kable.PlatformAdvertisement
import com.juul.kable.PlatformScanner
import com.juul.kable.btleplug.ffi.PeripheralProperties
import com.juul.kable.btleplug.ffi.ScanCallback
import com.juul.kable.btleplug.ffi.scan
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

internal class BtleplugScanner : PlatformScanner {

    override val advertisements: Flow<PlatformAdvertisement> = callbackFlow {
        val callback = object : ScanCallback {
            override suspend fun update(peripheral: PeripheralProperties) {
                trySend(BtleplugAdvertisement(peripheral))
            }
        }

        val handle = scan(callback)
        awaitClose {
            handle.close()
            handle.destroy()
        }
    }

}
