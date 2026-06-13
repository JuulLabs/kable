package com.juul.sensortag

import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    configureLogging()
    headlessApp()
}
