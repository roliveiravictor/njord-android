package com.njord.mobile.model

enum class Destination(val title: String, val subtitle: String) {
    Home("Home", "Balance, open P&L, and strategy health"),
    Performance("Performance", "Performance history filtered by strategy"),
    Live("Live", "Current open positions and real-time P&L"),
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
    Warn("Warning"),
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
    val performanceStrategyFilter: StrategyFilter = StrategyFilter.All,
    val logFilter: LogFilter = LogFilter.All,
    val logQuery: String = "",
    val logStrategyFilter: StrategyFilter = StrategyFilter.All,
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
    val hunchReports: List<HunchReport> = emptyList(),
    val hunchReportLoading: Boolean = false,
    val hunchReportError: Boolean = false,
    val performanceSnapshot: PerformanceSnapshot? = null,
    val performanceLoading: Boolean = false,
    val performanceError: Boolean = false,
    val homeSnapshot: HomeSnapshot? = null,
    val homeLoading: Boolean = false,
    val homeError: Boolean = false,
    val activitySummary: ActivitySummary? = null,
    val activityCycles: List<StrategyCycle> = emptyList(),
    val activityLoading: Boolean = false,
    val activityError: Boolean = false,
    val livePositions: List<LivePosition> = emptyList(),
    val liveAnalytics: LiveAnalyticsSnapshot? = null,
    val liveIncidents: List<Incident> = emptyList(),
    val dismissedIncidentIds: Set<String> = emptySet(),
    val liveLoading: Boolean = false,
    val liveError: Boolean = false,
    val selectedIncident: Incident? = null,
    val selectedPosition: LivePosition? = null,
    val selectedPerformancePosition: PerformancePosition? = null
)

sealed interface NjordAction {
    data class Navigate(val destination: Destination) : NjordAction
    data class SetLiveStrategyFilter(val filter: StrategyFilter) : NjordAction
    data class SetLiveSideFilter(val filter: SideFilter) : NjordAction
    data class SetPerformanceStrategyFilter(val filter: StrategyFilter) : NjordAction
    data class SetLogFilter(val filter: LogFilter) : NjordAction
    data class SetLogQuery(val query: String) : NjordAction
    data class SetLogStrategyFilter(val filter: StrategyFilter) : NjordAction
    data class IncidentsSeeded(val incidents: List<Incident>) : NjordAction
    data class IncidentAcknowledgementsLoaded(val incidentIds: Set<String>) : NjordAction
    data class IncidentsReceived(val incidents: List<Incident>) : NjordAction
    data class SelectIncident(val incident: Incident) : NjordAction
    data object CloseIncident : NjordAction
    data class DismissIncident(val incidentId: String) : NjordAction
    data class SelectPosition(val position: LivePosition) : NjordAction
    data object ClosePosition : NjordAction
    data class SelectPerformancePosition(val position: PerformancePosition) : NjordAction
    data object ClosePerformancePosition : NjordAction
    data object ActivityLoading : NjordAction
    data class ActivityLoaded(val summary: ActivitySummary, val cycles: List<StrategyCycle>) : NjordAction
    data object ActivityError : NjordAction
    data object LiveLoading : NjordAction
    data class LiveLoaded(
        val positions: List<LivePosition>,
        val analytics: LiveAnalyticsSnapshot?,
        val incidents: List<Incident>
    ) : NjordAction
    data object LiveError : NjordAction
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
    data class HunchReportLoaded(val reports: List<HunchReport>) : NjordAction
    data object HunchReportError : NjordAction
    data object PerformanceLoading : NjordAction
    data class PerformanceLoaded(val snapshot: PerformanceSnapshot) : NjordAction
    data object PerformanceError : NjordAction
    data object HomeLoading : NjordAction
    data class HomeLoaded(val snapshot: HomeSnapshot) : NjordAction
    data object HomeError : NjordAction
}

private fun mergeIncidentsById(
    existing: List<Incident>,
    incoming: List<Incident>,
    dismissedIds: Set<String> = emptySet()
): List<Incident> {
    val visibleExisting = existing.filterNot { it.id in dismissedIds }
    if (incoming.isEmpty()) return visibleExisting
    val existingIds = visibleExisting.mapTo(mutableSetOf()) { it.id }
    return visibleExisting + incoming.filter { it.id !in dismissedIds && it.id !in existingIds }
}

fun reduce(state: NjordUiState, action: NjordAction): NjordUiState =
    when (action) {
        is NjordAction.Navigate -> state.copy(
            destination = action.destination,
            selectedIncident = null,
            selectedPosition = null,
            selectedPerformancePosition = null
        )

        is NjordAction.SetLiveStrategyFilter -> state.copy(liveStrategyFilter = action.filter)
        is NjordAction.SetLiveSideFilter -> state.copy(liveSideFilter = action.filter)
        is NjordAction.SetPerformanceStrategyFilter -> state.copy(performanceStrategyFilter = action.filter)
        is NjordAction.SetLogFilter -> state.copy(logFilter = action.filter)
        is NjordAction.SetLogQuery -> state.copy(logQuery = action.query)
        is NjordAction.SetLogStrategyFilter -> state.copy(logStrategyFilter = action.filter)
        is NjordAction.IncidentAcknowledgementsLoaded -> state.copy(
            dismissedIncidentIds = action.incidentIds,
            liveIncidents = state.liveIncidents.filterNot { it.id in action.incidentIds },
            selectedIncident = state.selectedIncident?.takeUnless { it.id in action.incidentIds }
        )
        is NjordAction.IncidentsSeeded -> state.copy(
            liveIncidents = mergeIncidentsById(emptyList(), action.incidents, state.dismissedIncidentIds)
        )
        is NjordAction.IncidentsReceived -> state.copy(
            liveIncidents = mergeIncidentsById(state.liveIncidents, action.incidents, state.dismissedIncidentIds)
        )
        is NjordAction.SelectIncident -> state.copy(selectedIncident = action.incident)
        NjordAction.CloseIncident -> state.copy(selectedIncident = null)
        is NjordAction.DismissIncident -> state.copy(
            liveIncidents = state.liveIncidents.filterNot { it.id == action.incidentId },
            dismissedIncidentIds = state.dismissedIncidentIds + action.incidentId,
            selectedIncident = null
        )

        is NjordAction.SelectPosition -> state.copy(selectedPosition = action.position)
        NjordAction.ClosePosition -> state.copy(selectedPosition = null)
        is NjordAction.SelectPerformancePosition -> state.copy(selectedPerformancePosition = action.position)
        NjordAction.ClosePerformancePosition -> state.copy(selectedPerformancePosition = null)
        NjordAction.ActivityLoading -> state.copy(activityLoading = true, activityError = false)
        is NjordAction.ActivityLoaded -> state.copy(
            activitySummary = action.summary,
            activityCycles = action.cycles,
            activityLoading = false,
            activityError = false
        )
        NjordAction.ActivityError -> state.copy(activityLoading = false, activityError = true)
        NjordAction.LiveLoading -> state.copy(liveLoading = true, liveError = false)
        is NjordAction.LiveLoaded -> state.copy(
            livePositions = action.positions,
            liveAnalytics = action.analytics,
            liveIncidents = mergeIncidentsById(state.liveIncidents, action.incidents, state.dismissedIncidentIds),
            liveLoading = false,
            liveError = false
        )
        NjordAction.LiveError -> state.copy(liveLoading = false, liveError = true)
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
            hunchReports = action.reports,
            hunchReportLoading = false,
            hunchReportError = false
        )
        NjordAction.HunchReportError -> state.copy(hunchReportLoading = false, hunchReportError = true)
        NjordAction.PerformanceLoading -> state.copy(performanceLoading = true, performanceError = false)
        is NjordAction.PerformanceLoaded -> state.copy(
            performanceSnapshot = action.snapshot,
            performanceLoading = false,
            performanceError = false
        )
        NjordAction.PerformanceError -> state.copy(performanceLoading = false, performanceError = true)
        NjordAction.HomeLoading -> state.copy(homeLoading = true, homeError = false)
        is NjordAction.HomeLoaded -> state.copy(
            homeSnapshot = action.snapshot.copy(
                incidents = action.snapshot.incidents.filterNot { it.id in state.dismissedIncidentIds }
            ),
            liveIncidents = mergeIncidentsById(state.liveIncidents, action.snapshot.incidents, state.dismissedIncidentIds),
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

fun visibleLogs(
    logs: List<LogEntry>,
    filter: LogFilter,
    query: String,
    strategyFilter: StrategyFilter
): List<LogEntry> {
    val normalizedQuery = query.trim().lowercase()
    return logs.filter { log ->
        val levelMatches = filter == LogFilter.All || log.level == filter
        val strategyMatches = strategyFilter == StrategyFilter.All || log.strategy == strategyFilter
        val queryMatches = normalizedQuery.isEmpty() ||
            listOf(log.title, log.message, log.searchText, log.time)
                .any { it.lowercase().contains(normalizedQuery) }
        levelMatches && strategyMatches && queryMatches
    }
}

data class HeroKpi(val label: String, val value: String)
data class MiniKpi(val label: String, val value: String, val subtext: String, val tone: Tone = Tone.Success)
data class HomeSnapshot(
    val totalBalance: String,
    val unrealizedPnl: String,
    val unrealizedPnlPct: String,
    val availableMargin: String,
    val inUse: String,
    val marginInUse: String,
    val openPositionCount: String,
    val strategies: List<StrategySummary>,
    val activitySummary: ActivitySummary?,
    val logsSummary: HomeLogsSummary,
    val heartbeatHealthy: Int,
    val heartbeatTotal: Int,
    val heartbeatLateCount: Int,
    val incidents: List<Incident>
)
data class StrategySummary(
    val name: String,
    val filter: StrategyFilter,
    val subtitle: String,
    val pnl: String,
    val pct: String,
    val live: Boolean,
    val assets: String? = null
)
data class ActivitySummary(val opened: String, val closed: String, val kept: String)
data class HomeLogsSummary(val warningCount: Int, val errorCount: Int, val totalCount: Int, val hours: Int)
data class PerformanceSnapshot(
    val totalEquity: String,
    val totalEquityTone: Tone,
    val returnBadge: String,
    val returnTone: Tone,
    val unrealizedPnl: String,
    val unrealizedTone: Tone,
    val todayPnl: String,
    val todayPct: String,
    val todayTone: Tone,
    val sevenDayPnl: String,
    val sevenDayPct: String,
    val sevenDayTone: Tone,
    val thirtyDayPnl: String,
    val thirtyDayPct: String,
    val thirtyDayTone: Tone,
    val historyMetrics: List<PerformanceMetric>,
    val streakMetrics: List<PerformanceMetric>,
    val monthlyStats: List<PerformanceMetric>,
    val equityStats: List<PerformanceMetric>,
    val equityCurve: List<ChartPoint>,
    val equityAxisLabels: List<String>,
    val drawdownStats: List<PerformanceMetric>,
    val drawdownCurve: List<ChartPoint>,
    val drawdownAxisLabels: List<String>,
    val monthlyReturns: List<PerformanceMonthReturn>,
    val latestClosedPositions: List<PerformancePosition>
)
data class PerformanceMetric(
    val label: String,
    val value: String,
    val tone: Tone = Tone.Muted,
    val subtext: String? = null
)
data class PerformanceMonthReturn(
    val month: String,
    val value: String,
    val progress: Float,
    val tone: Tone
)
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

data class LiveAnalyticsSnapshot(
    val totalContribution: String,
    val totalContributionTone: Tone,
    val strategyContributions: List<LiveContribution>,
    val summaryItems: List<MiniKpi>,
    val largestWinner: LiveOutcome?,
    val largestLoser: LiveOutcome?,
    val integrityItems: List<MiniKpi>,
    val longCount: Int,
    val shortCount: Int,
    val longPct: Float
)

data class LiveContribution(
    val strategy: String,
    val progress: Float,
    val value: String,
    val tone: Tone
)

data class LiveOutcome(
    val symbol: String,
    val amount: String,
    val percent: String,
    val tone: Tone
)

data class PerformancePosition(
    val symbol: String,
    val side: String,
    val strategy: StrategyFilter,
    val strategyName: String,
    val subtitle: String,
    val pnl: String,
    val pct: String,
    val entry: String,
    val exit: String,
    val capital: String,
    val size: String,
    val opened: String,
    val closed: String,
    val reason: String,
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

data class HeartbeatRoutine(val name: String, val status: String, val lastSeenAt: java.time.Instant?, val cadence: String, val tone: Tone, val secondsOverdue: Int?, val expectedCadenceSeconds: Int)
data class LogEntry(
    val level: LogFilter,
    val strategy: StrategyFilter,
    val title: String,
    val message: String,
    val time: String,
    val searchText: String
)

data class ActivityAction(val label: String, val symbol: String, val side: String)
data class StrategyCycle(val strategy: String, val actions: List<ActivityAction>)
data class ChartPoint(
    val x: Float,
    val y: Float,
    val valueLabel: String = "",
    val pointLabel: String = ""
)
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
