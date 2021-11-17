package com.juul.kable

public class ManufacturerDataFilter(
    public val manufacturerId: Int,
    public val manufacturerData: ByteArray,
    public val manufacturerDataMask: ByteArray?
)
