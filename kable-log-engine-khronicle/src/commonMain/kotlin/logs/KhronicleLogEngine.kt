package com.juul.kable.logs.khronicle

import com.juul.kable.logs.LogEngine
import com.juul.khronicle.HideFromStackTraceTag
import com.juul.khronicle.Key
import com.juul.khronicle.Log

public object Kable : Key<Boolean>

public object KhronicleLogEngine : LogEngine {
    override fun verbose(throwable: Throwable?, tag: String, message: String) {
        Log.verbose(throwable, tag) { metadata ->
            metadata[Kable] = true
            message
        }
    }

    override fun debug(throwable: Throwable?, tag: String, message: String) {
        Log.debug(throwable, tag) { metadata ->
            metadata[Kable] = true
            message
        }
    }

    override fun info(throwable: Throwable?, tag: String, message: String) {
        Log.info(throwable, tag) { metadata ->
            metadata[Kable] = true
            message
        }
    }

    override fun warn(throwable: Throwable?, tag: String, message: String) {
        Log.warn(throwable, tag) { metadata ->
            metadata[Kable] = true
            message
        }
    }

    override fun error(throwable: Throwable?, tag: String, message: String) {
        Log.error(throwable, tag) { metadata ->
            metadata[Kable] = true
            message
        }
    }

    override fun assert(throwable: Throwable?, tag: String, message: String) {
        Log.assert(throwable, tag) { metadata ->
            metadata[Kable] = true
            message
        }
    }
}
