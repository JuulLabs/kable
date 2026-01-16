package com.juul.kable.logs

import js.errors.JsErrorLike
import js.errors.toJsErrorLike
import kotlin.js.JsAny
import kotlin.js.js

private val console: Console = js("console")

// The actual console interface is far more flexible than this, but
private external interface Console : JsAny {
    fun debug(format: String, tag: String, message: String)
    fun debug(format: String, tag: String, message: String, error: JsErrorLike?)
    fun log(format: String, tag: String, message: String)
    fun log(format: String, tag: String, message: String, error: JsErrorLike?)
    fun info(format: String, tag: String, message: String)
    fun info(format: String, tag: String, message: String, error: JsErrorLike?)
    fun warn(format: String, tag: String, message: String)
    fun warn(format: String, tag: String, message: String, error: JsErrorLike?)
    fun error(format: String, tag: String, message: String)
    fun error(format: String, tag: String, message: String, error: JsErrorLike?)
    fun assert(assertion: Boolean, format: String, tag: String, message: String)
    fun assert(assertion: Boolean, format: String, tag: String, message: String, error: JsErrorLike?)
}

public actual object SystemLogEngine : LogEngine {

    actual override fun verbose(throwable: Throwable?, tag: String, message: String) {
        debug(throwable, tag, message)
    }

    actual override fun debug(throwable: Throwable?, tag: String, message: String) {
        if (throwable == null) {
            console.debug("[%s] %s", tag, message)
        } else {
            console.debug("[%s] %s\n%o", tag, message, throwable.toJsErrorLike())
        }
    }

    actual override fun info(throwable: Throwable?, tag: String, message: String) {
        if (throwable == null) {
            console.info("[%s] %s", tag, message)
        } else {
            console.info("[%s] %s\n%o", tag, message, throwable.toJsErrorLike())
        }
    }

    actual override fun warn(throwable: Throwable?, tag: String, message: String) {
        if (throwable == null) {
            console.warn("[%s] %s", tag, message)
        } else {
            console.warn("[%s] %s\n%o", tag, message, throwable.toJsErrorLike())
        }
    }

    actual override fun error(throwable: Throwable?, tag: String, message: String) {
        if (throwable == null) {
            console.error("[%s] %s", tag, message)
        } else {
            console.error("[%s] %s\n%o", tag, message, throwable.toJsErrorLike())
        }
    }

    actual override fun assert(throwable: Throwable?, tag: String, message: String) {
        if (throwable == null) {
            console.assert(false, "[%s] %s", tag, message)
        } else {
            console.assert(false, "[%s] %s\n%o", tag, message, throwable.toJsErrorLike())
        }
    }
}
