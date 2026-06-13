import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.juul.sensortag.App
import org.jetbrains.skiko.wasm.onWasmReady

@OptIn(ExperimentalComposeUiApi::class)
actual fun startComposeApp() {
    onWasmReady {
        ComposeViewport("ComposeTarget") {
            App()
        }
    }
}
