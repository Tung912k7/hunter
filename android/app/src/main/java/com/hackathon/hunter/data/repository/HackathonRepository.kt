package com.hackathon.hunter.data.repository

import com.hackathon.hunter.data.local.entity.HackathonEntity
import kotlinx.coroutines.flow.Flow

interface HackathonRepository {
    fun getHackathons(): Flow<List<HackathonEntity>>
    fun getHackathonById(id: Int): Flow<HackathonEntity?>
    suspend fun fetchHackathons(
        isVietnamEligible: Boolean? = null,
        isOnline: Boolean? = null,
        prizeType: String? = null,
        minPrizeValue: Double? = null,
        platforms: List<String>? = null,
        query: String? = null
    ): Result<Unit>
    suspend fun toggleBookmark(id: Int, isBookmarked: Boolean)
    suspend fun reportHackathon(id: Int): Result<Unit>
}
