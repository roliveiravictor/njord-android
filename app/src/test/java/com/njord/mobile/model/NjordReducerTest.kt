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
}
