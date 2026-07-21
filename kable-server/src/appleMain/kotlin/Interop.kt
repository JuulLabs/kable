@file:OptIn(ExperimentalForeignApi::class)

package com.juul.kable.server

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Runnable
import platform.CoreBluetooth.CBUUID
import platform.Foundation.NSData
import platform.Foundation.create
import platform.darwin.dispatch_async
import platform.darwin.dispatch_queue_create
import platform.darwin.dispatch_queue_t
import platform.posix.memcpy
import kotlin.coroutines.CoroutineContext
import kotlin.uuid.Uuid

// https://stackoverflow.com/a/58521109
internal fun NSData.toByteArray(): ByteArray = ByteArray(length.toInt()).apply {
    if (length > 0u) {
        usePinned {
            memcpy(it.addressOf(0), bytes, length)
        }
    }
}

// https://stackoverflow.com/a/58521109
internal fun ByteArray.toNSData(): NSData = memScoped {
    NSData.create(
        bytes = allocArrayOf(this@toNSData),
        length = size.convert(),
    )
}

internal fun Uuid.toCBUUID(): CBUUID = CBUUID.UUIDWithString(toString())

internal fun CBUUID.toUuid(): Uuid = UUIDString
    .lowercase()
    .let {
        when (it.length) {
            4 -> Uuid.parse("0000$it-0000-1000-8000-00805f9b34fb")
            8 -> Uuid.parse("$it-0000-1000-8000-00805f9b34fb")
            else -> Uuid.parse(it)
        }
    }

internal class QueueDispatcher(
    label: String,
) : CoroutineDispatcher() {

    val dispatchQueue: dispatch_queue_t = dispatch_queue_create(label, attr = null)

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        dispatch_async(dispatchQueue) {
            block.run()
        }
    }
}
