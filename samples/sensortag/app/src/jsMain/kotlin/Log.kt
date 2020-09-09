package com.juul.sensortag

actual object Log {

    actual fun info(message: String) {
        console.info(message)
    }
}
