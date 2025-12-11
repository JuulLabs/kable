package com.juul.kable.logs.khronicle

import com.juul.kable.logs.LogEngine
import com.juul.khronicle.Key
import com.juul.khronicle.Log

public object Kable : Key<Boolean>

public object KhronicleLogEngine : LogEngine {
    override fun verbose(throwable: Throwable?, tag: String, message: String) {
        Log.verbose(tag, throwable) { metadata ->
            metadata[Kable] = true
            message
        }
    }

    override fun debug(throwable: Throwable?, tag: String, message: String) {
        Log.debug(tag, throwable) { metadata ->
            metadata[Kable] = true
            message
        }
    }

    override fun info(throwable: Throwable?, tag: String, message: String) {
        Log.info(tag, throwable) { metadata ->
            metadata[Kable] = true
            message
        }
    }

    override fun warn(throwable: Throwable?, tag: String, message: String) {
        Log.warn(tag, throwable) { metadata ->
            metadata[Kable] = true
            message
        }
    }

    override fun error(throwable: Throwable?, tag: String, message: String) {
        Log.error(tag, throwable) { metadata ->
            metadata[Kable] = true
            message
        }
    }

    override fun assert(throwable: Throwable?, tag: String, message: String) {
        Log.assert(tag, throwable) { metadata ->
            metadata[Kable] = true
            message
        }
    }
}
