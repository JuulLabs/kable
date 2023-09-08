@file:Suppress("ktlint:standard:filename")

package com.juul.kable

public actual open class IOException actual constructor(
    message: String?,
    cause: Throwable?,
) : Exception(message)
