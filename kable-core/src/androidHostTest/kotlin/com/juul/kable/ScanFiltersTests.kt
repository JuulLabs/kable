package com.juul.kable

import android.bluetooth.le.ScanFilter
import android.os.ParcelUuid
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.toJavaUuid

@RunWith(RobolectricTestRunner::class)
class ScanFiltersTests {

    @Test
    fun toScanFilters_filterPredicatesWithOnlyNativeFilters_allNativeFilters() {
        val serviceUuid1 = Bluetooth.BaseUuid + 1
        val name1 = "exact1"
        val address1 = "00:1A:2B:3C:4D:5E"

        val serviceUuid2 = Bluetooth.BaseUuid + 2
        val name2 = "exact2"

        val filterPredicates = listOf(
            FilterPredicate(
                listOf(
                    Filter.Service(serviceUuid1),
                    Filter.Name.Exact(name1),
                    Filter.Address(address1),
                ),
            ),
            FilterPredicate(
                listOf(
                    Filter.Service(serviceUuid2),
                    Filter.Name.Exact(name2),
                ),
            ),
        )

        assertEquals(
            expected = ScanFilters(
                native = listOf(
                    ScanFilter.Builder().apply {
                        setServiceUuid(ParcelUuid(serviceUuid1.toJavaUuid()))
                        setDeviceName(name1)
                        setDeviceAddress(address1)
                    }.build(),
                    ScanFilter.Builder().apply {
                        setServiceUuid(ParcelUuid(serviceUuid2.toJavaUuid()))
                        setDeviceName(name2)
                    }.build(),
                ),
                flow = emptyList(),
            ),
            actual = filterPredicates.toScanFilters(),
        )
    }

    @Test
    fun toScanFilters_singleFilterPredicateWithMultipleNameFilters_overlaysNativeAndFlowFilters() {
        val serviceUuid = Bluetooth.BaseUuid + 1
        val nameA = "exactA"
        val nameB = "exactB"

        val filterPredicates = listOf(
            FilterPredicate(
                listOf(
                    Filter.Service(serviceUuid),
                    Filter.Name.Exact(nameA),
                    Filter.Name.Exact(nameB),
                ),
            ),
        )

        assertEquals(
            expected = ScanFilters(
                native = listOf(
                    ScanFilter.Builder().apply {
                        setServiceUuid(ParcelUuid(serviceUuid.toJavaUuid()))
                        setDeviceName(nameA)
                    }.build(),
                ),
                flow = listOf(
                    FilterPredicate(listOf(Filter.Name.Exact(nameB))),
                ),
            ),
            actual = filterPredicates.toScanFilters(),
        )
    }

    @Test
    fun toScanFilters_multipleFilterPredicatesWithMultipleNameFilters_allFlowFilters() {
        val serviceUuid1 = Bluetooth.BaseUuid + 1
        val name1A = "exactA"
        val name1B = "exactB"

        val serviceUuid2 = Bluetooth.BaseUuid + 2

        val filterPredicates = listOf(
            FilterPredicate(
                listOf(
                    Filter.Service(serviceUuid1),
                    Filter.Name.Exact(name1A),
                    Filter.Name.Exact(name1B),
                ),
            ),
            FilterPredicate(
                listOf(
                    Filter.Service(serviceUuid2),
                ),
            ),
        )

        assertEquals(
            expected = ScanFilters(
                native = emptyList(),
                flow = listOf(
                    FilterPredicate(
                        listOf(
                            Filter.Service(serviceUuid1),
                            Filter.Name.Exact(name1A),
                            Filter.Name.Exact(name1B),
                        ),
                    ),
                    FilterPredicate(
                        listOf(
                            Filter.Service(serviceUuid2),
                        ),
                    ),
                ),
            ),
            actual = filterPredicates.toScanFilters(),
        )
    }

    @Test
    fun toScanFilters_singleFilterPredicate_overlaysNativeAndFlowFilters() {
        val serviceUuid = Bluetooth.BaseUuid + 1
        val namePrefix = "prefix"

        val filterPredicates = listOf(
            FilterPredicate(
                listOf(
                    Filter.Service(serviceUuid),
                    Filter.Name.Prefix(namePrefix),
                ),
            ),
        )

        assertEquals(
            expected = ScanFilters(
                native = listOf(
                    ScanFilter.Builder().apply {
                        setServiceUuid(ParcelUuid(serviceUuid.toJavaUuid()))
                    }.build(),
                ),
                flow = listOf(
                    FilterPredicate(listOf(Filter.Name.Prefix(namePrefix))),
                ),
            ),
            actual = filterPredicates.toScanFilters(),
        )
    }

    @Test
    fun toScanFilters_multipleFilterPredicates_allFlowFilters() {
        val serviceUuid1 = Bluetooth.BaseUuid + 1
        val namePrefix1 = "prefix1"

        val serviceUuid2 = Bluetooth.BaseUuid + 2
        val namePrefix2 = "prefix2"

        val filterPredicates = listOf(
            FilterPredicate(
                listOf(
                    Filter.Service(serviceUuid1),
                    Filter.Name.Prefix(namePrefix1),
                ),
            ),
            FilterPredicate(
                listOf(
                    Filter.Service(serviceUuid2),
                    Filter.Name.Prefix(namePrefix2),
                ),
            ),
        )

        assertEquals(
            expected = ScanFilters(
                native = emptyList(),
                flow = listOf(
                    FilterPredicate(
                        listOf(
                            Filter.Service(serviceUuid1),
                            Filter.Name.Prefix(namePrefix1),
                        ),
                    ),
                    FilterPredicate(
                        listOf(
                            Filter.Service(serviceUuid2),
                            Filter.Name.Prefix(namePrefix2),
                        ),
                    ),
                ),
            ),
            actual = filterPredicates.toScanFilters(),
        )
    }
}
