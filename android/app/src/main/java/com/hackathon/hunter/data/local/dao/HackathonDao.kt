package com.hackathon.hunter.data.local.dao

import androidx.room.*
import com.hackathon.hunter.data.local.entity.HackathonEntity
import com.hackathon.hunter.data.local.entity.ReportedHackathonEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class HackathonDao {

    @Query("SELECT * FROM hackathons WHERE isVietnamEligible = 1 AND isReportedByUser = 0 ORDER BY id DESC")
    abstract fun getHackathons(): Flow<List<HackathonEntity>>

    @Query("SELECT * FROM hackathons WHERE id = :id")
    abstract fun getHackathonById(id: Int): Flow<HackathonEntity?>

    @Query("SELECT * FROM hackathons WHERE id = :id")
    abstract suspend fun getHackathonByIdOneShot(id: Int): HackathonEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertAllIgnore(hackathons: List<HackathonEntity>): List<Long>

    @Update
    abstract suspend fun update(hackathon: HackathonEntity)

    @Transaction
    open suspend fun insertOrUpdate(hackathons: List<HackathonEntity>) {
        val insertResults = insertAllIgnore(hackathons)
        for (i in insertResults.indices) {
            if (insertResults[i] == -1L) {
                val item = hackathons[i]
                updateKeepLocalState(
                    id = item.id,
                    platform = item.platform,
                    platformId = item.platformId,
                    title = item.title,
                    description = item.description,
                    url = item.url,
                    rulesUrl = item.rulesUrl,
                    prizeType = item.prizeType,
                    prizeCurrency = item.prizeCurrency,
                    prizeValue = item.prizeValue,
                    isOnline = item.isOnline,
                    startDate = item.startDate,
                    endDate = item.endDate,
                    isVietnamEligible = item.isVietnamEligible,
                    reportCount = item.reportCount
                )
            }
        }
    }

    @Query("UPDATE hackathons SET platform = :platform, platformId = :platformId, title = :title, description = :description, url = :url, rulesUrl = :rulesUrl, prizeType = :prizeType, prizeCurrency = :prizeCurrency, prizeValue = :prizeValue, isOnline = :isOnline, startDate = :startDate, endDate = :endDate, isVietnamEligible = :isVietnamEligible, reportCount = :reportCount WHERE id = :id")
    abstract suspend fun updateKeepLocalState(
        id: Int,
        platform: String,
        platformId: String,
        title: String,
        description: String?,
        url: String,
        rulesUrl: String?,
        prizeType: String,
        prizeCurrency: String,
        prizeValue: Double,
        isOnline: Boolean,
        startDate: String?,
        endDate: String?,
        isVietnamEligible: Boolean,
        reportCount: Int
    )

    @Query("UPDATE hackathons SET isBookmarked = :isBookmarked WHERE id = :id")
    abstract suspend fun toggleBookmark(id: Int, isBookmarked: Boolean)

    @Query("UPDATE hackathons SET isReportedByUser = 1 WHERE id = :id")
    abstract suspend fun markAsReported(id: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertReportedLog(log: ReportedHackathonEntity)

    @Query("SELECT hackathonId FROM reported_hackathons")
    abstract suspend fun getReportedLogs(): List<Int>

    @Query("DELETE FROM hackathons")
    abstract suspend fun clearAllHackathons()

    @Query("DELETE FROM reported_hackathons")
    abstract suspend fun clearAllReportedLogs()

    @Transaction
    open suspend fun clearAll() {
        clearAllHackathons()
        clearAllReportedLogs()
    }
}
