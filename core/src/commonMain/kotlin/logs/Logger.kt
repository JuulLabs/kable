package com.juul.kable.logs

import com.juul.kable.logs.Logging.Level.Data
import com.juul.kable.logs.Logging.Level.Events

internal class Logger(
    private val logging: Logging,
    private val tag: String = "Kable",
    private val identifier: String?,
) {

    inline fun verbose(throwable: Throwable? = null, crossinline init: LogMessage.() -> Unit) {
        if (logging.level == Events || logging.level == Data) {
            val message = LogMessage(logging, identifier)
            message.init()
            logging.engine.verbose(throwable, tag, message.build())
        }
    }

    inline fun debug(throwable: Throwable? = null, crossinline init: LogMessage.() -> Unit) {
        if (logging.level == Events || logging.level == Data) {
            val message = LogMessage(logging, identifier)
            message.init()
            logging.engine.debug(throwable, tag, message.build())
        }
    }

    inline fun info(throwable: Throwable? = null, crossinline init: LogMessage.() -> Unit) {
        if (logging.level == Events || logging.level == Data) {
            val message = LogMessage(logging, identifier)
            message.init()
            logging.engine.info(throwable, tag, message.build())
        }
    }

    inline fun warn(throwable: Throwable? = null, crossinline init: LogMessage.() -> Unit) {
        val message = LogMessage(logging, identifier)
        message.init()
        logging.engine.warn(throwable, tag, message.build())
    }

    inline fun error(throwable: Throwable? = null, crossinline init: LogMessage.() -> Unit) {
        val message = LogMessage(logging, identifier)
        message.init()
        logging.engine.error(throwable, tag, message.build())
    }

    inline fun assert(throwable: Throwable? = null, crossinline init: LogMessage.() -> Unit) {
        val message = LogMessage(logging, identifier)
        message.init()
        logging.engine.assert(throwable, tag, message.build())
    }
}
