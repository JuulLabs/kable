package com.juul.kable.server

import kotlin.uuid.Uuid

internal actual class AdvertisementParameters(
    val name: String?,
    val services: List<Uuid>,
)

public actual class AdvertisementParametersBuilder internal actual constructor() {

    public actual var name: String? = null
    public actual var services: List<Uuid> = emptyList()

    internal actual fun build(): AdvertisementParameters = AdvertisementParameters(
        name = name,
        services = services.toList(),
    )
}
