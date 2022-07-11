package com.juul.kable

import com.benasher44.uuid.Uuid

internal fun <T : Descriptor> List<T>.first(
    descriptorUuid: Uuid,
): T = firstOrNull(descriptorUuid)
    ?: throw NoSuchElementException("Descriptor $descriptorUuid not found")

internal fun <T : Descriptor> List<T>.firstOrNull(
    descriptorUuid: Uuid,
): T? = firstOrNull { it.descriptorUuid == descriptorUuid }
