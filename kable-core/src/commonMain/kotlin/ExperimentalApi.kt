package com.juul.kable

import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.annotation.AnnotationTarget.PROPERTY
import kotlin.annotation.AnnotationTarget.TYPEALIAS

/** Marks API that is experimental and/or likely to change. */
@Target(TYPEALIAS, FUNCTION, PROPERTY, CLASS)
@Retention(AnnotationRetention.BINARY)
@RequiresOptIn(level = RequiresOptIn.Level.ERROR)
public annotation class ExperimentalApi
