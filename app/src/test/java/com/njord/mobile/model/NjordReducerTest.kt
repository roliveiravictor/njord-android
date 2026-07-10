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
        assertTrue(incident.id in state.dismissedIncidentIds)
        assertNull(state.selectedIncident)
    }

    @Test
    fun liveLoaded_ignoresDismissedIncidentsFromServerRefresh() {
        val incident = ReducerFixtures.incidents.first()
        val position = ReducerFixtures.livePositions.first()
        val state = reduce(
            NjordUiState(dismissedIncidentIds = setOf(incident.id)),
            NjordAction.LiveLoaded(listOf(position), null, listOf(incident))
        )

        assertTrue(state.liveIncidents.isEmpty())
    }

    @Test
    fun homeLoaded_ignoresDismissedIncidentsFromServerRefresh() {
        val incident = ReducerFixtures.incidents.first()
        val snapshot = ReducerFixtures.homeSnapshot.copy(incidents = listOf(incident))
        val state = reduce(
            NjordUiState(dismissedIncidentIds = setOf(incident.id)),
            NjordAction.HomeLoaded(snapshot)
        )

        assertTrue(state.liveIncidents.isEmpty())
        assertTrue(state.homeSnapshot?.incidents.orEmpty().isEmpty())
    }

    @Test
    fun acknowledgedIncidentsLoaded_filtersSeededIncidents() {
        val incident = ReducerFixtures.incidents.first()
        val loaded = reduce(
            NjordUiState(selectedIncident = incident, liveIncidents = listOf(incident)),
            NjordAction.IncidentAcknowledgementsLoaded(setOf(incident.id))
        )
        val seeded = reduce(loaded, NjordAction.IncidentsSeeded(listOf(incident)))

        assertTrue(seeded.liveIncidents.isEmpty())
        assertNull(seeded.selectedIncident)
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
    fun hunchReportLoaded_replacesReportsAndClearsLoading() {
        val report = ReducerFixtures.hunchReport.copy(signal = "BUY", confidence = "MEDIUM")
        val state = reduce(NjordUiState(hunchReportLoading = true), NjordAction.HunchReportLoaded(listOf(report)))

        assertEquals(listOf(report), state.hunchReports)
        assertFalse(state.hunchReportLoading)
        assertFalse(state.hunchReportError)
    }

    @Test
    fun hunchReportError_setsErrorTrueAndKeepsFallbackReports() {
        val initial = NjordUiState(hunchReportLoading = true)
        val state = reduce(initial, NjordAction.HunchReportError)

        assertEquals(initial.hunchReports, state.hunchReports)
        assertTrue(state.hunchReportError)
        assertFalse(state.hunchReportLoading)
    }

    @Test
    fun performanceLoading_setsLoadingTrueAndErrorFalse() {
        val state = reduce(NjordUiState(performanceError = true), NjordAction.PerformanceLoading)

        assertTrue(state.performanceLoading)
        assertFalse(state.performanceError)
    }

    @Test
    fun performanceLoaded_replacesSnapshotAndClearsLoading() {
        val snapshot = PerformanceSnapshot(
            totalEquity = "\$18.4k",
            totalEquityTone = Tone.Success,
            returnBadge = "ALL +84.2%",
            returnTone = Tone.Success,
            unrealizedPnl = "-\$428",
            unrealizedTone = Tone.Danger,
            todayPnl = "+\$96",
            todayPct = "+0.5%",
            todayTone = Tone.Success,
            sevenDayPnl = "+\$812",
            sevenDayPct = "+4.6%",
            sevenDayTone = Tone.Success,
            thirtyDayPnl = "+\$2.4k",
            thirtyDayPct = "+14.8%",
            thirtyDayTone = Tone.Success,
            historyMetrics = emptyList(),
            streakMetrics = emptyList(),
            monthlyStats = emptyList(),
            equityStats = emptyList(),
            equityCurve = emptyList(),
            equityAxisLabels = emptyList(),
            drawdownStats = emptyList(),
            drawdownCurve = emptyList(),
            drawdownAxisLabels = emptyList(),
            monthlyReturns = emptyList(),
            latestClosedPositions = emptyList()
        )
        val state = reduce(NjordUiState(performanceLoading = true), NjordAction.PerformanceLoaded(snapshot))

        assertEquals(snapshot, state.performanceSnapshot)
        assertFalse(state.performanceLoading)
        assertFalse(state.performanceError)
    }

    @Test
    fun performanceError_setsErrorTrueAndKeepsFallbackSnapshot() {
        val initial = NjordUiState(performanceLoading = true)
        val state = reduce(initial, NjordAction.PerformanceError)

        assertEquals(initial.performanceSnapshot, state.performanceSnapshot)
        assertTrue(state.performanceError)
        assertFalse(state.performanceLoading)
    }

    @Test
    fun homeError_marksCachedStrategiesOffline() {
        val strategy = StrategySummary(
            name = "Big Bang",
            filter = StrategyFilter.BigBang,
            subtitle = "1 position",
            pnl = "+\$42",
            pct = "+1.2%",
            live = true,
            assets = "HYPE"
        )
        val initial = NjordUiState(
            homeLoading = true,
            homeSnapshot = ReducerFixtures.homeSnapshot.copy(strategies = listOf(strategy))
        )
        val state = reduce(initial, NjordAction.HomeError)

        assertFalse(state.homeSnapshot?.strategies.orEmpty().single().live)
        assertTrue(state.homeError)
        assertFalse(state.homeLoading)
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

    val homeSnapshot = HomeSnapshot(
        totalBalance = "\$18.4k",
        unrealizedPnl = "+\$42",
        unrealizedPnlPct = "+0.2%",
        availableMargin = "\$12.1k",
        inUse = "\$6.3k",
        marginInUse = "34%",
        openPositionCount = "2",
        strategies = emptyList(),
        activitySummary = null,
        logsSummary = HomeLogsSummary(warningCount = 0, errorCount = 0, totalCount = 0, hours = 24),
        heartbeatHealthy = 7,
        heartbeatTotal = 8,
        heartbeatLateCount = 1,
        incidents = emptyList()
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
