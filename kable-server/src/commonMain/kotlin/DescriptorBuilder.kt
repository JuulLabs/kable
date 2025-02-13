package com.juul.kable.server

import kotlin.uuid.Uuid

public class DescriptorBuilder(
    private val uuid: Uuid,
) {

    private var onRead: (suspend ReadAction.() -> Unit)? = null
    private var onWrite: (suspend (value: ByteArray) -> Unit)? = null

    public fun onRead(
        action: suspend ReadAction.() -> Unit,
    ) {
        TODO()
    }

    public fun onWrite(
        action: suspend (value: ByteArray) -> Unit,
    ) {
        require(onWrite == null) { "onWrite" }
        onWrite = action
    }
}
