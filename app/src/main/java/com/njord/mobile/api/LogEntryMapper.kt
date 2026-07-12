package com.njord.mobile.api

import com.njord.mobile.model.LogEntry
import com.njord.mobile.model.LogFilter
import com.njord.mobile.model.StrategyFilter
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

private val TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")

internal fun mapApiEntries(entries: List<LogApiEntry>): List<LogEntry> =
    entries.map { entry ->
        val causeStrategy = entry.causeStrategy
            ?.let(::parseStrategy)
            ?.takeUnless { it == StrategyFilter.All }
        val entryStrategy = entry.strategy
            ?.let(::parseStrategy)
            ?.takeUnless { it == StrategyFilter.All }
        val contentStrategy = parseStrategy("${entry.title} ${entry.message}")
            .takeUnless { it == StrategyFilter.All }
        val strategy = causeStrategy ?: entryStrategy ?: contentStrategy ?: parseStrategy(entry.title)
        val strategyTitle = entry.causeStrategyName
            ?: causeStrategy?.label
            ?: entry.strategyName
            ?: entryStrategy?.label
            ?: strategy.takeUnless { it == StrategyFilter.All }?.label
            ?: entry.title.ifBlank { "System" }
        LogEntry(
            level = when (entry.level) {
                "WARNING" -> LogFilter.Warn
                "ERROR" -> LogFilter.Error
                else -> LogFilter.Info
            },
            strategy = strategy,
            title = entry.title.ifBlank { strategyTitle },
            message = entry.message,
            time = parseTimestamp(entry.timestamp),
            searchText = listOfNotNull(
                entry.title,
                entry.causeStrategy,
                entry.causeStrategyName,
                entry.strategy,
                entry.strategyName,
                entry.message
            ).joinToString(" ")
        )
    }

private fun parseTimestamp(timestamp: String): String =
    runCatching { OffsetDateTime.parse(timestamp).format(TIME_FORMATTER) }.getOrNull()
        ?: runCatching { LocalDateTime.parse(timestamp).format(TIME_FORMATTER) }.getOrDefault(timestamp)

internal fun parseStrategy(title: String): StrategyFilter {
    val lower = title.lowercase().replace("_", " ").replace("-", " ")
    return when {
        lower.contains("big bang") || lower.contains("bigbang") -> StrategyFilter.BigBang
        lower.contains("wcr") -> StrategyFilter.Wcr
        lower.contains("hunch") -> StrategyFilter.Hunch
        else -> StrategyFilter.All
    }
}
