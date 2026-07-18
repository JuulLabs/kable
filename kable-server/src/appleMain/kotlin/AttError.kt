package com.juul.kable.server

import platform.CoreBluetooth.CBATTError
import platform.CoreBluetooth.CBATTErrorAttributeNotFound
import platform.CoreBluetooth.CBATTErrorAttributeNotLong
import platform.CoreBluetooth.CBATTErrorInsufficientAuthentication
import platform.CoreBluetooth.CBATTErrorInsufficientAuthorization
import platform.CoreBluetooth.CBATTErrorInsufficientEncryption
import platform.CoreBluetooth.CBATTErrorInsufficientEncryptionKeySize
import platform.CoreBluetooth.CBATTErrorInsufficientResources
import platform.CoreBluetooth.CBATTErrorInvalidAttributeValueLength
import platform.CoreBluetooth.CBATTErrorInvalidHandle
import platform.CoreBluetooth.CBATTErrorInvalidOffset
import platform.CoreBluetooth.CBATTErrorInvalidPdu
import platform.CoreBluetooth.CBATTErrorPrepareQueueFull
import platform.CoreBluetooth.CBATTErrorReadNotPermitted
import platform.CoreBluetooth.CBATTErrorRequestNotSupported
import platform.CoreBluetooth.CBATTErrorUnlikelyError
import platform.CoreBluetooth.CBATTErrorUnsupportedGroupType
import platform.CoreBluetooth.CBATTErrorWriteNotPermitted

internal val AttError.cbAttError: CBATTError
    get() = when (this) {
        AttError.InvalidHandle -> CBATTErrorInvalidHandle
        AttError.ReadNotPermitted -> CBATTErrorReadNotPermitted
        AttError.WriteNotPermitted -> CBATTErrorWriteNotPermitted
        AttError.InvalidPdu -> CBATTErrorInvalidPdu
        AttError.InsufficientAuthentication -> CBATTErrorInsufficientAuthentication
        AttError.RequestNotSupported -> CBATTErrorRequestNotSupported
        AttError.InvalidOffset -> CBATTErrorInvalidOffset
        AttError.InsufficientAuthorization -> CBATTErrorInsufficientAuthorization
        AttError.PrepareQueueFull -> CBATTErrorPrepareQueueFull
        AttError.AttributeNotFound -> CBATTErrorAttributeNotFound
        AttError.AttributeNotLong -> CBATTErrorAttributeNotLong
        AttError.InsufficientEncryptionKeySize -> CBATTErrorInsufficientEncryptionKeySize
        AttError.InvalidAttributeValueLength -> CBATTErrorInvalidAttributeValueLength
        AttError.UnlikelyError -> CBATTErrorUnlikelyError
        AttError.InsufficientEncryption -> CBATTErrorInsufficientEncryption
        AttError.UnsupportedGroupType -> CBATTErrorUnsupportedGroupType
        AttError.InsufficientResources -> CBATTErrorInsufficientResources
    }
