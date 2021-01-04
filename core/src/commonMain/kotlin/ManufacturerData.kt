package com.juul.kable

public class ManufacturerData(
    /**
     * Two-octet [Company Identifier Code][https://www.bluetooth.com/specifications/assigned-numbers/company-identifiers/]
     */
    public val code: Int,

    /**
     * the Manufacturer Data (not including the leading two identifier octets)
     */
    public val data: ByteArray,
)
