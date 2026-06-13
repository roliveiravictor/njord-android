package com.njord.mobile.model

enum class Destination(val title: String, val subtitle: String) {
    Home("Home", "Balance, open P&L, and strategy health"),
    Portfolio("Portfolio", "Performance history filtered by strategy"),
    Live("Live", "Current open positions and real-time P&L"),
    Risk("Risk", "Exposure, stops, and divergence"),
    More("More", "Operational diagnostics and reports"),
    Activity("Activity", "Candle-close cycles and strategy activity"),
    Heartbeat("Heartbeat", "8 concrete Njord service routines"),
    Logs("Logs", "Latest 24 hours"),
    Reports("Reports", "Hunch signal report");

    val navGroup: Destination
        get() = when (this) {
            Activity, Heartbeat, Logs, Reports -> More
            else -> this
        }
}

enum class StrategyFilter(val label: String, val key: String) {
    All("All", "all"),
    BigBang("Big Bang", "big-bang"),
    Wcr("WCR", "wcr"),
    Hunch("Hunch", "hunch")
}

enum class SideFilter(val label: String) {
    All("All"),
    Long("L"),
    Short("S")
}

enum class LogFilter(val label: String) {
    All("All"),
    Info("Info"),
    Warn("Warn"),
    Error("Error")
}

enum class Tone {
    Primary,
    Success,
    Warning,
    Danger,
    Info,
    Muted
}

data class NjordUiState(
    val destination: Destination = Destination.Home,
    val liveStrategyFilter: StrategyFilter = StrategyFilter.All,
    val liveSideFilter: SideFilter = SideFilter.All,
    val portfolioStrategyFilter: StrategyFilter = StrategyFilter.All,
    val logFilter: LogFilter = LogFilter.All,
    val logQuery: String = "",
    val logs: List<LogEntry> = emptyList(),
    val logsLoading: Boolean = false,
    val logsError: Boolean = false,
    val heartbeatRoutines: List<HeartbeatRoutine> = emptyList(),
    val heartbeatHealthyCount: Int = 0,
    val heartbeatLateCount: Int = 0,
    val heartbeatCriticalCount: Int = 0,
    val heartbeatTotalCount: Int = 0,
    val heartbeatLoading: Boolean = false,
    val heartbeatError: Boolean = false,
    val hunchReport: HunchReport? = null,
    val hunchReportLoading: Boolean = false,
    val hunchReportError: Boolean = false,
    val homeSnapshot: HomeSnapshot? = null,
    val homeLoading: Boolean = false,
    val homeError: Boolean = false,
    val activitySummary: ActivitySummary? = null,
    val activityCycles: List<StrategyCycle> = emptyList(),
    val activityLoading: Boolean = false,
    val activityError: Boolean = false,
    val dismissedIncidentIds: Set<String> = emptySet(),
    val selectedIncident: Incident? = null,
    val selectedPosition: LivePosition? = null
)

sealed interface NjordAction {
    data class Navigate(val destination: Destination) : NjordAction
    data class SetLiveStrategyFilter(val filter: StrategyFilter) : NjordAction
    data class SetLiveSideFilter(val filter: SideFilter) : NjordAction
    data class SetPortfolioStrategyFilter(val filter: StrategyFilter) : NjordAction
    data class SetLogFilter(val filter: LogFilter) : NjordAction
    data class SetLogQuery(val query: String) : NjordAction
    data class SelectIncident(val incident: Incident) : NjordAction
    data object CloseIncident : NjordAction
    data class DismissIncident(val incidentId: String) : NjordAction
    data class SelectPosition(val position: LivePosition) : NjordAction
    data object ClosePosition : NjordAction
    data object ActivityLoading : NjordAction
    data class ActivityLoaded(val summary: ActivitySummary, val cycles: List<StrategyCycle>) : NjordAction
    data object ActivityError : NjordAction
    data object LogsLoading : NjordAction
    data class LogsLoaded(val entries: List<LogEntry>) : NjordAction
    data object LogsError : NjordAction
    data object HeartbeatLoading : NjordAction
    data class HeartbeatLoaded(
        val routines: List<HeartbeatRoutine>,
        val healthyCount: Int,
        val lateCount: Int,
        val criticalCount: Int,
        val totalCount: Int
    ) : NjordAction
    data object HeartbeatError : NjordAction
    data object HunchReportLoading : NjordAction
    data class HunchReportLoaded(val report: HunchReport) : NjordAction
    data object HunchReportError : NjordAction
    data object HomeLoading : NjordAction
    data class HomeLoaded(val snapshot: HomeSnapshot) : NjordAction
    data object HomeError : NjordAction
}

fun reduce(state: NjordUiState, action: NjordAction): NjordUiState =
    when (action) {
        is NjordAction.Navigate -> state.copy(
            destination = action.destination,
            selectedIncident = null,
            selectedPosition = null
        )

        is NjordAction.SetLiveStrategyFilter -> state.copy(liveStrategyFilter = action.filter)
        is NjordAction.SetLiveSideFilter -> state.copy(liveSideFilter = action.filter)
        is NjordAction.SetPortfolioStrategyFilter -> state.copy(portfolioStrategyFilter = action.filter)
        is NjordAction.SetLogFilter -> state.copy(logFilter = action.filter)
        is NjordAction.SetLogQuery -> state.copy(logQuery = action.query)
        is NjordAction.SelectIncident -> state.copy(selectedIncident = action.incident)
        NjordAction.CloseIncident -> state.copy(selectedIncident = null)
        is NjordAction.DismissIncident -> state.copy(
            dismissedIncidentIds = state.dismissedIncidentIds + action.incidentId,
            selectedIncident = null
        )

        is NjordAction.SelectPosition -> state.copy(selectedPosition = action.position)
        NjordAction.ClosePosition -> state.copy(selectedPosition = null)
        NjordAction.ActivityLoading -> state.copy(activityLoading = true, activityError = false)
        is NjordAction.ActivityLoaded -> state.copy(
            activitySummary = action.summary,
            activityCycles = action.cycles,
            activityLoading = false,
            activityError = false
        )
        NjordAction.ActivityError -> state.copy(activityLoading = false, activityError = true)
        NjordAction.LogsLoading -> state.copy(logsLoading = true, logsError = false)
        is NjordAction.LogsLoaded -> state.copy(logs = action.entries, logsLoading = false, logsError = false)
        NjordAction.LogsError -> state.copy(logsLoading = false, logsError = true)
        NjordAction.HeartbeatLoading -> state.copy(heartbeatLoading = true, heartbeatError = false)
        is NjordAction.HeartbeatLoaded -> state.copy(
            heartbeatRoutines = action.routines,
            heartbeatHealthyCount = action.healthyCount,
            heartbeatLateCount = action.lateCount,
            heartbeatCriticalCount = action.criticalCount,
            heartbeatTotalCount = action.totalCount,
            heartbeatLoading = false,
            heartbeatError = false
        )
        NjordAction.HeartbeatError -> state.copy(heartbeatLoading = false, heartbeatError = true)
        NjordAction.HunchReportLoading -> state.copy(hunchReportLoading = true, hunchReportError = false)
        is NjordAction.HunchReportLoaded -> state.copy(
            hunchReport = action.report,
            hunchReportLoading = false,
            hunchReportError = false
        )
        NjordAction.HunchReportError -> state.copy(hunchReportLoading = false, hunchReportError = true)
        NjordAction.HomeLoading -> state.copy(homeLoading = true, homeError = false)
        is NjordAction.HomeLoaded -> state.copy(
            homeSnapshot = action.snapshot,
            homeLoading = false,
            homeError = false
        )
        NjordAction.HomeError -> state.copy(homeLoading = false, homeError = true)
    }

fun visibleLivePositions(
    positions: List<LivePosition>,
    strategyFilter: StrategyFilter,
    sideFilter: SideFilter
): List<LivePosition> =
    positions.filter { position ->
        val strategyMatches = strategyFilter == StrategyFilter.All || position.strategy == strategyFilter
        val sideMatches = when (sideFilter) {
            SideFilter.All -> true
            SideFilter.Long -> position.side.equals("Long", ignoreCase = true)
            SideFilter.Short -> position.side.equals("Short", ignoreCase = true)
        }
        strategyMatches && sideMatches
    }

fun visibleLogs(logs: List<LogEntry>, filter: LogFilter, query: String): List<LogEntry> {
    val normalizedQuery = query.trim().lowercase()
    return logs.filter { log ->
        val levelMatches = filter == LogFilter.All || log.level == filter
        val queryMatches = normalizedQuery.isEmpty() ||
            listOf(log.title, log.message, log.searchText, log.time)
                .any { it.lowercase().contains(normalizedQuery) }
        levelMatches && queryMatches
    }
}

data class HeroKpi(val label: String, val value: String)
data class MiniKpi(val label: String, val value: String, val subtext: String, val tone: Tone = Tone.Success)
data class HomeSnapshot(
    val totalEquity: String,
    val unrealizedPnl: String,
    val unrealizedPnlPct: String,
    val availableMargin: String,
    val inUse: String,
    val openPositionCount: String,
    val strategies: List<StrategySummary>,
    val activitySummary: ActivitySummary?,
    val heartbeatHealthy: Int,
    val heartbeatTotal: Int,
    val heartbeatLateCount: Int
)
data class StrategySummary(
    val name: String,
    val subtitle: String,
    val pnl: String,
    val pct: String,
    val live: Boolean,
    val assets: String? = null
)
data class ActivitySummary(val opened: String, val closed: String, val kept: String)
data class Incident(
    val id: String,
    val title: String,
    val subtitle: String,
    val current: String,
    val threshold: String,
    val detail: String,
    val badge: String,
    val tone: Tone,
    val age: String,
    val reason: String
)

data class PortfolioPosition(
    val symbol: String,
    val side: String,
    val strategy: StrategyFilter,
    val subtitle: String,
    val pnl: String,
    val pct: String,
    val entry: String,
    val current: String,
    val capital: String,
    val size: String,
    val tone: Tone
)

data class LivePosition(
    val id: String,
    val symbol: String,
    val side: String,
    val strategy: StrategyFilter,
    val strategyName: String,
    val opened: String,
    val pnl: String,
    val pct: String,
    val size: String,
    val capital: String,
    val entry: String,
    val current: String,
    val trendUp: Boolean
)

data class RiskCheck(val title: String, val subtitle: String, val badge: String, val tone: Tone)
data class HeartbeatRoutine(val name: String, val status: String, val age: String, val cadence: String, val tone: Tone)
data class LogEntry(
    val level: LogFilter,
    val title: String,
    val message: String,
    val time: String,
    val searchText: String
)

data class ActivityAction(val label: String, val symbol: String, val side: String)
data class StrategyCycle(val strategy: String, val actions: List<ActivityAction>)
data class ChartPoint(val x: Float, val y: Float)
data class ReportFactor(val text: String, val isRisk: Boolean = false)
data class LayerScore(val name: String, val score: String, val tone: Tone)
data class HunchReport(
    val title: String,
    val persistedAge: String,
    val signal: String,
    val signalTone: Tone,
    val confidence: String,
    val score: String,
    val date: String,
    val btcPriceAtSignal: String,
    val currentBtcPrice: String,
    val priceDelta: String,
    val priceDeltaTone: Tone,
    val wasSignalCorrect: String,
    val wasSignalCorrectTone: Tone,
    val keyFactors: List<ReportFactor>,
    val risks: List<ReportFactor>,
    val layerScores: List<LayerScore>
)
