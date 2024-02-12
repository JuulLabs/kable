package com.juul.kable

internal fun jvmNotImplementedException(): Nothing = throw NotImplementedError(
    "JVM target not yet implemented. See https://github.com/JuulLabs/kable/issues/380 for details.",
)
