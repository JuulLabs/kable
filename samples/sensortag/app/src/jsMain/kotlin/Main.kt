import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.juul.sensortag.App
import com.juul.sensortag.configureLogging
import org.jetbrains.skiko.wasm.onWasmReady

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    configureLogging()
    onWasmReady {
        CanvasBasedWindow("SensorTag") {
            App()
        }
    }
}
