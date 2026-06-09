import androidx.compose.ui.ExperimentalComposeUiApi
import com.juul.sensortag.App
import com.juul.sensortag.configureLogging

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    configureLogging()
    startComposeApp()
}

expect fun startComposeApp()
