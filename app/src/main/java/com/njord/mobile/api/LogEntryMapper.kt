package com.njord.mobile.api

import com.njord.mobile.model.LogEntry
import com.njord.mobile.model.LogFilter
import com.njord.mobile.model.StrategyFilter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")

internal fun mapApiEntries(entries: List<LogApiEntry>): List<LogEntry> =
    entries.map { entry ->
        LogEntry(
            level = when (entry.level) {
                "WARNING" -> LogFilter.Warn
                "ERROR" -> LogFilter.Error
                else -> LogFilter.Info
            },
            strategy = parseStrategy(entry.title),
            title = entry.title,
            message = entry.message,
            time = runCatching { LocalDateTime.parse(entry.timestamp).format(TIME_FORMATTER) }
                .getOrDefault(entry.timestamp),
            searchText = "${entry.title} ${entry.message}"
        )
    }

internal fun parseStrategy(title: String): StrategyFilter {
    val lower = title.lowercase()
    return when {
        lower.contains("big bang") || lower.contains("bigbang") -> StrategyFilter.BigBang
        lower.contains("wcr") -> StrategyFilter.Wcr
        lower.contains("hunch") -> StrategyFilter.Hunch
        else -> StrategyFilter.All
    }
}
