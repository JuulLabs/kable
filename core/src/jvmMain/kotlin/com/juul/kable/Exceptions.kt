package com.juul.kable

public fun jvmNotImplementedException(): Nothing = throw NotImplementedError(
    "JVM implementation is not available at the moment. Jvm target exists to allow compilation " +
        "along with other supported targets.",
)
