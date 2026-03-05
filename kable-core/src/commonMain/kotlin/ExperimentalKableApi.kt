package com.juul.kable

import kotlin.RequiresOptIn.Level.ERROR
import kotlin.annotation.AnnotationRetention.BINARY
import kotlin.annotation.AnnotationTarget.ANNOTATION_CLASS
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.CONSTRUCTOR
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.annotation.AnnotationTarget.LOCAL_VARIABLE
import kotlin.annotation.AnnotationTarget.PROPERTY
import kotlin.annotation.AnnotationTarget.PROPERTY_GETTER
import kotlin.annotation.AnnotationTarget.PROPERTY_SETTER
import kotlin.annotation.AnnotationTarget.TYPEALIAS
import kotlin.annotation.AnnotationTarget.VALUE_PARAMETER

@Deprecated(
    message = "Renamed to ExperimentalKableApi",
    replaceWith = ReplaceWith("ExperimentalKableApi"),
)
@Target(TYPEALIAS, FUNCTION, PROPERTY, CLASS)
@Retention(AnnotationRetention.BINARY)
@RequiresOptIn(level = RequiresOptIn.Level.ERROR)
public annotation class ExperimentalApi

/** Marks API that is experimental and/or likely to change. */
@RequiresOptIn(level = ERROR)
@Retention(BINARY)
@Target(
    CLASS,
    ANNOTATION_CLASS,
    PROPERTY,
    FIELD,
    LOCAL_VARIABLE,
    VALUE_PARAMETER,
    CONSTRUCTOR,
    FUNCTION,
    PROPERTY_GETTER,
    PROPERTY_SETTER,
    TYPEALIAS,
)
@MustBeDocumented
public annotation class ExperimentalKableApi
