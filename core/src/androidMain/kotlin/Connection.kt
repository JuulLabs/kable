package com.juul.kable

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGatt.GATT_SUCCESS
import android.bluetooth.BluetoothProfile.STATE_CONNECTED
import android.bluetooth.BluetoothProfile.STATE_DISCONNECTED
import android.os.RemoteException
import com.juul.kable.gatt.Callback
import com.juul.kable.gatt.ConnectionLostException
import com.juul.kable.gatt.ConnectionState
import com.juul.kable.gatt.ConnectionStatus
import com.juul.kable.gatt.GattStatusException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

public class OutOfOrderGattCallbackException internal constructor(
    message: String
) : IllegalStateException(message)

private val Success = ConnectionStatus(GATT_SUCCESS)
private val Disconnected = ConnectionState(STATE_DISCONNECTED)
private val Connected = ConnectionState(STATE_CONNECTED)

internal class Connection(
    private val bluetoothGatt: BluetoothGatt,
    private val dispatcher: CoroutineDispatcher,
    val callback: Callback,
) {

    private val lock = Mutex()

    suspend inline fun <reified T> request(
        crossinline action: BluetoothGatt.() -> Boolean
    ): T = lock.withLock {
        withContext(dispatcher) {
            // todo: Exception type (other than RemoteException)?
            action.invoke(bluetoothGatt) || throw RemoteException("BluetoothGatt method call returned false")
        }

        val response = try {
            callback.onResponse.receive()
        } catch (e: CancellationException) {
            throw CancellationException("Waiting for response was cancelled", e)
        } catch (e: ConnectionLostException) {
            throw ConnectionLostException(cause = e)
        }

        // `lock` should always enforce a 1:1 matching of request to response, but if an Android `BluetoothGattCallback`
        // method gets called out of order then we'll cast to the wrong response type.
        response as? T
            ?: throw OutOfOrderGattCallbackException(
                "Unexpected response type ${response.javaClass.simpleName} received"
            )
    }

    suspend fun suspendUntilConnected() {
        callback.onConnectionStateChange
            .onEach { event ->
                val (status, newState) = event
                if (status != Success) throw GattStatusException(event.toString())
                if (newState == Disconnected) throw ConnectionLostException()
            }
            .first { (_, newState) -> newState == Connected }
    }

    suspend fun suspendUntilDisconnected() {
        callback.onConnectionStateChange
            .onEach { event ->
                val (status, newState) = event
                if (status != Success && newState != Disconnected)
                    throw GattStatusException(event.toString())
            }
            .first { (_, newState) -> newState == Disconnected }
    }
}
