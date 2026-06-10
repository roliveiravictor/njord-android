package com.njord.mobile.ui

import android.content.Context
import android.graphics.BitmapFactory
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
import androidx.compose.runtime.setValue
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
import com.njord.mobile.model.LivePosition
import com.njord.mobile.model.LogEntry
import com.njord.mobile.model.LogFilter
import com.njord.mobile.model.MiniKpi
import com.njord.mobile.model.NjordAction
import com.njord.mobile.model.NjordMockData
import com.njord.mobile.model.NjordUiState
import com.njord.mobile.model.PortfolioPosition
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
private val Success = Color(0xFF6EE7A8)
private val Warning = Color(0xFFFFD166)
private val Danger = Color(0xFFFF7F9A)
private val Info = Color(0xFF9CB7FF)
private val PortfolioTileSurface = Color(0xFF171C24)
private val LiveFilterSurface = Color(0xFF15171B)
private val LiveStrategyFilterActive = Color(0xFF263846)
private val LiveCardSurface = Color(0xFF151A21)
private val LiveTileSurface = Color(0xFF25292F)
private val LiveErrorSurface = Color(0xFF18161B)

private data class PortfolioStat(
    val label: String,
    val value: String,
    val tone: Tone = Tone.Muted,
    val subtext: String? = null
)

private data class MonthReturn(
    val month: String,
    val value: String,
    val progress: Float,
    val tone: Tone
)

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
                Destination.Activity -> item { ActivityScreen() }
                Destination.Heartbeat -> item { HeartbeatScreen() }
                Destination.Logs -> item { LogsScreen(state, onAction) }
                Destination.Reports -> item { ReportsScreen() }
            }
            item { Spacer(Modifier.height(12.dp)) }
        }

        state.selectedIncident?.let { incident ->
            IncidentDialog(
                incident = incident,
                onClose = { onAction(NjordAction.CloseIncident) },
                onDismissIncident = { onAction(NjordAction.DismissIncident(incident.id)) }
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

@Composable
private fun HomeScreen(state: NjordUiState, onAction: (NjordAction) -> Unit) {
    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        HomeEquityHero { onAction(NjordAction.Navigate(Destination.Portfolio)) }

        SectionTitle("Strategies")
        NjordMockData.strategySummaries.forEach {
            HomeStrategyCard(it) { onAction(NjordAction.Navigate(Destination.Live)) }
        }

        SectionTitle("Activity")
        HomeActivityCard { onAction(NjordAction.Navigate(Destination.Activity)) }

        SectionTitle("Heartbeat")
        HomeHeartbeatCard { onAction(NjordAction.Navigate(Destination.Heartbeat)) }

        SectionTitle("Incidents")
        HomeIncidentsCard(
            incidents = NjordMockData.incidents.filterNot { it.id in state.dismissedIncidentIds },
            onClick = { onAction(NjordAction.Navigate(Destination.Live)) }
        )
    }
}

@Composable
private fun PortfolioScreen(state: NjordUiState, onAction: (NjordAction) -> Unit) {
    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PortfolioPerformanceHero()
        FilterRow(
            items = StrategyFilter.entries,
            selected = state.portfolioStrategyFilter,
            label = { it.label },
            onSelect = { onAction(NjordAction.SetPortfolioStrategyFilter(it)) }
        )
        SectionTitle("Live metrics")
        PortfolioMetricGrid()
        SectionTitle("Monthly stats")
        PortfolioMonthlyStats()
        SectionTitle("Performance history")
        PortfolioHistoryCard(
            title = "Portfolio P&L over time",
            trailing = "30D trend",
            stats = listOf(
                PortfolioStat("Current", "$18.4k"),
                PortfolioStat("High", "$19.1k"),
                PortfolioStat("30D P&L", "+$2.4k", Tone.Success)
            )
        ) {
            ChartCanvas(
                positive = true,
                showFill = true,
                axisLabels = listOf("May 10", "May 20", "May 30", "Today")
            )
        }
        DrawdownHistoryCard()
        ReturnByMonthCard()
    }
}

@Composable
private fun LiveScreen(state: NjordUiState, onAction: (NjordAction) -> Unit) {
    val positions = visibleLivePositions(
        NjordMockData.livePositions,
        state.liveStrategyFilter,
        state.liveSideFilter
    )
    val incidents = NjordMockData.incidents
        .filter { it.tone == Tone.Danger && it.id !in state.dismissedIncidentIds }

    Column(
        Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LiveFilterBar(
            strategyFilter = state.liveStrategyFilter,
            sideFilter = state.liveSideFilter,
            onStrategySelect = { onAction(NjordAction.SetLiveStrategyFilter(it)) },
            onSideSelect = { onAction(NjordAction.SetLiveSideFilter(it)) }
        )
        LiveIncidentCarousel(incidents) { onAction(NjordAction.SelectIncident(it)) }
        if (positions.isEmpty()) {
            NjordCard(Modifier.testTag("emptyLivePositions")) {
                Text("No open positions match this filter.", color = TextMuted, fontSize = 13.sp)
            }
        } else {
            positions.forEach { position ->
                LivePositionCard(position) { onAction(NjordAction.SelectPosition(position)) }
            }
        }
        LiveAnalyticsSections()
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
private fun ActivityScreen() {
    val summary = NjordMockData.activitySummary

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
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Candle close",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActivitySummaryChip("+${summary.opened}", "Opened")
                ActivitySummaryChip("-${summary.closed}", "Closed")
                ActivitySummaryChip("•${summary.kept}", "Kept")
            }

            Spacer(Modifier.height(18.dp))
            HorizontalDivider(color = Outline.copy(alpha = 0.85f))
            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                NjordMockData.cycles.forEachIndexed { index, cycle ->
                    ActivityCycleTable(cycle, isLast = index == NjordMockData.cycles.lastIndex)
                }
            }
        }
    }
}

@Composable
private fun HeartbeatScreen() {
    Column(
        Modifier
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        HeartbeatHealthCard()
        Text(
            "Service routines",
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(top = 18.dp, bottom = 4.dp, start = 4.dp)
        )
        NjordMockData.heartbeatRoutines.forEach { HeartbeatRow(it) }
    }
}

@Composable
private fun LogsScreen(state: NjordUiState, onAction: (NjordAction) -> Unit) {
    val logs = visibleLogs(NjordMockData.logs, state.logFilter, state.logQuery)
    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Latest logs", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            Text("Last 24h", color = TextMuted, fontSize = 12.sp)
        }
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
        FilterRow(
            items = LogFilter.entries,
            selected = state.logFilter,
            label = { it.label },
            onSelect = { onAction(NjordAction.SetLogFilter(it)) }
        )
        if (logs.isEmpty()) {
            NjordCard(Modifier.testTag("emptyLogs")) {
                Text("No logs match this filter.", color = TextMuted)
            }
        } else {
            logs.forEach { LogRow(it) }
        }
    }
}

@Composable
private fun ReportsScreen() {
    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ReportReferencePanel()
        NjordCard {
            Text("Key factors", color = TextPrimary, fontWeight = FontWeight.ExtraBold)
            NjordMockData.reportFactors.filterNot { it.isRisk }.forEach { ReportFactorRow(it) }
        }
        NjordCard {
            Text("Risks", color = TextPrimary, fontWeight = FontWeight.ExtraBold)
            NjordMockData.reportFactors.filter { it.isRisk }.forEach { ReportFactorRow(it) }
        }
        NjordCard {
            Text("Layer scores", color = TextPrimary, fontWeight = FontWeight.ExtraBold)
            NjordMockData.layerScores.forEach { LayerScoreRow(it) }
        }
    }
}

@Composable
private fun ReportReferencePanel() {
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
                "Hunch BTC Signal — 2026-06-08",
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
                    Text("Persisted 18m ago", color = TextMuted, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
            Spacer(Modifier.height(28.dp))
            ReportSignalBanner()
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
            ReportSummaryRow("Last Signal", "SELL", Tone.Danger)
            ReportSummaryRow("Last Confidence", "HIGH", Tone.Muted)
            ReportSummaryRow("Last Score", "-0.468", Tone.Muted)
            ReportSummaryRow("Last Signal Date", "2026-06-07", Tone.Muted)
            ReportSummaryRow("Last BTC Price", "$62,215.00", Tone.Muted)
            ReportSummaryRow("Current BTC Price", "$63,193.50", Tone.Muted)
            ReportSummaryRow("Price Delta", "+1.57%", Tone.Success)
            ReportSummaryRow("Last Signal Correct", "NO", Tone.Danger, showDivider = false)
        }
    }
}

@Composable
private fun ReportSignalBanner() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFFF315F))
            .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(18.dp))
            .padding(horizontal = 18.dp, vertical = 25.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "SELL",
            color = Color.White,
            fontSize = 36.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(10.dp))
        Text(
            "Confidence: HIGH | Score: -0.523",
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
private fun HomeEquityHero(onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF173747), Color(0xFF151A22))))
            .clickable(onClick = onClick)
            .padding(22.dp)
            .testTag("homeEquityHero")
    ) {
        Text("$18,420.00", color = TextPrimary, fontSize = 38.sp, fontWeight = FontWeight.ExtraBold)
        Text("Total Equity", color = TextMuted, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text("+$428.00", color = Success, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.width(8.dp))
            Text("+3.8% unrealized", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        HorizontalDivider(Modifier.padding(vertical = 18.dp), color = Color.White.copy(alpha = 0.1f))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            HomeHeroKpi("AVAILABLE", "$7.8K")
            HomeHeroKpi("IN USE", "$3.1K")
            HomeHeroKpi("OPEN", "18 Pos")
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
private fun PortfolioPerformanceHero() {
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
                Text("$18.4k", color = Success, fontSize = 31.sp, fontWeight = FontWeight.ExtraBold)
                Text("Total equity", color = TextMuted, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Info.copy(alpha = 0.22f))
                    .padding(horizontal = 11.dp, vertical = 6.dp)
            ) {
                Text("ALL +127.4%", color = Color(0xFFBFD0FF), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
        Spacer(Modifier.height(28.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PortfolioStatTile("TODAY", "+$96", Tone.Success, "+0.5%", Modifier.weight(1f))
            PortfolioStatTile("7D", "+$812", Tone.Success, "+4.6%", Modifier.weight(1f))
            PortfolioStatTile("30D", "+$2.4k", Tone.Success, "+14.8%", Modifier.weight(1f))
        }
    }
}

@Composable
private fun PortfolioMetricGrid() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PortfolioStatTile("REALIZED P&L", "+$1.9k", Tone.Success, "Closed positions", Modifier.weight(1f))
            PortfolioStatTile("UNREALIZED P&L", "+$428", Tone.Success, "Open positions", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PortfolioStatTile("WIN RATE", "56%", Tone.Muted, "124 closed trades", Modifier.weight(1f))
            PortfolioStatTile("PROFIT FACTOR", "1.42", Tone.Muted, "Gross profit / loss", Modifier.weight(1f))
        }
    }
}

@Composable
private fun PortfolioMonthlyStats() {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        PortfolioStatTile("BEST MONTH", "+6.2%", Tone.Success, "April", Modifier.weight(1f))
        PortfolioStatTile("WORST MONTH", "-1.1%", Tone.Danger, "March", Modifier.weight(1f))
        PortfolioStatTile("AVERAGE", "+2.8%", Tone.Success, "Last 6 months", Modifier.weight(1f))
    }
}

@Composable
private fun PortfolioHistoryCard(
    title: String,
    trailing: String,
    stats: List<PortfolioStat>,
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
private fun DrawdownHistoryCard() {
    PortfolioHistoryCard(
        title = "Drawdown",
        trailing = "Risk depth",
        stats = listOf(
            PortfolioStat("Current", "-2.1%", Tone.Warning),
            PortfolioStat("Max", "-6.4%", Tone.Danger),
            PortfolioStat("Recovery", "63%")
        )
    ) {
        ChartCanvas(
            positive = false,
            showFill = true,
            axisLabels = listOf("0%", "-3%", "-6%", "Recovery")
        )
    }
}

@Composable
private fun ReturnByMonthCard() {
    val months = listOf(
        MonthReturn("Jan", "+4.1%", 0.66f, Tone.Success),
        MonthReturn("Feb", "+2.3%", 0.48f, Tone.Success),
        MonthReturn("Mar", "-1.1%", 0.24f, Tone.Danger),
        MonthReturn("Apr", "+6.2%", 0.88f, Tone.Success),
        MonthReturn("May", "+3.8%", 0.62f, Tone.Success),
        MonthReturn("Jun", "+1.4%", 0.38f, Tone.Success)
    )

    PortfolioHistoryCard(
        title = "Return by month",
        trailing = "6-month view",
        stats = listOf(
            PortfolioStat("Best", "+6.2%", Tone.Success),
            PortfolioStat("Worst", "-1.1%", Tone.Danger),
            PortfolioStat("Average", "+2.8%", Tone.Success)
        )
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            months.forEach { month ->
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
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(summary.name, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(4.dp))
                Text(summary.subtitle, color = TextMuted, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                summary.assets?.let { assets ->
                    Spacer(Modifier.height(5.dp))
                    Text(assets, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (summary.pnl.isNotBlank() || summary.pct.isNotBlank()) {
                    Spacer(Modifier.height(20.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(summary.pnl, color = Success, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                        if (summary.pct.isNotBlank()) {
                            Spacer(Modifier.width(7.dp))
                            Text(summary.pct, color = Success, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
            SmallLiveDot()
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
private fun HomeActivityCard(onClick: () -> Unit) {
    NjordCard(Modifier.testTag("homeActivityCard"), onClick = onClick) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Candle close", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HomeCyclePill("OPENED", NjordMockData.activitySummary.opened, Modifier.weight(1f))
            HomeCyclePill("CLOSED", NjordMockData.activitySummary.closed, Modifier.weight(1f))
            HomeCyclePill("KEPT", NjordMockData.activitySummary.kept, Modifier.weight(1f))
        }
    }
}

@Composable
private fun HomeHeartbeatCard(onClick: () -> Unit) {
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
                Text("Heartbeat health", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(3.dp))
                Text("Weekly performance report late", color = TextMuted, fontSize = 13.sp)
            }
            Text("7/8", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.width(8.dp))
            Badge("1 late", Tone.Warning)
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
        HomeIncidentLine("Open failed · ETH", "8m")
        HomeIncidentLine("Near P&L stop · BTC", "12m")
        HomeIncidentLine("Retraining failed", "38m")
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
    LiveCard(Modifier.testTag("livePosition-${position.symbol}"), onClick = onClick) {
        Row(verticalAlignment = Alignment.Top) {
            CoinIcon(position.symbol, tone)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        position.symbol,
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.width(8.dp))
                    Badge(position.side, if (position.side == "Short") Tone.Danger else Tone.Success)
                }
                Text(
                    "${position.strategyName} · ${position.opened} open",
                    color = TextMuted,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(position.pnl, color = toneColor(tone), fontSize = 23.sp, fontWeight = FontWeight.ExtraBold)
                Text(position.pct, color = toneColor(tone), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LiveValueTile("SIZE", position.size.substringBefore(" "), Modifier.weight(1f))
            LiveValueTile("CAPITAL", position.capital, Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            LiveFooterValue("ENTRY", position.entry, Modifier.weight(1f))
            LiveFooterValue("CURRENT", position.current, Modifier.weight(1f), tone)
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
private fun LiveAnalyticsSections() {
    SectionTitle("Open P&L by strategy")
    NjordCard {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Text("Current contribution", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
            Column(horizontalAlignment = Alignment.End) {
                Text("Open positions only", color = TextMuted, fontSize = 12.sp)
                Text("+$428 total", color = TextPrimary, fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(16.dp))
        LiveContributionRow("Big Bang", 0.82f, "+$303", Tone.Success)
        LiveContributionRow("WCR", 0.50f, "+$184", Tone.Success)
        LiveContributionRow("Hunch", 0.18f, "-$59", Tone.Danger)
    }

    SectionTitle("Live summary")
    LiveMetricPanel(
        items = listOf(
            MiniKpi("POSITIONS", "18", "Current open", Tone.Primary),
            MiniKpi("LONG", "12", "66.7% of book", Tone.Muted),
            MiniKpi("SHORT", "6", "33.3% of book", Tone.Muted),
            MiniKpi("OPEN P&L", "+$428", "All strategies", Tone.Success),
            MiniKpi("CAPITAL", "$10.4k", "Displayed filter", Tone.Muted),
            MiniKpi("AVG AGE", "11h", "Current cycle", Tone.Muted)
        )
    )

    SectionTitle("Live metrics")
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        LiveOutcomeTile("LARGEST WINNER", "HYPE", "+$186", "+8.4%", Tone.Success, Modifier.weight(1f))
        LiveOutcomeTile("LARGEST LOSER", "SOL", "-$48", "-1.2%", Tone.Danger, Modifier.weight(1f))
    }

    SectionTitle("Position integrity")
    LiveIntegrityPanel(
        items = listOf(
            MiniKpi("MATCHED", "18", "Local + exchange", Tone.Success),
            MiniKpi("UNCLAIMED", "1", "Exchange only", Tone.Warning),
            MiniKpi("MISSING", "0", "Local only", Tone.Muted),
            MiniKpi("DUPLICATE", "0", "Multi-owner", Tone.Muted)
        )
    )
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
        items.chunked(3).forEach { rowItems ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowItems.forEach { item ->
                    LiveMetricTile(item.label, item.value, item.subtext, item.tone, Modifier.weight(1f))
                }
                repeat(3 - rowItems.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun LiveIntegrityPanel(items: List<MiniKpi>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Surface1)
            .border(1.dp, Outline.copy(alpha = 0.65f), RoundedCornerShape(22.dp))
            .padding(14.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items.forEach { item ->
                LiveIntegrityTile(item.label, item.value, item.subtext, item.tone, Modifier.weight(1f))
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
private fun LiveMetricTile(label: String, value: String, subtext: String, tone: Tone, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxWidth()
            .heightIn(min = 84.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(if (tone == Tone.Primary) PrimaryContainer.copy(alpha = 0.45f) else Surface3.copy(alpha = 0.78f))
            .border(1.dp, if (tone == Tone.Primary) Primary.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.07f), RoundedCornerShape(18.dp))
            .padding(horizontal = 10.dp, vertical = 11.dp)
    ) {
        Text(label, color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(6.dp))
        Text(value, color = toneColor(tone), fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(2.dp))
        Text(subtext, color = TextMuted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
        Text(symbol, color = toneColor(tone), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(amount, color = toneColor(tone), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
            Text(" · $percent", color = TextMuted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun LiveIntegrityTile(label: String, value: String, subtext: String, tone: Tone, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxWidth()
            .heightIn(min = 98.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (tone == Tone.Warning) Warning.copy(alpha = 0.16f) else Surface3.copy(alpha = 0.78f))
            .border(1.dp, toneColor(tone).copy(alpha = if (tone == Tone.Muted) 0.08f else 0.35f), RoundedCornerShape(16.dp))
            .padding(horizontal = 4.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, color = TextMuted, fontSize = 7.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(7.dp))
        Text(value, color = toneColor(tone), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
        Text(subtext, color = TextMuted, fontSize = 8.sp, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
private fun HeartbeatHealthCard() {
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
            fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.6.sp
        )
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "7 / 8 healthy",
                color = TextPrimary,
                fontSize = 39.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HeartbeatStatusPill("7 OK", Tone.Success)
                HeartbeatStatusPill("1 late", Tone.Warning)
                HeartbeatStatusPill("0 critical", Tone.Danger)
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
        Text(label, color = toneColor(tone), fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun HeartbeatRow(routine: HeartbeatRoutine) {
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 132.dp)
            .clip(shape)
            .background(Surface1)
            .border(1.dp, Outline.copy(alpha = 0.78f), shape)
            .padding(horizontal = 30.dp, vertical = 24.dp)
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
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(32.dp))
                Text(
                    routine.age,
                    color = TextMuted,
                    fontSize = 20.sp,
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
private fun LogRow(log: LogEntry) {
    NjordCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Badge(log.level.label, log.level.toTone())
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(log.title, color = TextPrimary, fontWeight = FontWeight.Bold)
                Text(log.message, color = TextMuted, fontSize = 12.sp)
            }
            Text(log.time, color = TextMuted2, fontSize = 12.sp)
        }
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
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Surface3.copy(alpha = 0.72f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.width(6.dp))
        Text(label, color = TextMuted, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
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
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .border(1.dp, Color(0xFF464B54), RoundedCornerShape(999.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(label, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.1.sp)
        }
    }
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
    axisLabels: List<String> = emptyList()
) {
    val points = if (positive) NjordMockData.equityCurve else NjordMockData.drawdownCurve
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
            points.forEachIndexed { index, point ->
                val offset = Offset(point.x * w, point.y * h)
                if (index == 0) path.moveTo(offset.x, offset.y) else path.lineTo(offset.x, offset.y)
            }
            if (showFill) {
                val fillPath = Path()
                points.forEachIndexed { index, point ->
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
    val sources = CoinLogoSourcesBySymbol[symbol]
    val context = LocalContext.current.applicationContext
    var logo by remember(symbol) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(symbol, sources) {
        logo = sources?.let { loadCoinLogo(context, symbol, it) }
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

private suspend fun loadCoinLogo(context: Context, symbol: String, sources: CoinLogoSources): ImageBitmap? =
    withContext(Dispatchers.IO) {
        CoinLogoCache.read(context.cacheDir, symbol)?.let { cachedBytes ->
            decodeLogoBytes(cachedBytes)?.let { return@withContext it }
            CoinLogoCache.delete(context.cacheDir, symbol)
        }

        listOf(sources.primaryUrl, sources.fallbackUrl).firstNotNullOfOrNull { url ->
            runCatching {
                val bytes = loadRemoteBitmapBytes(url) ?: return@runCatching null
                decodeLogoBytes(bytes)?.also {
                    CoinLogoCache.write(context.cacheDir, symbol, bytes)
                }
            }.getOrNull()
        } ?: findCoinGeckoLogoUrl(symbol)?.let { url ->
            runCatching {
                val bytes = loadRemoteBitmapBytes(url) ?: return@runCatching null
                decodeLogoBytes(bytes)?.also {
                    CoinLogoCache.write(context.cacheDir, symbol, bytes)
                }
            }.getOrNull()
        }
    }

private fun findCoinGeckoLogoUrl(symbol: String): String? {
    val query = URLEncoder.encode(symbol.lowercase(), "UTF-8")
    val searchUrl = "https://api.coingecko.com/api/v3/search?query=$query"
    val response = loadRemoteText(searchUrl) ?: return null
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

private fun loadRemoteText(url: String): String? {
    val connection = openHttpConnection(url)

    return try {
        if (connection.responseCode !in 200..299) return null
        connection.inputStream.bufferedReader().use { it.readText() }
    } finally {
        connection.disconnect()
    }
}

private fun loadRemoteBitmapBytes(url: String): ByteArray? {
    val connection = openHttpConnection(url)

    return try {
        if (connection.responseCode !in 200..299) return null
        connection.inputStream.use { it.readBytes() }
    } finally {
        connection.disconnect()
    }
}

private fun decodeLogoBytes(bytes: ByteArray): ImageBitmap? =
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()

private fun openHttpConnection(url: String): HttpURLConnection =
    (URL(url).openConnection() as HttpURLConnection).apply {
        connectTimeout = 4_000
        readTimeout = 4_000
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
