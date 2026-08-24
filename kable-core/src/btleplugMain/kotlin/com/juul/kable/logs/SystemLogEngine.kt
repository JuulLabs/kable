package com.juul.kable.logs

public actual object SystemLogEngine : LogEngine {

    actual override fun verbose(throwable: Throwable?, tag: String, message: String) {
        println("[V/$tag]: $message")
        throwable?.printStackTrace()
    }

    actual override fun debug(throwable: Throwable?, tag: String, message: String) {
        println("[D/$tag]: $message")
        throwable?.printStackTrace()
    }

    actual override fun info(throwable: Throwable?, tag: String, message: String) {
        println("[I/$tag]: $message")
        throwable?.printStackTrace()
    }

    actual override fun warn(throwable: Throwable?, tag: String, message: String) {
        println("[W/$tag]: $message")
        throwable?.printStackTrace()
    }

    actual override fun error(throwable: Throwable?, tag: String, message: String) {
        println("[E/$tag]: $message")
        throwable?.printStackTrace()
    }

    actual override fun assert(throwable: Throwable?, tag: String, message: String) {
        println("[A/$tag]: $message")
        throwable?.printStackTrace()
    }
}
