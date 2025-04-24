import com.juul.kable.Scanner
import kotlin.test.Test
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.runTest

class BtleplugTests {

    @Test
    fun test() = runTest {
        Scanner().advertisements
            .runningFold(setOf<String>()) { acc, item -> acc + item.name!! }
            .filter { it.isNotEmpty() }
            .distinctUntilChanged()
            .take(3)
            .collect {
                println(it.joinToString())
            }
    }
}
