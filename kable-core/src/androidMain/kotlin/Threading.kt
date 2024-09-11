package com.juul.kable

import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.android.asCoroutineDispatcher
import kotlinx.coroutines.newSingleThreadContext

public sealed class Threading {

    internal abstract val dispatcher: CoroutineDispatcher
    internal abstract val strategy: ThreadingStrategy

    /** Used on Android O (API 26) and above. */
    internal data class Handler(
        val thread: HandlerThread,
        val handler: android.os.Handler,
        override val dispatcher: CoroutineDispatcher,
        override val strategy: ThreadingStrategy,
    ) : Threading()

    /** Used on Android versions **lower** than Android O (API 26). */
    internal data class SingleThreadContext(
        val name: String,
        override val dispatcher: ExecutorCoroutineDispatcher,
        override val strategy: ThreadingStrategy,
    ) : Threading()
}

internal fun Threading.release() {
    strategy.release(this)
}

public val Threading.name: String
    get() = when (this) {
        is Threading.Handler -> thread.name
        is Threading.SingleThreadContext -> name
    }

public fun Threading.shutdown() {
    when (this) {
        is Threading.Handler -> thread.quit()
        is Threading.SingleThreadContext -> dispatcher.close()
    }
}

/**
 * Creates [Threading] that can be used for Bluetooth communication. The returned [Threading] is
 * returned in a started state and must be [shutdown] when no longer needed.
 */
public fun ThreadingStrategy.Threading(name: String): Threading =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val thread = HandlerThread(name).apply { start() }
        val handler = Handler(thread.looper)
        val dispatcher = handler.asCoroutineDispatcher()
        Threading.Handler(thread, handler, dispatcher, this)
    } else { // Build.VERSION.SDK_INT < Build.VERSION_CODES.O
        @OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
        Threading.SingleThreadContext(name, newSingleThreadContext(name), this)
    }
