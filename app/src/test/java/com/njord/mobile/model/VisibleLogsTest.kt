package com.njord.mobile.model

import org.junit.Assert.assertEquals
import org.junit.Test

class VisibleLogsTest {

    private fun log(
        level: LogFilter = LogFilter.Info,
        strategy: StrategyFilter = StrategyFilter.All,
        title: String = "title",
        message: String = "msg"
    ) = LogEntry(
        level = level,
        strategy = strategy,
        title = title,
        message = message,
        time = "14:00",
        searchText = "$title $message"
    )

    @Test
    fun visibleLogs_strategyFilter_hidesNonMatchingLogs() {
        val logs = listOf(log(strategy = StrategyFilter.BigBang), log(strategy = StrategyFilter.Wcr))
        val result = visibleLogs(logs, LogFilter.All, "", StrategyFilter.BigBang)
        assertEquals(1, result.size)
        assertEquals(StrategyFilter.BigBang, result[0].strategy)
    }

    @Test
    fun visibleLogs_strategyAll_showsAllLogs() {
        val logs = listOf(log(strategy = StrategyFilter.BigBang), log(strategy = StrategyFilter.Wcr))
        val result = visibleLogs(logs, LogFilter.All, "", StrategyFilter.All)
        assertEquals(2, result.size)
    }

    @Test
    fun visibleLogs_strategyAndLevelFiltersCompose() {
        val logs = listOf(
            log(level = LogFilter.Error, strategy = StrategyFilter.BigBang),
            log(level = LogFilter.Info, strategy = StrategyFilter.BigBang),
            log(level = LogFilter.Error, strategy = StrategyFilter.Wcr)
        )
        val result = visibleLogs(logs, LogFilter.Error, "", StrategyFilter.BigBang)
        assertEquals(1, result.size)
        assertEquals(LogFilter.Error, result[0].level)
        assertEquals(StrategyFilter.BigBang, result[0].strategy)
    }

    @Test
    fun visibleLogs_strategyAndQueryFiltersCompose() {
        val logs = listOf(
            log(strategy = StrategyFilter.BigBang, title = "Big Bang opened ETH Long"),
            log(strategy = StrategyFilter.Wcr, title = "WCR rebalance kept")
        )
        val result = visibleLogs(logs, LogFilter.All, "ETH", StrategyFilter.All)
        assertEquals(1, result.size)
        assertEquals("Big Bang opened ETH Long", result[0].title)
    }
}
