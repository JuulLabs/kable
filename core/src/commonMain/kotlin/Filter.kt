package com.juul.kable

import com.benasher44.uuid.Uuid
import com.juul.kable.Filter.Name.Exact
import com.juul.kable.Filter.Name.Prefix
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

    public sealed class Name : Filter() {
        /**
         * | Platform   | Supported | Details                                   |
         * |------------|:---------:|-------------------------------------------|
         * | Android    | Yes       | Supported natively                        |
         * | Apple      | Yes       | Support provided by Kable via flow filter |
         * | JavaScript | Yes       | Supported natively                        |
         */
        public data class Exact(
            public val exact: String,
        ) : Name()

        /**
         * | Platform   | Supported | Details                                   |
         * |------------|:---------:|-------------------------------------------|
         * | Android    | Yes       | Support provided by Kable via flow filter |
         * | Apple      | Yes       | Support provided by Kable via flow filter |
         * | JavaScript | Yes       | Supported natively                        |
         */
        public data class Prefix(
            val prefix: String,
        ) : Name()
    }

    /**
     * | Platform   | Supported | Details                                |
     * |------------|:---------:|----------------------------------------|
     * | Android    | Yes       | Supported natively                     |
     * | Apple      | No        | Throws [UnsupportedOperationException] |
     * | JavaScript | No        | Throws [UnsupportedOperationException] |
     */
    public data class Address(
        public val address: String,
    ) : Filter()

    /**
     * Provides support for filtering against advertisement manufacturer data.
     *
     * If only portions of the manufacturer [data] needs to match, then [dataMask] can be used to identify the relevant
     * bits.
     *
     * Some examples to demonstrate the [dataMask] functionality:
     *
     * | [dataMask] value          | Bit representation  | [data] only needs to match...                                  |
     * |---------------------------|---------------------|----------------------------------------------------------------|
     * | `byteArrayOf(0x0F, 0x00)` | 0000 1111 0000 0000 | bits 0-3 of the first byte of advertisement manufacturer data. |
     * | `byteArrayOf(0x00, 0xFF)` | 0000 0000 1111 1111 | the 2nd byte of advertisement manufacturer data.               |
     * | `byteArrayOf(0xF0)`       | 1111 0000           | bits 4-7 of the first byte of advertisement manufacturer data. |
     *
     * | Platform   | Supported | Details                                   |
     * |------------|:---------:|-------------------------------------------|
     * | Android    | Yes       | Supported natively                        |
     * | Apple      | Yes       | Support provided by Kable via flow filter |
     * | JavaScript | Yes       | Supported natively                        |
     *
     * JavaScript support was added in Chrome 92 according to: https://developer.chrome.com/articles/bluetooth/#manufacturer-data-filter
     */
    public class ManufacturerData(

        /**
         * Company identifier (16-bit).
         * A negative [id] is considered an invalid id.
         *
         * List of assigned numbers can be found at (section: 7 Company Identifiers): https://www.bluetooth.com/specifications/assigned-numbers/
         */
        public val id: Int,

        public val data: ByteArray,

        /**
         * For any bit in the mask, set it to 1 if advertisement manufacturer data needs to match the corresponding bit
         * in [data], otherwise set it to 0. If [dataMask] is not `null`, then it must have the same length as [data].
         */
        public val dataMask: ByteArray? = null,
    ) : Filter() {

        public constructor(id: ByteArray, data: ByteArray, dataMask: ByteArray? = null) : this(id.toShort(), data, dataMask)

        init {
            require(id >= 0) { "Company identifier cannot be negative, was $id" }
            require(id <= 65535) { "Company identifier cannot be more than 16-bits (65535), was $id" }
            if (dataMask != null) requireDataAndMaskHaveSameLength(data, dataMask)
        }
    }
}

internal fun Filter.Service.matches(services: List<Uuid>?): Boolean {
    if (services == null) return false
    return this.uuid in services
}

internal fun Filter.Address.matches(address: String?): Boolean {
    if (address == null) return false
    return this.address == address
}

internal fun Filter.Name.matches(name: String?): Boolean {
    if (name == null) return false
    return when (this) {
        is Exact -> name == exact
        is Prefix -> name.startsWith(prefix)
    }
}

internal fun Filter.ManufacturerData.matches(data: ByteArray?): Boolean {
    if (data == null) return false
    if (dataMask == null) return this.data.contentEquals(data)
    requireDataAndMaskHaveSameLength(data, dataMask)
    for (i in this.data.indices) {
        if (dataMask[i] and this.data[i] != dataMask[i] and data[i]) return false
    }
    return true
}

internal fun Filter.ManufacturerData.matches(id: Int?, data: ByteArray?): Boolean {
    if (this.id != id) return false
    return matches(data)
}

private fun requireDataAndMaskHaveSameLength(data: ByteArray, dataMask: ByteArray) =
    require(data.size == dataMask.size) { "Data mask length (${dataMask.size}) must match data length (${data.size})" }
