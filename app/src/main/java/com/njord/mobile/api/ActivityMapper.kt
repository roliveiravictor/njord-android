package com.njord.mobile.api

import com.njord.mobile.model.ActivityAction
import com.njord.mobile.model.ActivitySummary
import com.njord.mobile.model.StrategyCycle

internal fun mapApiActivity(response: ActivityApiResponse): Pair<ActivitySummary, List<StrategyCycle>> {
    val latestCycle = response.cycles.firstOrNull()
    val summary = ActivitySummary(
        opened = (latestCycle?.totalOpened ?: 0).toString(),
        closed = (latestCycle?.totalClosed ?: 0).toString(),
        kept = (latestCycle?.totalKept ?: 0).toString()
    )

    val actionsByStrategy = linkedMapOf<String, MutableList<ActivityAction>>()
    latestCycle?.strategies?.forEach { strategy ->
        val strategyName = formatStrategyName(strategy.name)
        val actions = actionsByStrategy.getOrPut(strategyName) { mutableListOf() }
        strategy.opened.forEach { actions.add(ActivityAction("Opened", formatSymbol(it.symbol), formatSide(it.side))) }
        strategy.closed.forEach { actions.add(ActivityAction("Closed", formatSymbol(it.symbol), formatSide(it.side))) }
        strategy.kept.forEach { actions.add(ActivityAction("Kept", formatSymbol(it.symbol), formatSide(it.side))) }
    }

    val cycles = actionsByStrategy.map { (strategyName, actions) ->
        StrategyCycle(strategy = strategyName, actions = actions)
    }
    return summary to cycles
}

private fun formatStrategyName(name: String): String =
    if (!name.contains("_") && !name.contains("-") && name.length <= 3) {
        name.uppercase()
    } else {
        name.split("_", "-").joinToString(" ") { part ->
            part.replaceFirstChar { it.uppercase() }
        }
    }

private fun formatSide(side: String): String =
    side.replaceFirstChar { it.uppercase() }

private fun formatSymbol(symbol: String): String =
    symbol.substringBefore("/")
        .substringBefore(":")
        .uppercase()
