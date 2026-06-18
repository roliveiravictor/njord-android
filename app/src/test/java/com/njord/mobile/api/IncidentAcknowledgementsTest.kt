package com.njord.mobile.api

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IncidentAcknowledgementsTest {
    @Test
    fun acknowledge_filtersMatchingIncidentJsonForTwentyFourHours() {
        val filesDir = createTempDir()
        try {
            IncidentAcknowledgements.acknowledge(filesDir, "2026-06-18T10:00:00Z-order", nowMillis = 1_000L)

            val ids = IncidentAcknowledgements.readActiveIds(filesDir, nowMillis = 1_000L)
            val filtered = IncidentAcknowledgements.filterIncidentJson(
                """
                [
                  {"timestamp":"2026-06-18T10:00:00Z","category":"order","title":"Dismissed"},
                  {"timestamp":"2026-06-18T10:05:00Z","category":"risk","title":"Visible"}
                ]
                """.trimIndent(),
                ids
            )

            assertTrue("2026-06-18T10:00:00Z-order" in ids)
            assertFalse(filtered.orEmpty().contains("Dismissed"))
            assertTrue(filtered.orEmpty().contains("Visible"))
        } finally {
            filesDir.deleteRecursively()
        }
    }

    @Test
    fun readActiveIds_expiresAcknowledgementsAfterTwentyFourHours() {
        val filesDir = createTempDir()
        try {
            IncidentAcknowledgements.acknowledge(filesDir, "expired-incident", nowMillis = 1_000L)

            val ids = IncidentAcknowledgements.readActiveIds(filesDir, nowMillis = 1_000L + 24 * 60 * 60 * 1000L)

            assertEquals(emptySet<String>(), ids)
        } finally {
            filesDir.deleteRecursively()
        }
    }

    private fun createTempDir(): File =
        kotlin.io.path.createTempDirectory("incident-ack-test").toFile()
}
