package com.juul.kable

import com.benasher44.uuid.Uuid

public actual sealed class Filter {

    /**
     * https://developer.android.com/reference/android/bluetooth/le/ScanFilter.Builder#setManufacturerData(int,%20byte[],%20byte[])
     */
    public class ManufacturerData(
        /** A negative [id] is considered an invalid id. */
        public val id: Int,

        public val data: ByteArray,

        /**
         * For any bit in the mask, set it to 1 if it needs to match the corresponding bit in [data], otherwise set it
         * to 0. [dataMask] must have the same length as [data].
         */
        public val dataMask: ByteArray?,
    ) : Filter()

    /**
     * https://developer.android.com/reference/android/bluetooth/le/ScanFilter.Builder#setDeviceName(java.lang.String)
     */
    public actual data class Name actual constructor(
        public val name: String,
    ) : Filter()

    /**
     * https://developer.android.com/reference/android/bluetooth/le/ScanFilter.Builder#setDeviceAddress(java.lang.String)
     */
    public data class Address(
        public val address: String,
    ) : Filter()

    public actual class Service actual constructor(
        public actual val uuid: Uuid,
    ) : Filter()
}
