package com.juul.kable

import com.juul.kable.Filter.Name.Exact
import com.juul.kable.Filter.Name.Prefix
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class FiltersTests {

    private val TEST_UUID_1 = Uuid.parse("deadbeef-0000-0000-0000-000000000000")
    private val TEST_UUID_2 = Uuid.parse("0badcafe-0000-0000-0000-000000000000")

    @Test
    fun matches_emptySetVsNothing_isTrue() {
        val filters = FiltersBuilder().build()
        assertTrue(filters.matches())
    }

    @Test
    fun matches_emptySetVsName_isTrue() {
        val filters = FiltersBuilder().build()
        assertTrue(filters.matches(name = "bob"))
    }

    @Test
    fun matches_serviceOrNameFilterVsNothing_isFalse() {
        val filters = FiltersBuilder().apply {
            match {
                services = listOf(TEST_UUID_1)
            }
            match {
                name = Exact("bob")
            }
        }.build()
        assertFalse(filters.matches())
    }

    @Test
    fun matches_serviceOrNameFilterVsServiceOnly_isTrue() {
        val filters = FiltersBuilder().apply {
            match {
                services = listOf(TEST_UUID_1)
            }
            match {
                name = Exact("bob")
            }
        }.build()
        assertTrue(filters.matches(services = listOf(TEST_UUID_1)))
    }

    @Test
    fun matches_serviceOrNameFilterVsNameOnly_isTrue() {
        val filters = FiltersBuilder().apply {
            match {
                services = listOf(TEST_UUID_1)
            }
            match {
                name = Exact("bob")
            }
        }.build()
        assertTrue(filters.matches(name = "bob"))
    }

    @Test
    fun matches_serviceOrNameFilterVsNamePrefix_isFalse() {
        val filters = FiltersBuilder().apply {
            match {
                services = listOf(TEST_UUID_1)
            }
            match {
                name = Exact("bob")
            }
        }.build()
        assertFalse(filters.matches(name = "b"))
    }

    @Test
    fun matches_serviceAndNameOrNamePrefixFilterVsName_isTrue() {
        val filters = FiltersBuilder().apply {
            match {
                services = listOf(TEST_UUID_1)
                name = Exact("bob")
            }
            match {
                name = Prefix("c")
            }
        }.build()
        assertTrue(filters.matches(name = "charlie"))
    }

    @Test
    fun matches_serviceAndNameOrNamePrefixFilterVsName_isFalse() {
        val filters = FiltersBuilder().apply {
            match {
                services = listOf(TEST_UUID_1)
                name = Exact("bob")
            }
            match {
                name = Prefix("c")
            }
        }.build()
        assertFalse(filters.matches(name = "bob"))
    }

    @Test
    fun matches_serviceAndNameOrNamePrefixFilterVsNameAndService_isTrue() {
        val filters = FiltersBuilder().apply {
            match {
                services = listOf(TEST_UUID_1)
                name = Exact("bob")
            }
            match {
                name = Prefix("c")
            }
        }.build()
        assertTrue(filters.matches(services = listOf(TEST_UUID_1), name = "bob"))
    }

    @Test
    fun matches_service1OrService2FilterVsSingleMatch_isTrue() {
        val filters = FiltersBuilder().apply {
            match {
                services = listOf(TEST_UUID_1)
            }
            match {
                services = listOf(TEST_UUID_2)
            }
        }.build()
        assertTrue(filters.matches(services = listOf(TEST_UUID_1)))
    }

    @Test
    fun matches_service1OrService2FilterVsDualMatch_isTrue() {
        val filters = FiltersBuilder().apply {
            match {
                services = listOf(TEST_UUID_1)
            }
            match {
                services = listOf(TEST_UUID_2)
            }
        }.build()
        assertTrue(filters.matches(services = listOf(TEST_UUID_1, TEST_UUID_2)))
    }

    // One slightly complex filter to save tedium of building hundreds of tests
    // If anything breaks here isolate it as a new isolated unit test
    @Test
    fun matches_complexFilter_multiTest() {
        val filters = FiltersBuilder().apply {
            match {
                // A
                services = listOf(TEST_UUID_1)
                name = Prefix("x")
            }
            match {
                // B
                services = listOf(TEST_UUID_2)
                name = Prefix("y")
            }
            match {
                // C
                manufacturerData = listOf(
                    Filter.ManufacturerData(37, byteArrayOf(2)),
                )
            }
            match {
                // D
                name = Exact("bob")
            }
            match {
                // E
                name = Exact("alice")
            }
            match {
                // F
                name = Exact("charlie")
            }
        }.build()
        assertFalse(
            actual = filters.matches(services = listOf(TEST_UUID_1, TEST_UUID_2)),
            message = "Expect false because all matching service filters require a name as well",
        )
        assertTrue(
            actual = filters.matches(services = listOf(TEST_UUID_1), name = "xavier"),
            message = "Expect true because we match on uuid and name prefix in predicate A",
        )
        assertTrue(
            actual = filters.matches(services = listOf(TEST_UUID_1), name = "alice"),
            message = "Expect true because we match on exact name in predicate E",
        )
        assertFalse(
            actual = filters.matches(name = "yani"),
            message = "Expect false because matching name prefix in predicate B requires a service uuid as well",
        )
        assertTrue(
            actual = filters.matches(manufacturerData = ManufacturerData(37, byteArrayOf(2))),
            message = "Expect true because we match manufacturer data in predicate C",
        )
    }
}
