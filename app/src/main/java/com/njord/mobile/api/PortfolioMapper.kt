package com.njord.mobile.api

import com.njord.mobile.model.ChartPoint
import com.njord.mobile.model.PortfolioMetric
import com.njord.mobile.model.PortfolioMonthReturn
import com.njord.mobile.model.PortfolioSnapshot
import com.njord.mobile.model.Tone
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

private val USD_FORMATTER = NumberFormat.getCurrencyInstance(Locale.US)
private val MONTH_OUTPUT = DateTimeFormatter.ofPattern("MMM", Locale.US)

internal fun mapApiPortfolio(response: PortfolioApiResponse): PortfolioSnapshot {
    val monthlyReturns = mapMonthlyReturns(response.monthlyReturns)
    val bestMonth = response.monthlyStats.bestMonth ?: response.monthlyReturns.maxByOrNull { it.pnlPct }
    val worstMonth = response.monthlyStats.worstMonth ?: response.monthlyReturns.minByOrNull { it.pnlPct }
    val averagePnl = response.monthlyStats.averageMonthlyPnl
        ?: response.monthlyReturns.takeIf { it.isNotEmpty() }?.map { it.pnlPct }?.average()

    val intradayPnl = response.liveMetrics.realizedPnl + response.liveMetrics.unrealizedPnl
    val todayPnlValue = response.performanceStrip.todayPnl ?: intradayPnl
    val todayPnlKnown = response.performanceStrip.todayPnl != null

    return PortfolioSnapshot(
        totalEquity = formatCompactCurrency(response.totalEquity),
        returnBadge = "ALL ${formatSignedPercent(response.allTimeReturnPct)}",
        returnTone = toneFor(response.allTimeReturnPct),
        todayPnl = formatSignedCurrency(todayPnlValue),
        todayPct = response.performanceStrip.todayPnlPct?.let(::formatSignedPercent)
            ?: if (todayPnlKnown) "No baseline" else "Intraday",
        todayTone = toneFor(todayPnlValue),
        sevenDayPnl = response.performanceStrip.sevenDayPnl?.let(::formatSignedCurrency) ?: "N/A",
        sevenDayPct = response.performanceStrip.sevenDayPnlPct?.let(::formatSignedPercent) ?: "No baseline",
        sevenDayTone = toneFor(response.performanceStrip.sevenDayPnl ?: 0.0),
        thirtyDayPnl = response.performanceStrip.thirtyDayPnl?.let(::formatSignedCurrency) ?: "N/A",
        thirtyDayPct = response.performanceStrip.thirtyDayPnlPct?.let(::formatSignedPercent) ?: "No baseline",
        thirtyDayTone = toneFor(response.performanceStrip.thirtyDayPnl ?: 0.0),
        liveMetrics = listOf(
            PortfolioMetric("REALIZED P&L", formatSignedCurrency(response.liveMetrics.realizedPnl), toneFor(response.liveMetrics.realizedPnl), "Closed positions"),
            PortfolioMetric("UNREALIZED P&L", formatSignedCurrency(response.liveMetrics.unrealizedPnl), toneFor(response.liveMetrics.unrealizedPnl), "Open positions"),
            PortfolioMetric("WIN RATE", "${formatNumber(response.liveMetrics.winRate)}%", Tone.Muted, "${response.liveMetrics.totalClosedTrades} closed trades"),
            PortfolioMetric("PROFIT FACTOR", formatNumber(response.liveMetrics.profitFactor), Tone.Muted, "Gross profit / loss")
        ),
        monthlyStats = listOf(
            PortfolioMetric("BEST MONTH", bestMonth?.let { formatSignedPercent(it.pnlPct) } ?: "N/A", bestMonth?.let { toneFor(it.pnlPct) } ?: Tone.Muted, bestMonth?.month?.let(::formatMonth) ?: "No data"),
            PortfolioMetric("WORST MONTH", worstMonth?.let { formatSignedPercent(it.pnlPct) } ?: "N/A", worstMonth?.let { toneFor(it.pnlPct) } ?: Tone.Muted, worstMonth?.month?.let(::formatMonth) ?: "No data"),
            PortfolioMetric("AVERAGE", averagePnl?.let(::formatSignedPercent) ?: "N/A", averagePnl?.let(::toneFor) ?: Tone.Muted, "Last ${response.monthlyReturns.size.coerceAtLeast(1)} months")
        ),
        equityStats = listOf(
            PortfolioMetric("Current", formatCompactCurrency(response.totalEquity)),
            PortfolioMetric("High", formatCompactCurrency(response.equityCurve.maxOfOrNull { it.equity } ?: response.totalEquity)),
            PortfolioMetric("30D P&L", response.performanceStrip.thirtyDayPnl?.let(::formatSignedCurrency) ?: "N/A", toneFor(response.performanceStrip.thirtyDayPnl ?: 0.0))
        ),
        equityCurve = normalizeSeries(response.equityCurve.map { it.equity }),
        equityAxisLabels = axisLabels(response.equityCurve.map { it.timestamp }, fallbackEnd = "Today"),
        drawdownStats = listOf(
            PortfolioMetric("Current", formatDrawdown(response.currentDrawdownPct), toneForDrawdown(response.currentDrawdownPct)),
            PortfolioMetric("Max", formatDrawdown(response.maxDrawdownPct), toneForDrawdown(response.maxDrawdownPct)),
            PortfolioMetric("Recovery", "${formatNumber(response.recoveryPct)}%")
        ),
        drawdownCurve = normalizeSeries(response.drawdownSeries.map { abs(it.drawdownPct) }, invert = false),
        drawdownAxisLabels = axisLabels(response.drawdownSeries.map { it.timestamp }, fallbackStart = "0%", fallbackEnd = "Recovery"),
        monthlyReturns = monthlyReturns
    )
}

private fun mapMonthlyReturns(returns: List<PortfolioMonthlyReturnApiResponse>): List<PortfolioMonthReturn> {
    val maxAbs = returns.maxOfOrNull { abs(it.pnlPct) }?.takeIf { it > 0.0 } ?: 1.0
    return returns.map {
        PortfolioMonthReturn(
            month = formatMonth(it.month),
            value = formatSignedPercent(it.pnlPct),
            progress = (abs(it.pnlPct) / maxAbs).toFloat().coerceIn(0.08f, 1f),
            tone = toneFor(it.pnlPct)
        )
    }
}

private fun normalizeSeries(values: List<Double>, invert: Boolean = true): List<ChartPoint> {
    if (values.isEmpty()) return emptyList()
    if (values.size == 1) return listOf(ChartPoint(0f, 0.5f), ChartPoint(1f, 0.5f))
    val min = values.minOrNull() ?: 0.0
    val max = values.maxOrNull() ?: min
    val range = (max - min).takeIf { it > 0.0 } ?: 1.0
    return values.mapIndexed { index, value ->
        val normalized = ((value - min) / range).toFloat()
        ChartPoint(
            x = index.toFloat() / (values.lastIndex).toFloat(),
            y = if (invert) 0.86f - normalized * 0.72f else 0.14f + normalized * 0.72f
        )
    }
}

private fun axisLabels(timestamps: List<String>, fallbackStart: String = "", fallbackEnd: String): List<String> {
    val start = timestamps.firstOrNull()?.let(::formatAxisDate) ?: fallbackStart
    val middle = timestamps.getOrNull(timestamps.size / 2)?.let(::formatAxisDate)
    val end = timestamps.lastOrNull()?.let(::formatAxisDate) ?: fallbackEnd
    return listOfNotNull(start.takeIf { it.isNotBlank() }, middle, end).distinct()
}

private fun formatAxisDate(value: String): String =
    runCatching {
        val date = LocalDate.parse(value.substringBefore("T"))
        "${date.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }} ${date.dayOfMonth}"
    }.getOrDefault(value.take(6))

private fun formatMonth(value: String): String =
    runCatching { LocalDate.parse("$value-01").format(MONTH_OUTPUT) }
        .getOrDefault(value.take(3))

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

private fun formatSignedPercent(value: Double): String =
    "${String.format(Locale.US, "%+.1f", value)}%"

private fun formatDrawdown(value: Double): String =
    "-${formatNumber(abs(value))}%"

private fun formatNumber(value: Double): String =
    String.format(Locale.US, "%.1f", value)

private fun toneFor(value: Double): Tone =
    if (value < 0.0) Tone.Danger else Tone.Success

private fun toneForDrawdown(value: Double): Tone =
    when {
        abs(value) >= 5.0 -> Tone.Danger
        abs(value) > 0.0 -> Tone.Warning
        else -> Tone.Muted
    }
