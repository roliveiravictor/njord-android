package com.njord.mobile.api

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class LogApiEntry(
    val level: String,
    val title: String,
    val message: String,
    val timestamp: String,
    val strategy: String? = null,
    val strategyName: String? = null,
    val causeStrategy: String? = null,
    val causeStrategyName: String? = null
)

data class HunchReportApiResponse(
    val date: String,
    val signal: String,
    val rawSignal: String?,
    val createdAt: String?,
    val confidence: String?,
    val score: Double?,
    val btcPriceAtSignal: Double?,
    val currentBtcPrice: Double?,
    val priceDeltaPct: Double?,
    val wasSignalCorrect: Boolean?,
    val keyFactors: List<String>,
    val risks: List<String>,
    val layerScores: Map<String, Double?>
)

data class HeartbeatApiService(
    val name: String,
    val displayName: String?,
    val status: String,
    val lastSeenAt: String?,
    val expectedCadenceSeconds: Int,
    val secondsOverdue: Int?
)

data class ActivityApiPosition(
    val symbol: String,
    val side: String
)

data class ActivityApiStrategy(
    val name: String,
    val opened: List<ActivityApiPosition>,
    val closed: List<ActivityApiPosition>,
    val kept: List<ActivityApiPosition>
)

data class ActivityApiCycle(
    val timestamp: String,
    val cycleStatus: String,
    val totalOpened: Int,
    val totalClosed: Int,
    val totalKept: Int,
    val strategies: List<ActivityApiStrategy>
)

data class ActivityApiResponse(
    val cycles: List<ActivityApiCycle>
)

data class HomeApiStrategy(
    val name: String,
    val isLive: Boolean,
    val positionCount: Int,
    val symbols: List<String>,
    val unrealizedPnl: Double?,
    val unrealizedPnlPct: Double?
)

data class HomeApiCycle(
    val timestamp: String,
    val openedCount: Int,
    val closedCount: Int,
    val keptCount: Int
)

data class HomeApiHeartbeat(
    val healthy: Int,
    val total: Int,
    val lateCount: Int
)

data class HomeApiLogs(
    val warningCount: Int,
    val errorCount: Int,
    val totalCount: Int,
    val hours: Int
)

data class HomeApiResponse(
    val totalBalance: Double,
    val availableMargin: Double,
    val inUse: Double,
    val marginInUse: Double,
    val openPositionCount: Int,
    val unrealizedPnl: Double,
    val unrealizedPnlPct: Double,
    val strategies: List<HomeApiStrategy>,
    val latestCycle: HomeApiCycle?,
    val heartbeat: HomeApiHeartbeat,
    val logs: HomeApiLogs,
    val incidents: List<LiveApiIncident>
)

data class PerformanceStripApiResponse(
    val todayPnl: Double?,
    val todayPnlPct: Double?,
    val sevenDayPnl: Double?,
    val sevenDayPnlPct: Double?,
    val thirtyDayPnl: Double?,
    val thirtyDayPnlPct: Double?
)

data class PerformanceEquityPointApiResponse(
    val timestamp: String,
    val equity: Double
)

data class PerformanceDrawdownPointApiResponse(
    val timestamp: String,
    val drawdownPct: Double
)

data class PerformanceMonthlyReturnApiResponse(
    val month: String,
    val pnl: Double,
    val pnlPct: Double
)

data class PerformanceMonthlyStatsApiResponse(
    val bestMonth: PerformanceMonthlyReturnApiResponse?,
    val worstMonth: PerformanceMonthlyReturnApiResponse?,
    val averageMonthlyPnl: Double?
)

data class PerformanceApiResponse(
    val totalEquity: Double,
    val allTimeReturnPct: Double,
    val performanceStrip: PerformanceStripApiResponse,
    val winRate: Double,
    val profitFactor: Double,
    val sharpeRatio: Double,
    val totalClosedTrades: Int,
    val equityCurve: List<PerformanceEquityPointApiResponse>,
    val drawdownSeries: List<PerformanceDrawdownPointApiResponse>,
    val maxDrawdownPct: Double,
    val currentDrawdownPct: Double,
    val recoveryPct: Double,
    val monthlyReturns: List<PerformanceMonthlyReturnApiResponse>,
    val monthlyStats: PerformanceMonthlyStatsApiResponse
)

data class LiveApiPosition(
    val id: String,
    val symbol: String,
    val side: String,
    val strategy: String,
    val strategyName: String,
    val opened: String,
    val entryPrice: Double,
    val currentPrice: Double,
    val capital: Double,
    val quantity: Double?,
    val unrealizedPnl: Double,
    val unrealizedPnlPct: Double,
    val trendUp: Boolean
)

data class LiveApiStrategyContribution(
    val strategyName: String,
    val unrealizedPnl: Double,
    val contributionPct: Double
)

data class LiveApiSummary(
    val positionCount: Int,
    val longCount: Int,
    val shortCount: Int,
    val longPct: Double,
    val shortPct: Double,
    val totalUnrealizedPnl: Double,
    val totalCapital: Double,
    val avgAgeHours: Double
)

data class LiveApiMetrics(
    val largestWinner: LiveApiPosition?,
    val largestLoser: LiveApiPosition?
)

data class LiveApiIntegrity(
    val matched: Int,
    val unclaimed: Int,
    val missing: Int,
    val duplicate: Int
)

data class LiveApiAnalytics(
    val strategyContributions: List<LiveApiStrategyContribution>,
    val liveSummary: LiveApiSummary?,
    val liveMetrics: LiveApiMetrics?,
    val integrity: LiveApiIntegrity?
)

data class LiveApiIncident(
    val timestamp: String,
    val level: String,
    val category: String,
    val title: String,
    val message: String,
    val strategy: String?,
    val symbol: String?
)

data class LiveApiResponse(
    val positions: List<LiveApiPosition>,
    val analytics: LiveApiAnalytics?,
    val incidents: List<LiveApiIncident>
)

sealed interface HomeResult {
    data object Loading : HomeResult
    data class Success(val response: HomeApiResponse) : HomeResult
    data class Error(val message: String) : HomeResult
}

sealed interface PerformanceResult {
    data object Loading : PerformanceResult
    data class Success(val response: PerformanceApiResponse) : PerformanceResult
    data class Error(val message: String) : PerformanceResult
}

sealed interface ActivityResult {
    data object Loading : ActivityResult
    data class Success(val response: ActivityApiResponse) : ActivityResult
    data class Error(val message: String) : ActivityResult
}

sealed interface LiveResult {
    data object Loading : LiveResult
    data class Success(val response: LiveApiResponse) : LiveResult
    data class Error(val message: String) : LiveResult
}

sealed interface LogsResult {
    data object Loading : LogsResult
    data class Success(val entries: List<LogApiEntry>) : LogsResult
    data class Error(val message: String) : LogsResult
}

sealed interface HeartbeatResult {
    data object Loading : HeartbeatResult
    data class Success(
        val healthyCount: Int,
        val lateCount: Int,
        val criticalCount: Int,
        val totalCount: Int,
        val services: List<HeartbeatApiService>
    ) : HeartbeatResult
    data class Error(val message: String) : HeartbeatResult
}

sealed interface HunchReportResult {
    data object Loading : HunchReportResult
    data class Success(val report: HunchReportApiResponse) : HunchReportResult
    data class Error(val message: String) : HunchReportResult
}

sealed interface ApiPayloadResult {
    data class Success(val body: String, val statusCode: Int, val path: String) : ApiPayloadResult
    data class Error(val statusCode: Int?, val path: String, val message: String) : ApiPayloadResult
}

object NjordApiClient {

    fun fetchHome(baseUrl: String, apiKey: String): HomeResult {
        return when (val payload = fetchHomePayload(baseUrl, apiKey)) {
            is ApiPayloadResult.Success -> parseHomeResponse(payload.body)
            is ApiPayloadResult.Error -> HomeResult.Error(payload.message)
        }
    }

    fun fetchActivity(baseUrl: String, apiKey: String): ActivityResult {
        return when (val payload = fetchActivityPayload(baseUrl, apiKey)) {
            is ApiPayloadResult.Success -> parseActivityResponse(payload.body)
            is ApiPayloadResult.Error -> ActivityResult.Error(payload.message)
        }
    }

    fun fetchLive(baseUrl: String, apiKey: String, strategy: String = "all"): LiveResult {
        return when (val payload = fetchLivePayload(baseUrl, apiKey, strategy)) {
            is ApiPayloadResult.Success -> parseLiveResponse(payload.body)
            is ApiPayloadResult.Error -> LiveResult.Error(payload.message)
        }
    }

    fun fetchPerformance(baseUrl: String, apiKey: String, strategy: String): PerformanceResult {
        return when (val payload = fetchPerformancePayload(baseUrl, apiKey, strategy)) {
            is ApiPayloadResult.Success -> parsePerformanceResponse(payload.body)
            is ApiPayloadResult.Error -> PerformanceResult.Error(payload.message)
        }
    }

    fun fetchLogs(baseUrl: String, apiKey: String): LogsResult {
        return when (val payload = fetchLogsPayload(baseUrl, apiKey)) {
            is ApiPayloadResult.Success -> parseLogsResponse(payload.body)
            is ApiPayloadResult.Error -> LogsResult.Error(payload.message)
        }
    }

    fun fetchHeartbeat(baseUrl: String, apiKey: String): HeartbeatResult {
        return when (val payload = fetchHeartbeatPayload(baseUrl, apiKey)) {
            is ApiPayloadResult.Success -> parseHeartbeatResponse(payload.body)
            is ApiPayloadResult.Error -> HeartbeatResult.Error(payload.message)
        }
    }

    fun fetchHunchReport(baseUrl: String, apiKey: String): HunchReportResult {
        return when (val payload = fetchHunchReportPayload(baseUrl, apiKey)) {
            is ApiPayloadResult.Success -> parseHunchReportResponse(payload.body)
            is ApiPayloadResult.Error -> HunchReportResult.Error(payload.message)
        }
    }

    fun fetchHomePayload(baseUrl: String, apiKey: String): ApiPayloadResult =
        fetchPayload("$baseUrl/v1/home", apiKey, "fetchHome")

    fun fetchActivityPayload(baseUrl: String, apiKey: String): ApiPayloadResult =
        fetchPayload(activityUrl(baseUrl), apiKey, "fetchActivity")

    fun fetchLivePayload(baseUrl: String, apiKey: String, strategy: String = "all"): ApiPayloadResult =
        fetchPayload(liveUrl(baseUrl, strategy), apiKey, "fetchLive")

    fun fetchPerformancePayload(baseUrl: String, apiKey: String, strategy: String): ApiPayloadResult =
        fetchPayload(performanceUrl(baseUrl, strategy), apiKey, "fetchPerformance")

    fun fetchLogsPayload(baseUrl: String, apiKey: String): ApiPayloadResult =
        fetchPayload(logsUrl(baseUrl), apiKey, "fetchLogs")

    fun fetchHeartbeatPayload(baseUrl: String, apiKey: String): ApiPayloadResult =
        fetchPayload("$baseUrl/v1/heartbeat", apiKey, "fetchHeartbeat")

    fun fetchHunchReportPayload(baseUrl: String, apiKey: String): ApiPayloadResult =
        fetchPayload("$baseUrl/v1/reports/hunch?date=latest", apiKey, "fetchHunchReport")

    private fun fetchPayload(url: String, apiKey: String, operation: String): ApiPayloadResult {
        Log.d("NjordApi", "$operation url=$url")
        val path = URL(url).file
        val connection = openConnection(url, apiKey)
        return try {
            val code = connection.responseCode
            Log.d("NjordApi", "$operation responseCode=$code")
            if (code !in 200..299) {
                val body = connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
                return ApiPayloadResult.Error(code, path, apiErrorMessage(body, "HTTP $code"))
            }
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            Log.d("NjordApi", "$operation body=$body")
            ApiPayloadResult.Success(body, code, path)
        } catch (e: Exception) {
            Log.e("NjordApi", "$operation error: ${e.javaClass.simpleName}: ${e.message}", e)
            ApiPayloadResult.Error(null, path, (e.message ?: "Unknown error").take(200))
        } finally {
            connection.disconnect()
        }
    }

    internal fun apiErrorMessage(body: String, fallback: String): String {
        if (body.isBlank()) return fallback
        return try {
            val root = JSONObject(body)
            root.firstOptionalString("message", "detail", "error", "title") ?: body.take(240)
        } catch (_: Exception) {
            body.take(240)
        }
    }

    internal fun parseHomeResponse(json: String): HomeResult {
        return try {
            val root = JSONObject(json)
            val strategiesArray = root.optJSONArray("strategies")
                ?: return HomeResult.Error("Missing 'strategies' key")
            val heartbeatObject = root.optJSONObject("heartbeat")
                ?: return HomeResult.Error("Missing 'heartbeat' key")
            val latestCycleObject = root.optJSONObject("latest_cycle")
            val logsObject = root.optJSONObject("logs")
            val incidentsArray = root.optJSONArray("incidents")
            val strategies = (0 until strategiesArray.length()).mapNotNull { i ->
                val obj = strategiesArray.optJSONObject(i) ?: return@mapNotNull null
                HomeApiStrategy(
                    name = obj.optString("name", ""),
                    isLive = obj.optBoolean("is_live", false),
                    positionCount = obj.optInt("position_count", 0),
                    symbols = obj.optionalStringList("symbols"),
                    unrealizedPnl = obj.optionalDouble("unrealized_pnl"),
                    unrealizedPnlPct = obj.optionalDouble("unrealized_pnl_pct")
                )
            }
            HomeResult.Success(
                HomeApiResponse(
                    totalBalance = root.optDouble("total_balance", 0.0),
                    availableMargin = root.optDouble("available_margin", 0.0),
                    inUse = root.optDouble("in_use", 0.0),
                    marginInUse = root.optDouble("margin_in_use", 0.0),
                    openPositionCount = root.optInt("open_position_count", 0),
                    unrealizedPnl = root.optDouble("unrealized_pnl", 0.0),
                    unrealizedPnlPct = root.optDouble("unrealized_pnl_pct", 0.0),
                    strategies = strategies,
                    latestCycle = latestCycleObject?.let {
                        HomeApiCycle(
                            timestamp = it.optString("timestamp", ""),
                            openedCount = it.requiredInt("opened_count"),
                            closedCount = it.requiredInt("closed_count"),
                            keptCount = it.requiredInt("kept_count")
                        )
                    },
                    heartbeat = HomeApiHeartbeat(
                        healthy = heartbeatObject.optInt("healthy", 0),
                        total = heartbeatObject.optInt("total", 0),
                        lateCount = heartbeatObject.optInt("late_count", 0)
                    ),
                    logs = HomeApiLogs(
                        warningCount = logsObject?.optInt("warning_count", 0) ?: 0,
                        errorCount = logsObject?.optInt("error_count", 0) ?: 0,
                        totalCount = logsObject?.optInt("total_count", 0) ?: 0,
                        hours = logsObject?.optInt("hours", 24) ?: 24
                    ),
                    incidents = (0 until (incidentsArray?.length() ?: 0)).mapNotNull { i ->
                        incidentsArray?.optJSONObject(i)?.let(::parseLiveIncident)
                    }
                )
            )
        } catch (e: Exception) {
            HomeResult.Error(e.message ?: "Parse error")
        }
    }

    internal fun parseActivityResponse(json: String): ActivityResult {
        return try {
            val root = JSONObject(json)
            val cyclesArray = root.optJSONArray("cycles") ?: return ActivityResult.Error("Missing 'cycles' key")
            val cycles = (0 until cyclesArray.length()).mapNotNull { i ->
                val cycleObj = cyclesArray.optJSONObject(i) ?: return@mapNotNull null
                val strategiesArray = cycleObj.optJSONArray("strategies") ?: return@mapNotNull null
                val strategies = (0 until strategiesArray.length()).mapNotNull { j ->
                    val stratObj = strategiesArray.optJSONObject(j) ?: return@mapNotNull null
                    ActivityApiStrategy(
                        name = stratObj.optString("name", ""),
                        opened = parsePositionArray(stratObj, "opened"),
                        closed = parsePositionArray(stratObj, "closed"),
                        kept = parsePositionArray(stratObj, "kept")
                    )
                }
                ActivityApiCycle(
                    timestamp = cycleObj.optString("timestamp", ""),
                    cycleStatus = cycleObj.optString("cycle_status", ""),
                    totalOpened = cycleObj.requiredInt("total_opened"),
                    totalClosed = cycleObj.requiredInt("total_closed"),
                    totalKept = cycleObj.requiredInt("total_kept"),
                    strategies = strategies
                )
            }
            ActivityResult.Success(ActivityApiResponse(cycles = cycles))
        } catch (e: Exception) {
            ActivityResult.Error(e.message ?: "Parse error")
        }
    }

    internal fun parsePerformanceResponse(json: String): PerformanceResult {
        return try {
            val root = JSONObject(json)
            val performanceStripObject = root.optJSONObject("performance_strip")
                ?: return PerformanceResult.Error("Missing 'performance_strip' key")
            val monthlyStatsObject = root.optJSONObject("monthly_stats") ?: JSONObject()
            PerformanceResult.Success(
                PerformanceApiResponse(
                    totalEquity = root.optDouble("total_equity", 0.0),
                    allTimeReturnPct = root.optDouble("all_time_return_pct", 0.0),
                    performanceStrip = PerformanceStripApiResponse(
                        todayPnl = performanceStripObject.optionalDouble("today_pnl"),
                        todayPnlPct = performanceStripObject.optionalDouble("today_pnl_pct"),
                        sevenDayPnl = performanceStripObject.optionalDouble("seven_day_pnl"),
                        sevenDayPnlPct = performanceStripObject.optionalDouble("seven_day_pnl_pct"),
                        thirtyDayPnl = performanceStripObject.optionalDouble("thirty_day_pnl"),
                        thirtyDayPnlPct = performanceStripObject.optionalDouble("thirty_day_pnl_pct")
                    ),
                    winRate = root.optDouble("win_rate", 0.0),
                    profitFactor = root.optDouble("profit_factor", 0.0),
                    sharpeRatio = root.optDouble("sharpe_ratio", 0.0),
                    totalClosedTrades = root.optInt("total_closed_trades", 0),
                    equityCurve = parseEquityCurve(root),
                    drawdownSeries = parseDrawdownSeries(root),
                    maxDrawdownPct = root.optDouble("max_drawdown_pct", 0.0),
                    currentDrawdownPct = root.optDouble("current_drawdown_pct", 0.0),
                    recoveryPct = root.optDouble("recovery_pct", 0.0),
                    monthlyReturns = parseMonthlyReturns(root.optJSONArray("monthly_returns")),
                    monthlyStats = PerformanceMonthlyStatsApiResponse(
                        bestMonth = parseMonthlyReturn(monthlyStatsObject.optJSONObject("best_month")),
                        worstMonth = parseMonthlyReturn(monthlyStatsObject.optJSONObject("worst_month")),
                        averageMonthlyPnl = monthlyStatsObject.optionalDouble("average_monthly_pnl")
                    )
                )
            )
        } catch (e: Exception) {
            PerformanceResult.Error(e.message ?: "Parse error")
        }
    }

    internal fun parseLiveResponse(json: String): LiveResult {
        return try {
            val root = JSONObject(json)
            val positionsArray = root.optJSONArray("positions")
                ?: return LiveResult.Error("Missing 'positions' key")
            val positions = (0 until positionsArray.length()).mapNotNull { i ->
                parseLivePosition(positionsArray.optJSONObject(i))
            }
            val analyticsObject = root.optJSONObject("analytics")
            val incidentsArray = root.optJSONArray("incidents")
            LiveResult.Success(
                LiveApiResponse(
                    positions = positions,
                    analytics = analyticsObject?.let(::parseLiveAnalytics),
                    incidents = (0 until (incidentsArray?.length() ?: 0)).mapNotNull { i ->
                        incidentsArray?.optJSONObject(i)?.let(::parseLiveIncident)
                    }
                )
            )
        } catch (e: Exception) {
            LiveResult.Error(e.message ?: "Parse error")
        }
    }

    private fun parseLiveAnalytics(obj: JSONObject): LiveApiAnalytics {
        val contributionsArray = obj.optJSONArray("strategy_contributions")
        val summaryObject = obj.optJSONObject("live_summary")
        val metricsObject = obj.optJSONObject("live_metrics")
        val integrityObject = obj.optJSONObject("integrity")
        return LiveApiAnalytics(
            strategyContributions = (0 until (contributionsArray?.length() ?: 0)).mapNotNull { i ->
                contributionsArray?.optJSONObject(i)?.let { contribution ->
                    LiveApiStrategyContribution(
                        strategyName = contribution.optString("strategy_name", ""),
                        unrealizedPnl = contribution.optDouble("unrealized_pnl", 0.0),
                        contributionPct = contribution.optDouble("contribution_pct", 0.0)
                    )
                }
            },
            liveSummary = summaryObject?.let {
                LiveApiSummary(
                    positionCount = it.optInt("position_count", 0),
                    longCount = it.optInt("long_count", 0),
                    shortCount = it.optInt("short_count", 0),
                    longPct = it.optDouble("long_pct", 0.0),
                    shortPct = it.optDouble("short_pct", 0.0),
                    totalUnrealizedPnl = it.optDouble("total_unrealized_pnl", 0.0),
                    totalCapital = it.optDouble("total_capital", 0.0),
                    avgAgeHours = it.optDouble("avg_age_hours", 0.0)
                )
            },
            liveMetrics = metricsObject?.let {
                LiveApiMetrics(
                    largestWinner = parseLivePosition(it.optJSONObject("largest_winner")),
                    largestLoser = parseLivePosition(it.optJSONObject("largest_loser"))
                )
            },
            integrity = integrityObject?.let {
                LiveApiIntegrity(
                    matched = it.optInt("matched", 0),
                    unclaimed = it.optInt("unclaimed", 0),
                    missing = it.optInt("missing", 0),
                    duplicate = it.optInt("duplicate", 0)
                )
            }
        )
    }

    private fun parseLivePosition(obj: JSONObject?): LiveApiPosition? {
        if (obj == null) return null
        return LiveApiPosition(
            id = obj.optString("id", ""),
            symbol = obj.optString("symbol", ""),
            side = obj.optString("side", ""),
            strategy = obj.optString("strategy", ""),
            strategyName = obj.optString("strategy_name", ""),
            opened = obj.optString("opened", ""),
            entryPrice = obj.optDouble("entry_price", 0.0),
            currentPrice = obj.optDouble("current_price", 0.0),
            capital = obj.optDouble("capital", 0.0),
            quantity = obj.optionalDouble("quantity"),
            unrealizedPnl = obj.optDouble("unrealized_pnl", 0.0),
            unrealizedPnlPct = obj.optDouble("unrealized_pnl_pct", 0.0),
            trendUp = obj.optBoolean("trend_up", false)
        )
    }

    internal fun parseLiveIncident(obj: JSONObject): LiveApiIncident =
        LiveApiIncident(
            timestamp = obj.optString("timestamp", ""),
            level = obj.optString("level", ""),
            category = obj.optString("category", ""),
            title = obj.optString("title", ""),
            message = obj.optString("message", ""),
            strategy = obj.optionalString("strategy"),
            symbol = obj.optionalString("symbol")
        )

    private fun parseEquityCurve(root: JSONObject): List<PerformanceEquityPointApiResponse> {
        val array = root.optJSONArray("equity_curve") ?: return emptyList()
        return (0 until array.length()).mapNotNull { i ->
            val obj = array.optJSONObject(i) ?: return@mapNotNull null
            PerformanceEquityPointApiResponse(
                timestamp = obj.optString("timestamp", ""),
                equity = obj.optDouble("equity", 0.0)
            )
        }
    }

    private fun parseDrawdownSeries(root: JSONObject): List<PerformanceDrawdownPointApiResponse> {
        val array = root.optJSONArray("drawdown_series") ?: return emptyList()
        return (0 until array.length()).mapNotNull { i ->
            val obj = array.optJSONObject(i) ?: return@mapNotNull null
            PerformanceDrawdownPointApiResponse(
                timestamp = obj.optString("timestamp", ""),
                drawdownPct = obj.optDouble("drawdown_pct", 0.0)
            )
        }
    }

    private fun parseMonthlyReturns(array: org.json.JSONArray?): List<PerformanceMonthlyReturnApiResponse> {
        if (array == null) return emptyList()
        return (0 until array.length()).mapNotNull { i ->
            parseMonthlyReturn(array.optJSONObject(i))
        }
    }

    private fun parseMonthlyReturn(obj: JSONObject?): PerformanceMonthlyReturnApiResponse? {
        if (obj == null) return null
        return PerformanceMonthlyReturnApiResponse(
            month = obj.optString("month", ""),
            pnl = obj.optDouble("pnl", 0.0),
            pnlPct = obj.optDouble("pnl_pct", 0.0)
        )
    }

    private fun parsePositionArray(obj: JSONObject, key: String): List<ActivityApiPosition> {
        val array = obj.optJSONArray(key) ?: return emptyList()
        return (0 until array.length()).mapNotNull { i ->
            val posObj = array.optJSONObject(i) ?: return@mapNotNull null
            ActivityApiPosition(
                symbol = posObj.optString("symbol", ""),
                side = posObj.optString("side", "")
            )
        }
    }

    internal fun parseLogsResponse(json: String): LogsResult {
        return try {
            val root = JSONObject(json)
            val entriesArray = root.optJSONArray("entries")
                ?: return LogsResult.Error("Missing 'entries' key")
            val entries = (0 until entriesArray.length()).mapNotNull { i ->
                val obj = entriesArray.optJSONObject(i) ?: return@mapNotNull null
                val cause = obj.optJSONObject("cause")
                LogApiEntry(
                    level = obj.optString("level", "INFO"),
                    title = obj.optString("title", ""),
                    message = obj.firstOptionalString("full_message", "fullMessage", "details", "detail", "text")
                        ?: obj.optString("message", ""),
                    timestamp = obj.optString("timestamp", ""),
                    strategy = obj.optionalString("strategy"),
                    strategyName = obj.optionalString("strategy_name"),
                    causeStrategy = obj.optionalString("cause_strategy") ?: cause?.optionalString("strategy"),
                    causeStrategyName = obj.optionalString("cause_strategy_name")
                        ?: cause?.firstOptionalString("strategy_name", "strategy_label", "display_name")
                )
            }
            LogsResult.Success(entries)
        } catch (e: Exception) {
            LogsResult.Error(e.message ?: "Parse error")
        }
    }

    internal fun parseHeartbeatResponse(json: String): HeartbeatResult {
        return try {
            val root = JSONObject(json)
            val servicesArray = root.optJSONArray("services")
                ?: return HeartbeatResult.Error("Missing 'services' key")
            val services = (0 until servicesArray.length()).mapNotNull { i ->
                val obj = servicesArray.optJSONObject(i) ?: return@mapNotNull null
                HeartbeatApiService(
                    name = obj.optString("name", ""),
                    displayName = obj.optionalString("display_name"),
                    status = obj.optString("status", "unknown"),
                    lastSeenAt = obj.optionalString("last_seen_at"),
                    expectedCadenceSeconds = obj.optInt("expected_cadence_seconds", 0),
                    secondsOverdue = obj.optionalInt("seconds_overdue")
                )
            }
            val normalizedServices = services.withHealthyVpnDefault()
            HeartbeatResult.Success(
                healthyCount = root.optInt("healthy_count", 0) + services.vpnHealthyDelta(),
                lateCount = (root.optInt("late_count", 0) - services.vpnLateDelta()).coerceAtLeast(0),
                criticalCount = (root.optInt("critical_count", 0) - services.vpnCriticalDelta()).coerceAtLeast(0),
                totalCount = root.optInt("total_count", services.size) + services.vpnMissingDelta(),
                services = normalizedServices
            )
        } catch (e: Exception) {
            HeartbeatResult.Error(e.message ?: "Parse error")
        }
    }

    internal fun parseHunchReportResponse(json: String): HunchReportResult {
        return try {
            val root = JSONObject(json)
            val date = root.optString("date").takeIf { it.isNotBlank() }
                ?: return HunchReportResult.Error("Missing 'date' key")
            val signal = root.optString("signal").takeIf { it.isNotBlank() }
                ?: return HunchReportResult.Error("Missing 'signal' key")
            HunchReportResult.Success(
                HunchReportApiResponse(
                    date = date,
                    signal = signal,
                    rawSignal = root.optionalString("raw_signal"),
                    createdAt = root.optionalString("created_at"),
                    confidence = root.optionalString("confidence"),
                    score = root.optionalDouble("score"),
                    btcPriceAtSignal = root.optionalDouble("btc_price_at_signal"),
                    currentBtcPrice = root.optionalDouble("current_btc_price"),
                    priceDeltaPct = root.optionalDouble("price_delta_pct"),
                    wasSignalCorrect = root.optionalBoolean("was_signal_correct"),
                    keyFactors = root.optionalStringList("key_factors"),
                    risks = root.optionalStringList("risks"),
                    layerScores = root.optionalLayerScores()
                )
            )
        } catch (e: Exception) {
            HunchReportResult.Error(e.message ?: "Parse error")
        }
    }

    private fun openConnection(url: String, apiKey: String): HttpURLConnection =
        (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 30_000
            readTimeout = 30_000
            requestMethod = "GET"
            setRequestProperty("User-Agent", "Njord Android")
            setRequestProperty("X-API-Key", apiKey)
            setRequestProperty("Cache-Control", "no-cache")
        }

    internal fun logsUrl(baseUrl: String): String =
        "$baseUrl/v1/logs?hours=24&limit=5000&exclude_heartbeat=true"

    internal fun activityUrl(baseUrl: String): String =
        "$baseUrl/v1/activity?limit=1"

    internal fun liveUrl(baseUrl: String, strategy: String = "all"): String =
        if (strategy == "all") "$baseUrl/v1/live" else "$baseUrl/v1/live?strategy=$strategy"

    internal fun performanceUrl(baseUrl: String, strategy: String): String =
        "$baseUrl/v1/performance?strategy=$strategy"

    internal fun extractIncidentsJson(body: String): String? =
        try { JSONObject(body).optJSONArray("incidents")?.toString() } catch (_: Exception) { null }

    internal fun mergeIncidentJson(existing: String?, incoming: String?): String {
        if (incoming == null) return existing ?: "[]"
        val result = JSONArray()
        val seenKeys = mutableSetOf<String>()
        fun addAll(json: String?) {
            if (json == null) return
            try {
                val arr = JSONArray(json)
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    val key = "${obj.optString("timestamp")}-${obj.optString("category")}"
                    if (seenKeys.add(key)) result.put(obj)
                }
            } catch (_: Exception) {}
        }
        addAll(existing)
        addAll(incoming)
        return result.toString()
    }

    internal fun deleteFromIncidentJson(existing: String, incidentId: String): String =
        try {
            val arr = JSONArray(existing)
            val result = JSONArray()
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val key = "${obj.optString("timestamp")}-${obj.optString("category")}"
                if (key != incidentId) result.put(obj)
            }
            result.toString()
        } catch (_: Exception) { existing }
}

private fun JSONObject.optionalString(name: String): String? =
    if (isNull(name)) null else optString(name).takeIf { it.isNotBlank() }

private fun JSONObject.firstOptionalString(vararg names: String): String? =
    names.firstNotNullOfOrNull(::optionalString)

private fun JSONObject.optionalDouble(name: String): Double? =
    if (isNull(name) || !has(name)) null else optDouble(name)

private fun JSONObject.optionalInt(name: String): Int? =
    if (isNull(name) || !has(name)) null else optInt(name)

private fun JSONObject.requiredInt(name: String): Int {
    if (isNull(name) || !has(name)) throw IllegalArgumentException("Missing '$name' key")
    return getInt(name)
}

private fun JSONObject.optionalBoolean(name: String): Boolean? =
    if (isNull(name) || !has(name)) null else optBoolean(name)

private fun JSONObject.optionalStringList(name: String): List<String> {
    val array = optJSONArray(name) ?: return emptyList()
    return (0 until array.length()).mapNotNull { index ->
        array.optString(index).takeIf { it.isNotBlank() }
    }
}

private fun JSONObject.optionalLayerScores(): Map<String, Double?> {
    val layerObject = optJSONObject("layer_scores") ?: return emptyMap()
    return layerObject.keys().asSequence().associateWith { key ->
        if (layerObject.isNull(key)) null else layerObject.optDouble(key)
    }
}

private fun List<HeartbeatApiService>.withHealthyVpnDefault(): List<HeartbeatApiService> {
    val vpnIndex = indexOfFirst { it.name == "vpn_heartbeat" }
    if (vpnIndex == -1) {
        return this + defaultHealthyVpnHeartbeat()
    }
    val vpn = this[vpnIndex]
    if (!vpn.isAbsentVpnHeartbeat()) {
        return this
    }
    return toMutableList().also { services ->
        services[vpnIndex] = vpn.copy(status = "healthy", secondsOverdue = null)
    }
}

private fun List<HeartbeatApiService>.vpnHealthyDelta(): Int =
    when {
        none { it.name == "vpn_heartbeat" } -> 1
        any { it.isAbsentVpnHeartbeat() } -> 1
        else -> 0
    }

private fun List<HeartbeatApiService>.vpnMissingDelta(): Int =
    if (none { it.name == "vpn_heartbeat" }) 1 else 0

private fun List<HeartbeatApiService>.vpnLateDelta(): Int =
    if (any { it.isAbsentVpnHeartbeat() && it.status.equals("late", ignoreCase = true) }) 1 else 0

private fun List<HeartbeatApiService>.vpnCriticalDelta(): Int =
    if (any { it.isAbsentVpnHeartbeat() && it.status.equals("critical", ignoreCase = true) }) 1 else 0

private fun HeartbeatApiService.isAbsentVpnHeartbeat(): Boolean =
    name == "vpn_heartbeat" &&
        lastSeenAt == null &&
        !status.equals("healthy", ignoreCase = true)

private fun defaultHealthyVpnHeartbeat(): HeartbeatApiService =
    HeartbeatApiService(
        name = "vpn_heartbeat",
        displayName = "VPN heartbeat",
        status = "healthy",
        lastSeenAt = null,
        expectedCadenceSeconds = 1200,
        secondsOverdue = null
    )
