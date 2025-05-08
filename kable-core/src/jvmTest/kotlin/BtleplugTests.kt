import com.juul.kable.Filter
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
        val scanner = Scanner {
            filters {
                // Empty prefix used as a proxy for non-null name.
                match { name = Filter.Name.Prefix("") }
            }
        }
        val advertisements = scanner.advertisements
            .take(3)
            .toList()
        for (advertisement in advertisements) {
            println(advertisement)
        }
    }
}
