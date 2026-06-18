package com.njord.mobile.api

import java.io.File
import org.json.JSONArray
import org.json.JSONObject

private const val IncidentAcknowledgementTtlMillis = 24 * 60 * 60 * 1000L

object IncidentAcknowledgements {
    fun readActiveIds(filesDir: File, nowMillis: Long = System.currentTimeMillis()): Set<String> {
        val json = NjordApiCache.read(filesDir, ApiCacheKey.IncidentAcknowledgements) ?: return emptySet()
        val active = mutableMapOf<String, Long>()
        runCatching {
            val root = JSONObject(json)
            root.keys().forEach { id ->
                val dismissedAt = root.optLong(id, -1L)
                if (dismissedAt >= 0L && nowMillis - dismissedAt < IncidentAcknowledgementTtlMillis) {
                    active[id] = dismissedAt
                }
            }
        }
        writeIds(filesDir, active)
        return active.keys
    }

    fun acknowledge(filesDir: File, incidentId: String, nowMillis: Long = System.currentTimeMillis()) {
        val active = readActiveMap(filesDir, nowMillis).toMutableMap()
        active[incidentId] = nowMillis
        writeIds(filesDir, active)
    }

    fun filterIncidentJson(json: String?, acknowledgedIds: Set<String>): String? {
        if (json == null || acknowledgedIds.isEmpty()) return json
        return runCatching {
            val incoming = JSONArray(json)
            val filtered = JSONArray()
            for (i in 0 until incoming.length()) {
                val obj = incoming.optJSONObject(i) ?: continue
                if (incidentId(obj) !in acknowledgedIds) {
                    filtered.put(obj)
                }
            }
            filtered.toString()
        }.getOrDefault(json)
    }

    private fun readActiveMap(filesDir: File, nowMillis: Long): Map<String, Long> {
        val json = NjordApiCache.read(filesDir, ApiCacheKey.IncidentAcknowledgements) ?: return emptyMap()
        return runCatching {
            val root = JSONObject(json)
            buildMap {
                root.keys().forEach { id ->
                    val dismissedAt = root.optLong(id, -1L)
                    if (dismissedAt >= 0L && nowMillis - dismissedAt < IncidentAcknowledgementTtlMillis) {
                        put(id, dismissedAt)
                    }
                }
            }
        }.getOrDefault(emptyMap())
    }

    private fun writeIds(filesDir: File, ids: Map<String, Long>) {
        val root = JSONObject()
        ids.forEach { (id, dismissedAt) -> root.put(id, dismissedAt) }
        NjordApiCache.write(filesDir, ApiCacheKey.IncidentAcknowledgements, root.toString())
    }

    private fun incidentId(obj: JSONObject): String =
        "${obj.optString("timestamp")}-${obj.optString("category")}"
}
