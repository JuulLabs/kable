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
                override suspend fun onAdvertisement(name: String) {
                    val advertisement = object : PlatformAdvertisement {
                        override val name: String get() = name
                        override val peripheralName: String?
                            get() = TODO("Not yet implemented")
                        override val identifier: Identifier
                            get() = TODO("Not yet implemented")
                        override val isConnectable: Boolean?
                            get() = TODO("Not yet implemented")
                        override val rssi: Int
                            get() = TODO("Not yet implemented")
                        override val txPower: Int?
                            get() = TODO("Not yet implemented")
                        override val uuids: List<Uuid>
                            get() = TODO("Not yet implemented")

                        override fun serviceData(uuid: Uuid): ByteArray? {
                            TODO("Not yet implemented")
                        }

                        override fun manufacturerData(companyIdentifierCode: Int): ByteArray? {
                            TODO("Not yet implemented")
                        }

                        override val manufacturerData: ManufacturerData?
                            get() = TODO("Not yet implemented")

                    }
                    channel.trySendBlocking(advertisement)
                }
            },
        )

        awaitClose {
            handle.close()
        }
    }
}
