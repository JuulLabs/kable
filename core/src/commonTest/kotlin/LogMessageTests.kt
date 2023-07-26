package com.juul.kable

import com.benasher44.uuid.uuidFrom
import com.juul.kable.logs.LogMessage
import com.juul.kable.logs.Logging
import com.juul.kable.logs.Logging.Format.Compact
import com.juul.kable.logs.Logging.Format.Multiline
import com.juul.kable.logs.Logging.Level.Data
import com.juul.kable.logs.detail
import kotlin.test.Test
import kotlin.test.assertEquals

private val TEST_UUID_1 = uuidFrom("ad0be000-0000-4000-a000-000000000000")
private val TEST_UUID_2 = uuidFrom("c0c0a000-0000-4000-a000-000000000000")
private val TEST_UUID_3 = uuidFrom("decade00-0000-4000-a000-000000000000")

class LogMessageTests {

    @Test
    fun loggingIdentifierTakesPrecedenceOverPlatformIdentifier() {
        val logging = Logging().apply {
            identifier = "logging"
            format = Compact
        }
        val log = LogMessage(logging, platformIdentifier = "platform").apply {
            message = "Hello, world!"
        }
        assertEquals(
            expected = "logging Hello, world!",
            actual = log.build(),
        )
    }

    @Test
    fun compactFormat_onlyMessage() {
        val logging = Logging().apply {
            format = Compact
        }
        val log = LogMessage(logging, platformIdentifier = "example").apply {
            message = "Hello, world!"
        }
        assertEquals(
            expected = "example Hello, world!",
            actual = log.build(),
        )
    }

    @Test
    fun multilineFormat_onlyMessage() {
        val logging = Logging().apply {
            format = Multiline
        }
        val log = LogMessage(logging, platformIdentifier = "example").apply {
            message = "Hello, world!"
        }
        assertEquals(
            expected = "example Hello, world!",
            actual = log.build(),
        )
    }

    @Test
    fun compactFormat_messageAndService() {
        val logging = Logging().apply {
            format = Compact
        }
        val log = LogMessage(logging, platformIdentifier = "example").apply {
            message = "Compact"
            detail("service", TEST_UUID_1)
        }
        assertEquals(
            expected = "example Compact(service=$TEST_UUID_1)",
            actual = log.build(),
        )
    }

    @Test
    fun multilineFormat_messageAndService() {
        val logging = Logging().apply {
            format = Multiline
        }
        val log = LogMessage(logging, platformIdentifier = "example", indent = "  ").apply {
            message = "test"
            detail("service", TEST_UUID_1)
        }
        assertEquals(
            expected = """
                example test
                  service: $TEST_UUID_1
            """.trimIndent(),
            actual = log.build(),
        )
    }

    @Test
    fun compactFormat_messageAndCharacteristic() {
        val logging = Logging().apply {
            format = Compact
        }
        val log = LogMessage(logging, platformIdentifier = "example").apply {
            message = "Compact"
            detail("service", TEST_UUID_1)
            detail("characteristic", TEST_UUID_2)
        }
        assertEquals(
            expected = "example Compact(service=$TEST_UUID_1, characteristic=$TEST_UUID_2)",
            actual = log.build(),
        )
    }

    @Test
    fun multilineFormat_messageAndCharacteristic() {
        val logging = Logging().apply {
            format = Multiline
        }
        val log = LogMessage(logging, platformIdentifier = "example", indent = "  ").apply {
            message = "Hello, world!"
            detail("service", TEST_UUID_1)
            detail("characteristic", TEST_UUID_2)
        }
        assertEquals(
            expected = """
                example Hello, world!
                  service: $TEST_UUID_1
                  characteristic: $TEST_UUID_2
            """.trimIndent(),
            actual = log.build(),
        )
    }

    @Test
    fun compactFormat_messageAndDescriptor() {
        val logging = Logging().apply {
            format = Compact
        }
        val log = LogMessage(logging, platformIdentifier = "example").apply {
            message = "Compact"
            detail("service", TEST_UUID_1)
            detail("characteristic", TEST_UUID_2)
            detail("descriptor", TEST_UUID_3)
        }
        assertEquals(
            expected = "example Compact(service=$TEST_UUID_1, characteristic=$TEST_UUID_2, descriptor=$TEST_UUID_3)",
            actual = log.build(),
        )
    }

    @Test
    fun multilineFormat_messageAndDescriptor() {
        val logging = Logging().apply {
            format = Multiline
        }
        val log = LogMessage(logging, platformIdentifier = "example", indent = "  ").apply {
            message = "Hello, world!"
            detail("service", TEST_UUID_1)
            detail("characteristic", TEST_UUID_2)
            detail("descriptor", TEST_UUID_3)
        }
        assertEquals(
            expected = """
                example Hello, world!
                  service: $TEST_UUID_1
                  characteristic: $TEST_UUID_2
                  descriptor: $TEST_UUID_3
            """.trimIndent(),
            actual = log.build(),
        )
    }

    @Test
    fun compactFormat_messageServiceAndNumericDetail() {
        val logging = Logging().apply {
            format = Compact
        }
        val log = LogMessage(logging, platformIdentifier = "example").apply {
            message = "Compact"
            detail("service", TEST_UUID_1)
            detail("extra", 1)
        }
        assertEquals(
            expected = "example Compact(service=$TEST_UUID_1, extra=1)",
            actual = log.build(),
        )
    }

    @Test
    fun multilineFormat_messageServiceAndNumericDetail() {
        val logging = Logging().apply {
            format = Multiline
        }
        val log = LogMessage(logging, platformIdentifier = "example", indent = "  ").apply {
            message = "Hello, world!"
            detail("service", TEST_UUID_1)
            detail("extra", 1)
        }
        assertEquals(
            expected = """
                example Hello, world!
                  service: $TEST_UUID_1
                  extra: 1
            """.trimIndent(),
            actual = log.build(),
        )
    }

    @Test
    fun compactFormatAndDefaultLogLevel_doesNotLogData() {
        val logging = Logging().apply {
            format = Compact
        }
        val log = LogMessage(logging, platformIdentifier = "example").apply {
            message = "Compact"
            detail(byteArrayOf(1, 2))
        }
        assertEquals(
            expected = "example Compact",
            actual = log.build(),
        )
    }

    @Test
    fun compactFormatAndDataLogLevel_logsData() {
        val logging = Logging().apply {
            format = Compact
            level = Data
        }
        val log = LogMessage(logging, platformIdentifier = "example").apply {
            message = "Compact"
            detail(byteArrayOf(1, 2))
        }
        assertEquals(
            expected = "example Compact(data=01 02)",
            actual = log.build(),
        )
    }
}
