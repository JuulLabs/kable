package com.juul.sensortag

import platform.Foundation.NSLog

actual object Log {

    actual fun info(message: String) {
        NSLog(message)
    }
}
