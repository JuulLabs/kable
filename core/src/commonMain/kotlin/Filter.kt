package com.juul.kable

import com.benasher44.uuid.Uuid
import kotlin.experimental.and

/**
 * All [Filter]s are supported on all platforms, except for [Filter.Address], which is only supported on Android.
 *
 * | Filter           | Android | Apple | JavaScript |
 * |------------------|:-------:|:-----:|:----------:|
 * | Service          |   ✓✓    |  ✓✓*  |     ✓✓     |
 * | Name             |   ✓✓    |   ✓   |     ✓✓     |
 * | NamePrefix       |    ✓    |   ✓   |     ✓✓     |
 * | Address          |   ✓✓    |       |            |
 * | ManufacturerData |   ✓✓    |   ✓   |     ✓✓     |
 *
 *  ✓✓ = Supported natively
 *   ✓ = Support provided by Kable via flow filter
 * ✓✓* = Supported natively if the only filter type used, otherwise falls back to flow filter
 */
public sealed class Filter {

    /**
     * | Platform   | Supported       | Details                                                                                      |
     * |------------|:---------------:|----------------------------------------------------------------------------------------------|
     * | Android    | Yes             | Supported natively                                                                           |
     * | Apple      | Yes<sup>1</sup> | Supported natively if the only filter type used, otherwise provided by Kable via flow filter |
     * | JavaScript | Yes             | Supported natively                                                                           |
     *
     * <sup>1</sup>: The recommended practice is to provide only [service filter][Filter.Service]s on Apple platform.
     * If any filters other than [Filter.Service] are used on Apple platform, then filtering will not be performed natively.
     */
    public data class Service(
        public val uuid: Uuid,
    ) : Filter()

    /**
     * | Platform   | Supported | Details                                   |
     * |------------|:---------:|-------------------------------------------|
     * | Android    | Yes       | Supported natively                        |
     * | Apple      | Yes       | Support provided by Kable via flow filter |
     * | JavaScript | Yes       | Supported natively                        |
     *
     * https://developer.android.com/reference/android/bluetooth/le/ScanFilter.Builder#setDeviceName(java.lang.String)
     */
    public data class Name(
        public val name: String,
    ) : Filter()

    /**
     * | Platform   | Supported | Details                                   |
     * |------------|:---------:|-------------------------------------------|
     * | Android    | Yes       | Support provided by Kable via flow filter |
     * | Apple      | Yes       | Support provided by Kable via flow filter |
     * | JavaScript | Yes       | Supported natively                        |
     */
    public data class NamePrefix(val prefix: String) : Filter()

    /**
     * | Platform   | Supported | Details                                |
     * |------------|:---------:|----------------------------------------|
     * | Android    | Yes       | Supported natively                     |
     * | Apple      | No        | Throws [UnsupportedOperationException] |
     * | JavaScript | No        | Throws [UnsupportedOperationException] |
     *
     * https://developer.android.com/reference/android/bluetooth/le/ScanFilter.Builder#setDeviceAddress(java.lang.String)
     */
    public data class Address(
        public val address: String,
    ) : Filter()

    /**
     * | Platform   | Supported | Details                                   |
     * |------------|:---------:|-------------------------------------------|
     * | Android    | Yes       | Supported natively                        |
     * | Apple      | Yes       | Support provided by Kable via flow filter |
     * | JavaScript | Yes       | Supported natively                        |
     *
     * https://developer.android.com/reference/android/bluetooth/le/ScanFilter.Builder#setManufacturerData(int,%20byte[],%20byte[])
     */
    public class ManufacturerData(
        /** A negative [id] is considered an invalid id. */
        public val id: Int,

        public val data: ByteArray,

        /**
         * For any bit in the mask, set it to 1 if it needs to match the corresponding bit in [data], otherwise set it
         * to 0. If [dataMask] is not `null`, then it must have the same length as [data].
         */
        public val dataMask: ByteArray?,
    ) : Filter() {
        init {
            if (dataMask != null) checkDataAndMask(data, dataMask)
        }
    }
}

internal fun Filter.Service.matches(services: List<Uuid>?): Boolean {
    if (services == null) return false
    return this.uuid in services
}

internal fun Filter.Name.matches(name: String?): Boolean {
    if (name == null) return false
    return this.name == name
}

internal fun Filter.NamePrefix.matches(name: String?): Boolean {
    if (name == null) return false
    return name.startsWith(prefix)
}

internal fun Filter.ManufacturerData.matches(data: ByteArray?): Boolean {
    if (data == null) return false
    if (dataMask == null) return this.data.contentEquals(data)
    checkDataAndMask(data, dataMask)
    for (i in this.data.indices) {
        if (dataMask[i] and this.data[i] != dataMask[i] and data[i]) return false
    }
    return true
}

private fun checkDataAndMask(data: ByteArray, dataMask: ByteArray) =
    check(data.size == dataMask.size) { "Data mask length (${dataMask.size}) must match data length (${data.size})" }
