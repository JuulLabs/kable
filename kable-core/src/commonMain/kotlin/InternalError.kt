package com.juul.kable

/**
 * An [Error] that signifies that an unexpected condition or state was encountered in the Kable
 * internals.
 *
 * May be thrown under the following (non-exhaustive) list of conditions:
 * - A new system level feature was added but Kable does not yet properly support it
 * - A programming error in Kable was encountered (e.g. a state when outside the designed bounds)
 *
 * Kable will likely be in an inconsistent state and will unlikely continue to function properly. It
 * is recommended that the application be restarted (e.g. by not catching this exception and
 * allowing the application to crash).
 *
 * If encountered, please report this exception (and provide logs) to:
 * https://github.com/JuulLabs/kable/issues
 */
@Suppress("ktlint:standard:indent")
public class InternalError internal constructor(
    message: String,
    cause: Throwable? = null,
) : Error(
    "$message, please report issue to https://github.com/JuulLabs/kable/issues and provide logs",
    cause,
)
