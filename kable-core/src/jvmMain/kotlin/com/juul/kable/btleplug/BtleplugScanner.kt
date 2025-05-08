package com.juul.kable.btleplug

import com.juul.kable.FilterPredicate
import com.juul.kable.PlatformAdvertisement
import com.juul.kable.PlatformScanner
import com.juul.kable.btleplug.ffi.PeripheralProperties
import com.juul.kable.btleplug.ffi.ScanCallback
import com.juul.kable.btleplug.ffi.scan
import com.juul.kable.logs.Logger
import com.juul.kable.logs.Logging
import com.juul.kable.matches
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.getOrElse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

private const val SEND_FAILED = "Unable to deliver advertisement event due to failure in flow or premature closing."

// TODO: This class should use Btleplug's native scan filtering instead of faking it after the fact.
internal class BtleplugScanner(
    private val filters: List<FilterPredicate>,
    logging: Logging,
) : PlatformScanner {

    private val logger = Logger(logging, tag = "Kable/Scanner", identifier = null)

    override val advertisements: Flow<PlatformAdvertisement> = callbackFlow {
        val callback = object : ScanCallback {
            override suspend fun update(peripheral: PeripheralProperties) {
                val advertisement = BtleplugAdvertisement(peripheral)
                val isMatch = filters.matches(
                    services = advertisement.uuids,
                    name = advertisement.name,
                    address = null, // TODO: Possible to support on non-Apple?
                    manufacturerData = advertisement.manufacturerData,
                    serviceData = advertisement.serviceData.mapValues { (_, value) -> value.toByteArray() },
                )
                if (isMatch) {
                    trySend(advertisement).getOrElse {
                        logger.warn { message = SEND_FAILED }
                    }
                }
            }
        }

        logger.info { message = "Starting scan" }
        val handle = scan(callback)
        awaitClose {
            logger.verbose { message = "Removing scan listener" }
            handle.close()
            handle.destroy()
        }
    }
}
