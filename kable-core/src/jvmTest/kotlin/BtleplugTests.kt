import com.juul.kable.Bluetooth
import com.juul.kable.Filter
import com.juul.kable.Peripheral
import com.juul.kable.Scanner
import kotlin.test.Test
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest

// Sensortag!
const val movementService16bitUuid = 0xAA80
private val AdvertisedServices = listOf(Bluetooth.BaseUuid + movementService16bitUuid)

class BtleplugTests {

    @Test
    fun test() = runTest {
        val scanner = Scanner {
            filters {
                match { services = AdvertisedServices }
            }
        }
        val advertisement = scanner.advertisements.first()
        println("Found: $advertisement")
        val peripheral = Peripheral(advertisement)
        println("Connecting")
        peripheral.connect()
        println("Connected")
    }
}
