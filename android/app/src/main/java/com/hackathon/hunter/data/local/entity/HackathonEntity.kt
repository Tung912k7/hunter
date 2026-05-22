package com.hackathon.hunter.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hackathons")
data class HackathonEntity(
    @PrimaryKey val id: Int,
    val platform: String,
    val platformId: String,
    val title: String,
    val description: String?,
    val url: String,
    val rulesUrl: String?,
    val prizeType: String, // "fiat" or "crypto"
    val prizeCurrency: String,
    val prizeValue: Double,
    val isOnline: Boolean,
    val startDate: String?,
    val endDate: String?,
    val isVietnamEligible: Boolean = true,
    val reportCount: Int = 0,
    val isReportedByUser: Boolean = false,
    val isBookmarked: Boolean = false,
    val createdAt: String? = null
)
