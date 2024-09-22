package com.juul.kable

import org.khronos.webgl.DataView
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
public actual interface PlatformAdvertisement : Advertisement {
    public fun serviceDataAsDataView(uuid: Uuid): DataView?
    public fun manufacturerDataAsDataView(companyIdentifierCode: Int): DataView?
}
