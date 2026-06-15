package com.njord.mobile.api

import com.njord.mobile.model.StrategyFilter
import com.njord.mobile.model.Tone
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

        assertEquals("https://noatun.dev/v1/logs?hours=24&limit=5000&exclude_heartbeat=true", url)
    }

    @Test
    fun activityUrl_requestsOnlyLatestCycle() {
        val url = NjordApiClient.activityUrl("https://noatun.dev")

        assertEquals("https://noatun.dev/v1/activity?limit=1", url)
    }

    @Test
    fun portfolioUrl_requestsSelectedStrategy() {
        val url = NjordApiClient.portfolioUrl("https://noatun.dev", "big_bang")

        assertEquals("https://noatun.dev/v1/portfolio?strategy=big_bang", url)
    }

    @Test
    fun liveUrl_requestsLiveEndpoint() {
        val url = NjordApiClient.liveUrl("https://noatun.dev")

        assertEquals("https://noatun.dev/v1/live", url)
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
    fun parseLogsResponse_readsStrategyMetadata() {
        val json = makeValidJson(
            """
                {
                  "level":"INFO",
                  "title":"session.snapshot",
                  "message":"Combining active logs into session snapshot",
                  "timestamp":"2026-06-12T14:23:01",
                  "strategy":"wcr",
                  "strategy_name":"WCR",
                  "cause_strategy":"big_bang",
                  "cause_strategy_name":"Big Bang"
                }
            """.trimIndent()
        )

        val result = NjordApiClient.parseLogsResponse(json)

        assertTrue(result is LogsResult.Success)
        val entry = (result as LogsResult.Success).entries.single()
        assertEquals("wcr", entry.strategy)
        assertEquals("WCR", entry.strategyName)
        assertEquals("big_bang", entry.causeStrategy)
        assertEquals("Big Bang", entry.causeStrategyName)
    }

    @Test
    fun parseLogsResponse_prefersFullMessageAndNestedCauseMetadata() {
        val json = makeValidJson(
            """
                {
                  "level":"INFO",
                  "title":"session.snapshot",
                  "message":"Combining active logs into session snapshot...",
                  "full_message":"Combining active logs into session snapshot with 24 records",
                  "timestamp":"2026-06-12T14:23:01",
                  "cause":{"strategy":"big_bang","strategy_name":"Big Bang"}
                }
            """.trimIndent()
        )

        val result = NjordApiClient.parseLogsResponse(json)

        assertTrue(result is LogsResult.Success)
        val entry = (result as LogsResult.Success).entries.single()
        assertEquals("Combining active logs into session snapshot with 24 records", entry.message)
        assertEquals("big_bang", entry.causeStrategy)
        assertEquals("Big Bang", entry.causeStrategyName)
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
              "total_balance": 18420.0,
              "available_margin": 7800.0,
              "in_use": 3100.0,
              "margin_in_use": 4200.0,
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
        assertEquals(18420.0, response.totalBalance, 0.0001)
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
    fun parsePortfolioResponse_wellFormedJson_returnsSuccessWithMetrics() {
        val json = """
            {
              "total_equity":18420.0,
              "initial_capital":10000.0,
              "all_time_return_pct":84.2,
              "performance_strip":{
                "today_pnl":96.0,
                "today_pnl_pct":0.5,
                "seven_day_pnl":812.0,
                "seven_day_pnl_pct":4.6,
                "thirty_day_pnl":2400.0,
                "thirty_day_pnl_pct":14.8
              },
              "live_metrics":{
                "realized_pnl":1900.0,
                "unrealized_pnl":428.0,
                "win_rate":56.0,
                "profit_factor":1.42,
                "total_closed_trades":124
              },
              "equity_curve":[
                {"timestamp":"2026-05-10","equity":16000.0},
                {"timestamp":"2026-06-10","equity":18420.0}
              ],
              "drawdown_series":[
                {"timestamp":"2026-05-10","drawdown_pct":0.0},
                {"timestamp":"2026-06-10","drawdown_pct":-2.1}
              ],
              "max_drawdown_pct":-6.4,
              "current_drawdown_pct":-2.1,
              "recovery_pct":63.0,
              "monthly_returns":[
                {"month":"2026-05","pnl":300.0,"pnl_pct":3.8},
                {"month":"2026-06","pnl":120.0,"pnl_pct":1.4}
              ],
              "monthly_stats":{
                "best_month":{"month":"2026-05","pnl":300.0,"pnl_pct":3.8},
                "worst_month":{"month":"2026-06","pnl":120.0,"pnl_pct":1.4},
                "average_monthly_pnl":2.6
              }
            }
        """.trimIndent()

        val result = NjordApiClient.parsePortfolioResponse(json)

        assertTrue(result is PortfolioResult.Success)
        val response = (result as PortfolioResult.Success).response
        assertEquals(18420.0, response.totalEquity, 0.0001)
        assertEquals(96.0, response.performanceStrip.todayPnl ?: 0.0, 0.0001)
        assertEquals(124, response.liveMetrics.totalClosedTrades)
        assertEquals(2, response.equityCurve.size)
        assertEquals(-2.1, response.drawdownSeries[1].drawdownPct, 0.0001)
        assertEquals("2026-05", response.monthlyStats.bestMonth?.month)
    }

    @Test
    fun parsePortfolioResponse_missingLiveMetrics_returnsError() {
        val result = NjordApiClient.parsePortfolioResponse("""{"performance_strip":{}}""")

        assertTrue(result is PortfolioResult.Error)
    }

    @Test
    fun parseLiveResponse_wellFormedJson_returnsPositionsAnalyticsAndIncidents() {
        val json = """
            {
              "positions": [
                {
                  "id": "atom/usdt-long",
                  "symbol": "ATOM/USDT",
                  "side": "Long",
                  "strategy": "big_bang",
                  "strategy_name": "Big Bang",
                  "opened": "2d",
                  "entry_price": 2.0137,
                  "current_price": 1.9939,
                  "capital": 343.5574,
                  "quantity": 170.61,
                  "unrealized_pnl": -3.3781,
                  "unrealized_pnl_pct": -0.9832,
                  "trend_up": false
                }
              ],
              "analytics": {
                "strategy_contributions": [
                  {"strategy_name": "Big Bang", "unrealized_pnl": -3.38, "contribution_pct": 0.9668}
                ],
                "live_summary": {
                  "position_count": 1,
                  "long_count": 1,
                  "short_count": 0,
                  "long_pct": 1.0,
                  "short_pct": 0.0,
                  "total_unrealized_pnl": -3.38,
                  "total_capital": 343.56,
                  "avg_age_hours": 48.0
                },
                "live_metrics": {
                  "largest_winner": null,
                  "largest_loser": {
                    "id": "atom/usdt-long",
                    "symbol": "ATOM/USDT",
                    "side": "Long",
                    "strategy": "big_bang",
                    "strategy_name": "Big Bang",
                    "opened": "2d",
                    "entry_price": 2.0137,
                    "current_price": 1.9939,
                    "capital": 343.5574,
                    "quantity": 170.61,
                    "unrealized_pnl": -3.3781,
                    "unrealized_pnl_pct": -0.9832,
                    "trend_up": false
                  }
                },
                "integrity": {"matched": 0, "unclaimed": 1, "missing": 1, "duplicate": 0}
              },
              "incidents": [
                {
                  "timestamp": "2026-06-14T20:24:02.551205+00:00",
                  "level": "ERROR",
                  "category": "safety_abort",
                  "title": "WCR safety abort: positions remain",
                  "message": "Positions remain after safety close",
                  "strategy": "wcr",
                  "symbol": null
                }
              ]
            }
        """.trimIndent()

        val result = NjordApiClient.parseLiveResponse(json)

        assertTrue(result is LiveResult.Success)
        val response = (result as LiveResult.Success).response
        assertEquals(1, response.positions.size)
        assertEquals("ATOM/USDT", response.positions[0].symbol)
        assertEquals(-3.3781, response.positions[0].unrealizedPnl, 0.0001)
        assertEquals(1, response.analytics?.strategyContributions?.size)
        assertEquals(1, response.analytics?.liveSummary?.positionCount)
        assertEquals(1, response.incidents.size)
        assertEquals("safety_abort", response.incidents[0].category)
    }

    @Test
    fun parseLiveResponse_missingPositions_returnsError() {
        val result = NjordApiClient.parseLiveResponse("""{"analytics":{}}""")

        assertTrue(result is LiveResult.Error)
    }

    @Test
    fun mapApiHome_formatsSnapshotForHomeCards() {
        val response = HomeApiResponse(
            totalBalance = 18420.0,
            availableMargin = 7800.0,
            inUse = 3100.0,
            marginInUse = 4200.0,
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

        assertEquals("\$18,420.00", snapshot.totalBalance)
        assertEquals("-\$428.00", snapshot.unrealizedPnl)
        assertEquals("-3.8% unrealized", snapshot.unrealizedPnlPct)
        assertEquals("\$7.8K", snapshot.availableMargin)
        assertEquals("\$3.1K", snapshot.inUse)
        assertEquals("\$4.2K", snapshot.marginInUse)
        assertEquals("18 Pos", snapshot.openPositionCount)
        assertEquals("2", snapshot.activitySummary?.opened)
        assertEquals("HYPE · BTC · ETH", snapshot.strategies[0].assets)
        assertEquals("+\$184.00", snapshot.strategies[0].pnl)
        assertEquals("+2.6%", snapshot.strategies[0].pct)
    }

    @Test
    fun mapApiPortfolio_formatsSnapshotForPortfolioCards() {
        val response = PortfolioApiResponse(
            totalEquity = 18420.0,
            initialCapital = 10000.0,
            allTimeReturnPct = 84.2,
            performanceStrip = PortfolioPerformanceStripApiResponse(
                todayPnl = 96.0,
                todayPnlPct = 0.5,
                sevenDayPnl = 812.0,
                sevenDayPnlPct = 4.6,
                thirtyDayPnl = 2400.0,
                thirtyDayPnlPct = 14.8
            ),
            liveMetrics = PortfolioLiveMetricsApiResponse(
                realizedPnl = 1900.0,
                unrealizedPnl = -428.0,
                winRate = 56.0,
                profitFactor = 1.42,
                totalClosedTrades = 124
            ),
            equityCurve = listOf(
                PortfolioEquityPointApiResponse("2026-05-10", 16000.0),
                PortfolioEquityPointApiResponse("2026-06-10", 18420.0)
            ),
            drawdownSeries = listOf(
                PortfolioDrawdownPointApiResponse("2026-05-10", 0.0),
                PortfolioDrawdownPointApiResponse("2026-06-10", -2.1)
            ),
            maxDrawdownPct = -6.4,
            currentDrawdownPct = -2.1,
            recoveryPct = 63.0,
            monthlyReturns = listOf(
                PortfolioMonthlyReturnApiResponse("2026-05", 300.0, 3.8),
                PortfolioMonthlyReturnApiResponse("2026-06", 120.0, 1.4)
            ),
            monthlyStats = PortfolioMonthlyStatsApiResponse(
                bestMonth = null,
                worstMonth = null,
                averageMonthlyPnl = null
            )
        )

        val snapshot = mapApiPortfolio(response)

        assertEquals("\$18.4K", snapshot.totalEquity)
        assertEquals("ALL +84.2%", snapshot.returnBadge)
        assertEquals("+\$96.00", snapshot.todayPnl)
        assertEquals("-\$428.00", snapshot.liveMetrics[1].value)
        assertEquals("56.0%", snapshot.liveMetrics[2].value)
        assertEquals("May", snapshot.monthlyReturns[0].month)
        assertEquals(2, snapshot.equityCurve.size)
        assertEquals("-2.1%", snapshot.drawdownStats[0].value)
    }

    @Test
    fun mapApiLive_formatsPositionsAnalyticsAndIncidentsForUi() {
        val position = LiveApiPosition(
            id = "atom/usdt-long",
            symbol = "ATOM/USDT",
            side = "Long",
            strategy = "big_bang",
            strategyName = "Big Bang",
            opened = "2d",
            entryPrice = 2.0137,
            currentPrice = 1.9939,
            capital = 343.5574,
            quantity = 170.61,
            unrealizedPnl = -3.3781,
            unrealizedPnlPct = -0.9832,
            trendUp = false
        )
        val response = LiveApiResponse(
            positions = listOf(position),
            analytics = LiveApiAnalytics(
                strategyContributions = listOf(LiveApiStrategyContribution("Big Bang", -3.38, 0.9668)),
                liveSummary = LiveApiSummary(
                    positionCount = 1,
                    longCount = 1,
                    shortCount = 0,
                    longPct = 1.0,
                    shortPct = 0.0,
                    totalUnrealizedPnl = -3.38,
                    totalCapital = 343.56,
                    avgAgeHours = 48.0
                ),
                liveMetrics = LiveApiMetrics(largestWinner = null, largestLoser = position),
                integrity = LiveApiIntegrity(matched = 0, unclaimed = 1, missing = 1, duplicate = 0)
            ),
            incidents = listOf(
                LiveApiIncident(
                    timestamp = "2026-06-14T20:24:02Z",
                    level = "ERROR",
                    category = "safety_abort",
                    title = "WCR safety abort",
                    message = "Positions remain after safety close",
                    strategy = "wcr",
                    symbol = null
                )
            )
        )

        val (positions, analytics, incidents) = mapApiLive(response, now = Instant.parse("2026-06-14T20:29:02Z"))

        assertEquals(1, positions.size)
        assertEquals("ATOM", positions[0].symbol)
        assertEquals(StrategyFilter.BigBang, positions[0].strategy)
        assertEquals("-\$3.38", positions[0].pnl)
        assertEquals("-1.0%", positions[0].pct)
        assertEquals("170.61 ATOM", positions[0].size)
        assertEquals("-\$3.38", analytics?.totalContribution)
        assertEquals("Big Bang", analytics?.strategyContributions?.single()?.strategy)
        assertEquals("2d", analytics?.summaryItems?.get(2)?.value)
        assertEquals("INTEGRITY", analytics?.summaryItems?.last()?.label)
        assertEquals("1/1", analytics?.summaryItems?.last()?.value)
        assertEquals("Cache vs. CEX", analytics?.summaryItems?.last()?.subtext)
        assertEquals("ATOM", analytics?.largestLoser?.symbol)
        assertEquals("1", analytics?.integrityItems?.get(1)?.value)
        assertEquals("WCR", incidents.single().subtitle)
        assertEquals("5m", incidents.single().age)
    }

    @Test
    fun mapApiActivity_usesLatestReturnedCycleAndNormalizesSymbols() {
        val response = ActivityApiResponse(
            cycles = listOf(
                ActivityApiCycle(
                    timestamp = "2026-06-14T00:07:44.757602+00:00",
                    cycleStatus = "complete",
                    totalOpened = 0,
                    totalClosed = 0,
                    totalKept = 1,
                    strategies = listOf(
                        ActivityApiStrategy(
                            name = "big_bang",
                            opened = emptyList(),
                            closed = emptyList(),
                            kept = listOf(ActivityApiPosition("ATOM/USDT:USDT", "long"))
                        )
                    )
                ),
                ActivityApiCycle(
                    timestamp = "2026-06-14T00:03:29.689404+00:00",
                    cycleStatus = "complete",
                    totalOpened = 2,
                    totalClosed = 1,
                    totalKept = 0,
                    strategies = listOf(
                        ActivityApiStrategy(
                            name = "wcr",
                            opened = listOf(
                                ActivityApiPosition("TON/USDT", "long"),
                                ActivityApiPosition("ENA/USDT", "short")
                            ),
                            closed = listOf(ActivityApiPosition("BTC/USDT", "long")),
                            kept = emptyList()
                        )
                    )
                )
            )
        )

        val (summary, cycles) = mapApiActivity(response)

        assertEquals("0", summary.opened)
        assertEquals("0", summary.closed)
        assertEquals("1", summary.kept)
        assertEquals(listOf("Big Bang"), cycles.map { it.strategy })
        assertEquals(listOf("ATOM"), cycles.single().actions.map { it.symbol })
        assertEquals(listOf("Long"), cycles.single().actions.map { it.side })
    }

    @Test
    fun mapApiActivity_ignoresOlderCyclesFromCachedMultiCycleResponses() {
        val response = ActivityApiResponse(
            cycles = listOf(
                ActivityApiCycle(
                    timestamp = "2026-06-14T00:03:29.689404+00:00",
                    cycleStatus = "complete",
                    totalOpened = 1,
                    totalClosed = 1,
                    totalKept = 0,
                    strategies = listOf(
                        ActivityApiStrategy(
                            name = "wcr",
                            opened = listOf(ActivityApiPosition("PENGU/USDT", "long")),
                            closed = listOf(ActivityApiPosition("BCH/USDT", "short")),
                            kept = emptyList()
                        )
                    )
                ),
                ActivityApiCycle(
                    timestamp = "2026-06-13T00:01:45.039083+00:00",
                    cycleStatus = "complete",
                    totalOpened = 0,
                    totalClosed = 1,
                    totalKept = 0,
                    strategies = listOf(
                        ActivityApiStrategy(
                            name = "wcr",
                            opened = emptyList(),
                            closed = listOf(ActivityApiPosition("FET/USDT", "short")),
                            kept = emptyList()
                        )
                    )
                )
            )
        )

        val (_, cycles) = mapApiActivity(response)

        assertEquals(1, cycles.size)
        assertEquals("WCR", cycles.single().strategy)
        assertEquals(listOf("PENGU", "BCH"), cycles.single().actions.map { it.symbol })
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
    fun mapApiReport_neutralSignalShowsCorrectAsNotApplicable() {
        val apiReport = HunchReportApiResponse(
            date = "2026-06-12",
            signal = "NEUTRAL",
            rawSignal = "NEUTRAL",
            createdAt = null,
            confidence = "low",
            score = 0.123,
            btcPriceAtSignal = 62215.0,
            currentBtcPrice = 63193.5,
            priceDeltaPct = 1.57,
            wasSignalCorrect = true,
            keyFactors = emptyList(),
            risks = emptyList(),
            layerScores = emptyMap()
        )

        val report = mapApiReport(apiReport)

        assertEquals("NEUTRAL", report.signal)
        assertEquals("N/A", report.wasSignalCorrect)
        assertEquals(Tone.Muted, report.wasSignalCorrectTone)
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
    fun parseHeartbeatResponse_addsHealthyVpnWhenServiceMissing() {
        val json = """
            {
              "healthy_count": 0,
              "late_count": 0,
              "critical_count": 0,
              "total_count": 0,
              "services": []
            }
        """.trimIndent()

        val result = NjordApiClient.parseHeartbeatResponse(json)

        assertTrue(result is HeartbeatResult.Success)
        val success = result as HeartbeatResult.Success
        assertEquals(1, success.healthyCount)
        assertEquals(0, success.lateCount)
        assertEquals(0, success.criticalCount)
        assertEquals(1, success.totalCount)
        assertEquals("vpn_heartbeat", success.services.single().name)
        assertEquals("healthy", success.services.single().status)
        assertEquals(null, success.services.single().lastSeenAt)
    }

    @Test
    fun parseHeartbeatResponse_treatsAbsentVpnHeartbeatAsHealthy() {
        val json = """
            {
              "healthy_count": 0,
              "late_count": 0,
              "critical_count": 0,
              "total_count": 1,
              "services": [
                {
                  "name": "vpn_heartbeat",
                  "display_name": "VPN heartbeat",
                  "status": "unknown",
                  "last_seen_at": null,
                  "expected_cadence_seconds": 1200,
                  "seconds_overdue": null
                }
              ]
            }
        """.trimIndent()

        val result = NjordApiClient.parseHeartbeatResponse(json)

        assertTrue(result is HeartbeatResult.Success)
        val success = result as HeartbeatResult.Success
        assertEquals(1, success.healthyCount)
        assertEquals(0, success.lateCount)
        assertEquals(0, success.criticalCount)
        assertEquals(1, success.totalCount)
        assertEquals("healthy", success.services.single().status)
    }

    @Test
    fun mapApiEntries_bigBangTitle_assignsBigBangStrategy() {
        val entries = listOf(makeApiEntry("INFO", "Big Bang open failed · ETH Long", "", "2026-06-12T14:00:00"))
        assertEquals(StrategyFilter.BigBang, mapApiEntries(entries)[0].strategy)
    }

    @Test
    fun mapApiEntries_wcrTitle_assignsWcrStrategy() {
        val entries = listOf(makeApiEntry("WARNING", "WCR retraining failed", "", "2026-06-12T14:00:00"))
        assertEquals(StrategyFilter.Wcr, mapApiEntries(entries)[0].strategy)
    }

    @Test
    fun mapApiEntries_hunchTitle_assignsHunchStrategy() {
        val entries = listOf(makeApiEntry("INFO", "Hunch report persisted", "", "2026-06-12T14:00:00"))
        assertEquals(StrategyFilter.Hunch, mapApiEntries(entries)[0].strategy)
    }

    @Test
    fun mapApiEntries_unrecognizedTitle_assignsAllStrategy() {
        val entries = listOf(makeApiEntry("WARN", "Weekly performance heartbeat late", "", "2026-06-12T14:00:00"))
        assertEquals(StrategyFilter.All, mapApiEntries(entries)[0].strategy)
    }

    @Test
    fun mapApiEntries_caseInsensitiveTitle_assignsStrategy() {
        val entries = listOf(makeApiEntry("INFO", "BIG BANG position opened", "", "2026-06-12T14:00:00"))
        assertEquals(StrategyFilter.BigBang, mapApiEntries(entries)[0].strategy)
    }

    @Test
    fun mapApiEntries_causeStrategy_setsCardTitleAndFilterStrategy() {
        val entries = listOf(
            LogApiEntry(
                level = "INFO",
                title = "session.snapshot",
                message = "Combining active logs into session snapshot",
                timestamp = "2026-06-12T14:00:00",
                strategy = "wcr",
                strategyName = "WCR",
                causeStrategy = "big_bang"
            )
        )

        val log = mapApiEntries(entries).single()

        assertEquals(StrategyFilter.BigBang, log.strategy)
        assertEquals("Big Bang", log.title)
    }

    private fun makeApiEntry(level: String, title: String, message: String, timestamp: String) =
        LogApiEntry(level = level, title = title, message = message, timestamp = timestamp)

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
        assertEquals("VPN", snapshot.routines[0].name)
        assertEquals("Healthy", snapshot.routines[0].status)
        assertEquals("3m ago", snapshot.routines[0].age)
        assertEquals("20m", snapshot.routines[0].cadence)
        assertEquals("Database", snapshot.routines[1].name)
        assertEquals("Unknown", snapshot.routines[1].status)
        assertEquals("Never", snapshot.routines[1].age)
        assertEquals("Late", snapshot.routines[2].status)
        assertEquals("7d", snapshot.routines[2].cadence)
    }
}
