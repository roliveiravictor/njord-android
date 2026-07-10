package com.njord.mobile.api

import com.njord.mobile.model.ActivitySummary
import com.njord.mobile.model.HomeLogsSummary
import com.njord.mobile.model.HomeSnapshot
import com.njord.mobile.model.StrategyFilter
import com.njord.mobile.model.StrategySummary
import java.text.NumberFormat
import java.time.Instant
import java.util.Locale
import kotlin.math.abs

private val USD_FORMATTER = NumberFormat.getCurrencyInstance(Locale.US)

internal fun mapApiHome(response: HomeApiResponse, now: Instant = Instant.now()): HomeSnapshot =
    HomeSnapshot(
        totalBalance = USD_FORMATTER.format(response.totalBalance),
        unrealizedPnl = formatSignedCurrency(response.unrealizedPnl),
        unrealizedPnlPct = "${formatSignedNumber(response.unrealizedPnlPct)}% unrealized",
        availableMargin = formatCompactCurrency(response.availableMargin),
        inUse = formatCompactCurrency(response.inUse),
        marginInUse = formatCompactCurrency(response.marginInUse),
        openPositionCount = "${response.openPositionCount} Pos",
        strategies = response.strategies.map(::mapHomeStrategy),
        activitySummary = response.latestCycle?.let {
            ActivitySummary(
                opened = it.openedCount.toString(),
                closed = it.closedCount.toString(),
                kept = it.keptCount.toString()
            )
        },
        logsSummary = HomeLogsSummary(
            warningCount = response.logs.warningCount,
            errorCount = response.logs.errorCount,
            totalCount = response.logs.totalCount,
            hours = response.logs.hours
        ),
        heartbeatHealthy = response.heartbeat.healthy,
        heartbeatTotal = response.heartbeat.total,
        heartbeatLateCount = response.heartbeat.lateCount,
        incidents = response.incidents.mapIndexed { index, incident ->
            mapApiIncident(incident, index, now)
        }
    )

private fun mapHomeStrategy(strategy: HomeApiStrategy): StrategySummary =
    StrategySummary(
        name = strategy.name,
        filter = strategy.name.toStrategyFilter(),
        subtitle = "${strategy.positionCount} ${if (strategy.positionCount == 1) "position" else "positions"}",
        pnl = strategy.unrealizedPnl?.let(::formatSignedCurrency).orEmpty(),
        pct = strategy.unrealizedPnlPct?.let { "${formatSignedNumber(it)}%" }.orEmpty(),
        live = strategy.isLive,
        assets = strategy.symbols.joinToString(" · ").takeIf { it.isNotBlank() }
    )

private fun String.toStrategyFilter(): StrategyFilter =
    when (lowercase().replace("_", " ").replace("-", " ").trim()) {
        "big bang" -> StrategyFilter.BigBang
        "wcr" -> StrategyFilter.Wcr
        "hunch" -> StrategyFilter.Hunch
        else -> StrategyFilter.All
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

private fun formatSignedNumber(value: Double): String =
    String.format(Locale.US, "%+.1f", value)
