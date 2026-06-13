package com.njord.mobile.api

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

object NjordApiClient {

    fun fetchLogs(baseUrl: String, apiKey: String): LogsResult {
        val connection = openConnection("$baseUrl/v1/logs?hours=24", apiKey)
        return try {
            if (connection.responseCode !in 200..299) {
                return LogsResult.Error("HTTP ${connection.responseCode}")
            }
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            parseLogsResponse(body)
        } catch (e: Exception) {
            LogsResult.Error(e.message ?: "Unknown error")
        } finally {
            connection.disconnect()
        }
    }

    fun fetchHeartbeat(baseUrl: String, apiKey: String): HeartbeatResult {
        val connection = openConnection("$baseUrl/v1/heartbeat", apiKey)
        return try {
            if (connection.responseCode !in 200..299) {
                return HeartbeatResult.Error("HTTP ${connection.responseCode}")
            }
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            parseHeartbeatResponse(body)
        } catch (e: Exception) {
            HeartbeatResult.Error(e.message ?: "Unknown error")
        } finally {
            connection.disconnect()
        }
    }

    fun fetchHunchReport(baseUrl: String, apiKey: String): HunchReportResult {
        val connection = openConnection("$baseUrl/v1/reports/hunch?date=latest", apiKey)
        return try {
            if (connection.responseCode !in 200..299) {
                return HunchReportResult.Error("HTTP ${connection.responseCode}")
            }
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            parseHunchReportResponse(body)
        } catch (e: Exception) {
            HunchReportResult.Error(e.message ?: "Unknown error")
        } finally {
            connection.disconnect()
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
            HeartbeatResult.Success(
                healthyCount = root.optInt("healthy_count", 0),
                lateCount = root.optInt("late_count", 0),
                criticalCount = root.optInt("critical_count", 0),
                totalCount = root.optInt("total_count", services.size),
                services = services
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
