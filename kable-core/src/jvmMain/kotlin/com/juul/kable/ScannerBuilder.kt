package com.juul.kable

import com.juul.kable.btleplug.ffi.ScanCallback
import com.juul.kable.btleplug.ffi.scan
import com.juul.kable.logs.LoggingBuilder
import kotlin.uuid.Uuid
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

public actual class ScannerBuilder {

    @Deprecated(
        message = "Use filters(FiltersBuilder.() -> Unit)",
        replaceWith = ReplaceWith("filters { }"),
        level = DeprecationLevel.HIDDEN,
    )
    public actual var filters: List<Filter>? = null

    public actual fun filters(builderAction: FiltersBuilder.() -> Unit) {
        jvmNotImplementedException()
    }

    public actual fun logging(init: LoggingBuilder) {
        jvmNotImplementedException()
    }

    internal actual fun build(): PlatformScanner = BtleplugScanner()
}

private class BtleplugScanner : PlatformScanner {
    override val advertisements: Flow<PlatformAdvertisement> = callbackFlow {
        val handle = scan(
            object : ScanCallback {
                override suspend fun manufacturerDataAdertisement(id: String, manufacturerData: Map<UShort, ByteArray>) {
                    TODO("Not yet implemented")
                }

                override suspend fun serviceDataAdvertisement(id: String, serviceData: Map<String, ByteArray>) {
                    TODO("Not yet implemented")
                }

                override suspend fun servicesAdvertisement(id: String, services: List<String>) {
                    TODO("Not yet implemented")
                }
            },
        )

        awaitClose {
            handle.close()
            handle.destroy()
        }
    }
}
