package com.hackathon.hunter.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.hackathon.hunter.data.local.entity.HackathonEntity

data class HackathonDto(
    @SerializedName("id") val id: Int,
    @SerializedName("platform") val platform: String,
    @SerializedName("platform_id") val platformId: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String?,
    @SerializedName("url") val url: String,
    @SerializedName("rules_url") val rulesUrl: String?,
    @SerializedName("prize_type") val prizeType: String,
    @SerializedName("prize_currency") val prizeCurrency: String,
    @SerializedName("prize_value") val prizeValue: Double,
    @SerializedName("is_online") val isOnline: Boolean,
    @SerializedName("start_date") val startDate: String?,
    @SerializedName("end_date") val endDate: String?,
    @SerializedName("is_vietnam_eligible") val isVietnamEligible: Boolean,
    @SerializedName("report_count") val reportCount: Int,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?
) {
    fun toEntity(isBookmarked: Boolean = false, isReportedByUser: Boolean = false): HackathonEntity {
        return HackathonEntity(
            id = id,
            platform = platform,
            platformId = platformId,
            title = title,
            description = description,
            url = url,
            rulesUrl = rulesUrl,
            prizeType = prizeType,
            prizeCurrency = prizeCurrency,
            prizeValue = prizeValue,
            isOnline = isOnline,
            startDate = startDate,
            endDate = endDate,
            isVietnamEligible = isVietnamEligible,
            reportCount = reportCount,
            isBookmarked = isBookmarked,
            isReportedByUser = isReportedByUser,
            createdAt = createdAt
        )
    }
}
