package com.juul.kable

import com.benasher44.uuid.Uuid

public actual sealed class Filter {

    public class ManufacturerData(
        public val id: Int,
        public val data: ByteArray,
        public val dataMask: ByteArray?
    ) : Filter()

    public actual class Service(
        public actual val uuid: Uuid
    ) : Filter()
}
