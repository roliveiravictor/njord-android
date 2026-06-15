package com.njord.mobile.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NjordReducerTest {
    @Test
    fun navigateToMoreChild_highlightsMoreGroup() {
        val state = reduce(NjordUiState(), NjordAction.Navigate(Destination.Logs))

        assertEquals(Destination.Logs, state.destination)
        assertEquals(Destination.More, state.destination.navGroup)
    }

    @Test
    fun liveFilters_filterByStrategyAndSide() {
        val filtered = visibleLivePositions(
            positions = ReducerFixtures.livePositions,
            strategyFilter = StrategyFilter.Wcr,
            sideFilter = SideFilter.Short
        )

        assertTrue(filtered.isNotEmpty())
        assertTrue(filtered.all { it.strategy == StrategyFilter.Wcr && it.side == "Short" })
    }

    @Test
    fun logFilters_matchSeverityAndSearch() {
        val filtered = visibleLogs(
            logs = ReducerFixtures.logs,
            filter = LogFilter.Error,
            query = "margin",
            strategyFilter = StrategyFilter.All
        )

        assertEquals(2, filtered.size)
        assertTrue(filtered.all { it.level == LogFilter.Error })
        assertTrue(filtered.all { it.searchText.lowercase().contains("margin") || it.message.lowercase().contains("margin") })
    }

    @Test
    fun dismissIncident_hidesIncidentBanner() {
        val incident = ReducerFixtures.incidents.first()
        val state = reduce(
            NjordUiState(selectedIncident = incident, liveIncidents = listOf(incident)),
            NjordAction.DismissIncident(incident.id)
        )

        assertFalse(state.liveIncidents.any { it.id == incident.id })
        assertNull(state.selectedIncident)
    }

    @Test
    fun selectPosition_opensBottomSheetState() {
        val position = ReducerFixtures.livePositions.first()
        val state = reduce(NjordUiState(), NjordAction.SelectPosition(position))

        assertEquals(position, state.selectedPosition)
        assertFalse(state.selectedPosition?.symbol.isNullOrBlank())
    }

    @Test
    fun logsLoading_setsLoadingTrueAndErrorFalse() {
        val state = reduce(NjordUiState(logsError = true), NjordAction.LogsLoading)

        assertTrue(state.logsLoading)
        assertFalse(state.logsError)
    }

    @Test
    fun logsLoaded_replacesLogsListAndClearsLoading() {
        val entry = LogEntry(level = LogFilter.Info, strategy = StrategyFilter.All, title = "hyperliquid.wcr", message = "ok", time = "14:23", searchText = "hyperliquid.wcr ok")
        val state = reduce(NjordUiState(logsLoading = true), NjordAction.LogsLoaded(listOf(entry)))

        assertEquals(listOf(entry), state.logs)
        assertFalse(state.logsLoading)
        assertFalse(state.logsError)
    }

    @Test
    fun logsError_setsErrorTrueAndClearsLoading() {
        val state = reduce(NjordUiState(logsLoading = true), NjordAction.LogsError)

        assertTrue(state.logsError)
        assertFalse(state.logsLoading)
    }

    @Test
    fun hunchReportLoading_setsLoadingTrueAndErrorFalse() {
        val state = reduce(NjordUiState(hunchReportError = true), NjordAction.HunchReportLoading)

        assertTrue(state.hunchReportLoading)
        assertFalse(state.hunchReportError)
    }

    @Test
    fun hunchReportLoaded_replacesReportAndClearsLoading() {
        val report = ReducerFixtures.hunchReport.copy(signal = "BUY", confidence = "MEDIUM")
        val state = reduce(NjordUiState(hunchReportLoading = true), NjordAction.HunchReportLoaded(report))

        assertEquals(report, state.hunchReport)
        assertFalse(state.hunchReportLoading)
        assertFalse(state.hunchReportError)
    }

    @Test
    fun hunchReportError_setsErrorTrueAndKeepsFallbackReport() {
        val initial = NjordUiState(hunchReportLoading = true)
        val state = reduce(initial, NjordAction.HunchReportError)

        assertEquals(initial.hunchReport, state.hunchReport)
        assertTrue(state.hunchReportError)
        assertFalse(state.hunchReportLoading)
    }

    @Test
    fun portfolioLoading_setsLoadingTrueAndErrorFalse() {
        val state = reduce(NjordUiState(portfolioError = true), NjordAction.PortfolioLoading)

        assertTrue(state.portfolioLoading)
        assertFalse(state.portfolioError)
    }

    @Test
    fun portfolioLoaded_replacesSnapshotAndClearsLoading() {
        val snapshot = PortfolioSnapshot(
            totalEquity = "\$18.4k",
            returnBadge = "ALL +84.2%",
            returnTone = Tone.Success,
            todayPnl = "+\$96",
            todayPct = "+0.5%",
            todayTone = Tone.Success,
            sevenDayPnl = "+\$812",
            sevenDayPct = "+4.6%",
            sevenDayTone = Tone.Success,
            thirtyDayPnl = "+\$2.4k",
            thirtyDayPct = "+14.8%",
            thirtyDayTone = Tone.Success,
            liveMetrics = emptyList(),
            monthlyStats = emptyList(),
            equityStats = emptyList(),
            equityCurve = emptyList(),
            equityAxisLabels = emptyList(),
            drawdownStats = emptyList(),
            drawdownCurve = emptyList(),
            drawdownAxisLabels = emptyList(),
            monthlyReturns = emptyList()
        )
        val state = reduce(NjordUiState(portfolioLoading = true), NjordAction.PortfolioLoaded(snapshot))

        assertEquals(snapshot, state.portfolioSnapshot)
        assertFalse(state.portfolioLoading)
        assertFalse(state.portfolioError)
    }

    @Test
    fun portfolioError_setsErrorTrueAndKeepsFallbackSnapshot() {
        val initial = NjordUiState(portfolioLoading = true)
        val state = reduce(initial, NjordAction.PortfolioError)

        assertEquals(initial.portfolioSnapshot, state.portfolioSnapshot)
        assertTrue(state.portfolioError)
        assertFalse(state.portfolioLoading)
    }

    @Test
    fun heartbeatLoading_setsLoadingTrueAndErrorFalse() {
        val state = reduce(NjordUiState(heartbeatError = true), NjordAction.HeartbeatLoading)

        assertTrue(state.heartbeatLoading)
        assertFalse(state.heartbeatError)
    }

    @Test
    fun heartbeatLoaded_replacesRoutinesAndCounts() {
        val routine = HeartbeatRoutine("VPN heartbeat", "Healthy", java.time.Instant.now().minusSeconds(60), "20m", Tone.Success, null, 1200)
        val state = reduce(
            NjordUiState(heartbeatLoading = true),
            NjordAction.HeartbeatLoaded(
                routines = listOf(routine),
                healthyCount = 1,
                lateCount = 2,
                criticalCount = 3,
                totalCount = 6
            )
        )

        assertEquals(listOf(routine), state.heartbeatRoutines)
        assertEquals(1, state.heartbeatHealthyCount)
        assertEquals(2, state.heartbeatLateCount)
        assertEquals(3, state.heartbeatCriticalCount)
        assertEquals(6, state.heartbeatTotalCount)
        assertFalse(state.heartbeatLoading)
        assertFalse(state.heartbeatError)
    }

    @Test
    fun heartbeatError_setsErrorTrueAndKeepsFallbackRoutines() {
        val initial = NjordUiState(heartbeatLoading = true)
        val state = reduce(initial, NjordAction.HeartbeatError)

        assertEquals(initial.heartbeatRoutines, state.heartbeatRoutines)
        assertTrue(state.heartbeatError)
        assertFalse(state.heartbeatLoading)
    }

    @Test
    fun liveLoading_setsLoadingTrueAndErrorFalse() {
        val state = reduce(NjordUiState(liveError = true), NjordAction.LiveLoading)

        assertTrue(state.liveLoading)
        assertFalse(state.liveError)
    }

    @Test
    fun liveLoaded_replacesPositionsAnalyticsAndIncidents() {
        val position = ReducerFixtures.livePositions.first()
        val analytics = LiveAnalyticsSnapshot(
            totalContribution = "+\$1.00",
            totalContributionTone = Tone.Success,
            strategyContributions = emptyList(),
            summaryItems = emptyList(),
            largestWinner = null,
            largestLoser = null,
            integrityItems = emptyList(),
            longCount = 0,
            shortCount = 0,
            longPct = 0f
        )
        val incident = ReducerFixtures.incidents.first()
        val state = reduce(
            NjordUiState(liveLoading = true),
            NjordAction.LiveLoaded(listOf(position), analytics, listOf(incident))
        )

        assertEquals(listOf(position), state.livePositions)
        assertEquals(analytics, state.liveAnalytics)
        assertEquals(listOf(incident), state.liveIncidents)
        assertFalse(state.liveLoading)
        assertFalse(state.liveError)
    }

    @Test
    fun liveError_setsErrorTrueAndKeepsCachedLiveData() {
        val position = ReducerFixtures.livePositions.first()
        val initial = NjordUiState(liveLoading = true, livePositions = listOf(position))
        val state = reduce(initial, NjordAction.LiveError)

        assertEquals(listOf(position), state.livePositions)
        assertTrue(state.liveError)
        assertFalse(state.liveLoading)
    }
}

private object ReducerFixtures {
    val livePositions = listOf(
        LivePosition(
            id = "hype-long",
            symbol = "HYPE",
            side = "Long",
            strategy = StrategyFilter.BigBang,
            strategyName = "Big Bang",
            opened = "10h",
            pnl = "+$186",
            pct = "+8.4%",
            size = "53.8 HYPE",
            capital = "$2.2k",
            entry = "$41.20",
            current = "$44.66",
            trendUp = true
        ),
        LivePosition(
            id = "arb-short",
            symbol = "ARB",
            side = "Short",
            strategy = StrategyFilter.Wcr,
            strategyName = "WCR",
            opened = "22h",
            pnl = "-$31",
            pct = "-2.2%",
            size = "1,050 ARB",
            capital = "$1.1k",
            entry = "$1.02",
            current = "$1.04",
            trendUp = false
        )
    )

    val logs = listOf(
        LogEntry(
            level = LogFilter.Error,
            strategy = StrategyFilter.BigBang,
            title = "Big Bang open failed",
            message = "Exchange rejected order: insufficient margin",
            time = "13:52",
            searchText = "Big Bang error open insufficient margin"
        ),
        LogEntry(
            level = LogFilter.Error,
            strategy = StrategyFilter.Wcr,
            title = "WCR close failed",
            message = "Hyperliquid rejected order: insufficient margin after retry",
            time = "13:48",
            searchText = "WCR error close insufficient margin"
        ),
        LogEntry(
            level = LogFilter.Warn,
            strategy = StrategyFilter.All,
            title = "Weekly performance heartbeat late",
            message = "Expected cadence missed",
            time = "13:34",
            searchText = "weekly report warning late"
        )
    )

    val incidents = listOf(
        Incident(
            id = "eth-open-failed",
            title = "ETH open failed",
            subtitle = "Big Bang / Long",
            current = "Rejected",
            threshold = "Margin",
            detail = "Open order was rejected because available margin was insufficient.",
            badge = "Error",
            tone = Tone.Danger,
            age = "8m",
            reason = "Margin"
        )
    )

    val hunchReport = HunchReport(
        title = "Hunch BTC Signal",
        persistedAge = "Persisted 18m ago",
        signal = "SELL",
        signalTone = Tone.Danger,
        confidence = "HIGH",
        score = "-0.523",
        date = "2026-06-07",
        btcPriceAtSignal = "$62,215.00",
        currentBtcPrice = "$63,193.50",
        priceDelta = "+1.57%",
        priceDeltaTone = Tone.Success,
        wasSignalCorrect = "NO",
        wasSignalCorrectTone = Tone.Danger,
        keyFactors = listOf(ReportFactor("ETF flow weakness reduced upside conviction.")),
        risks = listOf(ReportFactor("BTC strength can invalidate the short-term bias.", isRisk = true)),
        layerScores = listOf(LayerScore("ETF flows", "-1.00", Tone.Danger))
    )
}
