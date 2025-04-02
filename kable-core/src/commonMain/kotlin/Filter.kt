package com.juul.kable

import com.juul.kable.Filter.Name.Exact
import com.juul.kable.Filter.Name.Prefix
import kotlin.experimental.and
import kotlin.uuid.Uuid

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

        /** Must be non-`null` if [dataMask] is non-`null`. */
        public val data: ByteArray? = null,

        /**
         * For any bit in the mask, set it to 1 if advertisement manufacturer data needs to match the corresponding bit
         * in [data], otherwise set it to 0. If [dataMask] is not `null`, then it must have the same length as [data].
         */
        public val dataMask: ByteArray? = null,
    ) : Filter() {

        public constructor(id: ByteArray, data: ByteArray? = null, dataMask: ByteArray? = null) : this(id.toShort(), data, dataMask)

        init {
            require(id >= 0) { "Company identifier cannot be negative, was $id" }
            require(id <= 65535) { "Company identifier cannot be more than 16-bits (65535), was $id" }
            if (data != null && data.isEmpty()) throw IllegalArgumentException("If data is present (non-null), it must be non-empty")
            if (dataMask != null) {
                requireNotNull(data) { "Data is null but must be non-null when dataMask is non-null" }
                requireDataAndMaskHaveSameLength(data, dataMask)
            }
        }

        override fun toString(): String =
            "ManufacturerData(id=$id, data=${data?.toHexString()}, dataMask=${dataMask?.toHexString()})"
    }

    /**
     * Provides support for filtering against advertisement service data.
     *
     * If only portions of the service [data] needs to match, then [dataMask] can be used to
     * identify the relevant bits.
     *
     * Some examples to demonstrate the [dataMask] functionality:
     *
     * | [dataMask] value          | Bit representation  | [data] only needs to match...                             |
     * |---------------------------|---------------------|-----------------------------------------------------------|
     * | `byteArrayOf(0x0F, 0x00)` | 0000 1111 0000 0000 | bits 0-3 of the first byte of advertisement service data. |
     * | `byteArrayOf(0x00, 0xFF)` | 0000 0000 1111 1111 | the 2nd byte of advertisement service data.               |
     * | `byteArrayOf(0xF0)`       | 1111 0000           | bits 4-7 of the first byte of advertisement service data. |
     *
     * | Platform   | Supported | Details                                   |
     * |------------|:---------:|-------------------------------------------|
     * | Android    | Yes       | Supported natively                        |
     * | Apple      | Yes       | Support provided by Kable via flow filter |
     * | JavaScript | Yes       | Support provided by Kable via flow filter |
     */
    public class ServiceData(

        public val uuid: Uuid,

        public val data: ByteArray? = null,

        /**
         * For any bit in the mask, set it to 1 if advertisement service data needs to match the
         * corresponding bit in [data], otherwise set it to 0. If [dataMask] is not `null`, then it
         * must have the same length as [data].
         */
        public val dataMask: ByteArray? = null,
    ) : Filter() {

        init {
            if (data != null && data.isEmpty()) throw IllegalArgumentException("If data is present (non-null), it must be non-empty")
            if (dataMask != null) {
                requireNotNull(data) { "Data is null but must be non-null when dataMask is non-null" }
                requireDataAndMaskHaveSameLength(data, dataMask)
            }
        }

        override fun toString(): String =
            "ServiceData(uuid=$uuid, data=${data?.toHexString()}, dataMask=${dataMask?.toHexString()})"
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

internal fun Filter.ManufacturerData.matches(id: Int?, data: ByteArray?): Boolean {
    if (this.id != id) return false
    return matches(data)
}

internal fun Filter.ManufacturerData.matches(data: ByteArray?): Boolean =
    this.data?.matches(data, dataMask) ?: true

internal fun Filter.ServiceData.matches(data: ByteArray?): Boolean =
    this.data?.matches(data, dataMask) ?: true

private fun ByteArray.matches(data: ByteArray?, mask: ByteArray?): Boolean {
    if (data == null) return false
    if (mask == null) return contentEquals(data)
    val lastMaskIndex = mask.indexOfLast { it != 0.toByte() }
    if (lastMaskIndex > data.lastIndex) return false
    for (i in 0..lastMaskIndex) {
        if (mask[i] and this[i] != mask[i] and data[i]) return false
    }
    return true
}

private fun requireDataAndMaskHaveSameLength(data: ByteArray, dataMask: ByteArray) =
    require(data.size == dataMask.size) { "Data mask length (${dataMask.size}) must match data length (${data.size})" }
