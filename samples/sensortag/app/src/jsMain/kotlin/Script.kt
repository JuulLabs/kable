package com.juul.sensortag

import com.juul.kable.Options
import com.juul.kable.Options.Filter.NamePrefix
import com.juul.kable.State.Disconnected
import com.juul.kable.requestPeripheral
import com.juul.tuulbox.logging.ConsoleLogger
import com.juul.tuulbox.logging.ConstantTagGenerator
import com.juul.tuulbox.logging.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.await
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

typealias MessageListener = (message: String) -> Unit
typealias MovementListener = (x: Float, y: Float, z: Float) -> Unit

private fun canonicalUuid(uuid: String): String = when (uuid.length) {
    4 -> "0000$uuid-0000-1000-8000-00805f9b34fb"
    else -> error("Canonical UUID length must be 4, was ${uuid.length}")
}

private const val movementSensorServiceUuid = "f000aa80-0451-4000-b000-000000000000"
private const val movementSensorDataUuid = "f000aa81-0451-4000-b000-000000000000"
private const val movementNotificationUuid = "f0002902-0451-4000-b000-000000000000"
private const val movementConfigurationUuid = "f000aa82-0451-4000-b000-000000000000"
private const val movementPeriodUuid = "f000aa83-0451-4000-b000-000000000000"
private val clientCharacteristicConfigUuid = canonicalUuid("2902")

class Script {

    init {
        Log.tagGenerator = ConstantTagGenerator(tag = "SensorTag")
        Log.dispatcher.install(ConsoleLogger)
    }

    private val scope = CoroutineScope(Job())

    private val options = Options(
        optionalServices = arrayOf(
            movementSensorServiceUuid,
            movementSensorDataUuid,
            movementNotificationUuid,
            movementConfigurationUuid,
            movementPeriodUuid,
            clientCharacteristicConfigUuid,
        ),
        filters = arrayOf(
            NamePrefix("SensorTag"),
            NamePrefix("CC2650 SensorTag"),
        )
    )

    private val statusListeners = mutableListOf<MessageListener>()

    @JsName("addStatusListener")
    fun addStatusListener(listener: MessageListener) {
        statusListeners += listener
    }

    @JsName("removeStatusListener")
    fun removeStatusListener(listener: MessageListener) {
        statusListeners -= listener
    }

    private fun emitStatus(status: String) {
        Log.verbose { status }
        statusListeners.forEach { it.invoke(status) }
    }

    private val movementListeners = mutableListOf<MovementListener>()

    @JsName("addMovementListener")
    fun addMovementListener(listener: MovementListener) {
        movementListeners += listener
    }

    @JsName("removeMovementListener")
    fun removeMovementListener(listener: MovementListener) {
        movementListeners -= listener
    }

    private fun emitMovement(movement: Vector3f) {
        val (x, y, z) = movement
        movementListeners.forEach { it.invoke(x, y, z) }
    }

    private var connection: Job? = null

    @JsName("connect")
    fun connect(): Unit {
        disconnect() // Clean up previous connection, if any.

        connection = scope.launch {
            val sensorTag = SensorTag(requestPeripheral(options).await())
            sensorTag.establishConnection()
            enableAutoReconnect(sensorTag)

            try {
                sensorTag.gyro.collect(::emitMovement)
            } finally {
                sensorTag.disconnect()
            }
        }.apply {
            invokeOnCompletion { cause ->
                Log.info { "invokeOnCompletion $cause" }
                emitStatus("Disconnected")
            }
        }
    }

    @JsName("disconnect")
    fun disconnect() {
        connection?.cancel()
        connection = null
    }

    private suspend fun SensorTag.establishConnection(): Unit = coroutineScope {
        emitStatus("Connecting")
        connect()
        enableGyro()
        emitStatus("Connected")
    }

    private fun CoroutineScope.enableAutoReconnect(
        sensorTag: SensorTag
    ) = sensorTag.state.onEach { state ->
        Log.info { "State: ${state::class.simpleName}" }
        if (state is Disconnected) {
            Log.info { "Waiting 5 seconds to reconnect..." }
            delay(5_000L)
            sensorTag.establishConnection()
        }
    }.launchIn(this)
}

