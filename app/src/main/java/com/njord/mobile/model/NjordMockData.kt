package com.njord.mobile.model

object NjordMockData {
    val heroKpis = listOf(
        HeroKpi("Equity", "$18,420"),
        HeroKpi("Margin", "40%"),
        HeroKpi("Open", "18")
    )

    val homeKpis = listOf(
        MiniKpi("Account balance", "$18,420", "+$1,204 today"),
        MiniKpi("Open P&L", "+$912", "+5.2%"),
        MiniKpi("Risk state", "Guarded", "1 warning", Tone.Warning),
        MiniKpi("Heartbeat", "7 / 8", "1 late", Tone.Warning)
    )

    val strategySummaries = listOf(
        StrategySummary("Big Bang", "2 positions · 2L", "+$212", "+4.1%", true, "HYPE · ETH"),
        StrategySummary("WCR", "3 positions · 2L / 1S", "+$184", "+2.6%", true, "HYPE · BTC · ETH"),
        StrategySummary("Hunch", "0 positions", "", "", true)
    )

    val activitySummary = ActivitySummary(opened = "2", closed = "1", kept = "15")

    val incidents = listOf(
        Incident(
            id = "btc-stop",
            title = "BTC near stop",
            subtitle = "Big Bang · Long",
            current = "-8.7%",
            threshold = "-10%",
            detail = "BTC is approaching the configured P&L stop threshold. Monitor position or related stop heartbeat.",
            badge = "Watch",
            tone = Tone.Warning,
            age = "12m",
            reason = "Stop"
        ),
        Incident(
            id = "eth-open-failed",
            title = "ETH open failed",
            subtitle = "Big Bang · Long",
            current = "Rejected",
            threshold = "Margin",
            detail = "Open order was rejected by the exchange because available margin was insufficient.",
            badge = "Error",
            tone = Tone.Danger,
            age = "8m",
            reason = "Margin"
        ),
        Incident(
            id = "arb-close-failed",
            title = "ARB close failed",
            subtitle = "WCR · Short",
            current = "Rejected",
            threshold = "Order",
            detail = "Close order was rejected. Position is still expected to be live until exchange state confirms closure.",
            badge = "Error",
            tone = Tone.Danger,
            age = "14m",
            reason = "Order"
        )
    )

    val portfolioPositions = listOf(
        PortfolioPosition("HYPE", "Long", StrategyFilter.BigBang, "53.8 HYPE · 10h open", "+$186", "+8.4%", "$41.20", "$44.66", "$2.2k", "53.8", Tone.Success),
        PortfolioPosition("ETH", "Long", StrategyFilter.BigBang, "1.52 ETH · 18h open", "+$212", "+4.1%", "$3,420", "$3,560", "$5.2k", "1.52", Tone.Success),
        PortfolioPosition("SOL", "Short", StrategyFilter.Wcr, "18.1 SOL · 6h open", "-$48", "-1.2%", "$165.00", "$167.00", "$3.0k", "18.1", Tone.Danger),
        PortfolioPosition("SUI", "Long", StrategyFilter.Wcr, "Balanced basket leg", "+$74", "+2.8%", "$3.68", "$3.78", "$1.4k", "370", Tone.Success),
        PortfolioPosition("ARB", "Short", StrategyFilter.Wcr, "Close retry pending", "-$31", "-2.2%", "$1.02", "$1.04", "$1.1k", "1,050", Tone.Danger)
    )

    val livePositions = listOf(
        LivePosition("hype-long", "HYPE", "Long", StrategyFilter.BigBang, "Big Bang", "10h", "+$186", "+8.4%", "53.8 HYPE", "$2.2k", "$41.20", "$44.66", true),
        LivePosition("eth-long", "ETH", "Long", StrategyFilter.BigBang, "Big Bang", "18h", "+$212", "+4.1%", "1.52 ETH", "$5.2k", "$3,420", "$3,560", true),
        LivePosition("sol-short", "SOL", "Short", StrategyFilter.Wcr, "WCR", "6h", "-$48", "-1.2%", "18.1 SOL", "$3.0k", "$165.00", "$167.00", false),
        LivePosition("sui-long", "SUI", "Long", StrategyFilter.Wcr, "WCR", "14h", "+$74", "+2.8%", "370 SUI", "$1.4k", "$3.68", "$3.78", true),
        LivePosition("arb-short", "ARB", "Short", StrategyFilter.Wcr, "WCR", "22h", "-$31", "-2.2%", "1,050 ARB", "$1.1k", "$1.02", "$1.04", false)
    )

    val heartbeatRoutines = listOf(
        HeartbeatRoutine("VPN", "Healthy", "2m ago", "20m", Tone.Success),
        HeartbeatRoutine("Strategy", "Healthy", "3m ago", "20m", Tone.Success),
        HeartbeatRoutine("Database", "Healthy", "12m ago", "20m", Tone.Success),
        HeartbeatRoutine("Log", "Healthy", "14m ago", "20m", Tone.Success),
        HeartbeatRoutine("Git", "Healthy", "8m ago", "20m", Tone.Success),
        HeartbeatRoutine("Weekly performance report", "Late", "26m ago", "20m", Tone.Warning),
        HeartbeatRoutine("PnL stop loss", "Healthy", "8m ago", "15m", Tone.Success),
        HeartbeatRoutine("PnL trailing stop", "Healthy", "9m ago", "15m", Tone.Success)
    )

    val logs = listOf(
        LogEntry(LogFilter.Error, StrategyFilter.BigBang, "Big Bang open failed · ETH Long", "Exchange rejected order: insufficient margin", "13:52", "Big Bang error open ETH long insufficient margin order rejected"),
        LogEntry(LogFilter.Error, StrategyFilter.Wcr, "WCR retraining failed", "Model artifact validation failed after training run", "13:22", "WCR model retraining failed artifact validation"),
        // parseStrategy("BTC near P&L stop threshold") returns All — BigBang assigned here as mock design intent
        LogEntry(LogFilter.Warn, StrategyFilter.BigBang, "BTC near P&L stop threshold", "Current -8.7% · threshold -10%", "13:48", "Big Bang BTC near PnL stop threshold"),
        LogEntry(LogFilter.Error, StrategyFilter.Wcr, "WCR close failed · ARB Short", "Hyperliquid rejected order: insufficient margin after retry", "13:48", "WCR error close ARB short insufficient margin order rejected hyperliquid"),
        LogEntry(LogFilter.Warn, StrategyFilter.All, "Weekly performance heartbeat late", "Expected 20m cadence · last success 26m ago", "13:34", "weekly report warning late performance heartbeat"),
        LogEntry(LogFilter.Info, StrategyFilter.BigBang, "Big Bang opened ETH Long", "Entry metadata persisted · probability 64%", "14:00", "Big Bang info opened ETH long entry persisted probability"),
        LogEntry(LogFilter.Info, StrategyFilter.Wcr, "WCR rebalance kept", "6 long / 6 short · no basket drift", "14:00", "WCR rebalance kept long short balanced"),
        LogEntry(LogFilter.Warn, StrategyFilter.All, "Price API delayed · SOL", "Current price refreshed after second attempt", "11:22", "price api warning stale current price SOL"),
        LogEntry(LogFilter.Info, StrategyFilter.Hunch, "Hunch report persisted", "Risk-on signal · confidence 73%", "09:05", "hunch report risk-on persisted signal")
    )

    val cycles = listOf(
        StrategyCycle(
            "Big Bang",
            listOf(
                ActivityAction("Opened", "ETH", "Long"),
                ActivityAction("Closed", "BTC", "Long"),
                ActivityAction("Kept", "HYPE", "Long")
            )
        ),
        StrategyCycle(
            "WCR",
            listOf(
                ActivityAction("Opened", "SOL", "Short"),
                ActivityAction("Kept", "SUI", "Long"),
                ActivityAction("Kept", "ARB", "Short")
            )
        )
    )

    val equityCurve = listOf(
        ChartPoint(0f, 0.58f),
        ChartPoint(0.12f, 0.52f),
        ChartPoint(0.24f, 0.62f),
        ChartPoint(0.38f, 0.46f),
        ChartPoint(0.50f, 0.40f),
        ChartPoint(0.64f, 0.34f),
        ChartPoint(0.78f, 0.28f),
        ChartPoint(1f, 0.18f)
    )

    val drawdownCurve = listOf(
        ChartPoint(0f, 0.18f),
        ChartPoint(0.18f, 0.26f),
        ChartPoint(0.36f, 0.48f),
        ChartPoint(0.54f, 0.36f),
        ChartPoint(0.72f, 0.58f),
        ChartPoint(1f, 0.42f)
    )

    val reportFactors = listOf(
        ReportFactor("US military pressure and geopolitical risk pushed the composite layer negative."),
        ReportFactor("ETF flow weakness and short-term risk sentiment reduced BTC upside conviction."),
        ReportFactor("Derivatives positioning remains crowded, increasing reversal risk around the signal."),
        ReportFactor("On-chain accumulation offsets part of the bearish macro and flow pressure."),
        ReportFactor("BTC strength above the signal price can invalidate the short-term SELL bias.", true),
        ReportFactor("Any ETF inflow reversal or macro relief may push the next signal back to neutral.", true)
    )

    val layerScores = listOf(
        LayerScore("Geopolitical", "-0.80", Tone.Danger),
        LayerScore("ETF flows", "-1.00", Tone.Danger),
        LayerScore("Macro", "0.00", Tone.Muted),
        LayerScore("Derivatives", "+0.20", Tone.Success),
        LayerScore("On-chain", "+0.50", Tone.Success),
        LayerScore("Sentiment", "0.00", Tone.Muted),
        LayerScore("Seasonality", "0.00", Tone.Muted)
    )

    val hunchReport = HunchReport(
        title = "Hunch BTC Signal - 2026-06-08",
        persistedAge = "Persisted 18m ago",
        signal = "SELL",
        signalTone = Tone.Danger,
        confidence = "HIGH",
        score = "-0.523",
        date = "2026-06-07",
        btcPriceAtSignal = "$62,215.00",
        currentBtcPrice = "$63,193.50",
        priceDelta = "+1.57%",
        priceDeltaTone = Tone.Success,
        wasSignalCorrect = "NO",
        wasSignalCorrectTone = Tone.Danger,
        keyFactors = reportFactors.filterNot { it.isRisk },
        risks = reportFactors.filter { it.isRisk },
        layerScores = layerScores
    )
}
