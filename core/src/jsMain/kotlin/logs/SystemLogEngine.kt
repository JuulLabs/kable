package com.juul.kable.logs

public actual object SystemLogEngine : LogEngine {

    override fun verbose(throwable: Throwable?, tag: String, message: String) {
        debug(throwable, tag, message)
    }

    override fun debug(throwable: Throwable?, tag: String, message: String) {
        if (throwable == null) {
            console.asDynamic().debug("[%s] %s", tag, message)
        } else {
            console.asDynamic().debug("[%s] %s\n%o", tag, message, throwable)
        }
    }

    override fun info(throwable: Throwable?, tag: String, message: String) {
        if (throwable == null) {
            console.info("[%s] %s", tag, message)
        } else {
            console.info("[%s] %s\n%o", tag, message, throwable)
        }
    }

    override fun warn(throwable: Throwable?, tag: String, message: String) {
        if (throwable == null) {
            console.warn("[%s] %s", tag, message)
        } else {
            console.warn("[%s] %s\n%o", tag, message, throwable)
        }
    }

    override fun error(throwable: Throwable?, tag: String, message: String) {
        if (throwable == null) {
            console.error("[%s] %s", tag, message)
        } else {
            console.error("[%s] %s\n%o", tag, message, throwable)
        }
    }

    override fun assert(throwable: Throwable?, tag: String, message: String) {
        if (throwable == null) {
            console.asDynamic().assert(false, "[%s] %s", tag, message)
        } else {
            console.asDynamic().assert(false, "[%s] %s\n%o", tag, message, throwable)
        }
    }
}
