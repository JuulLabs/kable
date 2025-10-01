package com.juul.kable

/**
 * ¡¡¡Use At Your Own Risk!!!
 *
 * Marks declarations that are internal to Kable. These declarations are exposed so that users of
 * the library can do more advanced or as of yet unimplemented things. Usage of these declarations
 * can break the usage of Kable and therefore bugs related to the usage of these declarations should
 * not be brought up to the Kable maintainers.
 */
@MustBeDocumented
@RequiresOptIn(level = RequiresOptIn.Level.ERROR)
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION)
public annotation class KableInternalApi
