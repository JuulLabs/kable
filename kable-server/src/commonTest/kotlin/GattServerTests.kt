package com.juul.kable.server

import com.juul.kable.Bluetooth
import com.juul.kable.characteristicOf
import com.juul.kable.logs.Logging
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

private val serviceUuid = Bluetooth.BaseUuid + 0x1815
private val characteristicUuid = Bluetooth.BaseUuid + 0x2A56
private val otherCharacteristicUuid = Bluetooth.BaseUuid + 0x2A57

private val characteristicKey = AttributeKey.Characteristic(serviceUuid, characteristicUuid)
private val cccdKey = AttributeKey.Descriptor(serviceUuid, characteristicUuid, clientCharacteristicConfigUuid)

private val timeout = 10.seconds

/**
 * Awaits [block] using real (wall-clock) time: `runTest` uses virtual time (which skips ahead when
 * the test dispatcher is idle), whereas the server under test runs on [Dispatchers.Default].
 */
private suspend fun <T> await(block: suspend () -> T): T =
    withContext(Dispatchers.Default) {
        withTimeout(timeout) { block() }
    }

private sealed class Reply {
    data class Success(val value: ByteArray?) : Reply()
    data class Failure(val error: AttError) : Reply()
}

private fun readRequest(
    central: Central,
    attribute: AttributeKey,
    offset: Int = 0,
): Pair<InboundRequest.Read, CompletableDeferred<Reply>> {
    val reply = CompletableDeferred<Reply>()
    val request = InboundRequest.Read(
        central = central,
        attribute = attribute,
        offset = offset,
        respond = { value -> reply.complete(Reply.Success(value)) },
        fail = { error -> reply.complete(Reply.Failure(error)) },
    )
    return request to reply
}

private fun writeRequest(
    central: Central,
    attribute: AttributeKey,
    value: ByteArray,
    offset: Int = 0,
    prepared: Boolean = false,
    responseNeeded: Boolean = true,
): Pair<InboundRequest.Write, CompletableDeferred<Reply>> {
    val reply = CompletableDeferred<Reply>()
    val request = InboundRequest.Write(
        central = central,
        attribute = attribute,
        value = value,
        offset = offset,
        prepared = prepared,
        respond = if (responseNeeded) ({ reply.complete(Reply.Success(null)) }) else null,
        fail = if (responseNeeded) ({ error -> reply.complete(Reply.Failure(error)) }) else null,
    )
    return request to reply
}

private fun executeWrite(
    central: Central,
    commit: Boolean,
): Pair<InboundRequest.ExecuteWrite, CompletableDeferred<Reply>> {
    val reply = CompletableDeferred<Reply>()
    val request = InboundRequest.ExecuteWrite(
        central = central,
        commit = commit,
        respond = { reply.complete(Reply.Success(null)) },
        fail = { error -> reply.complete(Reply.Failure(error)) },
    )
    return request to reply
}

private fun server(
    engine: FakeServerEngine,
    builderAction: GattServerBuilder.() -> Unit,
): GattServerImpl {
    val builder = GattServerBuilder().apply(builderAction)
    return GattServerImpl(builder.build(), Logging(), engine)
}

class GattServerTests {

    @Test
    fun read_ofStaticValue_respondsWithValue() = runTest {
        val engine = FakeServerEngine()
        val server = server(engine) {
            service(serviceUuid) {
                characteristic(characteristicUuid) { value = byteArrayOf(1, 2, 3) }
            }
        }
        server.start()

        val (request, reply) = readRequest(FakeCentral(1), characteristicKey)
        engine.requests.send(request)
        val result = await { reply.await() }

        assertContentEquals(byteArrayOf(1, 2, 3), assertIs<Reply.Success>(result).value)
        server.stop()
    }

    @Test
    fun read_withOffset_respondsWithSlicedValue() = runTest {
        val engine = FakeServerEngine()
        val server = server(engine) {
            service(serviceUuid) {
                characteristic(characteristicUuid) {
                    onRead { byteArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9) }
                }
            }
        }
        server.start()

        val (request, reply) = readRequest(FakeCentral(1), characteristicKey, offset = 4)
        engine.requests.send(request)
        val result = await { reply.await() }
        assertContentEquals(byteArrayOf(4, 5, 6, 7, 8, 9), assertIs<Reply.Success>(result).value)

        val (invalid, invalidReply) = readRequest(FakeCentral(1), characteristicKey, offset = 20)
        engine.requests.send(invalid)
        val invalidResult = await { invalidReply.await() }
        assertEquals(AttError.InvalidOffset, assertIs<Reply.Failure>(invalidResult).error)

        server.stop()
    }

    @Test
    fun read_whenHandlerThrowsGattError_respondsWithError() = runTest {
        val engine = FakeServerEngine()
        val server = server(engine) {
            service(serviceUuid) {
                characteristic(characteristicUuid) {
                    onRead { throw GattErrorException(AttError.InsufficientAuthorization) }
                }
            }
        }
        server.start()

        val (request, reply) = readRequest(FakeCentral(1), characteristicKey)
        engine.requests.send(request)
        val result = await { reply.await() }

        assertEquals(AttError.InsufficientAuthorization, assertIs<Reply.Failure>(result).error)
        server.stop()
    }

    @Test
    fun write_invokesHandlerAndResponds() = runTest {
        val engine = FakeServerEngine()
        val written = CompletableDeferred<ByteArray>()
        val server = server(engine) {
            service(serviceUuid) {
                characteristic(characteristicUuid) {
                    onWrite { value -> written.complete(value) }
                }
            }
        }
        server.start()

        val (request, reply) = writeRequest(FakeCentral(1), characteristicKey, byteArrayOf(4, 5, 6))
        engine.requests.send(request)

        assertContentEquals(byteArrayOf(4, 5, 6), await { written.await() })
        assertIs<Reply.Success>(await { reply.await() })
        server.stop()
    }

    @Test
    fun writeWithoutResponse_invokesHandler() = runTest {
        val engine = FakeServerEngine()
        val written = CompletableDeferred<ByteArray>()
        val server = server(engine) {
            service(serviceUuid) {
                characteristic(characteristicUuid) {
                    onWrite { value -> written.complete(value) }
                }
            }
        }
        server.start()

        val (request, _) = writeRequest(FakeCentral(1), characteristicKey, byteArrayOf(7), responseNeeded = false)
        engine.requests.send(request)

        assertContentEquals(byteArrayOf(7), await { written.await() })
        server.stop()
    }

    @Test
    fun write_whenHandlerThrowsGattError_respondsWithError() = runTest {
        val engine = FakeServerEngine()
        val server = server(engine) {
            service(serviceUuid) {
                characteristic(characteristicUuid) {
                    onWrite { throw GattErrorException(AttError.InvalidAttributeValueLength) }
                }
            }
        }
        server.start()

        val (request, reply) = writeRequest(FakeCentral(1), characteristicKey, byteArrayOf(1))
        engine.requests.send(request)
        val result = await { reply.await() }

        assertEquals(AttError.InvalidAttributeValueLength, assertIs<Reply.Failure>(result).error)
        server.stop()
    }

    @Test
    fun preparedWrites_areAssembledOnCommit() = runTest {
        val engine = FakeServerEngine()
        val written = CompletableDeferred<ByteArray>()
        val server = server(engine) {
            service(serviceUuid) {
                characteristic(characteristicUuid) {
                    onWrite { value -> written.complete(value) }
                }
            }
        }
        server.start()
        val central = FakeCentral(1)

        val fragment1 = "Hello ".encodeToByteArray()
        val fragment2 = "World".encodeToByteArray()
        val (write1, reply1) = writeRequest(central, characteristicKey, fragment1, offset = 0, prepared = true)
        val (write2, reply2) = writeRequest(central, characteristicKey, fragment2, offset = fragment1.size, prepared = true)
        engine.requests.send(write1)
        engine.requests.send(write2)
        assertIs<Reply.Success>(await { reply1.await() })
        assertIs<Reply.Success>(await { reply2.await() })
        assertFalse(written.isCompleted, "Write handler should not be invoked until committed")

        val (execute, executeReply) = executeWrite(central, commit = true)
        engine.requests.send(execute)

        assertContentEquals("Hello World".encodeToByteArray(), await { written.await() })
        assertIs<Reply.Success>(await { executeReply.await() })
        server.stop()
    }

    @Test
    fun preparedWrites_whenAborted_doNotInvokeHandler() = runTest {
        val engine = FakeServerEngine()
        val written = CompletableDeferred<ByteArray>()
        val server = server(engine) {
            service(serviceUuid) {
                characteristic(characteristicUuid) {
                    onWrite { value -> written.complete(value) }
                }
            }
        }
        server.start()
        val central = FakeCentral(1)

        val (write, writeReply) = writeRequest(central, characteristicKey, byteArrayOf(1), prepared = true)
        engine.requests.send(write)
        assertIs<Reply.Success>(await { writeReply.await() })

        val (abort, abortReply) = executeWrite(central, commit = false)
        engine.requests.send(abort)

        assertIs<Reply.Success>(await { abortReply.await() })
        assertFalse(written.isCompleted, "Write handler should not be invoked for aborted writes")
        server.stop()
    }

    @Test
    fun cccdWrite_managesSubscriptionLifecycle() = runTest {
        val engine = FakeServerEngine()
        val server = server(engine) {
            service(serviceUuid) {
                characteristic(characteristicUuid) {
                    onSubscription {
                        send(byteArrayOf(9))
                        awaitCancellation()
                    }
                }
            }
        }
        server.start()
        val central = FakeCentral(1)
        val characteristic = characteristicOf(serviceUuid, characteristicUuid)

        // Subscribe (via CCCD write).
        val (enable, enableReply) = writeRequest(central, cccdKey, byteArrayOf(0x01, 0x00))
        engine.requests.send(enable)
        assertIs<Reply.Success>(await { enableReply.await() })

        // `onSubscription` action runs (sending an initial notification).
        val notification = await { engine.notifications.receive() }
        assertContentEquals(byteArrayOf(9), notification.value)
        assertEquals(central.identifier, notification.central.identifier)

        // Subscribers (and centrals) are tracked.
        await { server.subscribers(characteristic).first { it.isNotEmpty() } }
        await { server.centrals.first { it.isNotEmpty() } }

        // CCCD read reflects subscription state.
        val (read, readReply) = readRequest(central, cccdKey)
        engine.requests.send(read)
        assertContentEquals(byteArrayOf(0x01, 0x00), assertIs<Reply.Success>(await { readReply.await() }).value)

        // Unsubscribe (via CCCD write).
        val (disable, disableReply) = writeRequest(central, cccdKey, byteArrayOf(0x00, 0x00))
        engine.requests.send(disable)
        assertIs<Reply.Success>(await { disableReply.await() })
        await { server.subscribers(characteristic).first { it.isEmpty() } }

        server.stop()
    }

    @Test
    fun cccdWrite_forUnsupportedMode_respondsWithError() = runTest {
        val engine = FakeServerEngine()
        val server = server(engine) {
            service(serviceUuid) {
                characteristic(characteristicUuid) {
                    onSubscription { awaitCancellation() } // Notifications (not indications).
                }
            }
        }
        server.start()

        val (enableIndications, reply) = writeRequest(FakeCentral(1), cccdKey, byteArrayOf(0x02, 0x00))
        engine.requests.send(enableIndications)
        val result = await { reply.await() }

        assertEquals(AttError.RequestNotSupported, assertIs<Reply.Failure>(result).error)
        server.stop()
    }

    @Test
    fun subscribeEvents_manageSubscriptionLifecycle() = runTest {
        val engine = FakeServerEngine()
        val server = server(engine) {
            service(serviceUuid) {
                characteristic(characteristicUuid) {
                    onSubscription { awaitCancellation() }
                }
            }
        }
        server.start()
        val central = FakeCentral(1)
        val characteristic = characteristicOf(serviceUuid, characteristicUuid)

        engine.requests.send(InboundRequest.Subscribe(central, characteristicKey))
        await { server.subscribers(characteristic).first { it.isNotEmpty() } }

        server.notify(characteristic, byteArrayOf(42))
        val notification = await { engine.notifications.receive() }
        assertContentEquals(byteArrayOf(42), notification.value)

        engine.requests.send(InboundRequest.Unsubscribe(central, characteristicKey))
        await { server.subscribers(characteristic).first { it.isEmpty() } }

        server.stop()
    }

    @Test
    fun centralDisconnected_cancelsSubscriptions() = runTest {
        val engine = FakeServerEngine()
        val cancelled = CompletableDeferred<Unit>()
        val server = server(engine) {
            service(serviceUuid) {
                characteristic(characteristicUuid) {
                    onSubscription {
                        try {
                            awaitCancellation()
                        } finally {
                            cancelled.complete(Unit)
                        }
                    }
                }
            }
        }
        server.start()
        val central = FakeCentral(1)
        val characteristic = characteristicOf(serviceUuid, characteristicUuid)

        engine.requests.send(InboundRequest.CentralConnected(central))
        engine.requests.send(InboundRequest.Subscribe(central, characteristicKey))
        await { server.subscribers(characteristic).first { it.isNotEmpty() } }

        engine.requests.send(InboundRequest.CentralDisconnected(central))
        await { cancelled.await() }
        await { server.subscribers(characteristic).first { it.isEmpty() } }
        await { server.centrals.first { it.isEmpty() } }

        server.stop()
    }

    @Test
    fun notify_forUnknownCharacteristic_throws() = runTest {
        val engine = FakeServerEngine()
        val server = server(engine) {
            service(serviceUuid) {
                characteristic(characteristicUuid) { onSubscription { awaitCancellation() } }
            }
        }
        server.start()

        assertFailsWith<NoSuchElementException> {
            server.notify(characteristicOf(serviceUuid, otherCharacteristicUuid), byteArrayOf(1))
        }
        server.stop()
    }

    @Test
    fun notify_forCharacteristicWithoutSubscriptionSupport_throws() = runTest {
        val engine = FakeServerEngine()
        val server = server(engine) {
            service(serviceUuid) {
                characteristic(characteristicUuid) { value = byteArrayOf(1) }
            }
        }
        server.start()

        assertFailsWith<IllegalArgumentException> {
            server.notify(characteristicOf(serviceUuid, characteristicUuid), byteArrayOf(1))
        }
        server.stop()
    }

    @Test
    fun stateTransitions_acrossStartAndStop() = runTest {
        val engine = FakeServerEngine()
        val server = server(engine) {
            service(serviceUuid) {
                characteristic(characteristicUuid) { value = byteArrayOf(1) }
            }
        }
        assertIs<GattServer.State.Stopped>(server.state.value)

        server.start()
        assertEquals(GattServer.State.Started, server.state.value)
        assertTrue(engine.isOpen.value)

        server.stop()
        assertIs<GattServer.State.Stopped>(server.state.value)
        assertFalse(engine.isOpen.value)

        // Server can be restarted after being stopped.
        server.start()
        assertEquals(GattServer.State.Started, server.state.value)
        assertEquals(2, engine.openCount)

        server.stop()
    }

    @Test
    fun start_afterClose_throws() = runTest {
        val engine = FakeServerEngine()
        val server = server(engine) {
            service(serviceUuid) {
                characteristic(characteristicUuid) { value = byteArrayOf(1) }
            }
        }
        server.close()

        assertFailsWith<IllegalStateException> { server.start() }
    }

    @Test
    fun advertise_isActiveWhileSuspended() = runTest {
        val engine = FakeServerEngine()
        val server = server(engine) {
            service(serviceUuid) {
                characteristic(characteristicUuid) { value = byteArrayOf(1) }
            }
        }
        server.start()

        val advertising = launch {
            server.advertise {
                services = listOf(serviceUuid)
            }
        }
        await { engine.advertising.first { it != null } }

        // Cancelling the caller stops advertising.
        advertising.cancel()
        await { engine.advertising.first { it == null } }

        server.stop()
    }

    @Test
    fun advertise_whenServerIsStopped_returnsNormally() = runTest {
        val engine = FakeServerEngine()
        val server = server(engine) {
            service(serviceUuid) {
                characteristic(characteristicUuid) { value = byteArrayOf(1) }
            }
        }
        server.start()

        val advertising = launch { server.advertise() }
        await { engine.advertising.first { it != null } }

        server.stop()

        // `advertise` returns normally (rather than throwing) when server is stopped.
        await { advertising.join() }
        assertFalse(advertising.isCancelled)
    }

    @Test
    fun advertise_whenNotStarted_throws() = runTest {
        val engine = FakeServerEngine()
        val server = server(engine) {
            service(serviceUuid) {
                characteristic(characteristicUuid) { value = byteArrayOf(1) }
            }
        }

        assertFailsWith<IllegalStateException> { server.advertise() }
    }
}
