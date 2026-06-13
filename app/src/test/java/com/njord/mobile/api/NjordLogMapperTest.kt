package com.njord.mobile.api

import com.njord.mobile.model.LogFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NjordLogMapperTest {

    private fun makeEntry(
        level: String,
        title: String = "hyperliquid.wcr",
        message: String = "msg",
        timestamp: String = "2026-06-12T14:23:01"
    ) = LogApiEntry(level = level, title = title, message = message, timestamp = timestamp)

    @Test
    fun mapApiEntries_infoLevel_mapsToLogFilterInfo() {
        val result = mapApiEntries(listOf(makeEntry("INFO")))

        assertEquals(LogFilter.Info, result[0].level)
    }

    @Test
    fun mapApiEntries_warningLevel_mapsToLogFilterWarn() {
        val result = mapApiEntries(listOf(makeEntry("WARNING")))

        assertEquals(LogFilter.Warn, result[0].level)
    }

    @Test
    fun mapApiEntries_errorLevel_mapsToLogFilterError() {
        val result = mapApiEntries(listOf(makeEntry("ERROR")))

        assertEquals(LogFilter.Error, result[0].level)
    }

    @Test
    fun mapApiEntries_unknownLevel_defaultsToInfo() {
        val result = mapApiEntries(listOf(makeEntry("DEBUG")))

        assertEquals(LogFilter.Info, result[0].level)
    }

    @Test
    fun mapApiEntries_timestamp_formattedAsHHmm() {
        val result = mapApiEntries(listOf(makeEntry("INFO", timestamp = "2026-06-12T14:23:01")))

        assertEquals("14:23", result[0].time)
    }

    @Test
    fun mapApiEntries_searchText_combinesTitleAndMessage() {
        val result = mapApiEntries(listOf(makeEntry("INFO", title = "hyperliquid.wcr", message = "Rebalance complete")))

        assertEquals("hyperliquid.wcr Rebalance complete", result[0].searchText)
    }

    @Test
    fun mapApiEntries_emptyList_returnsEmptyList() {
        val result = mapApiEntries(emptyList())

        assertTrue(result.isEmpty())
    }
}
