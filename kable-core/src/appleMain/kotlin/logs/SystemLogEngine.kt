package com.juul.kable.logs

import platform.Foundation.NSLog

public actual object SystemLogEngine : LogEngine {
    actual override fun verbose(throwable: Throwable?, tag: String, message: String) {
        log("V", tag, message, throwable)
    }

    actual override fun debug(throwable: Throwable?, tag: String, message: String) {
        log("D", tag, message, throwable)
    }

    actual override fun info(throwable: Throwable?, tag: String, message: String) {
        log("I", tag, message, throwable)
    }

    actual override fun warn(throwable: Throwable?, tag: String, message: String) {
        log("W", tag, message, throwable)
    }

    actual override fun error(throwable: Throwable?, tag: String, message: String) {
        log("E", tag, message, throwable)
    }

    actual override fun assert(throwable: Throwable?, tag: String, message: String) {
        log("A", tag, message, throwable)
    }

    private fun log(level: String, tag: String, message: String, throwable: Throwable?) {
        if (throwable == null) {
            NSLog("%s/%s: %s", level, tag, message)
        } else {
            NSLog("%s/%s: %s\n%s", level, tag, message, throwable.stackTraceToString())
        }
    }
}
