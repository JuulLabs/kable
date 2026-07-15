package com.juul.kable

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Serializes an [Advertisement] as a snapshot of its data (rather than the platform specific
 * objects it wraps), making it usable (for example) as a navigation argument in Compose Navigation.
 *
 * Serialization captures the data of the [Advertisement] at the time of serialization.
 * Deserialization produces a [PlatformAdvertisement] that holds the previously captured data
 * (deserialized advertisements are not "live" — properties reflect the advertisement at the time
 * it was serialized).
 *
 * Serialized advertisements are only guaranteed to deserialize on the same platform they were
 * serialized on ([Advertisement.identifier] is a platform specific value that is not portable
 * across platforms).
 *
 * This serializer is applied by default (via `@Serializable(with = ...)` on [Advertisement]), so
 * [Advertisement] properties may be used in serializable classes without additional configuration.
 */
public object AdvertisementSerializer : KSerializer<Advertisement> {

    override val descriptor: SerialDescriptor = AdvertisementCapture.serializer().descriptor

    override fun serialize(encoder: Encoder, value: Advertisement) {
        val capture = (value as? PlatformAdvertisement)?.capture() ?: value.captureCommon()
        encoder.encodeSerializableValue(AdvertisementCapture.serializer(), capture)
    }

    override fun deserialize(decoder: Decoder): Advertisement =
        decoder.decodeSerializableValue(AdvertisementCapture.serializer()).restore()
}

/**
 * Serializes a [PlatformAdvertisement] as a snapshot of its data (rather than the platform
 * specific objects it wraps), making it usable (for example) as a navigation argument in Compose
 * Navigation.
 *
 * Serialization captures the data of the [PlatformAdvertisement] at the time of serialization.
 * Deserialization produces a [PlatformAdvertisement] that holds the previously captured data
 * (deserialized advertisements are not "live" — properties reflect the advertisement at the time
 * it was serialized).
 *
 * Serialized advertisements are only guaranteed to deserialize on the same platform they were
 * serialized on ([Advertisement.identifier] is a platform specific value that is not portable
 * across platforms).
 *
 * This serializer is applied by default (via `@Serializable(with = ...)` on
 * [PlatformAdvertisement]), so [PlatformAdvertisement] properties may be used in serializable
 * classes without additional configuration.
 */
public object PlatformAdvertisementSerializer : KSerializer<PlatformAdvertisement> {

    override val descriptor: SerialDescriptor = AdvertisementCapture.serializer().descriptor

    override fun serialize(encoder: Encoder, value: PlatformAdvertisement) {
        encoder.encodeSerializableValue(AdvertisementCapture.serializer(), value.capture())
    }

    override fun deserialize(decoder: Decoder): PlatformAdvertisement =
        decoder.decodeSerializableValue(AdvertisementCapture.serializer()).restore()
}
