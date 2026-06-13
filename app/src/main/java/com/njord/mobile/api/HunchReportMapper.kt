package com.njord.mobile.api

import com.njord.mobile.model.HunchReport
import com.njord.mobile.model.LayerScore
import com.njord.mobile.model.ReportFactor
import com.njord.mobile.model.Tone
import java.text.NumberFormat
import java.time.Duration
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException
import java.util.Locale

private val USD_FORMATTER = NumberFormat.getCurrencyInstance(Locale.US)

internal fun mapApiReport(report: HunchReportApiResponse): HunchReport {
    val rawSignal = report.rawSignal ?: report.signal
    return HunchReport(
        title = "Hunch BTC Signal - ${report.date}",
        persistedAge = report.createdAt?.let(::formatPersistedAge) ?: "Latest persisted report",
        signal = rawSignal.uppercase(),
        signalTone = signalTone(rawSignal),
        confidence = report.confidence?.uppercase() ?: "N/A",
        score = report.score?.let { formatScore(it) } ?: "N/A",
        date = report.date,
        btcPriceAtSignal = report.btcPriceAtSignal?.let(USD_FORMATTER::format) ?: "N/A",
        currentBtcPrice = report.currentBtcPrice?.let(USD_FORMATTER::format) ?: "N/A",
        priceDelta = report.priceDeltaPct?.let { formatSignedPercent(it) } ?: "N/A",
        priceDeltaTone = report.priceDeltaPct?.let(::numberTone) ?: Tone.Muted,
        wasSignalCorrect = report.wasSignalCorrect?.let { if (it) "YES" else "NO" } ?: "N/A",
        wasSignalCorrectTone = when (report.wasSignalCorrect) {
            true -> Tone.Success
            false -> Tone.Danger
            null -> Tone.Muted
        },
        keyFactors = report.keyFactors.map { ReportFactor(it) },
        risks = report.risks.map { ReportFactor(it, isRisk = true) },
        layerScores = mapLayerScores(report.layerScores)
    )
}

private fun mapLayerScores(scores: Map<String, Double?>): List<LayerScore> =
    scores.mapNotNull { (key, value) ->
        value?.let {
            LayerScore(
                name = key.split("_").joinToString(" ") { part ->
                    part.replaceFirstChar { char -> char.uppercase() }
                },
                score = formatScore(it),
                tone = numberTone(it)
            )
        }
    }

private fun signalTone(signal: String): Tone =
    when (signal.uppercase()) {
        "BUY", "BULLISH" -> Tone.Success
        "SELL", "BEARISH" -> Tone.Danger
        else -> Tone.Muted
    }

private fun numberTone(value: Double): Tone =
    when {
        value > 0.0 -> Tone.Success
        value < 0.0 -> Tone.Danger
        else -> Tone.Muted
    }

private fun formatScore(value: Double): String =
    String.format(Locale.US, "%+.3f", value)

private fun formatSignedPercent(value: Double): String =
    String.format(Locale.US, "%+.2f%%", value)

private fun formatPersistedAge(createdAt: String): String =
    try {
        val created = OffsetDateTime.parse(createdAt)
        val rawElapsed = Duration.between(created, OffsetDateTime.now())
        val elapsed = if (rawElapsed.isNegative) rawElapsed.negated() else rawElapsed
        when {
            elapsed.toMinutes() < 1 -> "Persisted just now"
            elapsed.toHours() < 1 -> "Persisted ${elapsed.toMinutes()}m ago"
            elapsed.toDays() < 1 -> "Persisted ${elapsed.toHours()}h ago"
            else -> "Persisted ${elapsed.toDays()}d ago"
        }
    } catch (_: DateTimeParseException) {
        "Persisted $createdAt"
    }
