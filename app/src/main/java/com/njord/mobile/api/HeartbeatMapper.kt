package com.njord.mobile.api

import com.njord.mobile.model.HeartbeatRoutine
import com.njord.mobile.model.Tone
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

data class HeartbeatSnapshot(
    val healthyCount: Int,
    val lateCount: Int,
    val criticalCount: Int,
    val totalCount: Int,
    val routines: List<HeartbeatRoutine>
)

internal fun mapApiHeartbeat(result: HeartbeatResult.Success, now: Instant = Instant.now()): HeartbeatSnapshot =
    HeartbeatSnapshot(
        healthyCount = result.healthyCount,
        lateCount = result.lateCount,
        criticalCount = result.criticalCount,
        totalCount = result.totalCount,
        routines = result.services.map { service ->
            HeartbeatRoutine(
                name = service.displayName ?: service.name,
                status = service.status.toHeartbeatLabel(),
                age = service.lastSeenAt.toAgeLabel(now),
                cadence = service.expectedCadenceSeconds.toCadenceLabel(),
                tone = service.status.toHeartbeatTone()
            )
        }
    )

private fun String.toHeartbeatLabel(): String =
    when (lowercase()) {
        "healthy" -> "Healthy"
        "late" -> "Late"
        "critical" -> "Critical"
        else -> "Unknown"
    }

private fun String.toHeartbeatTone(): Tone =
    when (lowercase()) {
        "healthy" -> Tone.Success
        "late" -> Tone.Warning
        "critical" -> Tone.Danger
        else -> Tone.Muted
    }

private fun String?.toAgeLabel(now: Instant): String {
    val lastSeen = this?.toInstantOrNull() ?: return "Never"
    val seconds = Duration.between(lastSeen, now).seconds.coerceAtLeast(0)
    return when {
        seconds < 60 -> "just now"
        seconds < 3_600 -> "${seconds / 60}m ago"
        seconds < 86_400 -> "${seconds / 3_600}h ago"
        else -> "${seconds / 86_400}d ago"
    }
}

private fun String.toInstantOrNull(): Instant? =
    runCatching { OffsetDateTime.parse(this).toInstant() }
        .recoverCatching { Instant.parse(this) }
        .recoverCatching { LocalDateTime.parse(this).toInstant(ZoneOffset.UTC) }
        .getOrNull()

private fun Int.toCadenceLabel(): String =
    when {
        this <= 0 -> "n/a"
        this % 86_400 == 0 -> "${this / 86_400}d"
        this % 3_600 == 0 -> "${this / 3_600}h"
        this % 60 == 0 -> "${this / 60}m"
        else -> "${this}s"
    }
