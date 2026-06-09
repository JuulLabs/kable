package com.juul.kable

import com.juul.kable.logs.Logger
import com.juul.kable.logs.Logging

/**
 * ¡¡¡Use At Your Own Risk!!!
 *
 * Marks declarations that are internal to Kable. These declarations are exposed so that users of
 * the library can do more advanced or as of yet unimplemented things. Usage of these declarations
 * can break the usage of Kable and therefore bugs related to the usage of these declarations should
 * not be brought up to the Kable maintainers.
 */
@MustBeDocumented
@Retention(AnnotationRetention.BINARY)
@RequiresOptIn(level = RequiresOptIn.Level.ERROR)
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION)
public annotation class KableInternalApi

private var hasDisplayedInternalLogWarning = false

internal fun displayInternalLogWarning(logging: Logging?) {
    if (!hasDisplayedInternalLogWarning && logging != null) {
        val logger = Logger(logging, "Kable/InternalApi", null)
        logger.warn {
            message =
                "You are using an internal API. Make sure you know what you are doing. Incorrect usage could break internal Kable state. Bugs related to internal API usage will be deprioritized by the Kable maintainers."
        }
        hasDisplayedInternalLogWarning = true
    }
}
