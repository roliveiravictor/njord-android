package com.njord.mobile.api

import com.njord.mobile.model.ActivityAction
import com.njord.mobile.model.ActivitySummary
import com.njord.mobile.model.StrategyCycle

internal fun mapApiActivity(response: ActivityApiResponse): Pair<ActivitySummary, List<StrategyCycle>> {
    val latest = response.cycles.firstOrNull()
    val summary = ActivitySummary(
        opened = (latest?.totalOpened ?: 0).toString(),
        closed = (latest?.totalClosed ?: 0).toString(),
        kept = (latest?.totalKept ?: 0).toString()
    )
    val cycles = (latest?.strategies ?: emptyList()).map { strategy ->
        StrategyCycle(
            strategy = formatStrategyName(strategy.name),
            actions = buildList {
                strategy.opened.forEach { add(ActivityAction("Opened", it.symbol.uppercase(), formatSide(it.side))) }
                strategy.closed.forEach { add(ActivityAction("Closed", it.symbol.uppercase(), formatSide(it.side))) }
                strategy.kept.forEach { add(ActivityAction("Kept", it.symbol.uppercase(), formatSide(it.side))) }
            }
        )
    }
    return summary to cycles
}

private fun formatStrategyName(name: String): String =
    name.split("_", "-").joinToString(" ") { part ->
        part.replaceFirstChar { it.uppercase() }
    }

private fun formatSide(side: String): String =
    side.replaceFirstChar { it.uppercase() }
