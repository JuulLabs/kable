@file:Suppress("ktlint:standard:filename")

package com.juul.kable

/**
 * Marks declarations that are **obsolete** in Kable API, which means that the design of the corresponding declarations
 * has known flaws/drawbacks and they will be redesigned or replaced in the future.
 *
 * Roughly speaking, these declarations will be deprecated in the future but there is no replacement for them yet, so
 * they cannot be deprecated right away.
 */
@MustBeDocumented
@Retention(value = AnnotationRetention.BINARY)
@RequiresOptIn(level = RequiresOptIn.Level.WARNING)
public annotation class ObsoleteKableApi
