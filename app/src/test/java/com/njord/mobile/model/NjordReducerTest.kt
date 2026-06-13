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
            positions = NjordMockData.livePositions,
            strategyFilter = StrategyFilter.Wcr,
            sideFilter = SideFilter.Short
        )

        assertTrue(filtered.isNotEmpty())
        assertTrue(filtered.all { it.strategy == StrategyFilter.Wcr && it.side == "Short" })
    }

    @Test
    fun logFilters_matchSeverityAndSearch() {
        val filtered = visibleLogs(
            logs = NjordMockData.logs,
            filter = LogFilter.Error,
            query = "margin"
        )

        assertEquals(2, filtered.size)
        assertTrue(filtered.all { it.level == LogFilter.Error })
        assertTrue(filtered.all { it.searchText.lowercase().contains("margin") || it.message.lowercase().contains("margin") })
    }

    @Test
    fun dismissIncident_hidesIncidentBanner() {
        val incident = NjordMockData.incidents.first()
        val state = reduce(
            NjordUiState(selectedIncident = incident),
            NjordAction.DismissIncident(incident.id)
        )

        assertTrue(incident.id in state.dismissedIncidentIds)
        assertNull(state.selectedIncident)
    }

    @Test
    fun selectPosition_opensBottomSheetState() {
        val position = NjordMockData.livePositions.first()
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
        val entry = LogEntry(level = LogFilter.Info, title = "hyperliquid.wcr", message = "ok", time = "14:23", searchText = "hyperliquid.wcr ok")
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
        val report = NjordMockData.hunchReport.copy(signal = "BUY", confidence = "MEDIUM")
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
    fun heartbeatLoading_setsLoadingTrueAndErrorFalse() {
        val state = reduce(NjordUiState(heartbeatError = true), NjordAction.HeartbeatLoading)

        assertTrue(state.heartbeatLoading)
        assertFalse(state.heartbeatError)
    }

    @Test
    fun heartbeatLoaded_replacesRoutinesAndCounts() {
        val routine = HeartbeatRoutine("VPN heartbeat", "Healthy", "1m ago", "20m", Tone.Success)
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
}
