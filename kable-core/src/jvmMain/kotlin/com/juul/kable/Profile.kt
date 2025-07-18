@file:JvmName("JvmProfileKt")

package com.juul.kable

import com.juul.kable.btleplug.BtleplugCharacteristic
import com.juul.kable.btleplug.BtleplugDescriptor
import com.juul.kable.btleplug.BtleplugService
import com.juul.kable.btleplug.ffi.Characteristic as FfiCharacteristic
import com.juul.kable.btleplug.ffi.Descriptor as FfiDescriptor
import com.juul.kable.btleplug.ffi.Service as FfiService

internal actual typealias PlatformService = FfiService
internal actual typealias PlatformCharacteristic = FfiCharacteristic
internal actual typealias PlatformDescriptor = FfiDescriptor

internal actual typealias PlatformDiscoveredService = BtleplugService
internal actual typealias PlatformDiscoveredCharacteristic = BtleplugCharacteristic
internal actual typealias PlatformDiscoveredDescriptor = BtleplugDescriptor
