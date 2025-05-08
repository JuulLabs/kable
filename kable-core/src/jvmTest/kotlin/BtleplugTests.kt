import com.juul.kable.Scanner
import kotlin.test.Test
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest

class BtleplugTests {

    @Test
    fun test() = runTest {
        val advertisements = Scanner().advertisements
            .take(5)
            .toList()
        for (advertisement in advertisements) {
            println(advertisement)
        }
    }
}
