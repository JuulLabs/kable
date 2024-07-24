package com.juul.kable.logs

public expect object SystemLogEngine : LogEngine {
    override fun verbose(throwable: Throwable?, tag: String, message: String)
    override fun debug(throwable: Throwable?, tag: String, message: String)
    override fun info(throwable: Throwable?, tag: String, message: String)
    override fun warn(throwable: Throwable?, tag: String, message: String)
    override fun error(throwable: Throwable?, tag: String, message: String)
    override fun assert(throwable: Throwable?, tag: String, message: String)
}
