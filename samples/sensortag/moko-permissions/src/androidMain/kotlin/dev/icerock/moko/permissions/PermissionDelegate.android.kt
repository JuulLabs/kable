/*
 * Copyright 2025 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

@file:Suppress("MatchingDeclarationName")

package dev.icerock.moko.permissions

import android.content.Context

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual interface PermissionDelegate {
    fun getPermissionStateOverride(applicationContext: Context): PermissionState?
    fun getPlatformPermission(): List<String>
}
