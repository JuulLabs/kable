package com.juul.kable.logs

public interface LogEngine {
    public fun verbose(throwable: Throwable?, tag: String, message: String)
    public fun debug(throwable: Throwable?, tag: String, message: String)
    public fun info(throwable: Throwable?, tag: String, message: String)
    public fun warn(throwable: Throwable?, tag: String, message: String)
    public fun error(throwable: Throwable?, tag: String, message: String)
    public fun assert(throwable: Throwable?, tag: String, message: String)
}
