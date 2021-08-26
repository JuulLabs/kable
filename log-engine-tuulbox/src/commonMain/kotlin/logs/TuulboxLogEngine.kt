package com.juul.kable.logs

import com.juul.tuulbox.logging.HideFromStackTraceTag
import com.juul.tuulbox.logging.Key
import com.juul.tuulbox.logging.Log

public object Kable : Key<Boolean>

public object TuulboxLogEngine : LogEngine, HideFromStackTraceTag {
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
