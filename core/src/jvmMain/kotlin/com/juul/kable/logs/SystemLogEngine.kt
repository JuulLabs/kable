package com.juul.kable.logs

import com.juul.kable.jvmNotImplementedException

public actual object SystemLogEngine : LogEngine {

    override fun verbose(throwable: Throwable?, tag: String, message: String) {
        debug(throwable, tag, message)
    }

    override fun debug(throwable: Throwable?, tag: String, message: String) {
        jvmNotImplementedException()
    }

    override fun info(throwable: Throwable?, tag: String, message: String) {
        jvmNotImplementedException()
    }

    override fun warn(throwable: Throwable?, tag: String, message: String) {
        jvmNotImplementedException()
    }

    override fun error(throwable: Throwable?, tag: String, message: String) {
        jvmNotImplementedException()
    }

    override fun assert(throwable: Throwable?, tag: String, message: String) {
        jvmNotImplementedException()
    }
}
