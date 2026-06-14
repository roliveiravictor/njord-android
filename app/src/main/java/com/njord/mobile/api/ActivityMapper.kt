package com.njord.mobile.api

import com.njord.mobile.model.ActivityAction
import com.njord.mobile.model.ActivitySummary
import com.njord.mobile.model.StrategyCycle

internal fun mapApiActivity(response: ActivityApiResponse): Pair<ActivitySummary, List<StrategyCycle>> {
    val summary = ActivitySummary(
        opened = response.cycles.sumOf { it.totalOpened }.toString(),
        closed = response.cycles.sumOf { it.totalClosed }.toString(),
        kept = response.cycles.sumOf { it.totalKept }.toString()
    )

    val actionsByStrategy = linkedMapOf<String, MutableList<ActivityAction>>()
    response.cycles.forEach { cycle ->
        cycle.strategies.forEach { strategy ->
            val strategyName = formatStrategyName(strategy.name)
            val actions = actionsByStrategy.getOrPut(strategyName) { mutableListOf() }
            strategy.opened.forEach { actions.add(ActivityAction("Opened", formatSymbol(it.symbol), formatSide(it.side))) }
            strategy.closed.forEach { actions.add(ActivityAction("Closed", formatSymbol(it.symbol), formatSide(it.side))) }
            strategy.kept.forEach { actions.add(ActivityAction("Kept", formatSymbol(it.symbol), formatSide(it.side))) }
        }
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
