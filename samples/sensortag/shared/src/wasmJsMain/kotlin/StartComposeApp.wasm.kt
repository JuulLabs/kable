import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.juul.sensortag.App

@OptIn(ExperimentalComposeUiApi::class)
actual fun startComposeApp() {
    ComposeViewport {
        App()
    }
}
