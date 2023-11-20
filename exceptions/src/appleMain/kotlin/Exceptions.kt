@file:Suppress("ktlint:standard:filename")

package com.juul.kable

public actual open class IOException actual constructor(
    message: String?,
    cause: Throwable?,
) : Exception(message) {
    public actual constructor() : this(null, null)
    public actual constructor(message: String?) : this(message, null)
    public actual constructor(cause: Throwable?) : this(null, cause)
}
