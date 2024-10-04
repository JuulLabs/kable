import android.bluetooth.BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION
import android.bluetooth.BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION
import android.bluetooth.BluetoothGatt.GATT_SUCCESS
import com.juul.kable.external.GATT_AUTH_FAIL

import com.juul.kable.BondRequiredException
import com.juul.kable.Peripheral
import com.juul.kable.Scanner
import com.juul.kable.checkResponse
import com.juul.kable.gatt.GattStatus
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFailsWith

class GattStatusTest {

    @Test
    fun checkSuccess() {
        checkResponse(
            GattStatus(GATT_SUCCESS),
        )
    }


    @Test
    fun checkAuthorizationError() {
        assertFailsWith<BondRequiredException> {
            checkResponse(
                GattStatus(GATT_AUTH_FAIL),
            )
        }

        assertFailsWith<BondRequiredException> {
            checkResponse(
                GattStatus(GATT_INSUFFICIENT_AUTHENTICATION),
            )
        }

        assertFailsWith<BondRequiredException> {
            checkResponse(
                GattStatus(GATT_INSUFFICIENT_ENCRYPTION),
            )
        }
    }

    @Test
    fun bonding() = runTest {
        val advertisement = Scanner {}.advertisements.onEach {
            println(it)
        }.filter {
            it.address == ""
        }.first()
        val peripheral = Peripheral(advertisement) {}
        peripheral.connect()
    }


}
