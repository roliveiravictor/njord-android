package com.njord.mobile.api

import com.njord.mobile.model.Incident
import com.njord.mobile.model.LiveAnalyticsSnapshot
import org.json.JSONArray
import com.njord.mobile.model.LiveContribution
import com.njord.mobile.model.LiveOutcome
import com.njord.mobile.model.LivePosition
import com.njord.mobile.model.MiniKpi
import com.njord.mobile.model.StrategyFilter
import com.njord.mobile.model.Tone
import java.text.NumberFormat
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.Locale
import kotlin.math.abs

private val USD_FORMATTER = NumberFormat.getCurrencyInstance(Locale.US)

internal fun mapApiLive(response: LiveApiResponse, now: Instant = Instant.now()): Triple<List<LivePosition>, LiveAnalyticsSnapshot?, List<Incident>> {
    val positions = response.positions.map(::mapLivePosition)
    val analytics = response.analytics?.let { mapLiveAnalytics(it, positions) }
    val incidents = response.incidents.mapIndexed { index, incident -> mapApiIncident(incident, index, now) }
    return Triple(positions, analytics, incidents)
}

private fun mapLivePosition(position: LiveApiPosition): LivePosition {
    val symbol = formatSymbol(position.symbol)
    return LivePosition(
        id = position.id.ifBlank { "${symbol.lowercase()}-${position.side.lowercase()}" },
        symbol = symbol,
        side = formatSide(position.side),
        strategy = position.strategy.toStrategyFilter(),
        strategyName = position.strategyName.ifBlank { position.strategy.toStrategyName() },
        opened = position.opened.ifBlank { "n/a" },
        pnl = formatSignedCurrency(position.unrealizedPnl),
        pct = formatSignedPercent(position.unrealizedPnlPct),
        size = "${formatQuantity(position.quantity ?: estimateQuantity(position))} $symbol",
        capital = formatCompactCurrency(position.capital),
        entry = formatPrice(position.entryPrice),
        current = formatPrice(position.currentPrice),
        trendUp = position.trendUp
    )
}

private fun mapLiveAnalytics(analytics: LiveApiAnalytics, positions: List<LivePosition>): LiveAnalyticsSnapshot {
    val summary = analytics.liveSummary
    val totalPnl = summary?.totalUnrealizedPnl
        ?: analytics.strategyContributions.sumOf { it.unrealizedPnl }
    val integrity = analytics.integrity
    val positionCount = summary?.positionCount ?: positions.size
    val cexPositionCount = integrity?.unclaimed ?: positionCount
    val integrityMismatchCount = abs(positionCount - cexPositionCount) + (integrity?.duplicate ?: 0)
    return LiveAnalyticsSnapshot(
        totalContribution = formatSignedCurrency(totalPnl),
        totalContributionTone = toneFor(totalPnl),
        strategyContributions = analytics.strategyContributions.map { contribution ->
            LiveContribution(
                strategy = contribution.strategyName.ifBlank { "Unknown" },
                progress = contribution.contributionPct.toFloat().coerceIn(0f, 1f),
                value = formatSignedCurrency(contribution.unrealizedPnl),
                tone = toneFor(contribution.unrealizedPnl)
            )
        },
        summaryItems = listOf(
            MiniKpi("DEPLOYED", formatCompactCurrency(summary?.totalCapital ?: 0.0), "Displayed filter", Tone.Muted),
            MiniKpi("AVG AGE", formatAgeHours(summary?.avgAgeHours ?: 0.0), "Current cycle", Tone.Muted),
            MiniKpi("LONG", (summary?.longCount ?: positions.count { it.side.equals("Long", ignoreCase = true) }).toString(), "${formatPercent(summary?.longPct ?: ratio(positions.count { it.side.equals("Long", ignoreCase = true) }, positions.size))} of book", Tone.Muted),
            MiniKpi("SHORT", (summary?.shortCount ?: positions.count { it.side.equals("Short", ignoreCase = true) }).toString(), "${formatPercent(summary?.shortPct ?: ratio(positions.count { it.side.equals("Short", ignoreCase = true) }, positions.size))} of book", Tone.Muted),
            MiniKpi("INTEGRITY", "$positionCount/$cexPositionCount", "Cache vs. CEX", integrityTone(integrityMismatchCount))
        ),
        largestWinner = analytics.liveMetrics?.largestWinner?.let(::mapLiveOutcome),
        largestLoser = analytics.liveMetrics?.largestLoser?.let(::mapLiveOutcome),
        integrityItems = listOf(
            MiniKpi("Matched", (analytics.integrity?.matched ?: 0).toString(), "", Tone.Success),
            MiniKpi("Exchange", (analytics.integrity?.unclaimed ?: 0).toString(), "", integrityTone(analytics.integrity?.unclaimed ?: 0)),
            MiniKpi("Local", (analytics.integrity?.missing ?: 0).toString(), "", integrityTone(analytics.integrity?.missing ?: 0))
        )
    )
}

private fun mapLiveOutcome(position: LiveApiPosition): LiveOutcome =
    LiveOutcome(
        symbol = formatSymbol(position.symbol),
        amount = formatSignedCurrency(position.unrealizedPnl),
        percent = formatSignedPercent(position.unrealizedPnlPct),
        tone = toneFor(position.unrealizedPnl)
    )

internal fun mapApiIncident(incident: LiveApiIncident, index: Int, now: Instant = Instant.now()): Incident {
    val tone = incident.level.toIncidentTone()
    val strategy = incident.strategy?.toStrategyName().orEmpty()
    val symbol = incident.symbol?.let(::formatSymbol).orEmpty()
    return Incident(
        id = "${incident.timestamp}-${incident.category}".ifBlank { "incident-$index" },
        title = incident.title.ifBlank { incident.category.toDisplayLabel() },
        subtitle = listOf(strategy, symbol).filter { it.isNotBlank() }.joinToString(" · ").ifBlank { incident.category.toDisplayLabel() },
        current = incident.level.ifBlank { "Event" }.toDisplayLabel(),
        threshold = incident.category.toDisplayLabel(),
        detail = incident.message.ifBlank { "Remote live incident did not include detail." },
        badge = incident.level.ifBlank { "Info" }.toDisplayLabel(),
        tone = tone,
        age = incident.timestamp.toAgeLabel(now),
        reason = incident.category.toDisplayLabel()
    )
}

internal fun parseIncidentsFromJson(json: String, now: Instant = Instant.now()): List<Incident> =
    try {
        val arr = JSONArray(json)
        (0 until arr.length()).mapIndexed { index, i ->
            arr.optJSONObject(i)
                ?.let { NjordApiClient.parseLiveIncident(it) }
                ?.let { mapApiIncident(it, index, now) }
        }.filterNotNull()
    } catch (_: Exception) { emptyList() }

private fun String.toStrategyFilter(): StrategyFilter =
    when (lowercase()) {
        "big_bang", "big-bang", "big bang" -> StrategyFilter.BigBang
        "wcr" -> StrategyFilter.Wcr
        "hunch" -> StrategyFilter.Hunch
        else -> StrategyFilter.All
    }

private fun String.toStrategyName(): String =
    when (toStrategyFilter()) {
        StrategyFilter.BigBang -> "Big Bang"
        StrategyFilter.Wcr -> "WCR"
        StrategyFilter.Hunch -> "Hunch"
        StrategyFilter.All -> if (isBlank()) "Unknown" else toDisplayLabel()
    }

private fun String.toIncidentTone(): Tone =
    when (lowercase()) {
        "error", "critical" -> Tone.Danger
        "warning", "warn" -> Tone.Warning
        else -> Tone.Info
    }

private fun String.toDisplayLabel(): String =
    split("_", "-", " ")
        .filter { it.isNotBlank() }
        .joinToString(" ") { part -> part.lowercase().replaceFirstChar { it.uppercase() } }

private fun formatSymbol(symbol: String): String =
    symbol.substringBefore("/")
        .substringBefore(":")
        .uppercase()

private fun formatSide(side: String): String =
    side.lowercase().replaceFirstChar { it.uppercase() }

private fun estimateQuantity(position: LiveApiPosition): Double =
    if (position.currentPrice > 0.0) position.capital / position.currentPrice else 0.0

private fun formatQuantity(value: Double): String =
    when {
        abs(value) >= 1_000.0 -> String.format(Locale.US, "%,.0f", value)
        abs(value) >= 10.0 -> String.format(Locale.US, "%,.2f", value).trimTrailingZeros()
        else -> String.format(Locale.US, "%,.4f", value).trimTrailingZeros()
    }

private fun formatSignedCurrency(value: Double): String {
    val formatted = USD_FORMATTER.format(abs(value))
    return if (value < 0.0) "-$formatted" else "+$formatted"
}

private fun formatCompactCurrency(value: Double): String {
    val absValue = abs(value)
    val sign = if (value < 0.0) "-" else ""
    return when {
        absValue >= 1_000_000.0 -> String.format(Locale.US, "%s$%.1fM", sign, absValue / 1_000_000.0)
        absValue >= 1_000.0 -> String.format(Locale.US, "%s$%.1fK", sign, absValue / 1_000.0)
        else -> "$sign${USD_FORMATTER.format(absValue)}"
    }
}

private fun formatPrice(value: Double): String =
    when {
        abs(value) >= 1.0 -> String.format(Locale.US, "\$%,.2f", value)
        abs(value) >= 0.01 -> String.format(Locale.US, "\$%.4f", value)
        else -> String.format(Locale.US, "\$%.6f", value)
    }

private fun formatSignedPercent(value: Double): String =
    "${String.format(Locale.US, "%+.1f", value)}%"

private fun formatPercent(value: Double): String =
    "${String.format(Locale.US, "%.1f", value * 100.0)}%"

private fun formatAgeHours(value: Double): String =
    when {
        value <= 0.0 -> "n/a"
        value < 24.0 -> "${String.format(Locale.US, "%.1f", value).trimTrailingZeros()}h"
        else -> "${String.format(Locale.US, "%.1f", value / 24.0).trimTrailingZeros()}d"
    }

private fun ratio(count: Int, total: Int): Double =
    if (total <= 0) 0.0 else count.toDouble() / total.toDouble()

private fun toneFor(value: Double): Tone =
    if (value < 0.0) Tone.Danger else Tone.Success

private fun integrityTone(count: Int): Tone =
    if (count > 0) Tone.Warning else Tone.Muted

private fun String.trimTrailingZeros(): String =
    replace(Regex("\\.?0+$"), "")

private fun String.toAgeLabel(now: Instant): String {
    val timestamp = toInstantOrNull() ?: return "now"
    val seconds = Duration.between(timestamp, now).seconds.coerceAtLeast(0)
    return when {
        seconds < 60 -> "now"
        seconds < 3_600 -> "${seconds / 60}m"
        seconds < 86_400 -> "${seconds / 3_600}h"
        else -> "${seconds / 86_400}d"
    }
}

private fun String.toInstantOrNull(): Instant? =
    runCatching { OffsetDateTime.parse(this).toInstant() }
        .recoverCatching { Instant.parse(this) }
        .recoverCatching { LocalDateTime.parse(this).toInstant(ZoneOffset.UTC) }
        .getOrNull()
