package com.njord.mobile

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.njord.mobile.model.Destination
import com.njord.mobile.model.LogEntry
import com.njord.mobile.model.LogFilter
import com.njord.mobile.model.NjordUiState
import com.njord.mobile.model.StrategyFilter
import com.njord.mobile.ui.NjordDashboardScreen
import org.junit.Rule
import org.junit.Test

class NjordLogsScreenTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun logsRow_clickExpandsFullMessageBody() {
        val longMessage = "Exchange rejected order after retry because available margin was below the strategy safety threshold and the connector returned a detailed diagnostic payload."

        compose.setContent {
            NjordDashboardScreen(
                state = NjordUiState(
                    destination = Destination.Logs,
                    logs = listOf(
                        LogEntry(
                            level = LogFilter.Error,
                            strategy = StrategyFilter.BigBang,
                            title = "Big Bang open failed · ETH Long",
                            message = longMessage,
                            time = "13:52",
                            searchText = "Big Bang ETH $longMessage"
                        )
                    )
                ),
                onAction = {}
            )
        }

        compose.onAllNodesWithTag("expandedLogMessage").assertCountEquals(0)
        compose.onNodeWithTag("logRowToggle").performClick()

        compose.onNodeWithTag("expandedLogMessage").assertIsDisplayed()
        compose.onNodeWithText(longMessage).assertIsDisplayed()

        compose.onNodeWithTag("logRowToggle").performClick()

        compose.onAllNodesWithTag("expandedLogMessage").assertCountEquals(0)
    }
}
