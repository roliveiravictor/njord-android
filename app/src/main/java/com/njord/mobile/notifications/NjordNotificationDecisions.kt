package com.njord.mobile.notifications

import com.njord.mobile.api.ActivityApiCycle
import com.njord.mobile.api.ActivityApiResponse
import com.njord.mobile.api.HeartbeatApiService
import com.njord.mobile.api.LiveApiIncident

data class LocalNotificationContent(
    val title: String,
    val body: String
)

object NjordNotificationDecisions {
    fun incidentNotification(incidents: List<LiveApiIncident>): LocalNotificationContent? {
        if (incidents.isEmpty()) return null
        val first = incidents.first()
        val title = first.title.ifBlank { "Njord incident reported" }
        val detail = first.message.ifBlank { first.category.ifBlank { "Open the app for incident details." } }
        val prefix = if (incidents.size == 1) "" else "${incidents.size} active incidents. "
        return LocalNotificationContent(title, "$prefix$detail")
    }

    fun activitySignature(response: ActivityApiResponse): String? =
        response.cycles.firstOrNull()?.activitySignature()

    fun activityNotification(
        response: ActivityApiResponse,
        previousSignature: String?
    ): Pair<LocalNotificationContent, String>? {
        val latest = response.cycles.firstOrNull() ?: return null
        val signature = latest.activitySignature()
        val updatedCount = latest.totalOpened + latest.totalClosed + latest.totalKept
        if (updatedCount <= 0 || signature == previousSignature) return null
        val body = "${latest.totalOpened} opened, ${latest.totalClosed} closed, ${latest.totalKept} kept"
        return LocalNotificationContent("Njord activity updated", body) to signature
    }

    fun heartbeatNotification(services: List<HeartbeatApiService>): LocalNotificationContent? {
        val unhealthy = services.filterNot { it.status.equals("healthy", ignoreCase = true) }
        if (unhealthy.isEmpty()) return null
        val names = unhealthy.take(3).joinToString(", ") { service ->
            service.displayName ?: service.name.replace('_', ' ')
        }
        val suffix = if (unhealthy.size > 3) " +${unhealthy.size - 3} more" else ""
        return LocalNotificationContent("Njord heartbeat needs attention", "$names$suffix")
    }

    private fun ActivityApiCycle.activitySignature(): String {
        val actions = strategies.joinToString("|") { strategy ->
            val opened = strategy.opened.joinToString(",") { "${it.symbol}:${it.side}" }
            val closed = strategy.closed.joinToString(",") { "${it.symbol}:${it.side}" }
            val kept = strategy.kept.joinToString(",") { "${it.symbol}:${it.side}" }
            "${strategy.name}:o=$opened:c=$closed:k=$kept"
        }
        return "$timestamp:$totalOpened:$totalClosed:$totalKept:$actions"
    }
}
