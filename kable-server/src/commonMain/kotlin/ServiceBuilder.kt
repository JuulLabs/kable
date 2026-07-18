package com.juul.kable.server

import com.juul.kable.ExperimentalKableApi
import kotlin.uuid.Uuid

@ExperimentalKableApi
public class ServiceBuilder internal constructor(
    private val uuid: Uuid,
    private val primary: Boolean,
) {

    private val characteristics = mutableMapOf<Uuid, CharacteristicBuilder>()

    /**
     * Declares a characteristic identified by [uuid].
     *
     * The [properties][com.juul.kable.Characteristic.Properties] of the characteristic are inferred
     * from the behavior declared via [builderAction]:
     *
     * | Declaration                             | Characteristic property               |
     * |-----------------------------------------|---------------------------------------|
     * | [value][CharacteristicBuilder.value]    | `read`                                |
     * | [onRead][CharacteristicBuilder.onRead]  | `read`                                |
     * | [onWrite][CharacteristicBuilder.onWrite] | `write` and/or `writeWithoutResponse` |
     * | [onSubscription][CharacteristicBuilder.onSubscription] | `notify` or `indicate` |
     *
     * @param uuid of the characteristic, must be unique within this service.
     */
    public fun characteristic(
        uuid: Uuid,
        builderAction: CharacteristicBuilder.() -> Unit,
    ) {
        require(uuid !in characteristics) { "Characteristic $uuid already declared in service ${this.uuid}" }
        characteristics[uuid] = CharacteristicBuilder(this.uuid, uuid).apply(builderAction)
    }

    internal fun build(): ServerService = ServerService(
        uuid = uuid,
        primary = primary,
        characteristics = characteristics.values.map(CharacteristicBuilder::build),
    )
}
