package com.hackathon.hunter.notifications

import org.junit.Assert.*
import org.junit.Test

class FCMServiceTest {

    @Test
    fun `shouldNotify returns true when event matches all preferences`() {
        val result = FCMFilterEvaluator.shouldNotify(
            id = 1,
            platform = "devpost",
            prizeType = "fiat",
            prizeValue = 5000.0,
            isOnline = true,
            isVietnamEligible = true,
            reportedLogs = emptyList(),
            minPrizeValue = 1000.0,
            filterPrizeType = "all",
            vietnamOnly = true,
            onlineOnly = true,
            activePlatforms = setOf("devpost", "devfolio")
        )
        assertTrue(result)
    }

    @Test
    fun `shouldNotify returns false when event was already reported`() {
        val result = FCMFilterEvaluator.shouldNotify(
            id = 1,
            platform = "devpost",
            prizeType = "fiat",
            prizeValue = 5000.0,
            isOnline = true,
            isVietnamEligible = true,
            reportedLogs = listOf(1), // already reported
            minPrizeValue = 1000.0,
            filterPrizeType = "all",
            vietnamOnly = true,
            onlineOnly = true,
            activePlatforms = setOf("devpost", "devfolio")
        )
        assertFalse(result)
    }

    @Test
    fun `shouldNotify returns false when event is not Vietnam eligible but preferences require it`() {
        val result = FCMFilterEvaluator.shouldNotify(
            id = 1,
            platform = "devpost",
            prizeType = "fiat",
            prizeValue = 5000.0,
            isOnline = true,
            isVietnamEligible = false,
            reportedLogs = emptyList(),
            minPrizeValue = 1000.0,
            filterPrizeType = "all",
            vietnamOnly = true, // Require Vietnam eligible
            onlineOnly = true,
            activePlatforms = setOf("devpost", "devfolio")
        )
        assertFalse(result)
    }

    @Test
    fun `shouldNotify returns true when event is not Vietnam eligible but preferences do not require it`() {
        val result = FCMFilterEvaluator.shouldNotify(
            id = 1,
            platform = "devpost",
            prizeType = "fiat",
            prizeValue = 5000.0,
            isOnline = true,
            isVietnamEligible = false,
            reportedLogs = emptyList(),
            minPrizeValue = 1000.0,
            filterPrizeType = "all",
            vietnamOnly = false, // Do not require Vietnam eligible
            onlineOnly = true,
            activePlatforms = setOf("devpost", "devfolio")
        )
        assertTrue(result)
    }

    @Test
    fun `shouldNotify returns false when prize value is lower than minimum preference`() {
        val result = FCMFilterEvaluator.shouldNotify(
            id = 1,
            platform = "devpost",
            prizeType = "fiat",
            prizeValue = 500.0, // lower than 1000.0
            isOnline = true,
            isVietnamEligible = true,
            reportedLogs = emptyList(),
            minPrizeValue = 1000.0,
            filterPrizeType = "all",
            vietnamOnly = true,
            onlineOnly = true,
            activePlatforms = setOf("devpost", "devfolio")
        )
        assertFalse(result)
    }

    @Test
    fun `shouldNotify returns false when prize type does not match filter`() {
        val result = FCMFilterEvaluator.shouldNotify(
            id = 1,
            platform = "devpost",
            prizeType = "crypto", // Event is crypto
            prizeValue = 5000.0,
            isOnline = true,
            isVietnamEligible = true,
            reportedLogs = emptyList(),
            minPrizeValue = 1000.0,
            filterPrizeType = "fiat", // Require fiat only
            vietnamOnly = true,
            onlineOnly = true,
            activePlatforms = setOf("devpost", "devfolio")
        )
        assertFalse(result)
    }

    @Test
    fun `shouldNotify returns false when platform is not active`() {
        val result = FCMFilterEvaluator.shouldNotify(
            id = 1,
            platform = "gitcoin", // Event is on gitcoin
            prizeType = "crypto",
            prizeValue = 5000.0,
            isOnline = true,
            isVietnamEligible = true,
            reportedLogs = emptyList(),
            minPrizeValue = 1000.0,
            filterPrizeType = "all",
            vietnamOnly = true,
            onlineOnly = true,
            activePlatforms = setOf("devpost", "devfolio") // gitcoin not active
        )
        assertFalse(result)
    }
}
