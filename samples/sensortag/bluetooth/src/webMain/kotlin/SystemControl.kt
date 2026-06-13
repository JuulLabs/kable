package com.juul.sensortag.bluetooth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
public actual fun rememberSystemControl(): SystemControl =
    remember { NopSystemControl }
