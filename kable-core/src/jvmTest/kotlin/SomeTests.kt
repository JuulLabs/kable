import com.juul.kable.btleplug.ffi3.helloWorld
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SomeTests {

    @Test
    fun test() = runTest {
        assertEquals("Hello, world!", helloWorld())
    }
}
