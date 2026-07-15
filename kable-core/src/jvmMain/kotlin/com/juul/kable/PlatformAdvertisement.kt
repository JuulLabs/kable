package com.juul.kable

import kotlinx.serialization.Serializable

@Serializable(with = PlatformAdvertisementSerializer::class)
public actual interface PlatformAdvertisement : Advertisement
