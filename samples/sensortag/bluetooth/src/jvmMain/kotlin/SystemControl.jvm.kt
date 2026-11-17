package com.juul.sensortag.bluetooth

import androidx.compose.runtime.Composable

@Composable
public actual fun rememberSystemControl(): SystemControl = NopSystemControl
