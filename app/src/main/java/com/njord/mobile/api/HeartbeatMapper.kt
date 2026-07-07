package com.njord.mobile.api

import com.njord.mobile.model.HeartbeatRoutine
import com.njord.mobile.model.Tone
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

private val redundantHeartbeatRoutineKeywords = Regex("\\b(heartbeat|liveness|snapshot)\\b", RegexOption.IGNORE_CASE)
private val whitespace = Regex("\\s+")

data class HeartbeatSnapshot(
    val healthyCount: Int,
    val lateCount: Int,
    val criticalCount: Int,
    val totalCount: Int,
    val routines: List<HeartbeatRoutine>
)

internal fun mapApiHeartbeat(result: HeartbeatResult.Success, now: Instant = Instant.now()): HeartbeatSnapshot {
    val routines = result.services
        .map { service -> service.toRoutine(now) }
        .sortedBy { routine -> routine.status.toHeartbeatSortRank() }

    return HeartbeatSnapshot(
        healthyCount = routines.count { it.status.equals("Healthy", ignoreCase = true) },
        lateCount = routines.count { it.status.equals("Late", ignoreCase = true) },
        criticalCount = routines.count { it.status.equals("Critical", ignoreCase = true) },
        totalCount = result.totalCount.coerceAtLeast(routines.size),
        routines = routines
    )
}

private fun HeartbeatApiService.toRoutine(now: Instant): HeartbeatRoutine {
    val lastSeen = lastSeenAt?.toInstantOrNull()
    val localSecondsOverdue = localSecondsOverdue(now, lastSeen, expectedCadenceSeconds)
    val effectiveStatus = status.toDeviceClockStatus(localSecondsOverdue, expectedCadenceSeconds)

    return HeartbeatRoutine(
        name = toRoutineName(),
        status = effectiveStatus.toHeartbeatLabel(),
        lastSeenAt = lastSeen,
        cadence = expectedCadenceSeconds.toCadenceLabel(),
        tone = effectiveStatus.toHeartbeatTone(),
        secondsOverdue = localSecondsOverdue ?: secondsOverdue,
        expectedCadenceSeconds = expectedCadenceSeconds
    )
}

private fun localSecondsOverdue(now: Instant, lastSeenAt: Instant?, expectedCadenceSeconds: Int): Int? {
    if (lastSeenAt == null || expectedCadenceSeconds <= 0) return null
    val elapsedSeconds = Duration.between(lastSeenAt, now).seconds.coerceAtLeast(0)
    return (elapsedSeconds - expectedCadenceSeconds).coerceAtLeast(0).toInt()
}

private fun String.toDeviceClockStatus(secondsOverdue: Int?, expectedCadenceSeconds: Int): String =
    when {
        equals("critical", ignoreCase = true) -> "critical"
        secondsOverdue == null || secondsOverdue == 0 -> lowercase()
        expectedCadenceSeconds > 0 && secondsOverdue > expectedCadenceSeconds -> "critical"
        else -> "late"
    }

private fun HeartbeatApiService.toRoutineName(): String {
    val source = displayName ?: name.replace('_', ' ').replace('-', ' ')
    val cleaned = source
        .replace(redundantHeartbeatRoutineKeywords, "")
        .replace(whitespace, " ")
        .trim()
    return cleaned.ifBlank { source }
}

private fun String.toHeartbeatSortRank(): Int =
    if (equals("healthy", ignoreCase = true)) 0 else 1

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

internal fun toAgeLabel(secondsOverdue: Int?, expectedCadenceSeconds: Int, lastSeenAt: Instant?): String {
    val seconds = when {
        secondsOverdue != null -> (expectedCadenceSeconds + secondsOverdue).toLong().coerceAtLeast(0)
        lastSeenAt != null -> Duration.between(lastSeenAt, Instant.now()).seconds.coerceAtLeast(0)
        else -> return "Never"
    }
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
