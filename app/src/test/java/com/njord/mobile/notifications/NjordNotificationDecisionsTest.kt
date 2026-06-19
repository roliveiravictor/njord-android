package com.njord.mobile.notifications

import com.njord.mobile.api.ActivityApiCycle
import com.njord.mobile.api.ActivityApiPosition
import com.njord.mobile.api.ActivityApiResponse
import com.njord.mobile.api.ActivityApiStrategy
import com.njord.mobile.api.HeartbeatApiService
import com.njord.mobile.api.LiveApiIncident
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class NjordNotificationDecisionsTest {
    @Test
    fun incidentNotification_returnsContentWhenIncidentsExist() {
        val content = NjordNotificationDecisions.incidentNotification(
            listOf(
                LiveApiIncident(
                    timestamp = "2026-06-19T12:00:00Z",
                    level = "error",
                    category = "safety_abort",
                    title = "Safety abort",
                    message = "Positions remain after safety close",
                    strategy = "wcr",
                    symbol = null
                )
            )
        )

        assertNotNull(content)
        assertEquals("Safety abort", content?.title)
        assertEquals("Positions remain after safety close", content?.body)
    }

    @Test
    fun activityNotification_requiresChangedCycleWithPositionUpdates() {
        val response = activityResponse(timestamp = "2026-06-19T21:30:00Z")
        val previous = NjordNotificationDecisions.activitySignature(response)

        assertNull(NjordNotificationDecisions.activityNotification(response, previous))

        val changed = activityResponse(timestamp = "2026-06-20T21:30:00Z")
        val decision = NjordNotificationDecisions.activityNotification(changed, previous)

        assertNotNull(decision)
        assertEquals("Njord activity updated", decision?.first?.title)
        assertEquals("1 opened, 0 closed, 1 kept", decision?.first?.body)
    }

    @Test
    fun activityNotification_ignoresEmptyPositionUpdates() {
        val response = ActivityApiResponse(
            cycles = listOf(
                ActivityApiCycle(
                    timestamp = "2026-06-19T21:30:00Z",
                    cycleStatus = "complete",
                    totalOpened = 0,
                    totalClosed = 0,
                    totalKept = 0,
                    strategies = emptyList()
                )
            )
        )

        assertNull(NjordNotificationDecisions.activityNotification(response, null))
    }

    @Test
    fun heartbeatNotification_listsUnhealthyServices() {
        val content = NjordNotificationDecisions.heartbeatNotification(
            listOf(
                HeartbeatApiService("vpn_heartbeat", "VPN heartbeat", "healthy", null, 1200, null),
                HeartbeatApiService("daily_report", "Daily report", "late", null, 86400, 60)
            )
        )

        assertNotNull(content)
        assertEquals("Njord heartbeat needs attention", content?.title)
        assertEquals("Daily report", content?.body)
    }

    private fun activityResponse(timestamp: String): ActivityApiResponse =
        ActivityApiResponse(
            cycles = listOf(
                ActivityApiCycle(
                    timestamp = timestamp,
                    cycleStatus = "complete",
                    totalOpened = 1,
                    totalClosed = 0,
                    totalKept = 1,
                    strategies = listOf(
                        ActivityApiStrategy(
                            name = "wcr",
                            opened = listOf(ActivityApiPosition("BTC/USDT", "long")),
                            closed = emptyList(),
                            kept = listOf(ActivityApiPosition("ETH/USDT", "short"))
                        )
                    )
                )
            )
        )
}
