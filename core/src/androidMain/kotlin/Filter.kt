package com.juul.kable

import com.benasher44.uuid.Uuid

public actual sealed class Filter {

    /**
     * https://developer.android.com/reference/android/bluetooth/le/ScanFilter.Builder#setManufacturerData(int,%20byte[],%20byte[])
     */
    public class ManufacturerData(
        /** A negative [id] is considered as invalid id. */
        public val id: Int,

        public val data: ByteArray,

        /**
         * For any bit in the mask, set it the 1 if it needs to match the one in [data], otherwise set it to 0.
         * [dataMask] must have the same length as [data].
         */
        public val dataMask: ByteArray?
    ) : Filter()

    public actual class Service(
        public actual val uuid: Uuid
    ) : Filter()
}
