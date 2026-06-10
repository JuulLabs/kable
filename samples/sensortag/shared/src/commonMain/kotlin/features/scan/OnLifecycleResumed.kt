package com.juul.sensortag.features.scan

import androidx.compose.runtime.Composable

@Composable
expect fun onLifecycleResumed(onResumed: () -> Unit)
