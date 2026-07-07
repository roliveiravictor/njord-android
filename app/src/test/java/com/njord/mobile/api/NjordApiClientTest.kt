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
    fun performanceUrl_requestsSelectedStrategy() {
        val url = NjordApiClient.performanceUrl("https://noatun.dev", "big_bang")

        assertEquals("https://noatun.dev/v1/performance?strategy=big_bang", url)
    }

    @Test
    fun liveUrl_requestsLiveEndpointWithNoStrategyParam() {
        val url = NjordApiClient.liveUrl("https://noatun.dev")

        assertEquals("https://noatun.dev/v1/live", url)
    }

    @Test
    fun liveUrl_appendsStrategyParamWhenNotAll() {
        val url = NjordApiClient.liveUrl("https://noatun.dev", "big_bang")

        assertEquals("https://noatun.dev/v1/live?strategy=big_bang", url)
    }

    @Test
    fun apiErrorMessage_prefersApiMessageField() {
        val message = NjordApiClient.apiErrorMessage(
            """{"message":"API key is invalid","detail":"not this"}""",
            "HTTP 401"
        )

        assertEquals("API key is invalid", message)
    }

    @Test
    fun apiErrorMessage_usesFallbackForBlankBody() {
        val message = NjordApiClient.apiErrorMessage("", "HTTP 500")

        assertEquals("HTTP 500", message)
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
    fun parseHomeResponse_wellFormedJson_returnsSuccessWithIncidents() {
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
              "logs": {
                "warning_count": 8,
                "error_count": 1,
                "total_count": 9,
                "hours": 24
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
        assertEquals(8, response.logs.warningCount)
        assertEquals(1, response.logs.errorCount)
        assertEquals(9, response.logs.totalCount)
        assertEquals(24, response.logs.hours)
        assertEquals(1, response.incidents.size)
        assertEquals("Remote incident", response.incidents[0].title)
        assertEquals("order", response.incidents[0].category)
    }

    @Test
    fun parseHomeResponse_missingHeartbeat_returnsError() {
        val result = NjordApiClient.parseHomeResponse("""{"strategies":[]}""")

        assertTrue(result is HomeResult.Error)
    }

    @Test
    fun parseHomeResponse_missingLogs_usesZeroSummary() {
        val json = """
            {
              "strategies": [],
              "heartbeat": {
                "healthy": 8,
                "total": 8,
                "late_count": 0
              }
            }
        """.trimIndent()

        val result = NjordApiClient.parseHomeResponse(json)

        assertTrue(result is HomeResult.Success)
        val response = (result as HomeResult.Success).response
        assertEquals(0, response.logs.warningCount)
        assertEquals(0, response.logs.errorCount)
        assertEquals(0, response.logs.totalCount)
        assertEquals(24, response.logs.hours)
    }

    @Test
    fun parseHomeResponse_missingLatestCycleCount_returnsError() {
        val json = """
            {
              "strategies": [],
              "latest_cycle": {
                "timestamp": "2026-06-15T00:06:06.182041+00:00",
                "opened_count": 7,
                "kept_count": 1
              },
              "heartbeat": {
                "healthy": 8,
                "total": 8,
                "late_count": 0
              }
            }
        """.trimIndent()

        val result = NjordApiClient.parseHomeResponse(json)

        assertTrue(result is HomeResult.Error)
    }

    @Test
    fun parseActivityResponse_missingCycleTotals_returnsError() {
        val json = """
            {
              "cycles": [
                {
                  "timestamp": "2026-06-15T00:06:06.182041+00:00",
                  "cycle_status": "complete",
                  "total_opened": 7,
                  "total_kept": 1,
                  "strategies": []
                }
              ]
            }
        """.trimIndent()

        val result = NjordApiClient.parseActivityResponse(json)

        assertTrue(result is ActivityResult.Error)
    }

    @Test
    fun parsePerformanceResponse_wellFormedJson_returnsSuccessWithMetrics() {
        val json = """
            {
              "total_equity":18420.0,
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
                "realized_pnl":62.71,
                "unrealized_pnl":4.48,
                "win_rate":40.0,
                "profit_factor":1.67,
                "sharpe_ratio":-1.15,
                "total_closed_trades":30,
                "average_trade_pct":-0.2443
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
              },
              "latest_closed_positions":[
                {
                  "symbol":"SOL/USDT",
                  "side":"short",
                  "strategy":"wcr",
                  "strategy_name":"WCR",
                  "entry_price":150.25,
                  "exit_price":142.5,
                  "quantity":3.2,
                  "capital_allocated":480.8,
                  "pnl":24.8,
                  "pnl_pct":5.16,
                  "entry_date":"2026-06-10T20:00:00Z",
                  "exit_date":"2026-06-14T20:00:00Z",
                  "closure_reason":"take_profit"
                }
              ]
            }
        """.trimIndent()

        val result = NjordApiClient.parsePerformanceResponse(json)

        assertTrue(result is PerformanceResult.Success)
        val response = (result as PerformanceResult.Success).response
        assertEquals(18420.0, response.totalEquity, 0.0001)
        assertEquals(96.0, response.performanceStrip.todayPnl ?: 0.0, 0.0001)
        assertEquals(30, response.totalClosedTrades)
        assertEquals(40.0, response.winRate, 0.0001)
        assertEquals(1.67, response.profitFactor, 0.0001)
        assertEquals(-1.15, response.sharpeRatio, 0.0001)
        assertEquals(2, response.equityCurve.size)
        assertEquals(-2.1, response.drawdownSeries[1].drawdownPct, 0.0001)
        assertEquals("2026-05", response.monthlyStats.bestMonth?.month)
        assertEquals("SOL/USDT", response.latestClosedPositions.single().symbol)
        assertEquals(24.8, response.latestClosedPositions.single().pnl, 0.0001)
    }

    @Test
    fun parsePerformanceResponse_missingLiveMetrics_returnsSuccess() {
        val result = NjordApiClient.parsePerformanceResponse("""{"performance_strip":{}}""")

        assertTrue(result is PerformanceResult.Success)
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
            heartbeat = HomeApiHeartbeat(healthy = 7, total = 8, lateCount = 1),
            logs = HomeApiLogs(warningCount = 8, errorCount = 1, totalCount = 9, hours = 24),
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

        val snapshot = mapApiHome(response, now = Instant.parse("2026-06-14T20:29:02Z"))

        assertEquals("\$18,420.00", snapshot.totalBalance)
        assertEquals("-\$428.00", snapshot.unrealizedPnl)
        assertEquals("-3.8% unrealized", snapshot.unrealizedPnlPct)
        assertEquals("\$7.8K", snapshot.availableMargin)
        assertEquals("\$3.1K", snapshot.inUse)
        assertEquals("\$4.2K", snapshot.marginInUse)
        assertEquals("18 Pos", snapshot.openPositionCount)
        assertEquals("2", snapshot.activitySummary?.opened)
        assertEquals(8, snapshot.logsSummary.warningCount)
        assertEquals(1, snapshot.logsSummary.errorCount)
        assertEquals(9, snapshot.logsSummary.totalCount)
        assertEquals(24, snapshot.logsSummary.hours)
        assertEquals("HYPE · BTC · ETH", snapshot.strategies[0].assets)
        assertEquals("+\$184.00", snapshot.strategies[0].pnl)
        assertEquals("+2.6%", snapshot.strategies[0].pct)
        assertEquals("WCR", snapshot.incidents.single().subtitle)
        assertEquals("5m", snapshot.incidents.single().age)
    }

    @Test
    fun mapApiPerformance_formatsSnapshotForPerformanceCards() {
        val response = PerformanceApiResponse(
            totalEquity = 18420.0,
            allTimeReturnPct = 84.2,
            performanceStrip = PerformanceStripApiResponse(
                todayPnl = 96.0,
                todayPnlPct = 0.5,
                sevenDayPnl = 812.0,
                sevenDayPnlPct = 4.6,
                thirtyDayPnl = 2400.0,
                thirtyDayPnlPct = 14.8
            ),
            winRate = 56.0,
            profitFactor = 1.42,
            sharpeRatio = 0.87,
            totalClosedTrades = 124,
            maxWinStreak = 5,
            maxLoseStreak = 3,
            currentStreak = 2,
            equityCurve = listOf(
                PerformanceEquityPointApiResponse("2026-05-10", 16000.0),
                PerformanceEquityPointApiResponse("2026-06-10", 18420.0)
            ),
            drawdownSeries = listOf(
                PerformanceDrawdownPointApiResponse("2026-05-10", 0.0),
                PerformanceDrawdownPointApiResponse("2026-06-10", -2.1)
            ),
            maxDrawdownPct = -6.4,
            currentDrawdownPct = -2.1,
            recoveryPct = 63.0,
            monthlyReturns = listOf(
                PerformanceMonthlyReturnApiResponse("2026-05", 300.0, 3.8),
                PerformanceMonthlyReturnApiResponse("2026-06", 120.0, 1.4)
            ),
            monthlyStats = PerformanceMonthlyStatsApiResponse(
                bestMonth = null,
                worstMonth = null,
                averageMonthlyPnl = null
            ),
            latestClosedPositions = listOf(
                PerformanceClosedPositionApiResponse(
                    symbol = "ATOM/USDT",
                    side = "long",
                    strategy = "big_bang",
                    strategyName = "Big Bang",
                    entryPrice = 2.0137,
                    exitPrice = 2.1939,
                    quantity = 170.61,
                    capitalAllocated = 343.5574,
                    pnl = 30.74,
                    pnlPct = 8.95,
                    entryDate = "2026-06-11T20:00:00Z",
                    exitDate = "2026-06-14T20:00:00Z",
                    closureReason = "take_profit"
                )
            )
        )

        val snapshot = mapApiPerformance(response)

        assertEquals("\$18.4K", snapshot.totalEquity)
        assertEquals(Tone.Success, snapshot.totalEquityTone)
        assertEquals("+84.2%", snapshot.returnBadge)
        assertEquals("N/A", snapshot.unrealizedPnl)
        assertEquals(Tone.Muted, snapshot.unrealizedTone)
        assertEquals("+\$96.00", snapshot.todayPnl)
        assertEquals("56.0%", snapshot.historyMetrics[0].value)
        assertEquals("1.4", snapshot.historyMetrics[1].value)
        assertEquals("0.9", snapshot.historyMetrics[2].value)
        assertEquals("CURRENT", snapshot.streakMetrics[0].label)
        assertEquals("+2", snapshot.streakMetrics[0].value)
        assertEquals(Tone.Success, snapshot.streakMetrics[0].tone)
        assertEquals("Win streak", snapshot.streakMetrics[0].subtext)
        assertEquals("WIN STREAK", snapshot.streakMetrics[1].label)
        assertEquals("5", snapshot.streakMetrics[1].value)
        assertEquals("LOSE STREAK", snapshot.streakMetrics[2].label)
        assertEquals("3", snapshot.streakMetrics[2].value)
        assertEquals("May", snapshot.monthlyReturns[0].month)
        assertEquals(2, snapshot.equityCurve.size)
        assertEquals("$16,000.00", snapshot.equityCurve[0].valueLabel)
        assertEquals("May 10", snapshot.equityCurve[0].pointLabel)
        assertEquals("-2.1%", snapshot.drawdownCurve[1].valueLabel)
        assertEquals("-2.1%", snapshot.drawdownStats[0].value)
        assertEquals("ATOM", snapshot.latestClosedPositions.single().symbol)
        assertEquals("+\$30.74", snapshot.latestClosedPositions.single().pnl)
        assertEquals("+9.0%", snapshot.latestClosedPositions.single().pct)
        assertEquals("Jun 14 · Take Profit", snapshot.latestClosedPositions.single().subtitle)
    }

    @Test
    fun mapApiPerformance_marksNegativeTotalEquityDanger() {
        val response = PerformanceApiResponse(
            totalEquity = -240.0,
            allTimeReturnPct = 12.0,
            performanceStrip = PerformanceStripApiResponse(
                todayPnl = null,
                todayPnlPct = null,
                sevenDayPnl = null,
                sevenDayPnlPct = null,
                thirtyDayPnl = null,
                thirtyDayPnlPct = null
            ),
            winRate = 0.0,
            profitFactor = 0.0,
            sharpeRatio = 0.0,
            totalClosedTrades = 0,
            maxWinStreak = 0,
            maxLoseStreak = 0,
            currentStreak = 0,
            equityCurve = emptyList(),
            drawdownSeries = emptyList(),
            maxDrawdownPct = 0.0,
            currentDrawdownPct = 0.0,
            recoveryPct = 0.0,
            monthlyReturns = emptyList(),
            monthlyStats = PerformanceMonthlyStatsApiResponse(
                bestMonth = null,
                worstMonth = null,
                averageMonthlyPnl = null
            ),
            latestClosedPositions = emptyList()
        )

        val snapshot = mapApiPerformance(response)

        assertEquals("-\$240.00", snapshot.totalEquity)
        assertEquals(Tone.Danger, snapshot.totalEquityTone)
        assertEquals(Tone.Success, snapshot.returnTone)
    }

    @Test
    fun mapApiPerformance_formatsNullCurrentStreakAsNotAvailable() {
        val response = PerformanceApiResponse(
            totalEquity = 100.0,
            allTimeReturnPct = 1.0,
            performanceStrip = PerformanceStripApiResponse(
                todayPnl = null,
                todayPnlPct = null,
                sevenDayPnl = null,
                sevenDayPnlPct = null,
                thirtyDayPnl = null,
                thirtyDayPnlPct = null
            ),
            winRate = 0.0,
            profitFactor = 0.0,
            sharpeRatio = 0.0,
            totalClosedTrades = 0,
            maxWinStreak = 5,
            maxLoseStreak = 3,
            currentStreak = null,
            equityCurve = emptyList(),
            drawdownSeries = emptyList(),
            maxDrawdownPct = 0.0,
            currentDrawdownPct = 0.0,
            recoveryPct = 0.0,
            monthlyReturns = emptyList(),
            monthlyStats = PerformanceMonthlyStatsApiResponse(
                bestMonth = null,
                worstMonth = null,
                averageMonthlyPnl = null
            ),
            latestClosedPositions = emptyList()
        )

        val snapshot = mapApiPerformance(response)

        assertEquals("N/A", snapshot.streakMetrics[0].value)
        assertEquals(Tone.Muted, snapshot.streakMetrics[0].tone)
        assertEquals("Aggregate", snapshot.streakMetrics[0].subtext)
        assertEquals("5", snapshot.streakMetrics[1].value)
        assertEquals("3", snapshot.streakMetrics[2].value)
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
        assertEquals("Leveraged capital", analytics?.summaryItems?.first()?.subtext)
        assertEquals("2d", analytics?.summaryItems?.get(1)?.value)
        assertEquals("INTEGRITY", analytics?.summaryItems?.get(2)?.label)
        assertEquals("1/1", analytics?.summaryItems?.get(2)?.value)
        assertEquals("Cache vs. CEX", analytics?.summaryItems?.get(2)?.subtext)
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
    fun parseHunchReportsResponse_wellFormedJson_returnsSuccessWithReports() {
        val json = """
            {
              "reports": [
                {"date":"2026-06-12","signal":"BEARISH","raw_signal":"SELL"},
                {"date":"2026-06-11","signal":"BULLISH","raw_signal":"BUY"},
                {"date":"2026-06-10","signal":"NEUTRAL","raw_signal":"NEUTRAL"}
              ]
            }
        """.trimIndent()

        val result = NjordApiClient.parseHunchReportsResponse(json)

        assertTrue(result is HunchReportsResult.Success)
        val reports = (result as HunchReportsResult.Success).reports
        assertEquals(3, reports.size)
        assertEquals(listOf("2026-06-12", "2026-06-11", "2026-06-10"), reports.map { it.date })
    }

    @Test
    fun parseHunchReportsResponse_emptyReportsArray_returnsSuccessWithEmptyList() {
        val result = NjordApiClient.parseHunchReportsResponse("""{"reports":[]}""")

        assertTrue(result is HunchReportsResult.Success)
        assertEquals(emptyList<HunchReportApiResponse>(), (result as HunchReportsResult.Success).reports)
    }

    @Test
    fun parseHunchReportsResponse_missingReportsKey_returnsError() {
        val result = NjordApiClient.parseHunchReportsResponse("""{}""")

        assertTrue(result is HunchReportsResult.Error)
    }

    @Test
    fun parseHunchReportsResponse_skipsMalformedEntries() {
        val json = """
            {
              "reports": [
                {"date":"2026-06-12","signal":"BEARISH"},
                {"date":"2026-06-11"}
              ]
            }
        """.trimIndent()

        val result = NjordApiClient.parseHunchReportsResponse(json)

        assertTrue(result is HunchReportsResult.Success)
        val reports = (result as HunchReportsResult.Success).reports
        assertEquals(1, reports.size)
        assertEquals("2026-06-12", reports.single().date)
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
    fun mapApiEntries_causeStrategy_setsFilterStrategyAndPreservesEntryTitle() {
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
        assertEquals("session.snapshot", log.title)
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

        val snapshot = mapApiHeartbeat(apiResult, now = Instant.parse("2026-06-12T14:25:00Z"))

        assertEquals(1, snapshot.healthyCount)
        assertEquals(1, snapshot.lateCount)
        assertEquals(0, snapshot.criticalCount)
        assertEquals(8, snapshot.totalCount)
        assertEquals("VPN", snapshot.routines[0].name)
        assertEquals("Healthy", snapshot.routines[0].status)
        assertEquals(Instant.parse("2026-06-12T14:20:00Z"), snapshot.routines[0].lastSeenAt)
        assertEquals("20m", snapshot.routines[0].cadence)
        assertEquals("Database", snapshot.routines[1].name)
        assertEquals("Unknown", snapshot.routines[1].status)
        assertEquals(null, snapshot.routines[1].lastSeenAt)
        assertEquals("Late", snapshot.routines[2].status)
        assertEquals("7d", snapshot.routines[2].cadence)
    }

    @Test
    fun mapApiHeartbeat_recomputesStatusAndCountsFromDeviceClock() {
        val apiResult = HeartbeatResult.Success(
            healthyCount = 8,
            lateCount = 0,
            criticalCount = 0,
            totalCount = 8,
            services = listOf(
                HeartbeatApiService(
                    name = "candle_close_liveness",
                    displayName = "Candle close liveness",
                    status = "healthy",
                    lastSeenAt = "2026-06-12T14:00:00Z",
                    expectedCadenceSeconds = 1200,
                    secondsOverdue = null
                ),
                HeartbeatApiService(
                    name = "activity_snapshot",
                    displayName = "Activity snapshot",
                    status = "healthy",
                    lastSeenAt = "2026-06-12T13:00:00Z",
                    expectedCadenceSeconds = 1200,
                    secondsOverdue = null
                )
            )
        )

        val snapshot = mapApiHeartbeat(apiResult, now = Instant.parse("2026-06-12T14:35:00Z"))

        assertEquals(0, snapshot.healthyCount)
        assertEquals(1, snapshot.lateCount)
        assertEquals(1, snapshot.criticalCount)
        assertEquals(8, snapshot.totalCount)
        assertEquals("Late", snapshot.routines[0].status)
        assertEquals(900, snapshot.routines[0].secondsOverdue)
        assertEquals("Critical", snapshot.routines[1].status)
        assertEquals(4500, snapshot.routines[1].secondsOverdue)
    }
}
