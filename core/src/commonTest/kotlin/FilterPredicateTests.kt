package com.juul.kable

import com.benasher44.uuid.uuidFrom
import com.juul.kable.Filter.Name
import com.juul.kable.FilterPredicateBuilder.Name.Exact
import com.juul.kable.FilterPredicateBuilder.Name.Prefix
import kotlin.test.Test
import kotlin.test.assertEquals
import com.juul.kable.Filter.ManufacturerData as ManufacturerDataFilter

class FilterPredicateTests {

    private val TEST_UUID_1 = uuidFrom("deadbeef-0000-0000-0000-000000000000")
    private val TEST_UUID_2 = uuidFrom("0badcafe-0000-0000-0000-000000000000")

    @Test
    fun matches_nameFilterVsExactMatch_isTrue() {
        val predicate = Name("bob").toPredicate()
        assertEquals(
            expected = true,
            actual = predicate.matches(name = "bob"),
        )
    }

    @Test
    fun matches_nameFilterVsWrongName_isFalse() {
        val predicate = Name("bob").toPredicate()
        assertEquals(
            expected = false,
            actual = predicate.matches(name = "bobby"),
        )
    }

    @Test
    fun matches_nameFilterVsNothing_isFalse() {
        val predicate = Name("bob").toPredicate()
        assertEquals(
            expected = false,
            actual = predicate.matches(),
        )
    }

    @Test
    fun matches_nameFilterVsService_isFalse() {
        val predicate = Name("bob").toPredicate()
        assertEquals(
            expected = false,
            actual = predicate.matches(services = listOf(TEST_UUID_1)),
        )
    }

    @Test
    fun matches_nameFilterVsServiceAndName_isTrue() {
        val predicate = Name("bob").toPredicate()
        assertEquals(
            expected = true,
            actual = predicate.matches(services = listOf(TEST_UUID_1), name = "bob"),
        )
    }

    @Test
    fun matches_serviceAndNameFilterVsExactMatch_isTrue() {
        val predicate = FilterPredicateBuilder().apply {
            services = listOf(TEST_UUID_1)
            name = Exact("bob")
        }.build()!!
        assertEquals(
            expected = true,
            actual = predicate.matches(services = listOf(TEST_UUID_1), name = "bob"),
        )
    }

    @Test
    fun matches_serviceAndNameFilterVsNothing_isFalse() {
        val predicate = FilterPredicateBuilder().apply {
            services = listOf(TEST_UUID_1)
            name = Exact("bob")
        }.build()!!
        assertEquals(
            expected = false,
            actual = predicate.matches(),
        )
    }

    @Test
    fun matches_serviceAndNameFilterVsServiceOnly_isFalse() {
        val predicate = FilterPredicateBuilder().apply {
            services = listOf(TEST_UUID_1)
            name = Exact("bob")
        }.build()!!
        assertEquals(
            expected = false,
            actual = predicate.matches(services = listOf(TEST_UUID_1)),
        )
    }

    @Test
    fun matches_serviceAndNameFilterVsNameOnly_isFalse() {
        val predicate = FilterPredicateBuilder().apply {
            services = listOf(TEST_UUID_1)
            name = Exact("bob")
        }.build()!!
        assertEquals(
            expected = false,
            actual = predicate.matches(name = "bob"),
        )
    }

    @Test
    fun matches_serviceAndNameFilterVsNamePrefix_isFalse() {
        val predicate = FilterPredicateBuilder().apply {
            services = listOf(TEST_UUID_1)
            name = Exact("bob")
        }.build()!!
        assertEquals(
            expected = false,
            actual = predicate.matches(name = "b"),
        )
    }

    @Test
    fun matches_serviceAndNamePrefixFilterVsNamePrefix_isTrue() {
        val predicate = FilterPredicateBuilder().apply {
            services = listOf(TEST_UUID_1)
            name = Prefix("b")
        }.build()!!
        assertEquals(
            expected = false,
            actual = predicate.matches(name = "bob"),
        )
    }

    @Test
    fun matches_serviceAndNameFilterVsManufacturerData_isFalse() {
        val predicate = FilterPredicateBuilder().apply {
            services = listOf(TEST_UUID_1)
            name = Exact("bob")
        }.build()!!
        assertEquals(
            expected = false,
            actual = predicate.matches(manufacturerData = ManufacturerData(42, byteArrayOf(1))),
        )
    }

    @Test
    fun matches_service1AndService2FilterVsSingleMatch_isFalse() {
        val predicate = FilterPredicateBuilder().apply {
            services = listOf(TEST_UUID_1, TEST_UUID_2)
        }.build()!!
        assertEquals(
            expected = false,
            actual = predicate.matches(services = listOf(TEST_UUID_1)),
        )
    }

    @Test
    fun matches_service1AndService2FilterVsDualMatch_isTrue() {
        val predicate = FilterPredicateBuilder().apply {
            services = listOf(TEST_UUID_1, TEST_UUID_2)
        }.build()!!
        assertEquals(
            expected = true,
            actual = predicate.matches(services = listOf(TEST_UUID_1, TEST_UUID_2)),
        )
    }

    @Test
    fun matches_service1FilterVsDualMatch_isTrue() {
        val predicate = FilterPredicateBuilder().apply {
            services = listOf(TEST_UUID_1)
        }.build()!!
        assertEquals(
            expected = true,
            actual = predicate.matches(services = listOf(TEST_UUID_1, TEST_UUID_2)),
        )
    }

    @Test
    fun matches_manufacturerDataFilterVsExactMatch_isTrue() {
        val predicate = ManufacturerDataFilter(37, byteArrayOf(2)).toPredicate()
        assertEquals(
            expected = true,
            actual = predicate.matches(manufacturerData = ManufacturerData(37, byteArrayOf(2))),
        )
    }

    @Test
    fun matches_manufacturerDataFilterVsIdMisMatch_isFalse() {
        val predicate = ManufacturerDataFilter(37, byteArrayOf(2)).toPredicate()
        assertEquals(
            expected = false,
            actual = predicate.matches(manufacturerData = ManufacturerData(31, byteArrayOf(2))),
        )
    }

    @Test
    fun matches_manufacturerDataFilterVsEmptyData_isFalse() {
        val predicate = ManufacturerDataFilter(37, byteArrayOf(2)).toPredicate()
        assertEquals(
            expected = false,
            actual = predicate.matches(manufacturerData = ManufacturerData(31, byteArrayOf())),
        )
    }

    @Test
    fun matches_manufacturerDataFilterVsDataMisMatchWithoutMask_isFalse() {
        val predicate = ManufacturerDataFilter(37, byteArrayOf(2)).toPredicate()
        assertEquals(
            expected = false,
            actual = predicate.matches(manufacturerData = ManufacturerData(37, byteArrayOf(3))),
        )
    }

    @Test
    fun matches_manufacturerDataFilterVsDataMatchWithMask_isTrue() {
        // Mask to match against only the single bit
        val predicate = ManufacturerDataFilter(37, byteArrayOf(2), byteArrayOf(2)).toPredicate()
        assertEquals(
            expected = true,
            actual = predicate.matches(manufacturerData = ManufacturerData(37, byteArrayOf(3))),
        )
    }
}

private fun Filter.toPredicate() = FilterPredicate(listOf(this))
