package com.juul.kable

import kotlinx.serialization.Serializable
import org.khronos.webgl.DataView
import kotlin.uuid.Uuid

@Serializable(with = PlatformAdvertisementSerializer::class)
public actual interface PlatformAdvertisement : Advertisement {
    public fun serviceDataAsDataView(uuid: Uuid): DataView?
    public fun manufacturerDataAsDataView(companyIdentifierCode: Int): DataView?
}
