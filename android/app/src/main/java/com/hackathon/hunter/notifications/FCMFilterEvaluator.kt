package com.hackathon.hunter.notifications

object FCMFilterEvaluator {
    fun shouldNotify(
        id: Int,
        platform: String,
        prizeType: String,
        prizeValue: Double,
        isOnline: Boolean,
        isVietnamEligible: Boolean,
        reportedLogs: List<Int>,
        minPrizeValue: Double,
        filterPrizeType: String,
        vietnamOnly: Boolean,
        onlineOnly: Boolean,
        activePlatforms: Set<String>
    ): Boolean {
        if (reportedLogs.contains(id)) return false
        if (vietnamOnly && !isVietnamEligible) return false
        if (onlineOnly && !isOnline) return false
        if (prizeValue < minPrizeValue) return false
        if (filterPrizeType != "all" && !prizeType.equals(filterPrizeType, ignoreCase = true)) return false
        if (!activePlatforms.contains(platform.lowercase())) return false
        return true
    }
}
