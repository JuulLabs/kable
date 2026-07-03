package com.juul.kable

import kotlin.RequiresOptIn.Level.ERROR
import kotlin.annotation.AnnotationRetention.BINARY
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.annotation.AnnotationTarget.PROPERTY

/**
 * Marks declarations that are internal to Kable. Use at your own risk, as misuse of these APIs is
 * likely to break Kable's internal machinery. These APIs may be removed in a future release.
 */
@MustBeDocumented
@RequiresOptIn(message = "This declaration is internal, use at your own risk.", level = ERROR)
@Retention(BINARY)
@Target(CLASS, PROPERTY, FUNCTION)
public annotation class KableInternalApi
