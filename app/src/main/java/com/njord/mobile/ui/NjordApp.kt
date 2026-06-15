package com.njord.mobile.ui

import android.content.Context
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.njord.mobile.model.ActivityAction
import com.njord.mobile.model.Destination
import com.njord.mobile.model.HeartbeatRoutine
import com.njord.mobile.model.Incident
import com.njord.mobile.model.LayerScore
import com.njord.mobile.model.LiveAnalyticsSnapshot
import com.njord.mobile.model.LivePosition
import com.njord.mobile.model.LogEntry
import com.njord.mobile.model.LogFilter
import com.njord.mobile.model.MiniKpi
import com.njord.mobile.model.NjordAction
import com.njord.mobile.api.ApiCacheKey
import com.njord.mobile.api.ApiPayloadResult
import com.njord.mobile.api.HeartbeatResult
import com.njord.mobile.api.HomeResult
import com.njord.mobile.api.HunchReportResult
import com.njord.mobile.api.LiveResult
import com.njord.mobile.api.LogsResult
import com.njord.mobile.api.NjordApiCache
import com.njord.mobile.api.NjordApiClient
import com.njord.mobile.api.ActivityResult
import com.njord.mobile.api.PortfolioResult
import com.njord.mobile.api.mapApiActivity
import com.njord.mobile.api.mapApiHeartbeat
import com.njord.mobile.api.mapApiHome
import com.njord.mobile.api.mapApiLive
import com.njord.mobile.api.mapApiPortfolio
import com.njord.mobile.api.mapApiReport
import com.njord.mobile.api.mapApiEntries
import com.njord.mobile.api.parseIncidentsFromJson
import com.njord.mobile.model.ActivitySummary
import com.njord.mobile.model.ChartPoint
import com.njord.mobile.model.HomeSnapshot
import com.njord.mobile.model.HunchReport
import com.njord.mobile.model.NjordMockData
import com.njord.mobile.model.NjordUiState
import com.njord.mobile.model.PortfolioMetric
import com.njord.mobile.model.PortfolioMonthReturn
import com.njord.mobile.model.PortfolioPosition
import com.njord.mobile.model.PortfolioSnapshot
import com.njord.mobile.model.ReportFactor
import com.njord.mobile.model.RiskCheck
import com.njord.mobile.model.SideFilter
import com.njord.mobile.model.StrategyCycle
import com.njord.mobile.model.StrategyFilter
import com.njord.mobile.model.StrategySummary
import com.njord.mobile.model.Tone
import com.njord.mobile.model.reduce
import com.njord.mobile.model.visibleLivePositions
import com.njord.mobile.model.visibleLogs
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

private val Bg = Color(0xFF0C1015)
private val Surface1 = Color(0xFF151A21)
private val Surface2 = Color(0xFF1B2028)
private val Surface3 = Color(0xFF24282F)
private val Primary = Color(0xFF8BD8FF)
private val PrimaryContainer = Color(0xFF123547)
private val TextPrimary = Color(0xFFE7ECF3)
private val TextMuted = Color(0xFFB2BBC7)
private val TextMuted2 = Color(0xFF8792A1)
private val Outline = Color(0xFF2C3441)
private val Success = Color(0xFF00E676)
private val Warning = Color(0xFFFFD166)
private val Danger = Color(0xFFFF2D55)
private val Info = Color(0xFF9CB7FF)
private val ActivityKept = Color(0xFFFF9F43)
private val ActivityOpen = Color(0xFF4DA3FF)
private val ActivityClosed = Color(0xFFB084FF)
private val PortfolioTileSurface = Color(0xFF171C24)
private val LiveFilterSurface = Color(0xFF15171B)
private val LiveStrategyFilterActive = Color(0xFF263846)
private val LiveCardSurface = Color(0xFF151A21)
private val LiveTileSurface = Color(0xFF25292F)
private val LiveErrorSurface = Color(0xFF18161B)

private data class CoinLogoSources(
    val primaryUrl: String,
    val fallbackUrl: String
)

private val CoinLogoSourcesBySymbol = mapOf(
    "ARB" to CoinLogoSources(
        primaryUrl = "https://cryptologos.cc/logos/arbitrum-arb-logo.png",
        fallbackUrl = "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/arbitrum/info/logo.png"
    ),
    "BTC" to CoinLogoSources(
        primaryUrl = "https://cryptologos.cc/logos/bitcoin-btc-logo.png",
        fallbackUrl = "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/bitcoin/info/logo.png"
    ),
    "ETH" to CoinLogoSources(
        primaryUrl = "https://cryptologos.cc/logos/ethereum-eth-logo.png",
        fallbackUrl = "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/info/logo.png"
    ),
    "HYPE" to CoinLogoSources(
        primaryUrl = "https://cryptologos.cc/logos/hyperliquid-hype-logo.png",
        fallbackUrl = "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/hyperliquid/info/logo.png"
    ),
    "SOL" to CoinLogoSources(
        primaryUrl = "https://cryptologos.cc/logos/solana-sol-logo.png",
        fallbackUrl = "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/solana/info/logo.png"
    ),
    "SUI" to CoinLogoSources(
        primaryUrl = "https://cryptologos.cc/logos/sui-sui-logo.png",
        fallbackUrl = "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/sui/info/logo.png"
    )
)

@Composable
fun NjordApp() {
    var state by remember { mutableStateOf(NjordUiState()) }
    val context = LocalContext.current.applicationContext

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            NjordApiCache.read(context.filesDir, ApiCacheKey.Incidents)?.let { json ->
                val seeded = parseIncidentsFromJson(json)
                if (seeded.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        state = reduce(state, NjordAction.IncidentsSeeded(seeded))
                    }
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Bg,
            surface = Surface1,
            primary = Primary,
            onPrimary = Color(0xFF042131),
            onBackground = TextPrimary,
            onSurface = TextPrimary
        )
    ) {
        Surface(Modifier.fillMaxSize(), color = Bg) {
            NjordDashboardScreen(
                state = state,
                onAction = { state = reduce(state, it) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NjordDashboardScreen(state: NjordUiState, onAction: (NjordAction) -> Unit) {
    val listState = rememberLazyListState()
    val context = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()
    LaunchedEffect(state.destination) {
        listState.scrollToItem(0)
    }

    Scaffold(
        containerColor = Bg,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            BottomNavBar(
                selected = state.destination.navGroup,
                onNavigate = { onAction(NjordAction.Navigate(it)) }
            )
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding()
                .testTag("screen-${state.destination.name}"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (
                state.destination != Destination.Home &&
                state.destination != Destination.Portfolio &&
                state.destination != Destination.Live &&
                state.destination != Destination.More &&
                state.destination != Destination.Activity &&
                state.destination != Destination.Heartbeat &&
                state.destination != Destination.Logs &&
                state.destination != Destination.Reports
            ) {
                item {
                    ScreenHeader(state.destination)
                }
            }
            when (state.destination) {
                Destination.Home -> item { HomeScreen(state, onAction) }
                Destination.Portfolio -> item { PortfolioScreen(state, onAction) }
                Destination.Live -> item { LiveScreen(state, onAction) }
                Destination.Risk -> item { RiskScreen() }
                Destination.More -> item { MoreScreen(onAction) }
                Destination.Activity -> item { ActivityScreen(state, onAction) }
                Destination.Heartbeat -> item { HeartbeatScreen(state, onAction) }
                Destination.Logs -> item { LogsScreen(state, onAction) }
                Destination.Reports -> item { ReportsScreen(state, onAction) }
            }
            item { Spacer(Modifier.height(12.dp)) }
        }

        state.selectedIncident?.let { incident ->
            IncidentDialog(
                incident = incident,
                onClose = { onAction(NjordAction.CloseIncident) },
                onDismissIncident = {
                    scope.launch(Dispatchers.IO) {
                        NjordApiCache.read(context.filesDir, ApiCacheKey.Incidents)?.let { json ->
                            val updated = NjordApiClient.deleteFromIncidentJson(json, incident.id)
                            NjordApiCache.write(context.filesDir, ApiCacheKey.Incidents, updated)
                        }
                    }
                    onAction(NjordAction.DismissIncident(incident.id))
                }
            )
        }

        state.selectedPosition?.let { position ->
            ModalBottomSheet(
                onDismissRequest = { onAction(NjordAction.ClosePosition) },
                containerColor = Surface1,
                contentColor = TextPrimary
            ) {
                PositionSheet(position, onClose = { onAction(NjordAction.ClosePosition) })
            }
        }
    }
}

@Composable
private fun ScreenHeader(destination: Destination) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        Text(destination.title, color = TextPrimary, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
        Text(destination.subtitle, color = TextMuted, fontSize = 13.sp)
    }
}

private suspend fun dispatchUiAction(onAction: (NjordAction) -> Unit, action: NjordAction) {
    withContext(Dispatchers.Main) {
        onAction(action)
    }
}

private suspend fun showApiFailureToast(context: Context, error: ApiPayloadResult.Error) {
    if (!com.njord.mobile.BuildConfig.DEBUG) return
    val status = error.statusCode?.toString() ?: "network"
    val msg = error.message.take(160)
    withContext(Dispatchers.Main) {
        Toast.makeText(context, "[$status] ${error.path}\n$msg", Toast.LENGTH_LONG).show()
    }
}

private suspend fun showApiParseFailureToast(context: Context, payload: ApiPayloadResult.Success, message: String) {
    showApiFailureToast(context, ApiPayloadResult.Error(payload.statusCode, payload.path, message))
}

@Composable
private fun HomeScreen(state: NjordUiState, onAction: (NjordAction) -> Unit) {
    val context = LocalContext.current.applicationContext
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            withContext(Dispatchers.IO) {
                NjordApiCache.read(context.filesDir, ApiCacheKey.Home)?.let { cachedBody ->
                    when (val cached = NjordApiClient.parseHomeResponse(cachedBody)) {
                        is HomeResult.Success -> {
                            // Strip incidents from cached snapshot — incidents.json is the authoritative source
                            val snapshot = mapApiHome(cached.response).copy(incidents = emptyList())
                            dispatchUiAction(onAction, NjordAction.HomeLoaded(snapshot))
                        }
                        is HomeResult.Error -> NjordApiCache.delete(context.filesDir, ApiCacheKey.Home)
                        else -> {}
                    }
                }

                when (val result = NjordApiClient.fetchHomePayload(
                    com.njord.mobile.BuildConfig.NJORD_API_BASE_URL,
                    com.njord.mobile.BuildConfig.NJORD_API_KEY
                )) {
                    is ApiPayloadResult.Success -> {
                        when (val parsed = NjordApiClient.parseHomeResponse(result.body)) {
                            is HomeResult.Success -> {
                                NjordApiCache.write(context.filesDir, ApiCacheKey.Home, result.body)
                                val snapshot = mapApiHome(parsed.response)
                                if (snapshot.incidents.isNotEmpty()) {
                                    val incomingJson = NjordApiClient.extractIncidentsJson(result.body)
                                    val existingJson = NjordApiCache.read(context.filesDir, ApiCacheKey.Incidents)
                                    NjordApiCache.write(
                                        context.filesDir,
                                        ApiCacheKey.Incidents,
                                        NjordApiClient.mergeIncidentJson(existingJson, incomingJson)
                                    )
                                }
                                dispatchUiAction(onAction, NjordAction.HomeLoaded(snapshot))
                            }
                            is HomeResult.Error -> showApiParseFailureToast(context, result, parsed.message)
                            else -> {}
                        }
                    }
                    is ApiPayloadResult.Error -> showApiFailureToast(context, result)
                }
            }
        }
    }

    val snapshot = state.homeSnapshot

    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        HomeEquityHero(snapshot) { onAction(NjordAction.Navigate(Destination.Portfolio)) }

        SectionTitle("Strategies")
        val strategies = snapshot?.strategies.orEmpty()
        if (strategies.isEmpty()) {
            NjordCard { Text("No strategy data available yet.", color = TextMuted, fontSize = 13.sp) }
        }
        strategies.forEach {
            HomeStrategyCard(it) { onAction(NjordAction.Navigate(Destination.Live)) }
        }

        SectionTitle("Activity")
        HomeActivityCard(snapshot?.activitySummary) { onAction(NjordAction.Navigate(Destination.Activity)) }

        SectionTitle("Heartbeat")
        HomeHeartbeatCard(snapshot) { onAction(NjordAction.Navigate(Destination.Heartbeat)) }

        if (state.liveIncidents.isNotEmpty()) {
            SectionTitle("Incidents")
            HomeIncidentsCard(
                incidents = state.liveIncidents,
                onClick = { onAction(NjordAction.Navigate(Destination.Live)) }
            )
        }
    }
}

@Composable
private fun PortfolioScreen(state: NjordUiState, onAction: (NjordAction) -> Unit) {
    val context = LocalContext.current.applicationContext
    val strategy = portfolioApiStrategy(state.portfolioStrategyFilter)
    val cacheKey = portfolioCacheKey(state.portfolioStrategyFilter)
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner, strategy) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            withContext(Dispatchers.IO) {
                NjordApiCache.read(context.filesDir, cacheKey)?.let { cachedBody ->
                    when (val cached = NjordApiClient.parsePortfolioResponse(cachedBody)) {
                        is PortfolioResult.Success -> dispatchUiAction(onAction, NjordAction.PortfolioLoaded(mapApiPortfolio(cached.response)))
                        is PortfolioResult.Error -> NjordApiCache.delete(context.filesDir, cacheKey)
                        else -> {}
                    }
                }

                dispatchUiAction(onAction, NjordAction.PortfolioLoading)
                when (val result = NjordApiClient.fetchPortfolioPayload(
                    com.njord.mobile.BuildConfig.NJORD_API_BASE_URL,
                    com.njord.mobile.BuildConfig.NJORD_API_KEY,
                    strategy
                )) {
                    is ApiPayloadResult.Success -> {
                        when (val parsed = NjordApiClient.parsePortfolioResponse(result.body)) {
                            is PortfolioResult.Success -> {
                                NjordApiCache.write(context.filesDir, cacheKey, result.body)
                                dispatchUiAction(onAction, NjordAction.PortfolioLoaded(mapApiPortfolio(parsed.response)))
                            }
                            is PortfolioResult.Error -> showApiParseFailureToast(context, result, parsed.message)
                            else -> {}
                        }
                    }
                    is ApiPayloadResult.Error -> showApiFailureToast(context, result)
                }
            }
        }
    }

    val snapshot = state.portfolioSnapshot ?: fallbackPortfolioSnapshot()

    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PortfolioPerformanceHero(snapshot)
        FilterRow(
            items = StrategyFilter.entries,
            selected = state.portfolioStrategyFilter,
            label = { it.label },
            onSelect = { onAction(NjordAction.SetPortfolioStrategyFilter(it)) }
        )
        SectionTitle("Live metrics")
        PortfolioMetricGrid(snapshot.liveMetrics)
        SectionTitle("Monthly stats")
        PortfolioMonthlyStats(snapshot.monthlyStats)
        SectionTitle("Performance history")
        PortfolioHistoryCard(
            title = "Portfolio P&L over time",
            trailing = "30D trend",
            stats = snapshot.equityStats
        ) {
            ChartCanvas(
                positive = true,
                showFill = true,
                axisLabels = snapshot.equityAxisLabels,
                points = snapshot.equityCurve
            )
        }
        DrawdownHistoryCard(snapshot)
        ReturnByMonthCard(snapshot)
    }
}

private fun portfolioApiStrategy(filter: StrategyFilter): String =
    when (filter) {
        StrategyFilter.All -> "all"
        StrategyFilter.BigBang -> "big_bang"
        StrategyFilter.Wcr -> "wcr"
        StrategyFilter.Hunch -> "hunch"
    }

private fun portfolioCacheKey(filter: StrategyFilter): ApiCacheKey =
    when (filter) {
        StrategyFilter.All -> ApiCacheKey.PortfolioAll
        StrategyFilter.BigBang -> ApiCacheKey.PortfolioBigBang
        StrategyFilter.Wcr -> ApiCacheKey.PortfolioWcr
        StrategyFilter.Hunch -> ApiCacheKey.PortfolioHunch
    }

private fun fallbackPortfolioSnapshot(): PortfolioSnapshot =
    PortfolioSnapshot(
        totalEquity = "$18.4k",
        returnBadge = "ALL +127.4%",
        returnTone = Tone.Info,
        todayPnl = "+$96",
        todayPct = "+0.5%",
        todayTone = Tone.Success,
        sevenDayPnl = "+$812",
        sevenDayPct = "+4.6%",
        sevenDayTone = Tone.Success,
        thirtyDayPnl = "+$2.4k",
        thirtyDayPct = "+14.8%",
        thirtyDayTone = Tone.Success,
        liveMetrics = listOf(
            PortfolioMetric("REALIZED P&L", "+$1.9k", Tone.Success, "Closed positions"),
            PortfolioMetric("UNREALIZED P&L", "+$428", Tone.Success, "Open positions"),
            PortfolioMetric("WIN RATE", "56%", Tone.Muted, "124 closed trades"),
            PortfolioMetric("PROFIT FACTOR", "1.42", Tone.Muted, "Gross profit / loss")
        ),
        monthlyStats = listOf(
            PortfolioMetric("BEST MONTH", "+6.2%", Tone.Success, "April"),
            PortfolioMetric("WORST MONTH", "-1.1%", Tone.Danger, "March"),
            PortfolioMetric("AVERAGE", "+2.8%", Tone.Success, "Last 6 months")
        ),
        equityStats = listOf(
            PortfolioMetric("Current", "$18.4k"),
            PortfolioMetric("High", "$19.1k"),
            PortfolioMetric("30D P&L", "+$2.4k", Tone.Success)
        ),
        equityCurve = NjordMockData.equityCurve,
        equityAxisLabels = listOf("May 10", "May 20", "May 30", "Today"),
        drawdownStats = listOf(
            PortfolioMetric("Current", "-2.1%", Tone.Warning),
            PortfolioMetric("Max", "-6.4%", Tone.Danger),
            PortfolioMetric("Recovery", "63%")
        ),
        drawdownCurve = NjordMockData.drawdownCurve,
        drawdownAxisLabels = listOf("0%", "-3%", "-6%", "Recovery"),
        monthlyReturns = listOf(
            PortfolioMonthReturn("Jan", "+4.1%", 0.66f, Tone.Success),
            PortfolioMonthReturn("Feb", "+2.3%", 0.48f, Tone.Success),
            PortfolioMonthReturn("Mar", "-1.1%", 0.24f, Tone.Danger),
            PortfolioMonthReturn("Apr", "+6.2%", 0.88f, Tone.Success),
            PortfolioMonthReturn("May", "+3.8%", 0.62f, Tone.Success),
            PortfolioMonthReturn("Jun", "+1.4%", 0.38f, Tone.Success)
        )
    )

@Composable
private fun LiveScreen(state: NjordUiState, onAction: (NjordAction) -> Unit) {
    val context = LocalContext.current.applicationContext
    val positions = visibleLivePositions(
        state.livePositions,
        state.liveStrategyFilter,
        state.liveSideFilter
    )
    val incidents = state.liveIncidents
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            withContext(Dispatchers.IO) {
                NjordApiCache.read(context.filesDir, ApiCacheKey.Live)?.let { cachedBody ->
                    when (val cached = NjordApiClient.parseLiveResponse(cachedBody)) {
                        is LiveResult.Success -> {
                            // Strip incidents from cache — incidents.json is the authoritative source
                            val (cachedPositions, cachedAnalytics, _) = mapApiLive(cached.response)
                            dispatchUiAction(onAction, NjordAction.LiveLoaded(cachedPositions, cachedAnalytics, emptyList()))
                        }
                        is LiveResult.Error -> NjordApiCache.delete(context.filesDir, ApiCacheKey.Live)
                        else -> {}
                    }
                }

                dispatchUiAction(onAction, NjordAction.LiveLoading)
                when (val result = NjordApiClient.fetchLivePayload(
                    com.njord.mobile.BuildConfig.NJORD_API_BASE_URL,
                    com.njord.mobile.BuildConfig.NJORD_API_KEY
                )) {
                    is ApiPayloadResult.Success -> {
                        when (val parsed = NjordApiClient.parseLiveResponse(result.body)) {
                            is LiveResult.Success -> {
                                NjordApiCache.write(context.filesDir, ApiCacheKey.Live, result.body)
                                val (livePositions, liveAnalytics, liveIncidents) = mapApiLive(parsed.response)
                                if (liveIncidents.isNotEmpty()) {
                                    val incomingJson = NjordApiClient.extractIncidentsJson(result.body)
                                    val existingJson = NjordApiCache.read(context.filesDir, ApiCacheKey.Incidents)
                                    NjordApiCache.write(
                                        context.filesDir,
                                        ApiCacheKey.Incidents,
                                        NjordApiClient.mergeIncidentJson(existingJson, incomingJson)
                                    )
                                }
                                dispatchUiAction(onAction, NjordAction.LiveLoaded(livePositions, liveAnalytics, liveIncidents))
                            }
                            is LiveResult.Error -> showApiParseFailureToast(context, result, parsed.message)
                            else -> {}
                        }
                    }
                    is ApiPayloadResult.Error -> showApiFailureToast(context, result)
                }
            }
        }
    }

    Column(
        Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LiveIncidentCarousel(incidents) { onAction(NjordAction.SelectIncident(it)) }
        LiveAnalyticsSections(state.liveAnalytics)
        LiveFilterBar(
            strategyFilter = state.liveStrategyFilter,
            sideFilter = state.liveSideFilter,
            onStrategySelect = { onAction(NjordAction.SetLiveStrategyFilter(it)) },
            onSideSelect = { onAction(NjordAction.SetLiveSideFilter(it)) }
        )
        if (positions.isEmpty()) {
            NjordCard(Modifier.testTag("emptyLivePositions")) {
                Text("No open positions match this filter.", color = TextMuted, fontSize = 13.sp)
            }
        } else {
            positions.forEach { position ->
                LivePositionCard(position) { onAction(NjordAction.SelectPosition(position)) }
            }
        }
    }
}

@Composable
private fun RiskScreen() {
    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        HeroCard(
            label = "Risk state",
            title = "Guarded",
            subtitle = "Exposure, stops, and divergence",
            footer = listOf("Margin" to "40%", "Stop gap" to "1.4%", "Orphans" to "0")
        )
        NjordMockData.riskChecks.forEach { RiskRow(it) }
    }
}

@Composable
private fun MoreScreen(onAction: (NjordAction) -> Unit) {
    Column(
        Modifier
            .padding(horizontal = 30.dp)
            .padding(top = 22.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Surface1)
                .border(BorderStroke(1.dp, Outline.copy(alpha = 0.75f)), RoundedCornerShape(24.dp))
        ) {
            MoreRow("Activity", Icons.Outlined.Menu) {
                onAction(NjordAction.Navigate(Destination.Activity))
            }
            HorizontalDivider(color = Outline.copy(alpha = 0.62f))
            MoreRow("Reports", Icons.AutoMirrored.Outlined.Article) {
                onAction(NjordAction.Navigate(Destination.Reports))
            }
            HorizontalDivider(color = Outline.copy(alpha = 0.62f))
            MoreRow("Heartbeat", Icons.AutoMirrored.Outlined.ShowChart) {
                onAction(NjordAction.Navigate(Destination.Heartbeat))
            }
            HorizontalDivider(color = Outline.copy(alpha = 0.62f))
            MoreRow("Logs", Icons.Outlined.Terminal) {
                onAction(NjordAction.Navigate(Destination.Logs))
            }
        }
    }
}

@Composable
private fun ActivityScreen(state: NjordUiState, onAction: (NjordAction) -> Unit) {
    val context = LocalContext.current.applicationContext
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            withContext(Dispatchers.IO) {
                NjordApiCache.read(context.filesDir, ApiCacheKey.Activity)?.let { cachedBody ->
                    when (val cached = NjordApiClient.parseActivityResponse(cachedBody)) {
                        is ActivityResult.Success -> {
                            val (summary, cycles) = mapApiActivity(cached.response)
                            dispatchUiAction(onAction, NjordAction.ActivityLoaded(summary, cycles))
                        }
                        is ActivityResult.Error -> NjordApiCache.delete(context.filesDir, ApiCacheKey.Activity)
                        else -> {}
                    }
                }

                when (val result = NjordApiClient.fetchActivityPayload(
                    com.njord.mobile.BuildConfig.NJORD_API_BASE_URL,
                    com.njord.mobile.BuildConfig.NJORD_API_KEY
                )) {
                    is ApiPayloadResult.Success -> {
                        when (val parsed = NjordApiClient.parseActivityResponse(result.body)) {
                            is ActivityResult.Success -> {
                                NjordApiCache.write(context.filesDir, ApiCacheKey.Activity, result.body)
                                val (summary, cycles) = mapApiActivity(parsed.response)
                                dispatchUiAction(onAction, NjordAction.ActivityLoaded(summary, cycles))
                            }
                            is ActivityResult.Error -> showApiParseFailureToast(context, result, parsed.message)
                            else -> {}
                        }
                    }
                    is ApiPayloadResult.Error -> showApiFailureToast(context, result)
                }
            }
        }
    }

    Column(
        Modifier
            .padding(horizontal = 16.dp)
            .padding(top = 6.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Surface1)
                .border(BorderStroke(1.dp, Outline.copy(alpha = 0.76f)), RoundedCornerShape(24.dp))
                .padding(horizontal = 18.dp, vertical = 18.dp)
                .testTag("activityReferencePanel")
        ) {
            Text(
                "Candle close",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.height(18.dp))
            val summary = state.activitySummary
            if (summary == null) {
                Text("No cycle data available yet.", color = TextMuted, fontSize = 13.sp)
            } else {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ActivitySummaryChip("+${summary.opened}", "Opened")
                    ActivitySummaryChip("-${summary.closed}", "Closed")
                    ActivitySummaryChip("•${summary.kept}", "Kept")
                }
                Spacer(Modifier.height(18.dp))
                HorizontalDivider(color = Outline.copy(alpha = 0.85f))
                if (state.activityCycles.isEmpty()) {
                    Spacer(Modifier.height(18.dp))
                    Text("No cycle data available yet.", color = TextMuted, fontSize = 13.sp)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                        state.activityCycles.forEachIndexed { index, cycle ->
                            ActivityCycleTable(cycle, isLast = index == state.activityCycles.lastIndex)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeartbeatScreen(state: NjordUiState, onAction: (NjordAction) -> Unit) {
    val context = LocalContext.current.applicationContext
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            withContext(Dispatchers.IO) {
                NjordApiCache.read(context.filesDir, ApiCacheKey.Heartbeat)?.let { cachedBody ->
                    when (val cached = NjordApiClient.parseHeartbeatResponse(cachedBody)) {
                        is HeartbeatResult.Success -> {
                            val snapshot = mapApiHeartbeat(cached)
                            dispatchUiAction(
                                onAction,
                                NjordAction.HeartbeatLoaded(
                                    routines = snapshot.routines,
                                    healthyCount = snapshot.healthyCount,
                                    lateCount = snapshot.lateCount,
                                    criticalCount = snapshot.criticalCount,
                                    totalCount = snapshot.totalCount
                                )
                            )
                        }
                        is HeartbeatResult.Error -> NjordApiCache.delete(context.filesDir, ApiCacheKey.Heartbeat)
                        else -> {}
                    }
                }

                when (val result = NjordApiClient.fetchHeartbeatPayload(
                    com.njord.mobile.BuildConfig.NJORD_API_BASE_URL,
                    com.njord.mobile.BuildConfig.NJORD_API_KEY
                )) {
                    is ApiPayloadResult.Success -> {
                        when (val parsed = NjordApiClient.parseHeartbeatResponse(result.body)) {
                            is HeartbeatResult.Success -> {
                                NjordApiCache.write(context.filesDir, ApiCacheKey.Heartbeat, result.body)
                                val snapshot = mapApiHeartbeat(parsed)
                                dispatchUiAction(
                                    onAction,
                                    NjordAction.HeartbeatLoaded(
                                        routines = snapshot.routines,
                                        healthyCount = snapshot.healthyCount,
                                        lateCount = snapshot.lateCount,
                                        criticalCount = snapshot.criticalCount,
                                        totalCount = snapshot.totalCount
                                    )
                                )
                            }
                            is HeartbeatResult.Error -> showApiParseFailureToast(context, result, parsed.message)
                            else -> {}
                        }
                    }
                    is ApiPayloadResult.Error -> showApiFailureToast(context, result)
                }
            }
        }
    }

    Column(
        Modifier
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        HeartbeatHealthCard(
            healthyCount = state.heartbeatHealthyCount,
            lateCount = state.heartbeatLateCount,
            criticalCount = state.heartbeatCriticalCount,
            totalCount = state.heartbeatTotalCount
        )
        Text(
            "Service routines",
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(top = 18.dp, bottom = 4.dp, start = 4.dp)
        )
        if (state.heartbeatRoutines.isEmpty()) {
            NjordCard { Text("No service routines available yet.", color = TextMuted, fontSize = 13.sp) }
        } else {
            state.heartbeatRoutines.forEach { HeartbeatRow(it) }
        }
    }
}

@Composable
private fun LogsScreen(state: NjordUiState, onAction: (NjordAction) -> Unit) {
    val context = LocalContext.current.applicationContext
    val logs = visibleLogs(state.logs, state.logFilter, state.logQuery, state.logStrategyFilter)
    var expandedLogIndex by remember { mutableStateOf<Int?>(null) }
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(state.logs, state.logFilter, state.logQuery, state.logStrategyFilter) {
        expandedLogIndex = null
    }

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            withContext(Dispatchers.IO) {
                NjordApiCache.read(context.filesDir, ApiCacheKey.Logs)?.let { cachedBody ->
                    when (val cached = NjordApiClient.parseLogsResponse(cachedBody)) {
                        is LogsResult.Success -> dispatchUiAction(onAction, NjordAction.LogsLoaded(mapApiEntries(cached.entries)))
                        is LogsResult.Error -> NjordApiCache.delete(context.filesDir, ApiCacheKey.Logs)
                        else -> {}
                    }
                }

                dispatchUiAction(onAction, NjordAction.LogsLoading)
                when (val result = NjordApiClient.fetchLogsPayload(
                    com.njord.mobile.BuildConfig.NJORD_API_BASE_URL,
                    com.njord.mobile.BuildConfig.NJORD_API_KEY
                )) {
                    is ApiPayloadResult.Success -> {
                        when (val parsed = NjordApiClient.parseLogsResponse(result.body)) {
                            is LogsResult.Success -> {
                                NjordApiCache.write(context.filesDir, ApiCacheKey.Logs, result.body)
                                dispatchUiAction(onAction, NjordAction.LogsLoaded(mapApiEntries(parsed.entries)))
                            }
                            is LogsResult.Error -> showApiParseFailureToast(context, result, parsed.message)
                            else -> {}
                        }
                    }
                    is ApiPayloadResult.Error -> showApiFailureToast(context, result)
                }
            }
        }
    }

    Column(
        Modifier
            .padding(horizontal = 16.dp)
            .padding(top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextField(
            value = state.logQuery,
            onValueChange = { onAction(NjordAction.SetLogQuery(it)) },
            modifier = Modifier.fillMaxWidth().testTag("logSearch"),
            leadingIcon = { Icon(Icons.Outlined.Search, null, tint = TextMuted) },
            placeholder = { Text("Search symbol, strategy, message") },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Surface1,
                unfocusedContainerColor = Surface1,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(18.dp)
        )
        LogsFilterBar(
            strategyFilter = state.logStrategyFilter,
            logFilter = state.logFilter,
            onStrategySelect = { onAction(NjordAction.SetLogStrategyFilter(it)) },
            onLogFilterSelect = { onAction(NjordAction.SetLogFilter(it)) }
        )
        if (logs.isEmpty()) {
            NjordCard(Modifier.testTag("emptyLogs")) {
                Text("No logs match this filter.", color = TextMuted)
            }
        } else {
            logs.forEachIndexed { index, log ->
                LogRow(
                    log = log,
                    expanded = expandedLogIndex == index,
                    onClick = {
                        expandedLogIndex = if (expandedLogIndex == index) null else index
                    }
                )
            }
        }
    }
}

@Composable
private fun ReportsScreen(state: NjordUiState, onAction: (NjordAction) -> Unit) {
    val context = LocalContext.current.applicationContext
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            withContext(Dispatchers.IO) {
                NjordApiCache.read(context.filesDir, ApiCacheKey.HunchReport)?.let { cachedBody ->
                    when (val cached = NjordApiClient.parseHunchReportResponse(cachedBody)) {
                        is HunchReportResult.Success -> dispatchUiAction(onAction, NjordAction.HunchReportLoaded(mapApiReport(cached.report)))
                        is HunchReportResult.Error -> NjordApiCache.delete(context.filesDir, ApiCacheKey.HunchReport)
                        else -> {}
                    }
                }

                dispatchUiAction(onAction, NjordAction.HunchReportLoading)
                when (val result = NjordApiClient.fetchHunchReportPayload(
                    com.njord.mobile.BuildConfig.NJORD_API_BASE_URL,
                    com.njord.mobile.BuildConfig.NJORD_API_KEY
                )) {
                    is ApiPayloadResult.Success -> {
                        when (val parsed = NjordApiClient.parseHunchReportResponse(result.body)) {
                            is HunchReportResult.Success -> {
                                NjordApiCache.write(context.filesDir, ApiCacheKey.HunchReport, result.body)
                                dispatchUiAction(onAction, NjordAction.HunchReportLoaded(mapApiReport(parsed.report)))
                            }
                            is HunchReportResult.Error -> showApiParseFailureToast(context, result, parsed.message)
                            else -> {}
                        }
                    }
                    is ApiPayloadResult.Error -> showApiFailureToast(context, result)
                }
            }
        }
    }

    val report = state.hunchReport

    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (report == null) {
            NjordCard { Text("No Hunch report available yet.", color = TextMuted) }
        } else {
            ReportReferencePanel(report)
            NjordCard {
                Text("Key factors", color = TextPrimary, fontWeight = FontWeight.ExtraBold)
                if (report.keyFactors.isEmpty()) {
                    Text("No key factors reported.", color = TextMuted, fontSize = 13.sp, modifier = Modifier.padding(top = 10.dp))
                } else {
                    report.keyFactors.forEach { ReportFactorRow(it) }
                }
            }
            NjordCard {
                Text("Risks", color = TextPrimary, fontWeight = FontWeight.ExtraBold)
                if (report.risks.isEmpty()) {
                    Text("No risks reported.", color = TextMuted, fontSize = 13.sp, modifier = Modifier.padding(top = 10.dp))
                } else {
                    report.risks.forEach { ReportFactorRow(it) }
                }
            }
            NjordCard {
                Text("Layer scores", color = TextPrimary, fontWeight = FontWeight.ExtraBold)
                if (report.layerScores.isEmpty()) {
                    Text("No layer scores reported.", color = TextMuted, fontSize = 13.sp, modifier = Modifier.padding(top = 10.dp))
                } else {
                    report.layerScores.forEach { LayerScoreRow(it) }
                }
            }
        }
    }
}

@Composable
private fun ReportReferencePanel(report: HunchReport) {
    val panelShape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(panelShape)
            .background(Surface1)
            .border(BorderStroke(1.dp, Outline.copy(alpha = 0.72f)), panelShape)
            .testTag("reportReferencePanel")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF29284D),
                            Color(0xFF202A3B),
                            Color(0xFF1A2530)
                        )
                    )
                )
                .padding(horizontal = 15.dp, vertical = 20.dp)
        ) {
            Text(
                report.title,
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(18.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text("Latest report", color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                    Text(report.persistedAge, color = TextMuted, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
            Spacer(Modifier.height(28.dp))
            ReportSignalBanner(report)
            Spacer(Modifier.height(14.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Surface1)
                .padding(horizontal = 15.dp, vertical = 28.dp)
        ) {
            Text(
                "SUMMARY",
                color = TextMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(20.dp))
            ReportSummaryRow("Last Signal", report.signal, report.signalTone)
            ReportSummaryRow("Last Confidence", report.confidence, Tone.Muted)
            ReportSummaryRow("Last Score", report.score, Tone.Muted)
            ReportSummaryRow("Last Signal Date", report.date, Tone.Muted)
            ReportSummaryRow("Last BTC Price", report.btcPriceAtSignal, Tone.Muted)
            ReportSummaryRow("Current BTC Price", report.currentBtcPrice, Tone.Muted)
            ReportSummaryRow("Price Delta", report.priceDelta, report.priceDeltaTone)
            ReportSummaryRow("Last Signal Correct", report.wasSignalCorrect, report.wasSignalCorrectTone, showDivider = false)
        }
    }
}

@Composable
private fun ReportSignalBanner(report: HunchReport) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(toneColor(report.signalTone))
            .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(18.dp))
            .padding(horizontal = 18.dp, vertical = 25.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            report.signal,
            color = Color.White,
            fontSize = 36.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(10.dp))
        Text(
            "Confidence: ${report.confidence} | Score: ${report.score}",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ReportSummaryRow(label: String, value: String, tone: Tone, showDivider: Boolean = true) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 13.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = TextMuted, fontSize = 15.sp)
            Text(value, color = toneColor(tone), fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
        }
        if (showDivider) {
            HorizontalDivider(color = Outline.copy(alpha = 0.72f))
        }
    }
}

@Composable
private fun BottomNavBar(selected: Destination, onNavigate: (Destination) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF11161D))
            .navigationBarsPadding()
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        NavItem("Home", Icons.Outlined.Home, selected == Destination.Home) { onNavigate(Destination.Home) }
        NavItem("Portfolio", Icons.Outlined.AccountBalanceWallet, selected == Destination.Portfolio) { onNavigate(Destination.Portfolio) }
        NavItem("Live", Icons.Outlined.Sensors, selected == Destination.Live) { onNavigate(Destination.Live) }
        NavItem("More", Icons.Outlined.MoreHoriz, selected == Destination.More) { onNavigate(Destination.More) }
    }
}

@Composable
private fun NavItem(label: String, icon: ImageVector, active: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 5.dp)
            .testTag("nav-$label"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(if (active) Color(0xFF14506A) else Color.Transparent)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Icon(icon, null, tint = if (active) Primary else TextMuted2, modifier = Modifier.size(21.dp))
        }
        Spacer(Modifier.height(3.dp))
        Text(label, color = if (active) Primary else TextMuted2, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun HeroCard(label: String, title: String, subtitle: String, footer: List<Pair<String, String>>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF16394A), Color(0xFF151A22), Color(0xFF11161D))))
            .padding(20.dp)
    ) {
        Text(label.uppercase(), color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
        Text(title, color = if (title.startsWith("+")) Success else TextPrimary, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold)
        Text(subtitle, color = TextMuted, fontSize = 13.sp)
        HorizontalDivider(Modifier.padding(vertical = 14.dp), color = Color.White.copy(alpha = 0.1f))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            footer.forEach { (k, v) ->
                Column {
                    Text(k.uppercase(), color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(v, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
private fun HomeEquityHero(snapshot: HomeSnapshot?, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF173747), Color(0xFF151A22))))
            .clickable(onClick = onClick)
            .padding(22.dp)
            .testTag("homeEquityHero")
    ) {
        Text(
            snapshot?.totalBalance ?: "Unavailable",
            color = TextPrimary,
            fontSize = 38.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text("Total Balance", color = TextMuted, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            val pnl = snapshot?.unrealizedPnl ?: "--"
            Text(
                pnl,
                color = if (pnl.startsWith("-")) Danger else Success,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.width(8.dp))
            Text(snapshot?.unrealizedPnlPct ?: "unrealized", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        HorizontalDivider(Modifier.padding(vertical = 18.dp), color = Color.White.copy(alpha = 0.1f))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            HomeHeroKpi("AVAILABLE", snapshot?.availableMargin ?: "--")
            HomeHeroKpi("IN USE", snapshot?.inUse ?: "--")
            HomeHeroKpi("MARGIN", snapshot?.marginInUse ?: "--")
            HomeHeroKpi("OPEN", snapshot?.openPositionCount ?: "--")
        }
    }
}

@Composable
private fun HomeHeroKpi(label: String, value: String) {
    Column {
        Text(label, color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(5.dp))
        Text(value, color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun PortfolioPerformanceHero(snapshot: PortfolioSnapshot) {
    val shape = RoundedCornerShape(28.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.linearGradient(listOf(Color(0xFF12303D), Color(0xFF121820))))
            .border(1.dp, Primary.copy(alpha = 0.22f), shape)
            .padding(22.dp)
            .testTag("portfolioPerformanceHero")
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text("PORTFOLIO PERFORMANCE", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(7.dp))
                Text(snapshot.totalEquity, color = Success, fontSize = 31.sp, fontWeight = FontWeight.ExtraBold)
                Text("Total equity", color = TextMuted, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(toneColor(snapshot.returnTone).copy(alpha = 0.22f))
                    .padding(horizontal = 11.dp, vertical = 6.dp)
            ) {
                Text(snapshot.returnBadge, color = Color(0xFFBFD0FF), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
        Spacer(Modifier.height(28.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PortfolioStatTile("TODAY", snapshot.todayPnl, snapshot.todayTone, snapshot.todayPct, Modifier.weight(1f))
            PortfolioStatTile("7D", snapshot.sevenDayPnl, snapshot.sevenDayTone, snapshot.sevenDayPct, Modifier.weight(1f))
            PortfolioStatTile("30D", snapshot.thirtyDayPnl, snapshot.thirtyDayTone, snapshot.thirtyDayPct, Modifier.weight(1f))
        }
    }
}

@Composable
private fun PortfolioMetricGrid(metrics: List<PortfolioMetric>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        metrics.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { metric ->
                    PortfolioStatTile(metric.label, metric.value, metric.tone, metric.subtext, Modifier.weight(1f))
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun PortfolioMonthlyStats(stats: List<PortfolioMetric>) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        stats.forEach { stat ->
            PortfolioStatTile(stat.label, stat.value, stat.tone, stat.subtext, Modifier.weight(1f))
        }
    }
}

@Composable
private fun PortfolioHistoryCard(
    title: String,
    trailing: String,
    stats: List<PortfolioMetric>,
    content: @Composable () -> Unit
) {
    NjordCard {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = TextPrimary, fontWeight = FontWeight.ExtraBold)
            Text(trailing, color = TextMuted, fontSize = 12.sp)
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            stats.forEach { stat ->
                PortfolioStatTile(stat.label, stat.value, stat.tone, stat.subtext, Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(14.dp))
        content()
    }
}

@Composable
private fun PortfolioStatTile(
    label: String,
    value: String,
    tone: Tone = Tone.Muted,
    subtext: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(PortfolioTileSurface)
            .border(1.dp, Color.White.copy(alpha = 0.065f), RoundedCornerShape(18.dp))
            .padding(horizontal = 10.dp, vertical = 12.dp)
    ) {
        Text(label.uppercase(), color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, maxLines = 2)
        Spacer(Modifier.height(5.dp))
        Text(value, color = toneColor(tone), fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
        subtext?.let {
            Spacer(Modifier.height(2.dp))
            Text(it, color = TextMuted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun DrawdownHistoryCard(snapshot: PortfolioSnapshot) {
    PortfolioHistoryCard(
        title = "Drawdown",
        trailing = "Risk depth",
        stats = snapshot.drawdownStats
    ) {
        ChartCanvas(
            positive = false,
            showFill = true,
            axisLabels = snapshot.drawdownAxisLabels,
            points = snapshot.drawdownCurve
        )
    }
}

@Composable
private fun ReturnByMonthCard(snapshot: PortfolioSnapshot) {
    PortfolioHistoryCard(
        title = "Return by month",
        trailing = "6-month view",
        stats = snapshot.monthlyStats
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            snapshot.monthlyReturns.forEach { month ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(month.month, color = TextMuted, fontSize = 13.sp, modifier = Modifier.width(34.dp))
                    Box(
                        Modifier
                            .weight(1f)
                            .height(10.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color.White.copy(alpha = 0.11f))
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth(month.progress.coerceIn(0f, 1f))
                                .height(10.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(toneColor(month.tone))
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(month.value, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.End, modifier = Modifier.width(54.dp))
                }
            }
        }
    }
}

@Composable
private fun KpiGrid(items: List<MiniKpi>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { item ->
                    NjordCard(Modifier.weight(1f)) {
                        Text(item.label, color = TextMuted, fontSize = 12.sp)
                        Text(item.value, color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                        Text(item.subtext, color = toneColor(item.tone), fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeStrategyCard(summary: StrategySummary, onClick: () -> Unit) {
    NjordCard(Modifier.testTag("homeStrategy-${summary.name}"), onClick = onClick) {
        val pnlTone = if (summary.pnl.startsWith("-")) Danger else Success
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(summary.name, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                summary.assets?.let { assets ->
                    Spacer(Modifier.height(5.dp))
                    Text(assets, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Spacer(Modifier.height(7.dp))
                Text(summary.subtitle, color = TextMuted, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Column(horizontalAlignment = Alignment.End) {
                SmallLiveDot()
                if (summary.pnl.isNotBlank() || summary.pct.isNotBlank()) {
                    Spacer(Modifier.height(28.dp))
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.End) {
                        Text(summary.pnl, color = pnlTone, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                        if (summary.pct.isNotBlank()) {
                            Spacer(Modifier.width(7.dp))
                            Text(summary.pct, color = pnlTone, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StrategyCard(summary: StrategySummary, onClick: () -> Unit) {
    NjordCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CoinIcon(summary.name.take(2), if (summary.live) Tone.Success else Tone.Danger)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(summary.name, color = TextPrimary, fontWeight = FontWeight.ExtraBold)
                Text(summary.subtitle, color = TextMuted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(summary.pnl, color = if (summary.pnl.startsWith("-")) Danger else Success, fontWeight = FontWeight.ExtraBold)
                Text(summary.pct, color = if (summary.pct.startsWith("-")) Danger else Success, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun HomeActivityCard(summary: ActivitySummary?, onClick: () -> Unit) {
    NjordCard(Modifier.testTag("homeActivityCard"), onClick = onClick) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Candle close", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        if (summary == null) {
            Text("No candle-close cycle available yet.", color = TextMuted, fontSize = 13.sp)
        } else {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HomeCyclePill("OPENED", summary.opened, Modifier.weight(1f))
                HomeCyclePill("CLOSED", summary.closed, Modifier.weight(1f))
                HomeCyclePill("KEPT", summary.kept, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun HomeHeartbeatCard(snapshot: HomeSnapshot?, onClick: () -> Unit) {
    val healthy = snapshot?.heartbeatHealthy ?: 0
    val total = snapshot?.heartbeatTotal ?: 0
    val lateCount = snapshot?.heartbeatLateCount ?: 0
    val subtitle = when {
        total == 0 -> "No service health available"
        lateCount == 0 -> "All monitored routines healthy"
        lateCount == 1 -> "1 routine late"
        else -> "$lateCount routines late"
    }
    val badge = when {
        lateCount == 0 -> "OK"
        lateCount == 1 -> "1 late"
        else -> "$lateCount late"
    }
    val badgeTone = if (lateCount == 0 && total > 0) Tone.Success else Tone.Warning
    HomeGradientCard(
        modifier = Modifier.testTag("homeHeartbeatCard"),
        gradient = Brush.linearGradient(
            colors = listOf(
                Color(0xFF171820),
                Color(0xFF1E1B1E),
                Color(0xFF2B2418)
            )
        ),
        borderColor = Warning.copy(alpha = 0.36f),
        onClick = onClick
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Current health", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(3.dp))
                Text(subtitle, color = TextMuted, fontSize = 13.sp)
            }
            Text("$healthy/$total", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.width(8.dp))
            Badge(badge, badgeTone)
        }
    }
}

@Composable
private fun HomeIncidentsCard(incidents: List<Incident>, onClick: () -> Unit) {
    HomeGradientCard(
        modifier = Modifier.testTag("homeIncidentsCard"),
        gradient = Brush.linearGradient(
            colors = listOf(
                Color(0xFF191B22),
                Color(0xFF241D27),
                Color(0xFF30202B)
            )
        ),
        borderColor = Danger.copy(alpha = 0.36f),
        onClick = onClick
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("${incidents.size} active", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
            Badge("View live", Tone.Danger)
        }
        Spacer(Modifier.height(12.dp))
        if (incidents.isEmpty()) {
            Text("No active incidents reported.", color = TextMuted, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        } else {
            incidents.take(3).forEach { incident ->
                HomeIncidentLine(incident.title, incident.age)
            }
        }
    }
}

@Composable
private fun HomeGradientCard(
    modifier: Modifier = Modifier,
    gradient: Brush,
    borderColor: Color,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(gradient)
            .border(1.dp, borderColor, shape)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        content()
    }
}

@Composable
private fun HomeCyclePill(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.055f))
            .padding(horizontal = 10.dp, vertical = 11.dp)
    ) {
        Text(label, color = TextMuted2, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
        Text(value, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun HomeIncidentLine(label: String, time: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(Danger))
        Spacer(Modifier.width(10.dp))
        Text(label, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
        Text(time, color = TextMuted2, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.End)
    }
}

@Composable
private fun SmallLiveDot() {
    val pulse by rememberInfiniteTransition(label = "liveDotPulse").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200),
            repeatMode = RepeatMode.Restart
        ),
        label = "liveDotPulseProgress"
    )
    val pulseSize = 14.dp + (8.dp * pulse)
    val pulseAlpha = 0.24f * (1f - pulse)

    Box(
        Modifier
            .size(22.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .size(pulseSize)
                .clip(CircleShape)
                .background(Success.copy(alpha = pulseAlpha))
        )
        Box(
            Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(Success.copy(alpha = 0.22f)),
            contentAlignment = Alignment.Center
        ) {
            Box(Modifier.size(7.dp).clip(CircleShape).background(Success))
        }
    }
}

@Composable
private fun PortfolioPositionCard(position: PortfolioPosition) {
    NjordCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CoinIcon(position.symbol, position.tone)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(position.symbol, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.width(8.dp))
                    Badge(position.side, if (position.side == "Short") Tone.Danger else Tone.Success)
                }
                Text(position.subtitle, color = TextMuted, fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(position.pnl, color = toneColor(position.tone), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                Text(position.pct, color = toneColor(position.tone), fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PriceChip("Entry", position.entry, Modifier.weight(1f))
            PriceChip("Current", position.current, Modifier.weight(1f), position.tone)
        }
    }
}

@Composable
private fun LiveFilterBar(
    strategyFilter: StrategyFilter,
    sideFilter: SideFilter,
    onStrategySelect: (StrategyFilter) -> Unit,
    onSideSelect: (SideFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(LiveFilterSurface)
            .border(1.dp, Outline.copy(alpha = 0.58f), RoundedCornerShape(24.dp))
            .padding(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            StrategyFilter.entries.forEach { filter ->
                LiveFilterPill(
                    label = filter.label,
                    active = filter == strategyFilter,
                    activeColor = LiveStrategyFilterActive,
                    activeTextColor = TextPrimary,
                    modifier = Modifier.testTag("filter-${filter.label}"),
                    onClick = { onStrategySelect(filter) }
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SideFilter.entries.forEach { filter ->
                LiveFilterPill(
                    label = filter.label,
                    active = filter == sideFilter,
                    activeColor = Primary,
                    activeTextColor = Color(0xFF052433),
                    modifier = Modifier.testTag("filter-${filter.label}"),
                    onClick = { onSideSelect(filter) }
                )
            }
        }
    }
}

@Composable
private fun LogsFilterBar(
    strategyFilter: StrategyFilter,
    logFilter: LogFilter,
    onStrategySelect: (StrategyFilter) -> Unit,
    onLogFilterSelect: (LogFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(LiveFilterSurface)
            .border(1.dp, Outline.copy(alpha = 0.58f), RoundedCornerShape(24.dp))
            .padding(6.dp)
            .testTag("logsFilterBar"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            StrategyFilter.entries.forEach { filter ->
                LiveFilterPill(
                    label = filter.label,
                    active = filter == strategyFilter,
                    activeColor = LiveStrategyFilterActive,
                    activeTextColor = TextPrimary,
                    modifier = Modifier.testTag("filter-${filter.label}"),
                    onClick = { onStrategySelect(filter) }
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf(LogFilter.All, LogFilter.Warn, LogFilter.Error).forEach { filter ->
                LiveFilterPill(
                    label = filter.label,
                    active = filter == logFilter,
                    activeColor = Primary,
                    activeTextColor = Color(0xFF052433),
                    modifier = Modifier.testTag("filter-${filter.label}"),
                    onClick = { onLogFilterSelect(filter) }
                )
            }
        }
    }
}

@Composable
private fun LiveFilterPill(
    label: String,
    active: Boolean,
    activeColor: Color,
    activeTextColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (active) activeColor else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (active) activeTextColor else TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1
        )
    }
}

@Composable
private fun LivePositionCard(position: LivePosition, onClick: () -> Unit) {
    val tone = if (position.pnl.startsWith("-")) Tone.Danger else Tone.Success
    val side = position.side.lowercase().replaceFirstChar { it.titlecase() }
    LiveCard(Modifier.testTag("livePosition-${position.symbol}"), onClick = onClick) {
        Row(verticalAlignment = Alignment.Top) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CoinIcon(position.symbol, tone)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    position.symbol,
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${position.strategyName} · ${position.opened} open",
                    color = TextMuted,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Badge(side, if (side == "Short") Tone.Danger else Tone.Success)
                    Spacer(Modifier.width(8.dp))
                    Text(position.pnl, color = toneColor(tone), fontSize = 23.sp, fontWeight = FontWeight.ExtraBold)
                }
                Text(position.pct, color = toneColor(tone), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun LiveCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(LiveCardSurface)
            .border(1.dp, Outline.copy(alpha = 0.48f), shape)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        content()
    }
}

@Composable
private fun LiveValueTile(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(LiveTileSurface)
            .border(1.dp, Color.White.copy(alpha = 0.055f), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(label, color = TextMuted2, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(4.dp))
        Text(value, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
    }
}

@Composable
private fun LiveFooterValue(label: String, value: String, modifier: Modifier = Modifier, tone: Tone = Tone.Muted) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = TextMuted2, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.width(8.dp))
        Text(
            value,
            color = toneColor(tone),
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun LiveAnalyticsSections(analytics: LiveAnalyticsSnapshot?) {
    if (analytics == null) return

    SectionTitle("Open P&L by strategy")
    NjordCard {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Text("Current contribution", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
            Text(
                analytics.totalContribution,
                color = toneColor(analytics.totalContributionTone),
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Spacer(Modifier.height(16.dp))
        if (analytics.strategyContributions.isEmpty()) {
            Text("No strategy contribution data available yet.", color = TextMuted, fontSize = 13.sp)
        } else {
            analytics.strategyContributions.forEach {
                LiveContributionRow(it.strategy, it.progress, it.value, it.tone)
            }
        }
    }

    SectionTitle("Live summary")
    LiveMetricPanel(items = analytics.summaryItems)

    SectionTitle("Live metrics")
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        analytics.largestWinner?.let {
            LiveOutcomeTile("LARGEST WINNER", it.symbol, it.amount, it.percent, it.tone, Modifier.weight(1f))
        } ?: LiveOutcomeTile("LARGEST WINNER", "N/A", "--", "--", Tone.Muted, Modifier.weight(1f))
        analytics.largestLoser?.let {
            LiveOutcomeTile("LARGEST LOSER", it.symbol, it.amount, it.percent, it.tone, Modifier.weight(1f))
        } ?: LiveOutcomeTile("LARGEST LOSER", "N/A", "--", "--", Tone.Muted, Modifier.weight(1f))
    }
}

@Composable
private fun LiveMetricPanel(items: List<MiniKpi>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Surface1)
            .border(1.dp, Outline.copy(alpha = 0.65f), RoundedCornerShape(22.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        listOf(items.take(2), items.drop(2)).filter { it.isNotEmpty() }.forEachIndexed { rowIndex, rowItems ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowItems.forEach { item ->
                    LiveMetricTile(item.label, item.value, item.subtext, item.tone, Modifier.weight(1f), compact = rowIndex > 0)
                }
            }
        }
    }
}

@Composable
private fun LiveContributionRow(strategy: String, progress: Float, value: String, tone: Tone) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(strategy, color = TextMuted, fontSize = 12.sp, modifier = Modifier.width(76.dp))
        Box(
            Modifier
                .weight(1f)
                .height(10.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Color.White.copy(alpha = 0.10f))
        ) {
            Box(
                Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .height(10.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(toneColor(tone))
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(value, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.End, modifier = Modifier.width(58.dp))
    }
}

@Composable
private fun LiveMetricTile(label: String, value: String, subtext: String, tone: Tone, modifier: Modifier = Modifier, compact: Boolean = false) {
    val isPositive = value.startsWith("+")
    val valueColor = when {
        isPositive -> Success
        value.startsWith("-") -> Danger
        else -> toneColor(tone)
    }
    Column(
        modifier
            .fillMaxWidth()
            .heightIn(min = 96.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF23303A),
                        Color(0xFF1B252E),
                        Color(0xFF171D25)
                    )
                )
            )
            .border(1.dp, Primary.copy(alpha = 0.30f), RoundedCornerShape(18.dp))
            .padding(horizontal = 12.dp, vertical = 13.dp)
    ) {
        Text(label, color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.2.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(9.dp))
        Text(value, color = valueColor, fontSize = if (compact) 18.sp else 24.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(4.dp))
        Text(subtext, color = TextMuted, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun LiveOutcomeTile(
    label: String,
    symbol: String,
    amount: String,
    percent: String,
    tone: Tone,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .fillMaxWidth()
            .heightIn(min = 78.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(LiveCardSurface)
            .border(1.dp, Outline.copy(alpha = 0.44f), RoundedCornerShape(18.dp))
            .padding(horizontal = 12.dp, vertical = 11.dp)
    ) {
        Text(label, color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(6.dp))
        Text(symbol, color = toneColor(tone), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(amount, color = toneColor(tone), fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
            Text(" · $percent", color = TextMuted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun RiskRow(check: RiskCheck) {
    NjordCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CoinIcon(check.badge.take(2), check.tone)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(check.title, color = TextPrimary, fontWeight = FontWeight.Bold)
                Text(check.subtitle, color = TextMuted, fontSize = 12.sp)
            }
            Badge(check.badge, check.tone)
        }
    }
}

@Composable
private fun HeartbeatHealthCard(
    healthyCount: Int,
    lateCount: Int,
    criticalCount: Int,
    totalCount: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Surface1)
            .border(1.dp, Outline.copy(alpha = 0.78f), RoundedCornerShape(24.dp))
            .padding(horizontal = 22.dp, vertical = 22.dp)
            .testTag("heartbeatHealthCard")
    ) {
        Text(
            "HEARTBEAT HEALTH",
            color = TextMuted2,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.4.sp
        )
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "$healthyCount / $totalCount",
                color = TextPrimary,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HeartbeatStatusPill("$healthyCount OK", Tone.Success)
                HeartbeatStatusPill("$lateCount late", Tone.Warning)
                HeartbeatStatusPill("$criticalCount critical", Tone.Danger)
            }
        }
    }
}

@Composable
private fun HeartbeatStatusPill(label: String, tone: Tone) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Surface3.copy(alpha = 0.58f))
            .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(999.dp))
            .padding(horizontal = 16.dp, vertical = 9.dp)
    ) {
        Text(label, color = toneColor(tone), fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun HeartbeatRow(routine: HeartbeatRoutine) {
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 112.dp)
            .clip(shape)
            .background(Surface1)
            .border(1.dp, Outline.copy(alpha = 0.78f), shape)
            .padding(horizontal = 28.dp, vertical = 20.dp)
            .testTag("heartbeatRoutine-${routine.name}")
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    routine.name,
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    routine.age,
                    color = TextMuted,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            HeartbeatRoutinePill(routine.status, routine.tone)
        }
    }
}

@Composable
private fun HeartbeatRoutinePill(label: String, tone: Tone) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(toneColor(tone).copy(alpha = 0.14f))
            .padding(horizontal = 18.dp, vertical = 10.dp)
    ) {
        Text(label, color = toneColor(tone), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun LogRow(log: LogEntry, expanded: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Surface1),
        border = BorderStroke(1.dp, Outline.copy(alpha = 0.65f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .testTag("logRow")
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Badge(log.level.label, log.level.toTone())
                Spacer(Modifier.width(10.dp))
                Text(
                    log.title,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = if (expanded) 2 else 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.width(10.dp))
                Text(log.time, color = TextMuted2, fontSize = 12.sp)
            }
            if (expanded) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Surface2)
                        .border(1.dp, Outline.copy(alpha = 0.75f), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                        .testTag("expandedLogMessage")
                ) {
                    Text(
                        log.message.wrapLongTokens(),
                        color = TextPrimary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        overflow = TextOverflow.Visible
                    )
                }
            } else {
                Text(
                    log.message,
                    color = TextMuted,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun String.wrapLongTokens(chunkSize: Int = 48): String =
    splitToSequence(' ').joinToString(" ") { token ->
        if (token.length <= chunkSize) {
            token
        } else {
            token.chunked(chunkSize).joinToString("\n")
        }
    }

@Composable
private fun MoreRow(title: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 58.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 48.dp)
            .testTag("more-$title"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = TextMuted, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(28.dp))
        Text(
            title,
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.weight(1f)
        )
        Text(">", color = TextMuted2, fontSize = 26.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ActivitySummaryChip(value: String, label: String) {
    val color = activityActionColor(label)

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.16f))
            .border(1.dp, color.copy(alpha = 0.36f), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(value, color = color, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.width(6.dp))
        Text(label, color = color, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun ActivityCycleTable(cycle: StrategyCycle, isLast: Boolean) {
    val opened = cycle.actions.count { it.label == "Opened" }
    val closed = cycle.actions.count { it.label == "Closed" }
    val kept = cycle.actions.count { it.label == "Kept" }
    val counts = buildList {
        if (opened > 0) add("+$opened")
        if (closed > 0) add("-$closed")
        if (kept > 0) add("•$kept")
    }.joinToString(" / ")

    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp, bottom = 13.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(cycle.strategy, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            Text(counts, color = TextMuted2, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF1A1D23))
                .border(1.dp, Color(0xFF343941), RoundedCornerShape(18.dp))
        ) {
            ActivityTableHeader()
            cycle.actions.forEachIndexed { index, action ->
                HorizontalDivider(color = Outline.copy(alpha = 0.72f))
                ActivityTableRow(action, showBottomPadding = index == cycle.actions.lastIndex)
            }
        }
        if (!isLast) {
            Spacer(Modifier.height(18.dp))
            HorizontalDivider(color = Outline.copy(alpha = 0.85f))
        }
    }
}

@Composable
private fun ActivityTableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 26.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("ACTION", color = TextMuted2, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.2.sp, modifier = Modifier.weight(1.08f))
        Text("ASSET", color = TextMuted2, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.2.sp, modifier = Modifier.weight(1.56f))
        Text("SIDE", color = TextMuted2, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.2.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ActivityTableRow(action: ActivityAction, showBottomPadding: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 26.dp)
            .padding(top = 12.dp, bottom = if (showBottomPadding) 14.dp else 12.dp)
            .testTag("activityAction-${action.symbol}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ActivityActionBadge(action.label.uppercase(), Modifier.weight(1.08f))
        Text(action.symbol, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1.56f))
        Text(
            action.side.uppercase(),
            color = if (action.side.equals("Long", ignoreCase = true)) Success else Danger,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.4.sp,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ActivityActionBadge(label: String, modifier: Modifier = Modifier) {
    val color = activityActionColor(label)

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(color.copy(alpha = 0.14f))
                .border(1.dp, color.copy(alpha = 0.42f), RoundedCornerShape(999.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(label, color = color, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.1.sp)
        }
    }
}

private fun activityActionColor(label: String): Color = when {
    label.startsWith("kept", ignoreCase = true) -> ActivityKept
    label.startsWith("open", ignoreCase = true) -> ActivityOpen
    label.startsWith("closed", ignoreCase = true) -> ActivityClosed
    else -> TextPrimary
}

@Composable
private fun ActivitySideBadge(label: String, tone: Tone) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(toneColor(tone).copy(alpha = 0.14f))
            .padding(horizontal = 5.dp, vertical = 4.dp)
    ) {
        Text(
            label,
            color = toneColor(tone),
            fontSize = 8.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun LineChartCard(title: String, subtitle: String, positive: Boolean) {
    NjordCard {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, color = TextPrimary, fontWeight = FontWeight.ExtraBold)
            Text(subtitle, color = if (positive) Success else Danger, fontSize = 12.sp)
        }
        Spacer(Modifier.height(8.dp))
        ChartCanvas(positive = positive)
    }
}

@Composable
private fun ChartCanvas(
    positive: Boolean,
    showFill: Boolean = false,
    axisLabels: List<String> = emptyList(),
    points: List<ChartPoint> = emptyList()
) {
    val chartPoints = points.ifEmpty { if (positive) NjordMockData.equityCurve else NjordMockData.drawdownCurve }
    val lineColor = if (positive) Success else Danger
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black.copy(alpha = 0.10f))
            .padding(8.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2.75f)
        ) {
            val w = size.width
            val h = size.height
            repeat(3) { idx ->
                val y = h * (idx + 1) / 4f
                drawLine(Color.White.copy(alpha = 0.08f), Offset(0f, y), Offset(w, y), strokeWidth = 1.dp.toPx())
            }
            val path = Path()
            chartPoints.forEachIndexed { index, point ->
                val offset = Offset(point.x * w, point.y * h)
                if (index == 0) path.moveTo(offset.x, offset.y) else path.lineTo(offset.x, offset.y)
            }
            if (showFill) {
                val fillPath = Path()
                chartPoints.forEachIndexed { index, point ->
                    val offset = Offset(point.x * w, point.y * h)
                    if (index == 0) fillPath.moveTo(offset.x, offset.y) else fillPath.lineTo(offset.x, offset.y)
                }
                fillPath.lineTo(w, h)
                fillPath.lineTo(0f, h)
                fillPath.close()
                drawPath(fillPath, lineColor.copy(alpha = 0.18f))
            }
            drawPath(path, lineColor, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
        if (axisLabels.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                axisLabels.forEach { label ->
                    Text(label, color = TextMuted2, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
private fun LiveIncidentCarousel(incidents: List<Incident>, onIncidentClick: (Incident) -> Unit) {
    if (incidents.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            LazyRow(
                modifier = Modifier.fillMaxWidth().testTag("liveIncidentCarousel"),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(incidents, key = { it.id }) { incident ->
                    Box(Modifier.width(maxWidth)) {
                        IncidentBanner(incident) { onIncidentClick(incident) }
                    }
                }
            }
        }
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            incidents.forEachIndexed { index, _ ->
                Box(
                    Modifier
                        .padding(horizontal = 3.dp)
                        .size(if (index == 0) 7.dp else 6.dp)
                        .clip(CircleShape)
                        .background(if (index == 0) Primary else Outline)
                        .testTag("liveIncidentDot-$index")
                )
            }
        }
    }
}

@Composable
private fun IncidentBanner(incident: Incident, onClick: () -> Unit) {
    val shape = RoundedCornerShape(22.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(LiveErrorSurface)
            .border(1.dp, Danger.copy(alpha = 0.24f), shape)
            .clickable(onClick = onClick)
            .testTag("incident-${incident.id}")
    ) {
        Box(
            Modifier
                .width(4.dp)
                .height(88.dp)
                .align(Alignment.CenterStart)
                .background(Danger)
        )
        Row(
            Modifier.padding(start = 20.dp, end = 14.dp, top = 15.dp, bottom = 15.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Badge(incident.badge.uppercase(), incident.tone)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        incident.title,
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    "${incident.subtitle.substringBefore(" ·")} · ${incident.reason.lowercase()} rejected · ${incident.age} ago",
                    color = TextMuted,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(incident.age, color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                Text("AGE", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun IncidentDialog(incident: Incident, onClose: () -> Unit, onDismissIncident: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.68f))
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().testTag("incidentDialog"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Surface2),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f))
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Column {
                    Text(incident.title, color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                    Text(incident.subtitle, color = TextMuted, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    LiveValueTile("CURRENT", incident.current, Modifier.weight(1f))
                    LiveValueTile("REASON", incident.reason, Modifier.weight(1f))
                }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Surface3.copy(alpha = 0.86f))
                        .border(1.dp, Color.White.copy(alpha = 0.07f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Text(incident.detail, color = TextPrimary, fontSize = 14.sp, lineHeight = 21.sp)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onClose,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Surface3, contentColor = TextPrimary),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Close", fontWeight = FontWeight.ExtraBold)
                    }
                    Button(
                        onClick = onDismissIncident,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5A2933), contentColor = Color(0xFFFFC0CA)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Dismiss", fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun PositionSheet(position: LivePosition, onClose: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(20.dp).testTag("positionSheet"), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CoinIcon(position.symbol, if (position.trendUp) Tone.Success else Tone.Danger)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(position.symbol, color = TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                Text("${position.strategyName} · ${position.side} · ${position.opened} open", color = TextMuted)
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Outlined.Close, null, tint = TextMuted)
            }
        }
        SummaryLine("P&L", "${position.pnl} ${position.pct}", if (position.pnl.startsWith("-")) Tone.Danger else Tone.Success)
        SummaryLine("Size", position.size, Tone.Muted)
        SummaryLine("Capital", position.capital, Tone.Muted)
        SummaryLine("Entry", position.entry, Tone.Muted)
        SummaryLine("Current", position.current, if (position.trendUp) Tone.Success else Tone.Danger)
        Spacer(Modifier.height(18.dp))
    }
}

@Composable
private fun NjordCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val clickable = if (onClick != null) modifier.clickable(onClick = onClick) else modifier
    Card(
        modifier = clickable.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Surface1),
        border = BorderStroke(1.dp, Outline.copy(alpha = 0.65f))
    ) {
        Column(Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        title,
        color = Color(0xFFD6DDE6),
        fontSize = 13.sp,
        fontWeight = FontWeight.ExtraBold,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun CyclePill(label: String, value: String) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.055f))
            .padding(horizontal = 18.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text(value, color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun <T> FilterRow(items: List<T>, selected: T, label: (T) -> String, onSelect: (T) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        items.forEach { item ->
            val active = item == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (active) PrimaryContainer else Surface1)
                    .clickable { onSelect(item) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .testTag("filter-${label(item)}")
                    .semantics { this.selected = active }
            ) {
                Text(label(item), color = if (active) Primary else TextMuted, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun Badge(label: String, tone: Tone) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(toneColor(tone).copy(alpha = 0.14f))
            .padding(horizontal = 9.dp, vertical = 5.dp)
    ) {
        Text(label, color = toneColor(tone), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun CoinIcon(text: String, tone: Tone) {
    val symbol = text.uppercase()
    val context = LocalContext.current.applicationContext
    var logo by remember(symbol) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(symbol) {
        logo = loadCoinLogo(context, symbol)
    }

    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(Surface3),
        contentAlignment = Alignment.Center
    ) {
        logo?.let {
            Image(
                bitmap = it,
                contentDescription = "$symbol logo",
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Fit
            )
        } ?: Text(symbol.take(4), color = toneColor(tone), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
    }
}

private suspend fun loadCoinLogo(context: Context, symbol: String): ImageBitmap? =
    withContext(Dispatchers.IO) {
        CoinLogoCache.read(context.cacheDir, symbol)?.let { cachedBytes ->
            decodeLogoBytes(cachedBytes)?.let { return@withContext it }
            CoinLogoCache.delete(context.cacheDir, symbol)
        }

        CoinLogoSourcesBySymbol[symbol]?.let { sources ->
            listOf(sources.primaryUrl, sources.fallbackUrl).firstNotNullOfOrNull { url ->
                runCatching {
                    val bytes = loadRemoteBitmapBytes(context, url) ?: return@runCatching null
                    decodeLogoBytes(bytes)?.also {
                        CoinLogoCache.write(context.cacheDir, symbol, bytes)
                    }
                }.getOrNull()
            }?.let { return@withContext it }
        }

        findCoinGeckoLogoUrl(context, symbol)?.let { url ->
            runCatching {
                val bytes = loadRemoteBitmapBytes(context, url) ?: return@runCatching null
                decodeLogoBytes(bytes)?.also {
                    CoinLogoCache.write(context.cacheDir, symbol, bytes)
                }
            }.getOrNull()
        }
    }

private suspend fun findCoinGeckoLogoUrl(context: Context, symbol: String): String? {
    val query = URLEncoder.encode(symbol.lowercase(), "UTF-8")
    val searchUrl = "https://api.coingecko.com/api/v3/search?query=$query"
    val response = loadRemoteText(context, searchUrl) ?: return null
    val coins = JSONObject(response).optJSONArray("coins") ?: return null

    val selectedCoin = (0 until coins.length())
        .asSequence()
        .mapNotNull { coins.optJSONObject(it) }
        .firstOrNull { it.optString("symbol").equals(symbol, ignoreCase = true) }
        ?: coins.optJSONObject(0)
        ?: return null

    return selectedCoin.optString("large").takeIf { it.startsWith("http") }
        ?: selectedCoin.optString("thumb").takeIf { it.startsWith("http") }
}

private suspend fun loadRemoteText(context: Context, url: String): String? {
    val path = URL(url).file
    val connection = openHttpConnection(url)

    return try {
        val code = connection.responseCode
        if (code !in 200..299) {
            val body = connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            showApiFailureToast(context, ApiPayloadResult.Error(code, path, NjordApiClient.apiErrorMessage(body, "HTTP $code")))
            return null
        }
        connection.inputStream.bufferedReader().use { it.readText() }
    } catch (e: Exception) {
        showApiFailureToast(context, ApiPayloadResult.Error(null, path, e.message ?: "Unknown error"))
        null
    } finally {
        connection.disconnect()
    }
}

private suspend fun loadRemoteBitmapBytes(context: Context, url: String): ByteArray? {
    val path = URL(url).file
    val connection = openHttpConnection(url)

    return try {
        val code = connection.responseCode
        if (code !in 200..299) {
            val body = connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            showApiFailureToast(context, ApiPayloadResult.Error(code, path, NjordApiClient.apiErrorMessage(body, "HTTP $code")))
            return null
        }
        connection.inputStream.use { it.readBytes() }
    } catch (e: Exception) {
        showApiFailureToast(context, ApiPayloadResult.Error(null, path, e.message ?: "Unknown error"))
        null
    } finally {
        connection.disconnect()
    }
}

private fun decodeLogoBytes(bytes: ByteArray): ImageBitmap? =
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()

private fun openHttpConnection(url: String): HttpURLConnection =
    (URL(url).openConnection() as HttpURLConnection).apply {
        connectTimeout = 15_000
        readTimeout = 15_000
        requestMethod = "GET"
        setRequestProperty("User-Agent", "Njord Android")
    }

@Composable
private fun PriceChip(label: String, value: String, modifier: Modifier = Modifier, tone: Tone = Tone.Muted) {
    Column(modifier.clip(RoundedCornerShape(14.dp)).background(Color.White.copy(alpha = 0.04f)).padding(9.dp)) {
        Text(label.uppercase(), color = TextMuted2, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
        Text(value, color = toneColor(tone), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun SummaryLine(label: String, value: String, tone: Tone) {
    Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = TextMuted)
        Text(value, color = toneColor(tone), fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun ReportFactorRow(factor: ReportFactor) {
    Row(Modifier.padding(top = 10.dp)) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(if (factor.isRisk) Danger else Primary).align(Alignment.CenterVertically))
        Spacer(Modifier.width(10.dp))
        Text(factor.text, color = TextMuted, fontSize = 13.sp)
    }
}

@Composable
private fun LayerScoreRow(score: LayerScore) {
    Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(score.name, color = TextMuted)
        Text(score.score, color = toneColor(score.tone), fontWeight = FontWeight.ExtraBold)
    }
}

private fun LogFilter.toTone(): Tone = when (this) {
    LogFilter.Info -> Tone.Info
    LogFilter.Warn -> Tone.Warning
    LogFilter.Error -> Tone.Danger
    LogFilter.All -> Tone.Muted
}

private fun toneColor(tone: Tone): Color = when (tone) {
    Tone.Primary -> Primary
    Tone.Success -> Success
    Tone.Warning -> Warning
    Tone.Danger -> Danger
    Tone.Info -> Info
    Tone.Muted -> TextMuted
}
