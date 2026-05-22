package com.hackathon.hunter.ui

import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.hackathon.hunter.data.local.entity.HackathonEntity
import com.hackathon.hunter.ui.components.FilterBottomSheet
import com.hackathon.hunter.ui.components.HackathonCard
import com.hackathon.hunter.ui.components.ReportConfirmationDialog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HackathonUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testHackathon = HackathonEntity(
        id = 1337,
        platform = "gitcoin",
        platformId = "gc-1337",
        title = "Global Web3 Hackathon",
        description = "A massive decentralised buildathon.",
        url = "https://gitcoin.co/hackathon/1337",
        rulesUrl = "https://gitcoin.co/hackathon/1337/rules",
        prizeType = "crypto",
        prizeCurrency = "ETH",
        prizeValue = 5.0,
        isOnline = true,
        startDate = "2026-06-01",
        endDate = "2026-06-15",
        isVietnamEligible = true,
        reportCount = 2,
        isReportedByUser = false,
        isBookmarked = false
    )

    @Test
    fun testHackathonCardAccessibilityAndTalkBack() {
        composeTestRule.setContent {
            HackathonCard(
                hackathon = testHackathon,
                onCardClick = {},
                onBookmarkClick = {},
                onReportClick = {}
            )
        }

        // 4. Accessibility TalkBack nodes exist on cards
        val expectedTalkBackDescription = "Cuộc thi: Global Web3 Hackathon. Nền tảng: gitcoin. Giải thưởng: Ξ5. Hợp lệ cho Việt Nam. Trạng thái: Chưa báo cáo."
        composeTestRule.onNodeWithContentDescription(expectedTalkBackDescription).assertExists()
    }

    @Test
    fun testClickingReportShowsConfirmationDialogAndHidingFlow() {
        var reportClicked = false
        var dialogDismissed = false
        var dialogConfirmed = false

        // 1. Render card & dialog dynamically based on state
        composeTestRule.setContent {
            var showDialog by remember { mutableStateOf(false) }
            var hackathonState by remember { mutableStateOf(testHackathon) }

            if (showDialog) {
                ReportConfirmationDialog(
                    onConfirm = {
                        dialogConfirmed = true
                        hackathonState = hackathonState.copy(isReportedByUser = true)
                        showDialog = false
                    },
                    onDismiss = {
                        dialogDismissed = true
                        showDialog = false
                    }
                )
            }

            HackathonCard(
                hackathon = hackathonState,
                onCardClick = {},
                onBookmarkClick = {},
                onReportClick = {
                    reportClicked = true
                    showDialog = true
                }
            )
        }

        // Assert card is displayed initially
        composeTestRule.onNodeWithText("Global Web3 Hackathon").assertIsDisplayed()

        // 1. Clicking "Báo cấm VN" shows the confirmation dialog.
        composeTestRule.onNodeWithText("Báo cấm VN").performClick()
        assertTrue(reportClicked)
        composeTestRule.onNodeWithText("Xác nhận báo cáo").assertIsDisplayed()

        // Test dismissing the dialog retains the card
        composeTestRule.onNodeWithText("Hủy").performClick()
        assertTrue(dialogDismissed)
        composeTestRule.onNodeWithText("Xác nhận báo cáo").assertDoesNotExist()
        composeTestRule.onNodeWithText("Global Web3 Hackathon").assertIsDisplayed()

        // Click report again to test confirm flow
        composeTestRule.onNodeWithText("Báo cấm VN").performClick()
        composeTestRule.onNodeWithText("Báo cáo").performClick()
        assertTrue(dialogConfirmed)

        // 2. Confirming the dialog triggers reporting action and card gets marked
        composeTestRule.onNodeWithText("Xác nhận báo cáo").assertDoesNotExist()
        composeTestRule.onNodeWithText("Đã báo cáo cấm VN").assertIsDisplayed()
    }

    @Test
    fun testModifyingFiltersUpdatesStates() {
        var vietnamOnlyState = true
        var prizeTypeState = "all"
        var selectedPlatformsState = setOf("gitcoin")
        var isOnlineState: Boolean? = true
        var minPrizeValueState = 1000.0
        var searchQueryState = ""
        var resetTriggered = false

        composeTestRule.setContent {
            FilterBottomSheet(
                onDismissRequest = {},
                vietnamOnly = vietnamOnlyState,
                onVietnamOnlyChange = { vietnamOnlyState = it },
                prizeType = prizeTypeState,
                onPrizeTypeChange = { prizeTypeState = it },
                selectedPlatforms = selectedPlatformsState,
                onPlatformToggle = {
                    selectedPlatformsState = if (selectedPlatformsState.contains(it)) {
                        selectedPlatformsState - it
                    } else {
                        selectedPlatformsState + it
                    }
                },
                isOnline = isOnlineState,
                onOnlineChange = { isOnlineState = it },
                minPrizeValue = minPrizeValueState,
                onMinPrizeValueChange = { minPrizeValueState = it },
                searchQuery = searchQueryState,
                onSearchQueryChange = { searchQueryState = it },
                onResetAll = { resetTriggered = true }
            )
        }

        // Verify elements render
        composeTestRule.onNodeWithText("Bộ lọc cuộc thi").assertIsDisplayed()
        
        // 3. Modifying filters updates items in list (interacted in sheet)
        // Click segment Crypto / Token
        composeTestRule.onNodeWithText("Crypto / Token").performClick()
        assertEquals("crypto", prizeTypeState)

        // Toggle platform Gitcoin off
        composeTestRule.onNodeWithText("Gitcoin").performClick()
        assertTrue(selectedPlatformsState.isEmpty())

        // Click reset all
        composeTestRule.onNodeWithText("Thiết lập lại").performClick()
        assertTrue(resetTriggered)
    }
}
