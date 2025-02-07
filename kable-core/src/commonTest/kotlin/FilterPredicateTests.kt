package com.juul.kable

import com.juul.kable.Filter.Name.Exact
import com.juul.kable.Filter.Name.Prefix
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.uuid.Uuid
import com.juul.kable.Filter.ManufacturerData as ManufacturerDataFilter

class FilterPredicateTests {

    private val TEST_UUID_1 = Uuid.parse("deadbeef-0000-0000-0000-000000000000")
    private val TEST_UUID_2 = Uuid.parse("0badcafe-0000-0000-0000-000000000000")

    @Test
    fun matches_nameFilterVsExactMatch_isTrue() {
        val predicate = Exact("bob").toPredicate()
        assertTrue(predicate.matches(name = "bob"))
    }

    @Test
    fun matches_nameFilterVsWrongName_isFalse() {
        val predicate = Exact("bob").toPredicate()
        assertFalse(predicate.matches(name = "bobby"))
    }

    @Test
    fun matches_nameFilterVsNothing_isFalse() {
        val predicate = Exact("bob").toPredicate()
        assertFalse(predicate.matches())
    }

    @Test
    fun matches_nameFilterVsService_isFalse() {
        val predicate = Exact("bob").toPredicate()
        assertFalse(predicate.matches(services = listOf(TEST_UUID_1)))
    }

    @Test
    fun matches_nameFilterVsServiceAndName_isTrue() {
        val predicate = Exact("bob").toPredicate()
        assertTrue(predicate.matches(services = listOf(TEST_UUID_1), name = "bob"))
    }

    @Test
    fun matches_serviceAndNameFilterVsExactMatch_isTrue() {
        val predicate = FilterPredicateBuilder().apply {
            services = listOf(TEST_UUID_1)
            name = Exact("bob")
        }.build()!!
        assertTrue(predicate.matches(services = listOf(TEST_UUID_1), name = "bob"))
    }

    @Test
    fun matches_serviceAndNameFilterVsNothing_isFalse() {
        val predicate = FilterPredicateBuilder().apply {
            services = listOf(TEST_UUID_1)
            name = Exact("bob")
        }.build()!!
        assertFalse(predicate.matches())
    }

    @Test
    fun matches_serviceAndNameFilterVsServiceOnly_isFalse() {
        val predicate = FilterPredicateBuilder().apply {
            services = listOf(TEST_UUID_1)
            name = Exact("bob")
        }.build()!!
        assertFalse(predicate.matches(services = listOf(TEST_UUID_1)))
    }

    @Test
    fun matches_serviceAndNameFilterVsNameOnly_isFalse() {
        val predicate = FilterPredicateBuilder().apply {
            services = listOf(TEST_UUID_1)
            name = Exact("bob")
        }.build()!!
        assertFalse(predicate.matches(name = "bob"))
    }

    @Test
    fun matches_serviceAndNameFilterVsNamePrefix_isFalse() {
        val predicate = FilterPredicateBuilder().apply {
            services = listOf(TEST_UUID_1)
            name = Exact("bob")
        }.build()!!
        assertFalse(predicate.matches(name = "b"))
    }

    @Test
    fun matches_serviceAndNamePrefixFilterVsNamePrefixOnly_isFalse() {
        val predicate = FilterPredicateBuilder().apply {
            services = listOf(TEST_UUID_1)
            name = Prefix("b")
        }.build()!!
        assertFalse(predicate.matches(name = "bob"))
    }

    @Test
    fun matches_serviceAndNameFilterVsManufacturerData_isFalse() {
        val predicate = FilterPredicateBuilder().apply {
            services = listOf(TEST_UUID_1)
            name = Exact("bob")
        }.build()!!
        assertFalse(predicate.matches(manufacturerData = ManufacturerData(42, byteArrayOf(1))))
    }

    @Test
    fun matches_service1AndService2FilterVsSingleMatch_isFalse() {
        val predicate = FilterPredicateBuilder().apply {
            services = listOf(TEST_UUID_1, TEST_UUID_2)
        }.build()!!
        assertFalse(predicate.matches(services = listOf(TEST_UUID_1)))
    }

    @Test
    fun matches_service1AndService2FilterVsDualMatch_isTrue() {
        val predicate = FilterPredicateBuilder().apply {
            services = listOf(TEST_UUID_1, TEST_UUID_2)
        }.build()!!
        assertTrue(predicate.matches(services = listOf(TEST_UUID_1, TEST_UUID_2)))
    }

    @Test
    fun matches_service1FilterVsDualMatch_isTrue() {
        val predicate = FilterPredicateBuilder().apply {
            services = listOf(TEST_UUID_1)
        }.build()!!
        assertTrue(predicate.matches(services = listOf(TEST_UUID_1, TEST_UUID_2)))
    }

    @Test
    fun matches_manufacturerDataFilterVsExactMatch_isTrue() {
        val predicate = ManufacturerDataFilter(37, byteArrayOf(2)).toPredicate()
        assertTrue(predicate.matches(manufacturerData = ManufacturerData(37, byteArrayOf(2))))
    }

    @Test
    fun matches_manufacturerDataFilterVsIdMisMatch_isFalse() {
        val predicate = ManufacturerDataFilter(37, byteArrayOf(2)).toPredicate()
        assertFalse(predicate.matches(manufacturerData = ManufacturerData(31, byteArrayOf(2))))
    }

    @Test
    fun matches_manufacturerDataFilterVsEmptyData_isFalse() {
        val predicate = ManufacturerDataFilter(37, byteArrayOf(2)).toPredicate()
        assertFalse(predicate.matches(manufacturerData = ManufacturerData(31, byteArrayOf())))
    }

    @Test
    fun matches_manufacturerDataFilterVsDataMisMatchWithoutMask_isFalse() {
        val predicate = ManufacturerDataFilter(37, byteArrayOf(2)).toPredicate()
        assertFalse(predicate.matches(manufacturerData = ManufacturerData(37, byteArrayOf(3))))
    }

    @Test
    fun matches_manufacturerDataFilterVsDataMatchWithMask_isTrue() {
        // Mask to match against only the single bit
        val predicate = ManufacturerDataFilter(37, byteArrayOf(2), byteArrayOf(2)).toPredicate()
        assertTrue(predicate.matches(manufacturerData = ManufacturerData(37, byteArrayOf(3))))
    }

    @Test
    fun matches_manufacturerDataFilterVsDataWithLengthLongerThanFilterDataThatMatches_isTrue() {
        val texasInstrumentsCompanyId = 0x000D
        val sensorTagManufacturerData = byteArrayOf(0x03, 0x00, 0x00)

        val predicate = ManufacturerDataFilter(
            id = texasInstrumentsCompanyId,
            data = byteArrayOf(0x03),
            dataMask = byteArrayOf(0xFF.toByte()),
        ).toPredicate()

        assertTrue(
            predicate.matches(
                manufacturerData = ManufacturerData(
                    code = texasInstrumentsCompanyId,
                    data = sensorTagManufacturerData,
                ),
            ),
        )
    }

    @Test
    fun matches_manufacturerDataFilterVsDataWithLengthLongerThanFilterDataThatDoesNotMatch_isFalse() {
        val texasInstrumentsCompanyId = 0x000D
        val sensorTagManufacturerData = byteArrayOf(0x03, 0x00, 0x00)

        val predicate = ManufacturerDataFilter(
            id = texasInstrumentsCompanyId,
            data = byteArrayOf(0x02),
            dataMask = byteArrayOf(0x0F.toByte()),
        ).toPredicate()

        assertFalse(
            predicate.matches(
                manufacturerData = ManufacturerData(
                    code = texasInstrumentsCompanyId,
                    data = sensorTagManufacturerData,
                ),
            ),
        )
    }

    @Test
    fun matches_manufacturerDataFilterVsDataWithLengthShorterThanFilterDataButMatchesMaskPortion_isTrue() {
        val texasInstrumentsCompanyId = 0x000D
        val sensorTagManufacturerData = byteArrayOf(0x03, 0x00, 0x00)

        val predicate = ManufacturerDataFilter(
            id = texasInstrumentsCompanyId,
            data = byteArrayOf(0x03, 0x00, 0x00, 0x00),
            dataMask = byteArrayOf(0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0x00.toByte()),
        ).toPredicate()

        assertTrue(
            predicate.matches(
                manufacturerData = ManufacturerData(
                    code = texasInstrumentsCompanyId,
                    data = sensorTagManufacturerData,
                ),
            ),
        )
    }

    @Test
    fun matches_manufacturerDataFilterVsDataWithLengthShorterThanFilterData_isFalse() {
        val texasInstrumentsCompanyId = 0x000D
        val sensorTagManufacturerData = byteArrayOf(0x03, 0x00, 0x00)

        val predicate = ManufacturerDataFilter(
            id = texasInstrumentsCompanyId,
            data = byteArrayOf(0x03, 0x00, 0x00, 0x00),
            dataMask = byteArrayOf(0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte()),
        ).toPredicate()

        assertFalse(
            predicate.matches(
                manufacturerData = ManufacturerData(
                    code = texasInstrumentsCompanyId,
                    data = sensorTagManufacturerData,
                ),
            ),
        )
    }

    @Test
    fun manufacturerDataFilter_nullDataWithNullDataMask_isAllowed() {
        ManufacturerDataFilter(
            id = 1,
            data = null,
            dataMask = null,
        )
    }

    @Test
    fun manufacturerDataFilter_nullDataWithEmptyDataMask_throwsIllegalArgumentException() {
        assertFailsWith<IllegalArgumentException> {
            ManufacturerDataFilter(
                id = 1,
                data = null,
                dataMask = byteArrayOf(),
            )
        }
    }

    @Test
    fun manufacturerDataFilter_nullDataWithNonNullDataMask_throwsIllegalArgumentException() {
        assertFailsWith<IllegalArgumentException> {
            ManufacturerDataFilter(
                id = 1,
                data = null,
                dataMask = byteArrayOf(0xFF.toByte(), 0xFF.toByte()),
            )
        }
    }

    @Test
    fun manufacturerDataFilter_emptyData_throwsIllegalArgumentException() {
        assertFailsWith<IllegalArgumentException> {
            ManufacturerDataFilter(
                id = 1,
                data = byteArrayOf(),
            )
        }
    }

    @Test
    fun matches_manufacturerDataFilterWithNullDataVsEmptyData_isTrue() {
        val predicate = ManufacturerDataFilter(
            id = 1,
            data = null,
            dataMask = null,
        ).toPredicate()

        assertTrue(
            predicate.matches(
                manufacturerData = ManufacturerData(
                    code = 1,
                    data = byteArrayOf(),
                ),
            ),
        )
    }

    @Test
    fun matches_manufacturerDataFilterWithNullDataVsData_isTrue() {
        val predicate = ManufacturerDataFilter(
            id = 1,
            data = null,
            dataMask = null,
        ).toPredicate()

        assertTrue(
            predicate.matches(
                manufacturerData = ManufacturerData(
                    code = 1,
                    data = byteArrayOf(0xFF.toByte(), 0xFF.toByte()),
                ),
            ),
        )
    }

    @Test
    fun matches_manufacturerDataFilterWithoutDataMaskVsData_isTrue() {
        val predicate = ManufacturerDataFilter(
            id = 1,
            data = byteArrayOf(0xFF.toByte(), 0xFF.toByte()),
            dataMask = null,
        ).toPredicate()

        assertTrue(
            predicate.matches(
                manufacturerData = ManufacturerData(
                    code = 1,
                    data = byteArrayOf(0xFF.toByte(), 0xFF.toByte()),
                ),
            ),
        )
    }

    @Test
    fun matches_manufacturerDataFilterWithoutDataMaskVsDifferentData_isFalse() {
        val predicate = ManufacturerDataFilter(
            id = 1,
            data = byteArrayOf(0xF0.toByte(), 0x0D.toByte()),
            dataMask = null,
        ).toPredicate()

        assertFalse(
            predicate.matches(
                manufacturerData = ManufacturerData(
                    code = 1,
                    data = byteArrayOf(0x12, 0x34),
                ),
            ),
        )
    }
}

private fun Filter.toPredicate() = FilterPredicate(listOf(this))
