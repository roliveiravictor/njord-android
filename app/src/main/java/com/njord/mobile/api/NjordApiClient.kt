package com.njord.mobile.api

import android.util.Log
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class LogApiEntry(
    val level: String,
    val title: String,
    val message: String,
    val timestamp: String
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
    val heartbeat: HomeApiHeartbeat
)

sealed interface HomeResult {
    data object Loading : HomeResult
    data class Success(val response: HomeApiResponse) : HomeResult
    data class Error(val message: String) : HomeResult
}

sealed interface ActivityResult {
    data object Loading : ActivityResult
    data class Success(val response: ActivityApiResponse) : ActivityResult
    data class Error(val message: String) : ActivityResult
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
    data class Success(val body: String) : ApiPayloadResult
    data class Error(val message: String) : ApiPayloadResult
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
        fetchPayload("$baseUrl/v1/activity", apiKey, "fetchActivity")

    fun fetchLogsPayload(baseUrl: String, apiKey: String): ApiPayloadResult =
        fetchPayload(logsUrl(baseUrl), apiKey, "fetchLogs")

    fun fetchHeartbeatPayload(baseUrl: String, apiKey: String): ApiPayloadResult =
        fetchPayload("$baseUrl/v1/heartbeat", apiKey, "fetchHeartbeat")

    fun fetchHunchReportPayload(baseUrl: String, apiKey: String): ApiPayloadResult =
        fetchPayload("$baseUrl/v1/reports/hunch?date=latest", apiKey, "fetchHunchReport")

    private fun fetchPayload(url: String, apiKey: String, operation: String): ApiPayloadResult {
        Log.d("NjordApi", "$operation url=$url")
        val connection = openConnection(url, apiKey)
        return try {
            val code = connection.responseCode
            Log.d("NjordApi", "$operation responseCode=$code")
            if (code !in 200..299) {
                return ApiPayloadResult.Error("HTTP $code")
            }
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            Log.d("NjordApi", "$operation body=$body")
            ApiPayloadResult.Success(body)
        } catch (e: Exception) {
            Log.e("NjordApi", "$operation error: ${e.javaClass.simpleName}: ${e.message}", e)
            ApiPayloadResult.Error(e.message ?: "Unknown error")
        } finally {
            connection.disconnect()
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
                            openedCount = it.optInt("opened_count", 0),
                            closedCount = it.optInt("closed_count", 0),
                            keptCount = it.optInt("kept_count", 0)
                        )
                    },
                    heartbeat = HomeApiHeartbeat(
                        healthy = heartbeatObject.optInt("healthy", 0),
                        total = heartbeatObject.optInt("total", 0),
                        lateCount = heartbeatObject.optInt("late_count", 0)
                    )
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
                    totalOpened = cycleObj.optInt("total_opened", 0),
                    totalClosed = cycleObj.optInt("total_closed", 0),
                    totalKept = cycleObj.optInt("total_kept", 0),
                    strategies = strategies
                )
            }
            ActivityResult.Success(ActivityApiResponse(cycles = cycles))
        } catch (e: Exception) {
            ActivityResult.Error(e.message ?: "Parse error")
        }
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
                LogApiEntry(
                    level = obj.optString("level", "INFO"),
                    title = obj.optString("title", ""),
                    message = obj.optString("message", ""),
                    timestamp = obj.optString("timestamp", "")
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
            connectTimeout = 4_000
            readTimeout = 4_000
            requestMethod = "GET"
            setRequestProperty("User-Agent", "Njord Android")
            setRequestProperty("X-API-Key", apiKey)
        }

    internal fun logsUrl(baseUrl: String): String =
        "$baseUrl/v1/logs?hours=24&limit=5000&exclude_heartbeat=true"
}

private fun JSONObject.optionalString(name: String): String? =
    if (isNull(name)) null else optString(name).takeIf { it.isNotBlank() }

private fun JSONObject.optionalDouble(name: String): Double? =
    if (isNull(name) || !has(name)) null else optDouble(name)

private fun JSONObject.optionalInt(name: String): Int? =
    if (isNull(name) || !has(name)) null else optInt(name)

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
