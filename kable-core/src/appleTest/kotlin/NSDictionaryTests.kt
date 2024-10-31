@file:OptIn(BetaInteropApi::class)

package com.juul.kable.test

import kotlinx.cinterop.BetaInteropApi
import platform.CoreBluetooth.CBAdvertisementDataManufacturerDataKey
import platform.CoreBluetooth.CBAdvertisementDataServiceUUIDsKey
import platform.CoreBluetooth.CBAdvertisementDataSolicitedServiceUUIDsKey
import platform.CoreBluetooth.CBUUID
import platform.Foundation.NSArray
import platform.Foundation.NSData
import platform.Foundation.NSDataBase64DecodingIgnoreUnknownCharacters
import platform.Foundation.NSDictionary
import platform.Foundation.create
import kotlin.test.Test
import kotlin.test.assertTrue

private const val FOO = "Zm9v" // Base64("foo")
private const val BAR = "YmFy" // Base64("bar")

class NSDictionaryTests {

    @Test
    fun twoDictionaries_asMapWithSameContents_isEqual() {
        val data1 = NSData.create(FOO, NSDataBase64DecodingIgnoreUnknownCharacters)!!
        val dictionary1 = NSDictionary.create(mapOf(CBAdvertisementDataManufacturerDataKey to data1))

        val data2 = NSData.create(FOO, NSDataBase64DecodingIgnoreUnknownCharacters)!!
        val dictionary2 = NSDictionary.create(mapOf(CBAdvertisementDataManufacturerDataKey to data2))

        assertTrue { dictionary1 == dictionary2 }
    }

    @Test
    fun twoDictionaries_asMapWithSameKeysAndDifferentData_isNotEqual() {
        val data1 = NSData.create(FOO, NSDataBase64DecodingIgnoreUnknownCharacters)!!
        val dictionary1 = NSDictionary.create(mapOf(CBAdvertisementDataManufacturerDataKey to data1))

        val data2 = NSData.create(BAR, NSDataBase64DecodingIgnoreUnknownCharacters)!!
        val dictionary2 = NSDictionary.create(mapOf(CBAdvertisementDataManufacturerDataKey to data2))

        assertTrue { dictionary1 != dictionary2 }
    }

    @Test
    fun twoDictionaries_asMapWithDifferentKeysAndDifferentData_isNotEqual() {
        val uuids = NSArray.create(listOf(CBUUID.UUIDWithString("07af6856-6779-4459-ba1d-085c08530931")))
        val dictionary1 = NSDictionary.create(mapOf(CBAdvertisementDataServiceUUIDsKey to uuids))

        val data = NSData.create(FOO, NSDataBase64DecodingIgnoreUnknownCharacters)!!
        val dictionary2 = NSDictionary.create(mapOf(CBAdvertisementDataManufacturerDataKey to data))

        assertTrue { dictionary1 != dictionary2 }
    }

    @Test
    fun twoDictionaries_asMapWithDifferentKeysAndSameData_isNotEqual() {
        val uuids = NSArray.create(listOf(CBUUID.UUIDWithString("07af6856-6779-4459-ba1d-085c08530931")))
        val dictionary1 = NSDictionary.create(mapOf(CBAdvertisementDataServiceUUIDsKey to uuids))
        val dictionary2 = NSDictionary.create(mapOf(CBAdvertisementDataSolicitedServiceUUIDsKey to uuids))
        assertTrue { dictionary1 != dictionary2 }
    }
}
