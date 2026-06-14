package com.njord.mobile

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test

class NjordAppTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun homeScreen_matchesReferenceContent() {
        compose.onNodeWithTag("homeEquityHero").assertIsDisplayed()
        compose.onNodeWithText("$18,420.00").assertIsDisplayed()
        compose.onNodeWithText("Total Equity").assertIsDisplayed()
        compose.onNodeWithText("Strategies").assertIsDisplayed()
        compose.onNodeWithText("2 positions · 2L").assertIsDisplayed()
        compose.onNodeWithText("HYPE · ETH").assertIsDisplayed()
        compose.onNodeWithText("3 positions · 2L / 1S").assertIsDisplayed()
        compose.onNodeWithText("HYPE · BTC · ETH").assertIsDisplayed()
        compose.onNodeWithText("0 positions").assertIsDisplayed()
        compose.onNodeWithText("Activity").assertIsDisplayed()
        compose.onNodeWithText("Candle close").assertIsDisplayed()
        compose.onNodeWithText("Heartbeat").assertIsDisplayed()
        compose.onNodeWithText("Current health").assertIsDisplayed()
        compose.onNodeWithText("Incidents").assertIsDisplayed()
        compose.onNodeWithText("3 active").assertIsDisplayed()
    }

    @Test
    fun bottomNavigation_switchesTopLevelScreens() {
        compose.onNodeWithTag("nav-Portfolio").performClick()
        compose.onNodeWithTag("screen-Portfolio").assertIsDisplayed()

        compose.onNodeWithTag("nav-Live").performClick()
        compose.onNodeWithTag("screen-Live").assertIsDisplayed()

        compose.onNodeWithTag("nav-Home").performClick()
        compose.onNodeWithTag("screen-Home").assertIsDisplayed()
    }

    @Test
    fun homeBalanceWidget_opensPortfolio() {
        compose.onNodeWithTag("homeEquityHero").performClick()

        compose.onNodeWithTag("screen-Portfolio").assertIsDisplayed()
    }

    @Test
    fun portfolioScreen_matchesReferenceContent() {
        compose.onNodeWithTag("nav-Portfolio").performClick()

        compose.onNodeWithText("PORTFOLIO PERFORMANCE").assertIsDisplayed()
        compose.onAllNodesWithText("$18.4k")[0].assertIsDisplayed()
        compose.onNodeWithText("Total equity").assertIsDisplayed()
        compose.onNodeWithText("ALL +127.4%").assertIsDisplayed()

        compose.onNodeWithText("Live metrics").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("REALIZED P&L").assertIsDisplayed()
        compose.onNodeWithText("UNREALIZED P&L").assertIsDisplayed()
        compose.onNodeWithText("Monthly stats").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Performance history").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun portfolioStrategyChips_remainSelectable() {
        compose.onNodeWithTag("nav-Portfolio").performClick()
        compose.onNodeWithTag("filter-WCR").performClick()

        compose.onNodeWithTag("filter-WCR").assertIsDisplayed()
        compose.onNodeWithTag("screen-Portfolio").assertIsDisplayed()
    }

    @Test
    fun moreMenu_opensActivityReportsHeartbeatLogs() {
        compose.onNodeWithTag("nav-More").performClick()
        compose.onNodeWithTag("more-Activity").performClick()
        compose.onNodeWithTag("screen-Activity").assertIsDisplayed()

        compose.onNodeWithTag("nav-More").performClick()
        compose.onNodeWithTag("more-Reports").performClick()
        compose.onNodeWithTag("screen-Reports").assertIsDisplayed()

        compose.onNodeWithTag("nav-More").performClick()
        compose.onNodeWithTag("more-Heartbeat").performClick()
        compose.onNodeWithTag("screen-Heartbeat").assertIsDisplayed()

        compose.onNodeWithTag("nav-More").performClick()
        compose.onNodeWithTag("more-Logs").performClick()
        compose.onNodeWithTag("screen-Logs").assertIsDisplayed()
    }

    @Test
    fun remoteBackedScreens_doNotShowLoadingCopy() {
        compose.onAllNodesWithText("Loading…").assertCountEquals(0)
        compose.onAllNodesWithText("Loading strategies…").assertCountEquals(0)
        compose.onAllNodesWithText("Loading latest cycle…").assertCountEquals(0)

        compose.onNodeWithTag("nav-More").performClick()
        compose.onNodeWithTag("more-Activity").performClick()
        compose.onAllNodesWithText("Loading activity…").assertCountEquals(0)

        compose.onNodeWithTag("nav-More").performClick()
        compose.onNodeWithTag("more-Reports").performClick()
        compose.onAllNodesWithText("Loading Hunch report…").assertCountEquals(0)

        compose.onNodeWithTag("nav-More").performClick()
        compose.onNodeWithTag("more-Heartbeat").performClick()
        compose.onAllNodesWithText("Loading heartbeat…").assertCountEquals(0)
        compose.onAllNodesWithText("Loading service health…").assertCountEquals(0)

        compose.onNodeWithTag("nav-More").performClick()
        compose.onNodeWithTag("more-Logs").performClick()
        compose.onAllNodesWithText("Loading logs…").assertCountEquals(0)
    }

    @Test
    fun moreScreen_matchesGroupedReferenceLayout() {
        compose.onNodeWithTag("nav-More").performClick()

        compose.onNodeWithTag("screen-More").assertIsDisplayed()
        compose.onNodeWithTag("more-Activity").assertIsDisplayed()
        compose.onNodeWithTag("more-Reports").assertIsDisplayed()
        compose.onNodeWithTag("more-Heartbeat").assertIsDisplayed()
        compose.onNodeWithTag("more-Logs").assertIsDisplayed()
        compose.onAllNodesWithText("Operational diagnostics and reports").assertCountEquals(0)
        compose.onAllNodesWithText("Candle-close cycles and strategy activity").assertCountEquals(0)
        compose.onAllNodesWithText("Hunch signal report").assertCountEquals(0)
        compose.onAllNodesWithText("8 concrete Njord service routines").assertCountEquals(0)
        compose.onAllNodesWithText("Latest 24 hours").assertCountEquals(0)
    }

    @Test
    fun heartbeatScreen_matchesReferenceLayout() {
        compose.onNodeWithTag("nav-More").performClick()
        compose.onNodeWithTag("more-Heartbeat").performClick()

        compose.onNodeWithTag("screen-Heartbeat").assertIsDisplayed()
        compose.onNodeWithTag("heartbeatHealthCard").assertIsDisplayed()
        compose.onAllNodesWithText("8 concrete Njord service routines").assertCountEquals(0)
        compose.onNodeWithText("HEARTBEAT HEALTH").assertIsDisplayed()
        compose.onNodeWithText("7 / 8").assertIsDisplayed()
        compose.onNodeWithText("7 OK").assertIsDisplayed()
        compose.onNodeWithText("1 late").assertIsDisplayed()
        compose.onNodeWithText("0 critical").assertIsDisplayed()
        compose.onNodeWithText("Service routines").assertIsDisplayed()
        compose.onNodeWithText("VPN heartbeat").assertIsDisplayed()
        compose.onNodeWithText("2m ago").assertIsDisplayed()
        compose.onNodeWithText("Strategy liveness").assertIsDisplayed()
        compose.onNodeWithText("Database snapshot").assertIsDisplayed()
        compose.onNodeWithText("Log snapshot").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun reportsScreen_matchesReferenceSummaryLayout() {
        compose.onNodeWithTag("nav-More").performClick()
        compose.onNodeWithTag("more-Reports").performClick()

        compose.onNodeWithTag("screen-Reports").assertIsDisplayed()
        compose.onNodeWithTag("reportReferencePanel").assertIsDisplayed()
        compose.onAllNodesWithText("Reports").assertCountEquals(0)
        compose.onAllNodesWithText("Hunch signal report").assertCountEquals(0)
        compose.onNodeWithText("Hunch BTC Signal — 2026-06-08").assertIsDisplayed()
        compose.onNodeWithText("Latest report").assertIsDisplayed()
        compose.onNodeWithText("Persisted 18m ago").assertIsDisplayed()
        compose.onAllNodesWithText("Fresh").assertCountEquals(0)
        compose.onAllNodesWithText("SELL")[0].assertIsDisplayed()
        compose.onNodeWithText("Confidence: HIGH | Score: -0.523").assertIsDisplayed()
        compose.onNodeWithText("SUMMARY").assertIsDisplayed()
        compose.onNodeWithText("Last Signal").assertIsDisplayed()
        compose.onNodeWithText("Last Confidence").assertIsDisplayed()
        compose.onNodeWithText("Last Score").assertIsDisplayed()
        compose.onNodeWithText("-0.468").assertIsDisplayed()
        compose.onNodeWithText("Last Signal Date").assertIsDisplayed()
        compose.onNodeWithText("2026-06-07").assertIsDisplayed()
        compose.onNodeWithText("Last BTC Price").assertIsDisplayed()
        compose.onNodeWithText("$62,215.00").assertIsDisplayed()
        compose.onNodeWithText("Current BTC Price").assertIsDisplayed()
        compose.onNodeWithText("$63,193.50").assertIsDisplayed()
        compose.onNodeWithText("Price Delta").assertIsDisplayed()
        compose.onNodeWithText("+1.57%").assertIsDisplayed()
        compose.onNodeWithText("Last Signal Correct").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("NO").assertIsDisplayed()
        compose.onNodeWithText("Key factors").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun activityScreen_matchesMoreReferenceLayout() {
        compose.onNodeWithTag("nav-More").performClick()
        compose.onNodeWithTag("more-Activity").performClick()

        compose.onNodeWithTag("screen-Activity").assertIsDisplayed()
        compose.onNodeWithTag("nav-More").assertIsDisplayed()
        compose.onNodeWithTag("activityReferencePanel").assertIsDisplayed()
        compose.onNodeWithText("Candle close").assertIsDisplayed()
        compose.onAllNodesWithText("Complete").assertCountEquals(0)
        compose.onAllNodesWithText("Candle-close cycles and strategy activity").assertCountEquals(0)
        compose.onNodeWithText("+2").assertIsDisplayed()
        compose.onNodeWithText("-1").assertIsDisplayed()
        compose.onNodeWithText("•15").assertIsDisplayed()
        compose.onAllNodesWithText("ACTION")[0].assertIsDisplayed()
        compose.onAllNodesWithText("ASSET")[0].assertIsDisplayed()
        compose.onAllNodesWithText("SIDE")[0].assertIsDisplayed()
        compose.onNodeWithText("Big Bang").assertIsDisplayed()
        compose.onNodeWithText("WCR").assertIsDisplayed()
        compose.onNodeWithText("ETH").assertIsDisplayed()
        compose.onNodeWithText("BTC").assertIsDisplayed()
        compose.onNodeWithText("HYPE").assertIsDisplayed()
        compose.onNodeWithText("SOL").assertIsDisplayed()
        compose.onNodeWithText("SUI").assertIsDisplayed()
        compose.onNodeWithText("ARB").assertIsDisplayed()
        compose.onAllNodesWithText("LONG")[0].assertIsDisplayed()
        compose.onAllNodesWithText("SHORT")[0].assertIsDisplayed()
        compose.onNodeWithTag("activityAction-ETH").assertIsDisplayed()
        compose.onNodeWithTag("activityAction-BTC").assertIsDisplayed()
        compose.onNodeWithTag("activityAction-HYPE").assertIsDisplayed()
    }

    @Test
    fun logsSearch_emptyStateAppears() {
        compose.onNodeWithTag("nav-More").performClick()
        compose.onNodeWithTag("more-Logs").performClick()
        compose.onAllNodesWithText("Latest logs").assertCountEquals(0)
        compose.onAllNodesWithText("Last 24h").assertCountEquals(0)
        compose.onNodeWithTag("logSearch").performTextInput("not a real njord log")

        compose.onNodeWithTag("emptyLogs").assertIsDisplayed()
    }

    @Test
    fun liveScreen_matchesReferenceFeedContent() {
        compose.onNodeWithTag("nav-Live").performClick()

        compose.onNodeWithTag("liveIncidentCarousel").assertIsDisplayed()
        compose.onNodeWithText("ETH open failed").assertIsDisplayed()
        compose.onNodeWithTag("liveIncidentDot-0").assertIsDisplayed()
        compose.onNodeWithTag("livePosition-HYPE").assertIsDisplayed()
        compose.onAllNodesWithText("SIZE")[0].assertIsDisplayed()
        compose.onAllNodesWithText("CAPITAL")[0].assertIsDisplayed()
        compose.onNodeWithText("Open P&L by strategy").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("+$428").assertIsDisplayed()
        compose.onAllNodesWithText("Open positions only").assertCountEquals(0)
        compose.onAllNodesWithText("+$428 total").assertCountEquals(0)
        compose.onNodeWithText("Live summary").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Position integrity").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun liveIncidentCarousel_showsSingleIncidentAtATime() {
        compose.onNodeWithTag("nav-Live").performClick()

        compose.onNodeWithTag("liveIncidentCarousel").assertIsDisplayed()
        compose.onNodeWithText("ETH open failed").assertIsDisplayed()
        compose.onAllNodesWithText("ARB close failed").assertCountEquals(0)
    }

    @Test
    fun livePositionTap_showsPositionBottomSheet() {
        compose.onNodeWithTag("nav-Live").performClick()
        compose.onNodeWithTag("livePosition-HYPE").performClick()

        compose.onNodeWithTag("positionSheet").assertIsDisplayed()
        compose.onNodeWithText("53.8 HYPE").assertIsDisplayed()
    }

    @Test
    fun liveIncidentCarousel_keepsIncidentModalTap() {
        compose.onNodeWithTag("nav-Live").performClick()
        compose.onNodeWithText("ETH open failed").performClick()

        compose.onNodeWithTag("incidentDialog").assertIsDisplayed()
        compose.onNodeWithText("Rejected").assertIsDisplayed()
        compose.onNodeWithText("Margin").assertIsDisplayed()
        compose.onNodeWithText("Open order was rejected by the exchange because available margin was insufficient.").assertIsDisplayed()
        compose.onNodeWithText("Close").assertIsDisplayed()
        compose.onNodeWithText("Dismiss").assertIsDisplayed()
    }

    @Test
    fun livePanels_useResponsiveRows() {
        compose.onNodeWithTag("nav-Live").performClick()

        compose.onNodeWithText("Live summary").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("POSITIONS").assertIsDisplayed()
        compose.onNodeWithText("OPEN P&L").assertIsDisplayed()
        compose.onNodeWithText("Position integrity").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("MATCHED").assertIsDisplayed()
        compose.onNodeWithText("DUPLICATE").assertIsDisplayed()
    }

    @Test
    fun liveIncidentDismiss_removesIncidentFromFeed() {
        compose.onNodeWithTag("nav-Live").performClick()
        compose.onNodeWithText("ETH open failed").performClick()
        compose.onNodeWithText("Dismiss").performClick()

        compose.onAllNodesWithText("ETH open failed").assertCountEquals(0)
        compose.onNodeWithTag("livePosition-HYPE").assertIsDisplayed()
    }

    @Test
    fun incidentTap_opensLiveView() {
        compose.onNodeWithTag("homeIncidentsCard").performClick()

        compose.onNodeWithTag("screen-Live").assertIsDisplayed()
        compose.onNodeWithTag("liveIncidentCarousel").assertIsDisplayed()
    }
}
