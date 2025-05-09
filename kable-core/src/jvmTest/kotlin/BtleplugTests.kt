import com.juul.kable.Bluetooth
import com.juul.kable.Peripheral
import com.juul.kable.Scanner
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

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
