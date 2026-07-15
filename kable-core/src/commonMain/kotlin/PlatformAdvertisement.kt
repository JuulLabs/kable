package com.juul.kable

import kotlinx.serialization.Serializable

/**
 * [PlatformAdvertisement] is serializable (e.g. usable as a Compose Navigation argument):
 * serialization captures a snapshot of the advertisement data, and deserialization restores an
 * advertisement holding the captured data. See [PlatformAdvertisementSerializer] for caveats.
 */
@Serializable(with = PlatformAdvertisementSerializer::class)
public expect interface PlatformAdvertisement : Advertisement
