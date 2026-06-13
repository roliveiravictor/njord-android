package com.njord.mobile.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class NjordApiClientTest {

    private fun makeValidJson(vararg entries: String): String {
        val entriesJson = entries.joinToString(",")
        return """{"entries":[$entriesJson],"total_count":${entries.size},"truncated":false}"""
    }

    private fun makeEntry(level: String, title: String, message: String, timestamp: String): String =
        """{"level":"$level","title":"$title","message":"$message","timestamp":"$timestamp","strategy":null,"symbol":null}"""

    @Test
    fun logsUrl_requestsFullServerPageForLatest24Hours() {
        val url = NjordApiClient.logsUrl("https://noatun.dev")

        assertEquals("https://noatun.dev/v1/logs?hours=24&limit=5000", url)
    }

    @Test
    fun parseLogsResponse_wellFormedJson_returnsSuccessWithEntries() {
        val json = makeValidJson(
            makeEntry("INFO", "hyperliquid.wcr", "Rebalance complete", "2026-06-12T14:23:01"),
            makeEntry("WARNING", "strategy.broker", "Slippage exceeded", "2026-06-12T14:24:00")
        )

        val result = NjordApiClient.parseLogsResponse(json)

        assertTrue(result is LogsResult.Success)
        val entries = (result as LogsResult.Success).entries
        assertEquals(2, entries.size)
        assertEquals("INFO", entries[0].level)
        assertEquals("hyperliquid.wcr", entries[0].title)
        assertEquals("Rebalance complete", entries[0].message)
        assertEquals("2026-06-12T14:23:01", entries[0].timestamp)
        assertEquals("WARNING", entries[1].level)
    }

    @Test
    fun parseLogsResponse_emptyEntriesArray_returnsSuccessWithEmptyList() {
        val json = """{"entries":[],"total_count":0,"truncated":false}"""

        val result = NjordApiClient.parseLogsResponse(json)

        assertTrue(result is LogsResult.Success)
        assertTrue((result as LogsResult.Success).entries.isEmpty())
    }

    @Test
    fun parseLogsResponse_malformedJson_returnsError() {
        val result = NjordApiClient.parseLogsResponse("not json")

        assertTrue(result is LogsResult.Error)
        assertNotNull((result as LogsResult.Error).message)
    }

    @Test
    fun parseLogsResponse_missingEntriesKey_returnsError() {
        val json = """{"total_count":0,"truncated":false}"""

        val result = NjordApiClient.parseLogsResponse(json)

        assertTrue(result is LogsResult.Error)
    }

    @Test
    fun parseHomeResponse_wellFormedJson_returnsSuccessWithoutMappingIncidents() {
        val json = """
            {
              "total_equity": 18420.0,
              "available_margin": 7800.0,
              "in_use": 3100.0,
              "open_position_count": 18,
              "unrealized_pnl": 428.0,
              "unrealized_pnl_pct": 3.8,
              "strategies": [
                {
                  "name": "Big Bang",
                  "is_live": true,
                  "position_count": 2,
                  "symbols": ["HYPE", "ETH"],
                  "unrealized_pnl": 212.0,
                  "unrealized_pnl_pct": 4.1
                }
              ],
              "latest_cycle": {
                "timestamp": "2026-06-12T14:00:00",
                "opened_count": 2,
                "closed_count": 1,
                "kept_count": 15
              },
              "heartbeat": {
                "healthy": 7,
                "total": 8,
                "late_count": 1
              },
              "incidents": [
                {
                  "timestamp": "2026-06-12T14:00:00",
                  "level": "error",
                  "category": "order",
                  "title": "Remote incident",
                  "message": "Ignored by Android home for now"
                }
              ]
            }
        """.trimIndent()

        val result = NjordApiClient.parseHomeResponse(json)

        assertTrue(result is HomeResult.Success)
        val response = (result as HomeResult.Success).response
        assertEquals(18420.0, response.totalEquity, 0.0001)
        assertEquals(1, response.strategies.size)
        assertEquals("Big Bang", response.strategies[0].name)
        assertEquals(listOf("HYPE", "ETH"), response.strategies[0].symbols)
        assertEquals(2, response.latestCycle?.openedCount)
        assertEquals(7, response.heartbeat.healthy)
        assertEquals(8, response.heartbeat.total)
    }

    @Test
    fun parseHomeResponse_missingHeartbeat_returnsError() {
        val result = NjordApiClient.parseHomeResponse("""{"strategies":[]}""")

        assertTrue(result is HomeResult.Error)
    }

    @Test
    fun mapApiHome_formatsSnapshotForHomeCards() {
        val response = HomeApiResponse(
            totalEquity = 18420.0,
            availableMargin = 7800.0,
            inUse = 3100.0,
            openPositionCount = 18,
            unrealizedPnl = -428.0,
            unrealizedPnlPct = -3.8,
            strategies = listOf(
                HomeApiStrategy(
                    name = "WCR",
                    isLive = true,
                    positionCount = 3,
                    symbols = listOf("HYPE", "BTC", "ETH"),
                    unrealizedPnl = 184.0,
                    unrealizedPnlPct = 2.6
                )
            ),
            latestCycle = HomeApiCycle(
                timestamp = "2026-06-12T14:00:00",
                openedCount = 2,
                closedCount = 1,
                keptCount = 15
            ),
            heartbeat = HomeApiHeartbeat(healthy = 7, total = 8, lateCount = 1)
        )

        val snapshot = mapApiHome(response)

        assertEquals("\$18,420.00", snapshot.totalEquity)
        assertEquals("-\$428.00", snapshot.unrealizedPnl)
        assertEquals("-3.8% unrealized", snapshot.unrealizedPnlPct)
        assertEquals("\$7.8K", snapshot.availableMargin)
        assertEquals("\$3.1K", snapshot.inUse)
        assertEquals("18 Pos", snapshot.openPositionCount)
        assertEquals("2", snapshot.activitySummary?.opened)
        assertEquals("HYPE · BTC · ETH", snapshot.strategies[0].assets)
        assertEquals("+\$184.00", snapshot.strategies[0].pnl)
        assertEquals("+2.6%", snapshot.strategies[0].pct)
    }

    @Test
    fun parseHunchReportResponse_wellFormedJson_returnsSuccessWithReport() {
        val json = """
            {
              "date":"2026-06-12",
              "signal":"BEARISH",
              "raw_signal":"SELL",
              "created_at":"2026-06-12T09:05:00+00:00",
              "confidence":"HIGH",
              "score":-0.523,
              "btc_price_at_signal":62215.0,
              "current_btc_price":63193.5,
              "price_delta_pct":1.57,
              "was_signal_correct":false,
              "key_factors":["ETF weakness","Macro pressure"],
              "risks":["Peace deal"],
              "layer_scores":{"geopolitical":-0.8,"macro":0.0,"on_chain":0.5}
            }
        """.trimIndent()

        val result = NjordApiClient.parseHunchReportResponse(json)

        assertTrue(result is HunchReportResult.Success)
        val report = (result as HunchReportResult.Success).report
        assertEquals("2026-06-12", report.date)
        assertEquals("BEARISH", report.signal)
        assertEquals("SELL", report.rawSignal)
        assertEquals(-0.523, report.score ?: 0.0, 0.0001)
        assertEquals(false, report.wasSignalCorrect)
        assertEquals(listOf("ETF weakness", "Macro pressure"), report.keyFactors)
        assertEquals(listOf("Peace deal"), report.risks)
        assertEquals(-0.8, report.layerScores["geopolitical"] ?: 0.0, 0.0001)
    }

    @Test
    fun parseHunchReportResponse_missingSignal_returnsError() {
        val result = NjordApiClient.parseHunchReportResponse("""{"date":"2026-06-12"}""")

        assertTrue(result is HunchReportResult.Error)
    }

    @Test
    fun mapApiReport_formatsReportForUi() {
        val apiReport = HunchReportApiResponse(
            date = "2026-06-12",
            signal = "BEARISH",
            rawSignal = "SELL",
            createdAt = "2026-06-12T09:05:00+00:00",
            confidence = "high",
            score = -0.523,
            btcPriceAtSignal = 62215.0,
            currentBtcPrice = 63193.5,
            priceDeltaPct = 1.57,
            wasSignalCorrect = false,
            keyFactors = listOf("ETF weakness"),
            risks = listOf("Peace deal"),
            layerScores = mapOf("geopolitical" to -0.8, "on_chain" to 0.5)
        )

        val report = mapApiReport(apiReport)

        assertEquals("Hunch BTC Signal - 2026-06-12", report.title)
        assertEquals("SELL", report.signal)
        assertEquals("HIGH", report.confidence)
        assertEquals("-0.523", report.score)
        assertEquals("$62,215.00", report.btcPriceAtSignal)
        assertEquals("$63,193.50", report.currentBtcPrice)
        assertEquals("+1.57%", report.priceDelta)
        assertEquals("NO", report.wasSignalCorrect)
        assertEquals(1, report.keyFactors.size)
        assertEquals(1, report.risks.size)
        assertEquals(2, report.layerScores.size)
    }

    @Test
    fun parseHeartbeatResponse_wellFormedJson_returnsSuccessWithServices() {
        val json = """
            {
              "healthy_count": 7,
              "late_count": 1,
              "critical_count": 0,
              "total_count": 8,
              "services": [
                {
                  "name": "vpn_heartbeat",
                  "display_name": "VPN heartbeat",
                  "status": "healthy",
                  "last_seen_at": "2026-06-12T14:20:00+00:00",
                  "expected_cadence_seconds": 1200,
                  "seconds_overdue": null
                }
              ]
            }
        """.trimIndent()

        val result = NjordApiClient.parseHeartbeatResponse(json)

        assertTrue(result is HeartbeatResult.Success)
        val success = result as HeartbeatResult.Success
        assertEquals(7, success.healthyCount)
        assertEquals(1, success.lateCount)
        assertEquals(0, success.criticalCount)
        assertEquals(8, success.totalCount)
        assertEquals(1, success.services.size)
        assertEquals("vpn_heartbeat", success.services[0].name)
        assertEquals("VPN heartbeat", success.services[0].displayName)
        assertEquals("healthy", success.services[0].status)
        assertEquals(1200, success.services[0].expectedCadenceSeconds)
        assertEquals(null, success.services[0].secondsOverdue)
    }

    @Test
    fun parseHeartbeatResponse_missingServicesKey_returnsError() {
        val result = NjordApiClient.parseHeartbeatResponse("""{"healthy_count":0,"total_count":0}""")

        assertTrue(result is HeartbeatResult.Error)
    }

    @Test
    fun mapApiHeartbeat_formatsRowsAndCountsForUi() {
        val apiResult = HeartbeatResult.Success(
            healthyCount = 7,
            lateCount = 1,
            criticalCount = 0,
            totalCount = 8,
            services = listOf(
                HeartbeatApiService(
                    name = "database_snapshot",
                    displayName = "Database snapshot",
                    status = "unknown",
                    lastSeenAt = null,
                    expectedCadenceSeconds = 1200,
                    secondsOverdue = null
                ),
                HeartbeatApiService(
                    name = "vpn_heartbeat",
                    displayName = "VPN heartbeat",
                    status = "healthy",
                    lastSeenAt = "2026-06-12T14:20:00+00:00",
                    expectedCadenceSeconds = 1200,
                    secondsOverdue = null
                ),
                HeartbeatApiService(
                    name = "weekly_performance_report",
                    displayName = "Weekly performance report",
                    status = "late",
                    lastSeenAt = "2026-06-12T13:20:00+00:00",
                    expectedCadenceSeconds = 604800,
                    secondsOverdue = 60
                )
            )
        )

        val snapshot = mapApiHeartbeat(apiResult, now = Instant.parse("2026-06-12T14:23:00Z"))

        assertEquals(7, snapshot.healthyCount)
        assertEquals(1, snapshot.lateCount)
        assertEquals(0, snapshot.criticalCount)
        assertEquals(8, snapshot.totalCount)
        assertEquals("VPN heartbeat", snapshot.routines[0].name)
        assertEquals("Healthy", snapshot.routines[0].status)
        assertEquals("3m ago", snapshot.routines[0].age)
        assertEquals("20m", snapshot.routines[0].cadence)
        assertEquals("Database snapshot", snapshot.routines[1].name)
        assertEquals("Unknown", snapshot.routines[1].status)
        assertEquals("Never", snapshot.routines[1].age)
        assertEquals("Late", snapshot.routines[2].status)
        assertEquals("7d", snapshot.routines[2].cadence)
    }
}
