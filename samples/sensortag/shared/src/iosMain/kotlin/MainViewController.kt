package com.juul.sensortag

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    configureLogging()
    return ComposeUIViewController {
        App()
    }
}
