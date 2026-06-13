package com.juul.sensortag.bluetooth

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
public actual fun rememberSystemControl(): SystemControl {
    val context = LocalContext.current
    val activity = context as? Activity ?: error("$context is not an instance of Activity")
    return remember(activity) {
        AndroidSystemControl(activity)
    }
}
