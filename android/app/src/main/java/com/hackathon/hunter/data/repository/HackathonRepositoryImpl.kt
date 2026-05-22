package com.hackathon.hunter.data.repository

import com.hackathon.hunter.data.local.dao.HackathonDao
import com.hackathon.hunter.data.local.entity.HackathonEntity
import com.hackathon.hunter.data.local.entity.ReportedHackathonEntity
import com.hackathon.hunter.data.remote.HackathonApiService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HackathonRepositoryImpl @Inject constructor(
    private val hackathonDao: HackathonDao,
    private val hackathonApiService: HackathonApiService
) : HackathonRepository {

    override fun getHackathons(): Flow<List<HackathonEntity>> {
        return hackathonDao.getHackathons()
    }

    override fun getHackathonById(id: Int): Flow<HackathonEntity?> {
        return hackathonDao.getHackathonById(id)
    }

    override suspend fun fetchHackathons(
        isVietnamEligible: Boolean?,
        isOnline: Boolean?,
        prizeType: String?,
        minPrizeValue: Double?,
        platforms: List<String>?,
        query: String?
    ): Result<Unit> {
        return try {
            val dtos = hackathonApiService.getHackathons(
                isVietnamEligible = isVietnamEligible,
                isOnline = isOnline,
                prizeType = prizeType,
                minPrizeValue = minPrizeValue,
                platforms = platforms,
                query = query
            )
            val entities = dtos.map { it.toEntity() }
            hackathonDao.insertOrUpdate(entities)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleBookmark(id: Int, isBookmarked: Boolean) {
        hackathonDao.toggleBookmark(id, isBookmarked)
    }

    override suspend fun reportHackathon(id: Int): Result<Unit> {
        // 1. Immediately update local DB to hide from UI
        hackathonDao.markAsReported(id)
        hackathonDao.insertReportedLog(ReportedHackathonEntity(id))

        // 2. Perform backend sync
        return try {
            val updatedDto = hackathonApiService.reportHackathon(id)
            
            // 3. Update the report count and eligibility stats locally
            val localEntity = hackathonDao.getHackathonByIdOneShot(id)
            if (localEntity != null) {
                val mergedEntity = localEntity.copy(
                    reportCount = updatedDto.reportCount,
                    isVietnamEligible = updatedDto.isVietnamEligible
                )
                hackathonDao.update(mergedEntity)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            // Keep the local reported status so it remains hidden, propagate exception
            Result.failure(e)
        }
    }
}
